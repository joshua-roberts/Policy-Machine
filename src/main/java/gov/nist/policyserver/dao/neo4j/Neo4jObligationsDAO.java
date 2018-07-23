package gov.nist.policyserver.dao.neo4j;

import gov.nist.policyserver.dao.ObligationsDAO;
import gov.nist.policyserver.exceptions.DatabaseException;
import gov.nist.policyserver.exceptions.InvalidPropertyException;
import gov.nist.policyserver.model.graph.nodes.Property;
import gov.nist.policyserver.obligations.EvrManager;
import gov.nist.policyserver.obligations.exceptions.InvalidEntityException;
import gov.nist.policyserver.obligations.model.*;
import gov.nist.policyserver.obligations.model.script.EvrScript;
import gov.nist.policyserver.obligations.model.script.rule.event.EvrOpertations;
import gov.nist.policyserver.obligations.model.script.rule.event.EvrPolicies;
import gov.nist.policyserver.obligations.model.script.rule.event.EvrSubject;
import gov.nist.policyserver.obligations.model.script.rule.event.EvrTarget;
import gov.nist.policyserver.obligations.model.script.rule.event.time.EvrEvent;
import gov.nist.policyserver.obligations.model.script.rule.event.time.EvrTime;
import gov.nist.policyserver.obligations.model.script.rule.event.time.EvrTimeElement;
import gov.nist.policyserver.obligations.model.script.rule.response.*;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static gov.nist.policyserver.dao.neo4j.Neo4jHelper.*;
import static gov.nist.policyserver.helpers.JsonHelper.*;
import static gov.nist.policyserver.obligations.EvrKeywords.*;

public class Neo4jObligationsDAO implements ObligationsDAO {

    public static final  String ASSIGN_ACTION_TAG     = "assign_action";
    public static final  String GRANT_ACTION_TAG       = "grant_action";
    public static final  String CREATE_ACTION_TAG      = "create_action";
    public static final  String DENY_ACTION_TAG        = "deny_action";
    public static final  String DELETE_ACTION_TAG      = "delete_action";

    public static void main(String[] args) throws SQLException, DatabaseException, IOException, ClassNotFoundException {
        Driver driver = new org.neo4j.jdbc.Driver();
        DriverManager.registerDriver(driver);
        Connection connection = DriverManager.getConnection("jdbc:neo4j:http://localhost:7474", "neo4j", "root");
        Neo4jObligationsDAO dao = new Neo4jObligationsDAO(connection);
    }

    private Connection connection;
    private EvrManager evrManager;

    public Neo4jObligationsDAO(Connection connection) throws DatabaseException, IOException, ClassNotFoundException, SQLException {
        this.connection = connection;
        evrManager = new EvrManager();

        System.out.println("Building obligations...");
        buildObligations();
    }

    private String getEvrId() {
        return UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
    }

    @Override
    public EvrManager getEvrManager() {
        return evrManager;
    }

    @Override
    public void buildObligations() throws DatabaseException, SQLException {
        //get scripts
        String cypher = "match(n:obligations:script) return n.evr_id, n.label";
        ResultSet rs = execute(connection, cypher);
        while (rs.next()) {
            String scriptId = rs.getString(1);
            String scriptLabel = rs.getString(2);

            EvrScript script = new EvrScript(scriptLabel);
            script.setRules(loadRules(scriptId));
            script.setEnabled(true);

            System.out.println("Adding script " + scriptLabel);
            evrManager.addScript(script);
        }
    }

    private List<EvrRule> loadRules(String scriptId) throws DatabaseException, SQLException {
        List<EvrRule> rules = new ArrayList<>();

        String cypher = "match(n:obligations:rules{evr_id:'" + scriptId + "'})<-[:rule]-(m:obligations:rule) return m.evr_id";
        ResultSet rs = execute(connection, cypher);
        while (rs.next()) {
            String ruleId = rs.getString(1);

            rules.add(loadRule(ruleId));
        }

        return rules;
    }

    private EvrRule loadRule(String ruleId) throws DatabaseException, SQLException {
        String cypher = "match(n:obligations:rule{evr_id:'" + ruleId + "'}) return n.label";
        ResultSet rs = execute(connection, cypher);

        String ruleLabel;
        if(rs.next()) {
            ruleLabel = rs.getString(1);
        } else {
            ruleLabel = getEvrId();
        }

        return new EvrRule(ruleLabel, loadEvent(ruleId), loadResponse(ruleId));
    }

    private EvrEvent loadEvent(String ruleId) throws DatabaseException, SQLException {
        EvrEvent event = new EvrEvent();

        List<EvrNode> children = getChildren(ruleId, EVENT_TAG);
        for(EvrNode child : children) {
            switch(child.getEvrType()) {
                case SUBJECT_TAG:
                    EvrSubject subject = loadSubject(child.getEvrId());
                    event.setSubject(subject);
                    break;
                case OPERATIONS_TAG:
                    EvrOpertations evrOperations = loadEvrOperations(child.getEvrId());
                    event.setEvrOperations(evrOperations);
                    break;
                case POLICIES_TAG:
                    EvrPolicies evrPolicies = loadEvrPolicies(child.getEvrId());
                    event.setEvrPolicies(evrPolicies);
                    break;
                case TARGET_TAG:
                    EvrTarget target = loadTarget(child.getEvrId());
                    event.setTarget(target);
                    break;
                case TIME_TAG:
                    EvrTime evrTime = loadTime(child.getEvrId());
                    event.setTime(evrTime);
                    break;
            }
        }

        return event;
    }

    private EvrSubject loadSubject(String evrId) throws DatabaseException, SQLException {
        EvrSubject subject = new EvrSubject();

        List<EvrNode> children = getChildren(evrId, SUBJECT_TAG);
        for(EvrNode child : children) {
            switch (child.getEvrType()) {
                case ENTITY_TAG:
                    EvrEntity entity = loadEntity(child.getEvrId());
                    subject.addEntity(entity);
                    break;
                case FUNCTION_TAG:
                    EvrFunction function = loadFunction(child.getEvrId());
                    subject.addEntity(new EvrEntity(function));
                    break;
                case PROCESS_TAG:
                    EvrProcess process = loadProcess(child.getEvrId());
                    subject.addEntity(new EvrEntity(process));
                    break;
            }
        }

        return subject;
    }

    private EvrEntity loadEntity(String evrId) throws DatabaseException, SQLException {
        //check if entity is a function -- if it has a function assigned to it
        String cypher = "match(n:obligations:entity{evr_id:'" + evrId + "'})<-[r:function]-(m:obligations:function) return m.evr_id";
        ResultSet rs = execute(connection, cypher);
        if(!rs.next()) {
            cypher = "match(n:obligations:entity{evr_id:'" + evrId + "'}) return n.name, n.type, n.properties, n.complement";
            rs = execute(connection, cypher);
            rs.next();

            String name = rs.getString(1);
            name = (name.equalsIgnoreCase("null")) ? null : name;

            String type = rs.getString(2);
            type = (type.equalsIgnoreCase("null")) ? null : type;

            List<Property> properties = new ArrayList<>();
            try {
                properties = strToPropertyList(rs.getString(3));
            }
            catch (InvalidPropertyException e) {
                e.printStackTrace();
            }
            boolean complement = rs.getBoolean(4);

            return new EvrEntity(name, type, properties, complement);
        } else {
            String functionEvrId = rs.getString(1);

            return new EvrEntity(loadFunction(functionEvrId));
        }
    }

    private EvrFunction loadFunction(String evrId) throws DatabaseException, SQLException {
        String functionName = getEvrNodeName(evrId, FUNCTION_TAG);

        EvrFunction function = new EvrFunction(functionName);

        //get arguments
        List<EvrNode> args = getChildren(evrId, FUNCTION_TAG);
        for(EvrNode evrNode : args) {
            if(evrNode.getEvrType().equalsIgnoreCase(ARG_TAG)) {
                List<EvrNode> argChildren = getChildren(evrNode.getEvrId(), evrNode.getEvrType());
                if(argChildren.isEmpty()) {
                    //just value
                    function.addArg(new EvrArg(getEvrNodeName(evrNode.getEvrId(), evrNode.getEvrType())));
                } else {
                    for(EvrNode argChild : argChildren) {
                        switch (argChild.getEvrType()) {
                            case FUNCTION_TAG:
                                function.addArg(new EvrArg(loadFunction(argChild.getEvrId())));
                                break;
                            case ENTITY_TAG:
                                function.addArg(new EvrArg(loadEntity(argChild.getEvrId())));
                                break;
                        }
                    }
                }
            }
        }

        return function;
    }

    private EvrProcess loadProcess(String evrId) throws DatabaseException, SQLException {
        List<EvrNode> children = getChildren(evrId, PROCESS_TAG);
        if(children.isEmpty()) {
            return new EvrProcess(Integer.valueOf(getEvrNodeName(evrId, PROCESS_TAG)));
        } else {
            EvrNode argChild = children.get(0);
            EvrFunction evrFunction = loadFunction(argChild.getEvrId());
            return  new EvrProcess(evrFunction);
        }
    }

    private EvrOpertations loadEvrOperations(String evrId) throws DatabaseException, SQLException {
        String cypher = "match(n:obligations:operations{evr_id:'" + evrId + "'}) return n.ops";
        ResultSet rs = execute(connection, cypher);
        rs.next();
        HashSet<String> ops = getStringSetFromJson(rs.getString(1));

        return new EvrOpertations(ops);
    }

    private EvrPolicies loadEvrPolicies(String evrId) throws DatabaseException, SQLException {
        String cypher = "match(n:obligations:policies{evr_id:'" + evrId + "'}) return n.or";
        ResultSet rs = execute(connection, cypher);
        rs.next();
        boolean isOr = rs.getBoolean(1);

        EvrPolicies evrPolicies = new EvrPolicies();
        evrPolicies.setOr(isOr);

        List<EvrNode> children = getChildren(evrId, POLICIES_TAG);
        for(EvrNode child : children) {
            EvrEntity evrEntity = loadEntity(child.getEvrId());
            evrPolicies.addEntity(evrEntity);
        }

        return evrPolicies;
    }

    private EvrTarget loadTarget(String evrId) throws DatabaseException, SQLException {
        EvrTarget evrTarget = new EvrTarget();

        List<EvrNode> children = getChildren(evrId, TARGET_TAG);
        for(EvrNode child : children) {
            switch (child.getEvrType()) {
                case "target_object":
                    List<EvrNode> targetObjects = getChildren(child.getEvrId(), "target_object");
                    if(targetObjects.isEmpty()) continue;

                    EvrNode entityNode = targetObjects.get(0);
                    evrTarget.setEntity(loadEntity(entityNode.getEvrId()));
                    break;
                case "target_containers":
                    List<EvrNode> targetContainers = getChildren(child.getEvrId(), "target_containers");
                    for(EvrNode evrNode : targetContainers) {
                        evrTarget.addContainer(loadEntity(evrNode.getEvrId()));
                    }

                    break;
            }
        }

        return evrTarget;
    }

    private EvrTime loadTime(String evrId) throws DatabaseException, SQLException {
        EvrTime evrTime = new EvrTime();

        List<EvrNode> children = getChildren(evrId, TIME_TAG);
        for(EvrNode child : children) {
            String cypher = "match(n:obligations:" + child.getEvrType() + "{evr_id:'" + child.getEvrId() + "'}) return n.values, n.start, n.end";
            ResultSet rs = execute(connection, cypher);
            rs.next();
            String values = rs.getString(1);
            String start = rs.getString(2);
            String end = rs.getString(3);

            EvrTimeElement evrTimeElement;
            if(values != null) {
                evrTimeElement = new EvrTimeElement(toIntList(values));
            } else {
                evrTimeElement = new EvrTimeElement(Integer.valueOf(start), Integer.valueOf(end));
            }

            switch (child.getEvrType()) {
                case DOW_TAG:
                    evrTime.setDow(evrTimeElement);
                    break;
                case DAY_TAG:
                    evrTime.setDay(evrTimeElement);
                    break;
                case MONTH_TAG:
                    evrTime.setMonth(evrTimeElement);
                    break;
                case YEAR_TAG:
                    evrTime.setYear(evrTimeElement);
                    break;
                case HOUR_TAG:
                    evrTime.setHour(evrTimeElement);
                    break;
            }
        }

        return evrTime;
    }


    private EvrResponse loadResponse(String ruleId) throws DatabaseException, SQLException {
        EvrResponse response = new EvrResponse();

        List<EvrNode> children = getChildren(ruleId, RESPONSE_TAG);
        for(EvrNode child : children) {
            switch(child.getEvrType()) {
                case CONDITION_TAG:
                    EvrCondition condition = loadCondition(child.getEvrId());
                    response.setCondition(condition);
                    break;
                case ASSIGN_ACTION_TAG:
                    EvrAssignAction evrAssignAction = loadAssign(child.getEvrId());
                    response.addAction(evrAssignAction);
                    break;
                case GRANT_ACTION_TAG:
                    EvrGrantAction evrGrantAction = loadGrant(child.getEvrId());
                    response.addAction(evrGrantAction);
                    break;
                case CREATE_ACTION_TAG:
                    EvrCreateAction evrCreateAction = loadCreate(child.getEvrId());
                    response.addAction(evrCreateAction);
                    break;
                case DENY_ACTION_TAG:
                    EvrDenyAction evrDenyAction = loadDeny(child.getEvrId());
                    response.addAction(evrDenyAction);
                    break;
                case DELETE_ACTION_TAG:
                    EvrDeleteAction evrDeleteAction = loadDelete(child.getEvrId());
                    response.addAction(evrDeleteAction);
                    break;
            }
        }
        return response;
    }

    private EvrDeleteAction loadDelete(String evrId) throws DatabaseException, SQLException {
        EvrDeleteAction action = new EvrDeleteAction();

        List<EvrNode> children = getChildren(evrId, "delete_action");
        for(EvrNode child : children) {
            switch(child.getEvrType()) {
                case ASSIGN_ACTION_TAG:
                    EvrAssignAction evrAssignAction = loadAssign(child.getEvrId());
                    action.setEvrAction(evrAssignAction);
                    break;
                case DENY_ACTION_TAG:
                    EvrDenyAction evrDenyAction = loadDeny(child.getEvrId());
                    action.setEvrAction(evrDenyAction);
                    break;
                case RULE_TAG:
                    EvrRule evrRule = loadRule(evrId);
                    action.setEvrRule(evrRule);
                    break;
            }
        }

        return action;
    }

    private EvrDenyAction loadDeny(String evrId) throws DatabaseException, SQLException {
        EvrDenyAction action = new EvrDenyAction();

        List<EvrNode> children = getChildren(evrId, "deny_action");
        for(EvrNode child : children) {
            switch (child.getEvrType()) {
                case SUBJECT_TAG:
                    EvrSubject evrSubject = loadSubject(child.getEvrId());
                    action.setSubject(evrSubject);
                    break;
                case OPERATIONS_TAG:
                    EvrOpertations evrOpertations = loadEvrOperations(child.getEvrId());
                    action.setEvrOperations(evrOpertations);
                    break;
                case TARGET_TAG:
                    EvrTarget evrTarget = loadTarget(child.getEvrId());
                    action.setTarget(evrTarget);
                    break;
            }
        }

        return action;
    }

    private EvrCreateAction loadCreate(String evrId) throws DatabaseException, SQLException {
        EvrCreateAction action = new EvrCreateAction();

        List<EvrNode> children = getChildren(evrId, "create_action");
        for(EvrNode child : children) {
            switch (child.getEvrType()) {
                case ENTITY_TAG:
                    EvrEntity entity = loadEntity(evrId);
                    action.setEntity(entity);
                    break;
                case TARGET_TAG:
                    EvrTarget evrTarget = loadTarget(evrId);
                    action.setTarget(evrTarget);
                    break;
                case RULE_TAG:
                    EvrRule evrRule = loadRule(evrId);
                    action.setRule(evrRule);
                    break;
            }
        }

        return action;
    }

    private EvrGrantAction loadGrant(String evrId) throws DatabaseException, SQLException {
        EvrGrantAction action = new EvrGrantAction();

        List<EvrNode> children = getChildren(evrId, GRANT_TAG);
        for(EvrNode child : children) {
            switch (child.getEvrType()) {
                case SUBJECT_TAG:
                    EvrSubject evrSubject = loadSubject(child.getEvrId());
                    action.setSubject(evrSubject);
                    break;
                case OPERATIONS_TAG:
                    EvrOpertations evrOpertations = loadEvrOperations(child.getEvrId());
                    action.setEvrOperations(evrOpertations);
                    break;
                case TARGET_TAG:
                    EvrTarget evrTarget = loadTarget(child.getEvrId());
                    action.setTarget(evrTarget);
                    break;
            }
        }

        return action;
    }

    private EvrAssignAction loadAssign(String evrId) throws DatabaseException, SQLException {
        EvrAssignAction evrAssignAction = new EvrAssignAction();

        List<EvrNode> children = getChildren(evrId, "assign_action");
        for(EvrNode child : children) {
            switch (child.getEvrType()) {
                case "assign_action_child":
                    List<EvrNode> childChildren = getChildren(child.getEvrId(), "assign_action_child");
                    for (EvrNode node : childChildren) {
                        switch (node.getEvrType()) {
                            case ENTITY_TAG:
                                EvrEntity entity = loadEntity(node.getEvrId());
                                evrAssignAction.setChild(entity);
                                break;
                            case FUNCTION_TAG:
                                EvrFunction function = loadFunction(node.getEvrId());
                                evrAssignAction.setChild(new EvrEntity(function));
                                break;
                        }
                    }
                    break;
                case "assign_action_parent":
                    List<EvrNode> parentChildren = getChildren(child.getEvrId(), "assign_action_parent");
                    for (EvrNode node : parentChildren) {
                        switch (node.getEvrType()) {
                            case ENTITY_TAG:
                                EvrEntity entity = loadEntity(node.getEvrId());
                                evrAssignAction.setParent(entity);
                                break;
                            case FUNCTION_TAG:
                                EvrFunction function = loadFunction(node.getEvrId());
                                evrAssignAction.setParent(new EvrEntity(function));
                                break;
                        }
                    }
                    break;
            }
        }

        return evrAssignAction;
    }

    private EvrCondition loadCondition(String evrId) throws DatabaseException, SQLException {
        EvrCondition evrCondition = new EvrCondition();

        boolean exists = getConditionExists(evrId);
        evrCondition.setExists(exists);

        List<EvrNode> children = getChildren(evrId, CONDITION_TAG);
        for(EvrNode child : children) {
            switch (child.getEvrType()) {
                case ENTITY_TAG:
                    EvrEntity entity = loadEntity(child.getEvrId());
                    evrCondition.setEntity(entity);
                    break;
                case FUNCTION_TAG:
                    EvrFunction evrFunction = loadFunction(child.getEvrId());
                    evrCondition.setEntity(new EvrEntity(evrFunction));
                    break;
            }
        }

        return evrCondition;
    }

    private boolean getConditionExists(String conditionId) throws DatabaseException, SQLException {
        String cypher = "match(n:condition{evr_id:'" + conditionId + "'}) return n.exists";
        ResultSet rs = execute(connection, cypher);
        if(rs.next()) {
            return rs.getBoolean(1);
        }

        return false;
    }

    private String getEvrNodeName(String evrId, String evrType) throws DatabaseException, SQLException {
        String cypher = "match(n:obligations:" + evrType + "{evr_id:'" + evrId + "'}) return n.name";
        ResultSet rs = execute(connection, cypher);
        rs.next();
        return rs.getString(1);
    }

    private List<EvrNode> getChildren(String evrId, String evrType) throws DatabaseException, SQLException {
        List<EvrNode> children = new ArrayList<>();

        String cypher = "match(n:obligations:" + evrType + "{evr_id:'" + evrId + "'})<-[r]-(m:obligations) return m.evr_id, TYPE(r)";
        ResultSet rs = execute(connection, cypher);
        while(rs.next()) {
            children.add(new EvrNode(rs.getString(1), rs.getString(2)));
        }

        return children;
    }

    class EvrNode {
        private String evrId;
        private String evrType;

        EvrNode(String evrId, String evrType) {
            this.evrId = evrId;
            this.evrType = evrType;
        }

        String getEvrId() {
            return evrId;
        }

        String getEvrType() {
            return evrType;
        }
    }

    @Override
    public String createScript(String scriptName) throws DatabaseException, SQLException {
        String id = getEvrId();

        //check script node exists
        String cypher = "match(n:obligations:scripts) return n";
        ResultSet rs = execute(connection, cypher);
        if(!rs.next()) {
            //create scripts node
            cypher = "create(:obligations:scripts{name:'scripts'})";
            execute(connection, cypher);
        }

        //create script node in scripts
        cypher = "match(n:obligations:scripts) " +
                "create (n)<-[:script]-(m:obligations:script{evr_id:'" + id + "', label:'" + scriptName + "', name:'script'})";
        execute(connection, cypher);

        //create rules node in script
        cypher = "match(n:obligations:script{evr_id:'" + id + "'}) " +
                "create (n)<-[:rules]-(m:obligations:rules{evr_id:'" + id + "', name:'rules'})";
        execute(connection, cypher);

        return id;
    }

    @Override
    public String createRule(String parentId, String parentLabel, String label) throws DatabaseException {
        String ruleId = getEvrId();

        //add rule to rules
        String cypher = "match(n:obligations:" + parentLabel + "{evr_id:'" + parentId + "'}) " +
                "create (n)<-[:rule]-(:obligations:rule{evr_id:'" + ruleId + "', label:'" + label + "', name:'rule'})";
        execute(connection, cypher);

        //create event node
        cypher = "match(n:obligations:rule{evr_id:'" + ruleId + "'}) " +
                "create (n)<-[:event]-(:obligations:event{evr_id:'" + ruleId + "', name:'event'})";
        execute(connection, cypher);

        //create response node
        cypher = "match(n:obligations:rule{evr_id:'" + ruleId + "'}) " +
                "create (n)<-[:response]-(:obligations:response{evr_id:'" + ruleId + "', name:'response'})";
        execute(connection, cypher);

        return ruleId;
    }

    @Override
    public String createSubject(String ruleId, String parentLabel) throws DatabaseException {
        String subjectId = getEvrId();

        String cypher = "match(n:obligations:" + parentLabel + "{evr_id:'" + ruleId + "'}) " +
                "create (n)<-[:subject]-(:obligations:subject{evr_id:'" + subjectId + "', name:'subject'})";
        execute(connection, cypher);

        return subjectId;
    }

    @Override
    public String createPolicies(String ruleId, String parentLabel, boolean isOr) throws DatabaseException {
        String policiesId = getEvrId();

        String cypher = "match(n:obligations:" + parentLabel + "{evr_id:'" + ruleId + "'}) " +
                "create (n)<-[:policies]-(:obligations:policies{evr_id:'" + policiesId + "', name:'policies', or:" +
                isOr + "})";
        execute(connection, cypher);

        return policiesId;
    }

    @Override
    public void createTime(String ruleId, EvrTime evrTime) throws DatabaseException {
        String timeId = getEvrId();

        String cypher = "match(n:obligations:event{evr_id:'" + ruleId + "'}) " +
                "create (n)<-[:time]-(:obligations:time{evr_id:'" + timeId + "'})";
        execute(connection, cypher);

        EvrTimeElement dow = evrTime.getDow();
        cypher = "match(n:obligations:time{evr_id:'" + timeId + "'}) " +
                "create (n)<-[:time_dow]-(:obligations:time_dow{" +
                "evr_id:'" + timeId + "'" +
                (dow.isRange() ? (", start: " + dow.getRange().getStart() +
                        ", end: " + dow.getRange().getEnd()) : ", values: " + dow.getValues()) +
                "})";
        execute(connection, cypher);

        EvrTimeElement day = evrTime.getDay();
        cypher = "match(n:obligations:time{evr_id:'" + timeId + "'}) " +
                "create (n)<-[:time_day]-(:obligations:time_day{" +
                "evr_id:'" + timeId + "'" +
                (day.isRange() ? (", start: " + day.getRange().getStart() +
                        ", end: " + day.getRange().getEnd()) : ", values: " + day.getValues()) +
                "})";
        execute(connection, cypher);

        EvrTimeElement month = evrTime.getMonth();
        cypher = "match(n:obligations:time{evr_id:'" + timeId + "'}) " +
                "create (n)<-[:time_month]-(:obligations:time_month{" +
                "evr_id:'" + timeId + "'" +
                (month.isRange() ? (", start: " + month.getRange().getStart() +
                        ", end: " + month.getRange().getEnd()) : ", values: " + month.getValues()) +
                "})";
        execute(connection, cypher);

        EvrTimeElement year = evrTime.getYear();
        cypher = "match(n:obligations:time{evr_id:'" + timeId + "'}) " +
                "create (n)<-[:time_year]-(:obligations:time_year{" +
                "evr_id:'" + timeId + "'" +
                (year.isRange() ? (", start: " + year.getRange().getStart() +
                        ", end: " + year.getRange().getEnd()) : ", values: " + year.getValues()) +
                "})";
        execute(connection, cypher);

        EvrTimeElement hour = evrTime.getHour();
        cypher = "match(n:obligations:time{evr_id:'" + timeId + "'}) " +
                "create (n)<-[:time_hour]-(:obligations:time_hour{" +
                "evr_id:'" + timeId + "'" +
                (hour.isRange() ? (", start: " + hour.getRange().getStart() +
                        ", end: " + hour.getRange().getEnd()) : ", values: " + hour.getValues()) +
                "})";
        execute(connection, cypher);
    }

    @Override
    public String createTarget(String ruleId, String parentLabel) throws DatabaseException {
        String targetId = getEvrId();

        //create target node in event
        String cypher = "match(n:obligations:" + parentLabel + "{evr_id:'" + ruleId + "'}) " +
                "create (n)<-[:target]-(:obligations:target{evr_id:'" + targetId + "', name:'target'})";
        execute(connection, cypher);

        //create targetObjects node in target node
        cypher = "match(n:obligations:target{evr_id:'" + targetId + "'}) " +
                "create (n)<-[:target_object]-(:obligations:target_object{evr_id:'" + targetId + "', name:'target_object'})";
        execute(connection, cypher);

        //create targetContainers node in target node
        cypher = "match(n:obligations:target{evr_id:'" + targetId + "'}) " +
                "create (n)<-[:target_containers]-(:obligations:target_containers{evr_id:'" + targetId + "', name:'target_containers'})";
        execute(connection, cypher);

        return targetId;
    }

    @Override
    public String createEntity(String parentId, String parentLabel, EvrEntity entity) throws DatabaseException {
        String entityId = getEvrId();

        //create the entity node
        String cypher = "match(n:obligations:" + parentLabel + "{evr_id:'" + parentId + "'}) " +
                "create (n)<-[:entity]-(:obligations:entity{evr_id:'" + entityId + "', name:'entity'})";
        execute(connection, cypher);

        return entityId;
    }

    @Override
    public void updateEntity(String entityId, EvrEntity entity) throws DatabaseException, InvalidEntityException {
        if (!entity.isList() && !entity.isFunction() && !entity.isProcess()) {
            //entity is a leaf -- base case
            //create entity and assign it to the parent
            if (entity.isNode()) {
                String cypher = "match(n:obligations:entity{evr_id:'" + entityId + "'}) set n.node=" + entity.getNode().getId();
                execute(connection, cypher);
            } else if (entity.isEvrNode()) {
                HashSet<String> propSet = new HashSet<>();
                for (Property prop : entity.getProperties()) {
                    propSet.add(prop.toString());
                }

                String cypher = "match(n:obligations:entity{evr_id:'" + entityId + "'}) set n += " +
                        "{" +
                        "name:'" + entity.getName() + "', " +
                        "type:'" + entity.getType() + "', " +
                        "properties:" + setToCypherArray(propSet) + ", " +
                        "complement:" + entity.isCompliment() + "" +
                        "}";
                execute(connection, cypher);
            }
        }
    }

    @Override
    public String createFunction(String parentId, String parentLabel, EvrFunction function) throws DatabaseException {
        String functionId = getEvrId();

        String cypher = "match(n:obligations:" + parentLabel + "{evr_id:'" + parentId + "'}) " +
                "create (n)<-[:function]-(:obligations:function{evr_id:'" + functionId + "', name:'" + function.getFunctionName() + "'})";
        execute(connection, cypher);

        return functionId;
    }

    @Override
    public String createProcess(String parentId, String parentLabel) throws DatabaseException {
        String processId = getEvrId();

        String cypher = "match(n:obligations:" + parentLabel + "{evr_id:'" + parentId + "'}) " +
                "create (n)<-[:process]-(:obligations:process{evr_id:'" + processId + "'})";
        execute(connection, cypher);

        return processId;
    }

    @Override
    public void updateProcess(String parentId, long process) throws DatabaseException {
        String cypher = "match(n:obligations:process{evr_id:'" + parentId + "'}) set n.name='" + process + "'";
        execute(connection, cypher);
    }

    @Override
    public String addFunctionArg(String functionId) throws DatabaseException {
        String argId = getEvrId();

        String cypher = "match(n:obligations:function{evr_id:'" + functionId + "'}) " +
                "create (n)<-[:arg]-(:obligations:arg{evr_id:'" + argId + "'})";
        execute(connection, cypher);

        return argId;
    }

    @Override
    public void addFunctionArgValue(String functionId, EvrArg arg) throws DatabaseException {
        String cypher = "match(n:obligations:function{evr_id:'" + functionId + "'}) " +
                "create (n)<-[:arg]-(:obligations:arg{evr_id:'" + functionId + "', name:'" + arg.getValue() + "'})";
        execute(connection, cypher);
    }

    @Override
    public String createCondition(String ruleId, boolean exists) throws DatabaseException {
        String conditionId = getEvrId();

        //create the entity node
        String cypher = "match(n:obligations:response{evr_id:'" + ruleId + "'}) " +
                "create (n)<-[:condition]-(:obligations:condition{evr_id:'" + conditionId + "', name:'condition', exists:" + exists + "})";
        execute(connection, cypher);

        return conditionId;
    }

    @Override
    public String createAssignAction(String parentId, String parentLabel) throws DatabaseException {
        String assignActionId = getEvrId();

        //create the entity node
        String cypher = "match(n:obligations:" + parentLabel + "{evr_id:'" + parentId + "'}) " +
                "create (n)<-[:assign_action]-(:obligations:assign_action{evr_id:'" + assignActionId + "', name:'assign_action'})";
        execute(connection, cypher);

        return assignActionId;
    }

    @Override
    public String createAssignActionParam(String assignActionId, String param) throws DatabaseException {
        String paramId = getEvrId();

        //create the entity node
        String cypher = "match(n:obligations:assign_action{evr_id:'" + assignActionId + "'}) " +
                "create (n)<-[:assign_action_" + param + "]-" +
                "(:obligations:assign_action_" + param + "{evr_id:'" + paramId + "', name:'assign_action_" + param + "'})";
        execute(connection, cypher);

        return paramId;
    }

    @Override
    public String createGrantAction(String parentId, String parentLabel) throws DatabaseException {
        String grantActionId = getEvrId();

        //create the entity node
        String cypher = "match(n:obligations:" + parentLabel + "{evr_id:'" + parentId + "'}) " +
                "create (n)<-[:grant_action]-(:obligations:grant_action{evr_id:'" + grantActionId + "', name:'grant_action'})";
        execute(connection, cypher);

        return grantActionId;
    }

    @Override
    public void createOperations(String parentId, String parentType, HashSet<String> ops) throws DatabaseException {
        HashSet<String> opSet = new HashSet<>(ops);

        String cypher = "match(n:obligations:" + parentType + "{evr_id:'" + parentId + "'}) " +
                "create (n)<-[:operations]-(:obligations:operations{evr_id:'" + parentId + "', name:'operations', " +
                "ops:" +
                setToCypherArray(opSet) + "})";
        execute(connection, cypher);
    }

    @Override
    public String createCreateAction(String parentId, String parentLabel) throws DatabaseException {
        String createActionId = getEvrId();

        //create the entity node
        String cypher = "match(n:obligations:" + parentLabel + "{evr_id:'" + parentId + "'}) " +
                "create (n)<-[:create_action]-" +
                "(:obligations:create_action{evr_id:'" + createActionId + "', name:'create_action'})";
        execute(connection, cypher);

        return createActionId;
    }

    @Override
    public String createDenyAction(String parentId, String parentLabel) throws DatabaseException {
        String denyActionId = getEvrId();

        //create the entity node
        String cypher = "match(n:obligations:" + parentLabel + "{evr_id:'" + parentId + "'}) " +
                "create (n)<-[:deny_action]-" +
                "(:obligations:deny_action{evr_id:'" + denyActionId + "', name:'deny_action'})";
        execute(connection, cypher);

        return denyActionId;
    }

    @Override
    public String createDeleteAction(String parentId, String parentLabel) throws DatabaseException {
        String deleteActionId = getEvrId();

        //create the entity node
        String cypher = "match(n:obligations:" + parentLabel + "{evr_id:'" + parentId + "'}) " +
                "create (n)<-[:delete_action]-" +
                "(:obligations:delete_action{evr_id:'" + deleteActionId + "', name:'delete_action'})";
        execute(connection, cypher);

        return deleteActionId;
    }

    @Override
    public void deleteObligations() throws DatabaseException {
        String cypher = "match(n:obligations) detach delete n";
        execute(connection, cypher);
    }

    @Override
    public void updateScript(String obligation, boolean enabled) throws DatabaseException {
        String cypher = "match(n:obligations:script{evr_id:'" + obligation + "'}) set n.enabled=" + enabled;
        execute(connection, cypher);
    }
}
