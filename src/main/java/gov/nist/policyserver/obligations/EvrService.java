package gov.nist.policyserver.obligations;

import gov.nist.policyserver.common.Constants;
import gov.nist.policyserver.exceptions.*;
import gov.nist.policyserver.model.graph.nodes.Node;
import gov.nist.policyserver.model.graph.nodes.NodeType;
import gov.nist.policyserver.model.graph.nodes.Property;
import gov.nist.policyserver.model.prohibitions.ProhibitionResource;
import gov.nist.policyserver.model.prohibitions.ProhibitionSubject;
import gov.nist.policyserver.model.prohibitions.ProhibitionSubjectType;
import gov.nist.policyserver.obligations.exceptions.InvalidEntityException;
import gov.nist.policyserver.obligations.exceptions.InvalidEvrException;
import gov.nist.policyserver.obligations.exceptions.NoProcessFoundException;
import gov.nist.policyserver.obligations.model.*;
import gov.nist.policyserver.obligations.model.script.EvrScript;
import gov.nist.policyserver.obligations.model.script.rule.event.EvrOpertations;
import gov.nist.policyserver.obligations.model.script.rule.event.EvrPolicies;
import gov.nist.policyserver.obligations.model.script.rule.event.EvrSubject;
import gov.nist.policyserver.obligations.model.script.rule.event.EvrTarget;
import gov.nist.policyserver.obligations.model.script.rule.event.time.EvrEvent;
import gov.nist.policyserver.obligations.model.script.rule.event.time.EvrTime;
import gov.nist.policyserver.obligations.model.script.rule.response.*;
import gov.nist.policyserver.service.AssignmentService;
import gov.nist.policyserver.service.NodeService;
import gov.nist.policyserver.service.ProhibitionsService;
import gov.nist.policyserver.service.Service;
import org.joda.time.LocalDateTime;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.sql.*;
import java.util.*;

import static gov.nist.policyserver.common.Constants.ANY_OPERATIONS;
import static gov.nist.policyserver.common.Constants.NAMESPACE_PROPERTY;

public class EvrService extends Service {

    private AssignmentService assignmentService = new AssignmentService();
    private ProhibitionsService prohibitionsService = new ProhibitionsService();
    private List<EvrEntity>      curSubjects;

    private EvrManager getEvrManager() throws ClassNotFoundException, SQLException, IOException, DatabaseException, InvalidPropertyException {
        return getDaoManager().getObligationsDAO().getEvrManager();
    }

    public void evr(String source) throws InvalidEvrException, SQLException, ParserConfigurationException, InvalidEntityException, SAXException, DatabaseException, IOException, InvalidPropertyException, ClassNotFoundException, ConfigurationException {
        getEvrManager().evr(source);
    }

    String createScript(EvrScript script) throws DatabaseException, SQLException, IOException, ClassNotFoundException, InvalidPropertyException {
        //add script to memory
        script.setEnabled(true);
        getEvrManager().addScript(script);

        return getDaoManager().getObligationsDAO().createScript(script.getScriptName());
    }

    String createRule(String parentId, String parentLabel, String label) throws DatabaseException, SQLException, IOException, ClassNotFoundException, InvalidPropertyException {
        //create rule node
        return getDaoManager().getObligationsDAO().createRule(parentId, parentLabel, label);
    }

    String createSubject(String ruleId, String parentLabel) throws DatabaseException, SQLException, IOException, ClassNotFoundException, InvalidPropertyException {
        return getDaoManager().getObligationsDAO().createSubject(ruleId, parentLabel);
    }

    String createEntity(String parentId, String parentLabel, EvrEntity evrEntity) throws DatabaseException, InvalidEntityException, SQLException, IOException, ClassNotFoundException, InvalidPropertyException {
        return getDaoManager().getObligationsDAO().createEntity(parentId, parentLabel, evrEntity);
    }

    String createFunction(String parentId, String parentLabel, EvrFunction function) throws DatabaseException, SQLException, IOException, ClassNotFoundException, InvalidPropertyException {
        return getDaoManager().getObligationsDAO().createFunction(parentId, parentLabel, function);
    }

    String createProcess(String parentId, String parentLabel) throws DatabaseException, SQLException, IOException, ClassNotFoundException, InvalidPropertyException {
        return getDaoManager().getObligationsDAO().createProcess(parentId, parentLabel);
    }

    void updateProcess(String parentId, long process) throws DatabaseException, SQLException, IOException, ClassNotFoundException, InvalidPropertyException {
        getDaoManager().getObligationsDAO().updateProcess(parentId, process);
    }

    String addFuntionArg(String functionId) throws DatabaseException, SQLException, IOException, ClassNotFoundException, InvalidPropertyException {
        return getDaoManager().getObligationsDAO().addFunctionArg(functionId);
    }

    void addFuntionArgValue(String functionId, EvrArg evrArg) throws DatabaseException, SQLException, IOException, ClassNotFoundException, InvalidPropertyException {
        getDaoManager().getObligationsDAO().addFunctionArgValue(functionId, evrArg);
    }

    void updateEntity(String entityId, EvrEntity evrEntity) throws DatabaseException, InvalidEntityException, SQLException, IOException, ClassNotFoundException, InvalidPropertyException {
        getDaoManager().getObligationsDAO().updateEntity(entityId, evrEntity);
    }

    String createPolicies(String ruleId, String parentLabel, boolean isOr) throws DatabaseException, SQLException, IOException, ClassNotFoundException, InvalidPropertyException {
        return getDaoManager().getObligationsDAO().createPolicies(ruleId, parentLabel, isOr);
    }

    void createOperations(String parentId, String parentType, HashSet<String> ops) throws DatabaseException, SQLException, IOException, ClassNotFoundException, InvalidPropertyException {
        getDaoManager().getObligationsDAO().createOperations(parentId, parentType, ops);
    }

    void createTime(String ruleId, EvrTime evrTime) throws DatabaseException, SQLException, IOException, ClassNotFoundException, InvalidPropertyException {
        getDaoManager().getObligationsDAO().createTime(ruleId, evrTime);
    }

    String createTarget(String ruleId, String parentLabel) throws DatabaseException, SQLException, IOException, ClassNotFoundException, InvalidPropertyException {
        return getDaoManager().getObligationsDAO().createTarget(ruleId, parentLabel);
    }

    String createCondition(String ruleId, boolean exists) throws DatabaseException, SQLException, IOException, ClassNotFoundException, InvalidPropertyException {
        return getDaoManager().getObligationsDAO().createCondition(ruleId, exists);
    }

    String createAssignAction(String parentId, String parentLabel) throws DatabaseException, SQLException, IOException, ClassNotFoundException, InvalidPropertyException {
        return getDaoManager().getObligationsDAO().createAssignAction(parentId, parentLabel);
    }

    String createAssignActionParam(String assignActionId, String param) throws DatabaseException, SQLException, IOException, ClassNotFoundException, InvalidPropertyException {
        return getDaoManager().getObligationsDAO().createAssignActionParam(assignActionId, param);
    }

    String createGrantAction(String parentId, String parentLabel) throws DatabaseException, SQLException, IOException, ClassNotFoundException, InvalidPropertyException {
        return getDaoManager().getObligationsDAO().createGrantAction(parentId, parentLabel);
    }

    String createCreateAction(String parentId, String parentLabel) throws DatabaseException, SQLException, IOException, ClassNotFoundException, InvalidPropertyException {
        return getDaoManager().getObligationsDAO().createCreateAction(parentId, parentLabel);
    }

    String createDenyAction(String parentId, String parentLabel) throws DatabaseException, SQLException, IOException, ClassNotFoundException, InvalidPropertyException {
        return getDaoManager().getObligationsDAO().createDenyAction(parentId, parentLabel);
    }

    String createDeleteAction(String parentId, String parentLabel) throws DatabaseException, SQLException, IOException, ClassNotFoundException, InvalidPropertyException {
        return getDaoManager().getObligationsDAO().createDeleteAction(parentId, parentLabel);
    }

    public void deleteObligations() throws ClassNotFoundException, SQLException, IOException, DatabaseException, InvalidPropertyException {
        getDaoManager().getObligationsDAO().deleteObligations();

        getEvrManager().deleteScripts();
    }

    public void processFileRead(Node node, Node user, long process) throws InvalidPropertyException, ProhibitionResourceExistsException, IOException, InvalidNodeTypeException, NodeNotFoundException, ClassNotFoundException, InvalidEntityException, DatabaseException, ConfigurationException, ProhibitionNameExistsException, ProhibitionDoesNotExistException, NullNameException, SQLException, InvalidEvrException, InvalidProhibitionSubjectTypeException {
        //if user is null, its a process
        EvrSubject evrSubject = new EvrSubject();
        if(process != 0) {
            evrSubject.addEntity(new EvrEntity(new EvrProcess(process)));
        } else {
            evrSubject.addEntity(new EvrEntity(user));
        }

        processEvent(evrSubject, new EvrPolicies(), Constants.FILE_READ, node);
    }

    public void processEvent(EvrSubject procSubject, EvrPolicies procPc, String procEvent, Node procTarget) throws InvalidNodeTypeException, InvalidEntityException, NodeNotFoundException, InvalidPropertyException, InvalidEvrException, SQLException, DatabaseException, ConfigurationException, ProhibitionResourceExistsException, ProhibitionNameExistsException, ProhibitionDoesNotExistException, InvalidProhibitionSubjectTypeException, NullNameException, IOException, ClassNotFoundException {
        //get all rules with the same event
        List<EvrRule> rules = getRules(procEvent);
        for(EvrRule rule : rules) {
            EvrEvent evrEvent = rule.getEvent();
            try {
                if(eventMatches(evrEvent, procSubject, procPc, procTarget)) {
                    EvrResponse response = rule.getResponse();
                    EvrCondition condition = response.getCondition();
                    if(checkCondition(condition)) {
                        this.curSubjects = procSubject.getEntities();

                        List<EvrAction> actions = response.getActions();
                        doActions(actions);
                    }
                }
            }
            catch (NoProcessFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean checkCondition(EvrCondition condition) throws InvalidEntityException, InvalidNodeTypeException, InvalidEvrException, InvalidPropertyException, SQLException, DatabaseException, IOException, ClassNotFoundException, NoProcessFoundException {
        if(condition == null) {
            return true;
        }

        EvrEntity entity = condition.getEntity();

        //if the entity is a function, eval function set entity = result
        if(entity.isFunction()) {
            entity = evalFunction(entity.getFunction());
        }

        //if its a node and exists than return true
        if(entity.isNode()) {
            HashSet<Node> nodes = getNodes(entity.getName(), entity.getType(), entity.getProperties());
            return !nodes.isEmpty();
        }

        //if its a value and is 'true' return true
        if(entity.isValue()) {
            return entity.getName().equalsIgnoreCase("true");
        }

        return false;
    }

    private HashSet<Node> getNodes(String name, String type, List<Property> properties) throws InvalidPropertyException, InvalidNodeTypeException, ClassNotFoundException, SQLException, DatabaseException, IOException {
        //get target node
        //get properties
        NodeType nodeType = (type != null) ? NodeType.toNodeType(type) : null;

        HashSet<Node> nodes = getGraph().getNodes();
        String namespace = "";
        if(properties != null) {
            for (Property prop : properties) {
                if (prop.getKey().equals(NAMESPACE_PROPERTY)) {
                    namespace = prop.getValue();
                }
            }
        }

        final String fNamespace = namespace;
        //check namespace match
        if(namespace != null && !namespace.isEmpty()){
            nodes.removeIf(node -> {
                try {
                    return !node.hasProperty(NAMESPACE_PROPERTY) || !node.getProperty(NAMESPACE_PROPERTY).getValue()
                            .equalsIgnoreCase(fNamespace);
                }
                catch (PropertyNotFoundException e) {
                    return true;
                }
            });
        }

        //check name match
        if(name != null){
            nodes.removeIf(node -> !node.getName().equals(name));
        }

        //check type match
        if(nodeType != null){
            nodes.removeIf(node -> !node.getType().equals(nodeType));
        }

        //check property match
        if(properties != null && !properties.isEmpty()) {
            nodes.removeIf(node -> {
                for (Property prop : properties) {
                    if(node.hasProperty(prop)) {
                        return false;
                    }
                }
                return true;
            });
        }

        return nodes;
    }

    private boolean eventMatches(EvrEvent evrEvent, EvrSubject procSubject, EvrPolicies procPc, Node procTarget) throws InvalidNodeTypeException, InvalidEntityException, NodeNotFoundException, InvalidPropertyException, InvalidEvrException, SQLException, DatabaseException, IOException, ClassNotFoundException, NoProcessFoundException {
        //check subject
        EvrSubject evrSubject = evrEvent.getSubject();
        EvrPolicies evrPc = evrEvent.getPolicies();
        EvrTarget evrTarget = evrEvent.getTarget();

        return subjectMatches(procSubject, evrSubject) &&
                pcMatches(procPc, evrPc) &&
                targetMatches(procTarget, evrTarget);
    }

    private boolean subjectMatches(EvrSubject procSubject, EvrSubject evrSubject) throws InvalidNodeTypeException, InvalidPropertyException, InvalidEntityException, NodeNotFoundException, InvalidEvrException, SQLException, DatabaseException, IOException, ClassNotFoundException, NoProcessFoundException {
        //if the event being checked is any than it matches
        if(evrSubject.isAny()) {
            return true;
        }

        List<EvrEntity> procEntities = procSubject.getEntities();
        List<EvrEntity> eventEntities = evrSubject.getEntities();
        for(EvrEntity procEntity : procEntities) {
            for(EvrEntity evrEntity : eventEntities) {
                //check process
                if(evrEntity.isProcess()) {
                    if(!procEntity.isProcess()) {
                        continue;
                    } else if (!checkProcess(procEntity, evrEntity)) {
                        return false;
                    }
                }

                //check node
                if(evrEntity.isNode()) {
                    if(!procEntity.isNode()) {
                        continue;
                    } else if(!checkNode(procEntity, evrEntity)) {
                        return false;
                    }
                }

                //if the entity is a function
                if(evrEntity.isFunction()) {
                    EvrEntity funcEntity = evalFunction(procEntity.getFunction());
                    //check process
                    if(funcEntity.isProcess()) {
                        if(!procEntity.isProcess()) {
                            continue;
                        } else if (!checkProcess(procEntity, evrEntity)) {
                            return false;
                        }
                    }

                    //check node
                    if(funcEntity.isNode()) {
                        if(!procEntity.isNode()) {
                            continue;
                        } else if(!checkNode(procEntity, evrEntity)) {
                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }

    private boolean pcMatches(EvrPolicies procPc, EvrPolicies evrPc) {
        if(evrPc == null) {
            return true;
        }

        if(evrPc.isAny()) {
            return true;
        }

        //TODO
        return true;
    }

    private boolean targetMatches(Node procTarget, EvrTarget evrTarget) throws InvalidNodeTypeException, InvalidEvrException, NodeNotFoundException, ClassNotFoundException, SQLException, IOException, DatabaseException, InvalidPropertyException {
        EvrEntity evrTargetEntity = evrTarget.getEntity();
        List<EvrEntity> evrTargetContainers = evrTarget.getContainers();

        Node evrTargetNode = null;

        if(!evrTargetEntity.isAny()) {
            // check that the nodes match
            HashSet<Node> nodes = getNodes(evrTargetEntity.getName(), evrTargetEntity.getType(), evrTargetEntity.getProperties());
            if(nodes.size() != 1) {
                throw new InvalidEvrException("Target entity (" + evrTargetEntity.getName() + ") can only be one node");
            }

            evrTargetNode = nodes.iterator().next();
            if(!procTarget.equals(evrTargetNode)) {
                return false;
            }
        }

        if(!evrTarget.isAnyContainer()) {
            //make sure the entity is in at least one container
            for(EvrEntity evrEntity : evrTargetContainers) {
                HashSet<Node> nodes = getNodes(evrEntity.getName(), evrEntity.getType(), evrEntity.getProperties());
                for(Node node : nodes) {
                    if(evrTargetNode != null) {
                        HashSet<Node> ascendants = assignmentService.getAscendants(evrTargetNode.getId());
                        ascendants.add(evrTargetNode);
                        if(ascendants.contains(node)) {
                            return true;
                        }
                    } else {
                        //any object, check the processed target is in the container
                        HashSet<Node> ascendants = assignmentService.getAscendants(node.getId());
                        ascendants.add(node);
                        if(ascendants.contains(procTarget)) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    private boolean checkProcess(EvrEntity procEntity, EvrEntity evrEntity) {
        return procEntity.isProcess() &&
                evrEntity.isProcess() &&
                procEntity.getProcess().equals(evrEntity.getProcess());
    }

    /**
     * Evaluate a function
     * @param function
     * @return
     */
    private EvrEntity evalFunction(EvrFunction function) throws InvalidEvrException, InvalidNodeTypeException, InvalidPropertyException, SQLException, DatabaseException, IOException, ClassNotFoundException, NoProcessFoundException {
        List<EvrEntity> args = evalArgs(function.getArgs());

        switch (function.getFunctionName()) {
            case "getNodeWithProperty":
                return getNodeWithProperty(args);
            case "current_process":
                return currentProcess();
            default:
                throw new InvalidEvrException("Function with name " + function.getFunctionName() + " does not exist");
        }
    }

    private EvrEntity currentProcess() throws NoProcessFoundException {
        for (EvrEntity evrEntity : curSubjects) {
            if(evrEntity.isProcess()) {
                return evrEntity;
            }
        }

        throw new NoProcessFoundException();
    }

    /*private EvrEntity getSqlValue(List<EvrEntity> args) throws InvalidEvrException, SQLException {
        //args should be db, table, column
        if(args.size() != 3) {
            throw new InvalidEvrException("Invalid number of parameters for function 'getSqlValue'. Expected 3, got " + args.size());
        }

        //check arg 1
        EvrEntity db = args.get(0);
        if(!db.isValue()) {
            throw new InvalidEvrException("First parameter for function 'getSqlValue' should be a value");
        }

        //check arg 2
        EvrEntity table = args.get(1);
        if(!table.isValue()) {
            throw new InvalidEvrException("Second parameter for function 'getSqlValue' should be a value");
        }

        //check arg 3
        EvrEntity column = args.get(2);
        if(!column.isValue()) {
            throw new InvalidEvrException("Third parameter for function 'getSqlValue' should be a value");
        }

        EvrEntity evrEntity = new EvrEntity();

        String sql = "select " + column.getName() + " from " + db.getName() + "." + table.getName() +
                " where " + getKeyString(table.getName()) + " " + getInString();
        Connection connection = dbManager.getConnection();
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(sql);
        while(resultSet.next()) {
            evrEntity = new EvrEntity(String.valueOf(resultSet.getObject(1)), null, null, false);
        }

        return evrEntity;
    }*/

    /**
     * number of args = 2
     * 1. property name
     * 2. property value
     * @param args
     * @return
     * @throws InvalidEvrException
     */
    private EvrEntity getNodeWithProperty(List<EvrEntity> args) throws InvalidEvrException, InvalidNodeTypeException, InvalidPropertyException, ClassNotFoundException, SQLException, IOException, DatabaseException {
        if(args.size() != 2) {
            throw new InvalidEvrException("Invalid number of parameters for function 'getNodeWithProperty'. Expected 2, got " + args.size());
        }

        //check arg 1
        EvrEntity propName = args.get(0);
        if(!propName.isValue()) {
            throw new InvalidEvrException("First parameter for function 'getNodeWithProperty' should be a value");
        }

        //check arg 2
        EvrEntity propValue = args.get(1);
        if(!propValue.isValue()) {
            throw new InvalidEvrException("Second parameter for function 'getNodeWithProperty' should be a value");
        }

        HashSet<Node> nodes = getNodes(null, null, Collections.singletonList(new Property(propName.getName(), propValue.getName())));

        List<EvrEntity> evrEntities = new ArrayList<>();
        for(Node node : nodes) {
            evrEntities.add(new EvrEntity(node));
        }

        return new EvrEntity(evrEntities);
    }

    /**
     * Take in a list of EvrArgs and return a list with no functions
     * @param args
     * @return
     */
    private List<EvrEntity> evalArgs(List<EvrArg> args) throws InvalidEvrException, InvalidNodeTypeException, InvalidPropertyException, SQLException, DatabaseException, IOException, ClassNotFoundException, NoProcessFoundException {
        List<EvrEntity> retArgs = new ArrayList<>();
        for(EvrArg arg : args) {
            if(arg.isFunction()) {
                retArgs.add(evalFunction(arg.getFunction()));
            } else if(arg.isEntity()){
                retArgs.add(arg.getEntity());
            } else if(arg.isValue()) {
                retArgs.add(new EvrEntity(arg.getValue(), null, null, false));
            }
        }

        return retArgs;
    }

    /**
     * This method assumes both are nodes, check for functions or processes are done elsewhere
     * @param procEntity
     * @param evrEntity
     * @return
     */
    private boolean checkNode(EvrEntity procEntity, EvrEntity evrEntity) throws InvalidNodeTypeException, InvalidEntityException, NodeNotFoundException, ClassNotFoundException, SQLException, IOException, DatabaseException, InvalidPropertyException {
        HashSet<Node> nodes = getNodes(evrEntity.getName(), evrEntity.getType(), evrEntity.getProperties());
        if(nodes.size() != 1) {
            throw new InvalidEntityException("The node (" + evrEntity.getName() + ", " + evrEntity.getType() + ") specified in the EVR script does not exist.");
        }

        Node checkNode = nodes.iterator().next();

        //check that the nodes are equal
        if(procEntity.getNode().equals(checkNode)) {
            return true;
        } else {
            //is the checked node an ascendant to the processed node
            nodes = assignmentService.getAscendants(procEntity.getNode().getId());
            return nodes.contains(checkNode);
        }

        //if it gets to this point, the nodes do not match
    }


    /*private EvrEntity checkSqlValue(List<EvrEntity> args) throws SQLException {
        String table = args.get(0).getName();
        String column = args.get(1).getName();
        String expected = args.get(2).getName();

        String keyStr = getKeyString(table);
        String inStr = getInString();

        String sql = "select " + column + " from " + table + " where " + keyStr + " " + inStr;

        Connection connection = dbManager.getConnection();
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(sql);
        while(resultSet.next()) {
            if(resultSet.getBoolean(1) != Boolean.parseBoolean(expected)) {
                return new EvrEntity("false", null, null, false);
            }
        }

        return new EvrEntity("true", null, null, false);
    }*/

    /*private String getKeyString(String table) throws SQLException {
        List<String> keys = getKeys(table);
        String concatKey = "concat(";
        for(int i = 0; i < keys.size(); i++){
            if(i == 0) {
                concatKey += keys.get(i);
            }else{
                concatKey += ",'+'," + keys.get(i);
            }
        }
        concatKey += ")";

        return concatKey;
    }*/

    /*private String getInString() {
        List<String> rows = dbManager.getRows();
        String in = "in (";
        for(int i = 0; i < rows.size(); i++) {
            if(i == 0) {
                in += "'" + rows.get(i) + "'";
            } else {
                in += ", '" + rows.get(i) + "'";
            }
        }
        in += ")";

        return in;
    }*/

    /*protected List<String> getKeys(String tableName) throws SQLException {
        PreparedStatement ps2 = dbManager.getConnection().prepareStatement("SELECT k.COLUMN_NAME " +
                "FROM information_schema.table_constraints t " +
                "LEFT JOIN information_schema.key_column_usage k " +
                "USING(constraint_name,table_schema,table_name) " +
                "WHERE t.constraint_type='PRIMARY KEY' " +
                "    AND t.table_schema=DATABASE() " +
                "    AND t.table_name='" + tableName + "' order by ordinal_position;");
        ResultSet rs2 = ps2.executeQuery();
        List<String> keys = new ArrayList<>();
        if (rs2 != null) {
            while (rs2.next()) {
                keys.add(tableName + "." + rs2.getString(1));
            }
        }
        return keys;
    }*/

    public List<EvrRule> getRules(String event) throws ClassNotFoundException, SQLException, DatabaseException, InvalidPropertyException, IOException {
        List<EvrRule> retRules = new ArrayList<>();
        for(EvrScript script : getEvrManager().getScripts()) {
            if(script.isEnabled()) {
                List<EvrRule> rules = script.getRules();
                for (EvrRule rule : rules) {
                    EvrEvent ruleEvent = rule.getEvent();

                    //check the event matches
                    if (ruleEvent.getOperations().getOps().contains(event)) {
                        retRules.add(rule);
                    }
                }
            }
        }

        return retRules;
    }

    private void doActions(List<EvrAction> actions) throws InvalidEntityException, InvalidNodeTypeException, SQLException, InvalidEvrException, InvalidPropertyException, NodeNotFoundException, DatabaseException, ProhibitionNameExistsException, InvalidProhibitionSubjectTypeException, NullNameException, IOException, ClassNotFoundException {
        try {
            for(EvrAction action : actions) {
                if (action instanceof EvrGrantAction) {
                    EvrGrantAction grantAction = (EvrGrantAction) action;
                    doGrant(grantAction);

                } else if (action instanceof EvrAssignAction) {
                    EvrAssignAction assignAction = (EvrAssignAction) action;
                    doAssign(assignAction);
                }
                else if (action instanceof EvrDenyAction) {
                    EvrDenyAction denyAction = (EvrDenyAction) action;
                    doDeny(denyAction);
                }
            }

        }catch (NoProcessFoundException e) {
            e.printStackTrace();
        }
    }

    private void doDeny(EvrDenyAction denyAction) throws DatabaseException, NodeNotFoundException, InvalidProhibitionSubjectTypeException, ProhibitionNameExistsException, InvalidEntityException, InvalidNodeTypeException, SQLException, InvalidEvrException, InvalidPropertyException, NullNameException, IOException, ClassNotFoundException, NoProcessFoundException {
        EvrSubject subject = denyAction.getSubject();
        EvrOpertations operations = denyAction.getOperations();
        EvrTarget target = denyAction.getTarget();

        HashSet<String> ops = operations.getOps();
        String[] opsArr = new String[ops.size()];
        opsArr = ops.toArray(opsArr);

        ProhibitionResource[] resources = new ProhibitionResource[target.getContainers().size()];
        List<EvrEntity> containers = target.getContainers();
        for(int i = 0; i < containers.size(); i++) {
            EvrEntity evrEntity = containers.get(i);
            if(evrEntity.isFunction()) {
                evrEntity = evalFunction(evrEntity.getFunction());
            }

            HashSet<Node> nodes = getNodes(evrEntity.getName(), evrEntity.getType(), evrEntity.getProperties());
            if(nodes.size() != 1) {
                throw new NodeNotFoundException("Error finding container node when creating a prohibition");
            }
            Node node = nodes.iterator().next();
            resources[i] = new ProhibitionResource(node.getId(), evrEntity.isCompliment());
        }

        List<EvrEntity> entities = subject.getEntities();
        for(EvrEntity evrEntity : entities) {
            //check if function
            if(evrEntity.isFunction()) {
                evrEntity = evalFunction(evrEntity.getFunction());
            }

            //get node if node
            ProhibitionSubject proSubject = null;
            if(evrEntity.isNode()) {
                HashSet<Node> nodes = getNodes(evrEntity.getName(), evrEntity.getType(), evrEntity.getProperties());
                if (nodes.size() != 1) {
                    throw new NodeNotFoundException("Error finding subject node when creating a prohibition");
                }
                Node node = nodes.iterator().next();
                proSubject = new ProhibitionSubject(node.getId(), ProhibitionSubjectType.toProhibitionSubjectType(node.getType().toString()));
            } else {
                //its a process
                EvrProcess process = evrEntity.getProcess();
                if(process.isFunction()) {
                    evrEntity = evalFunction(process.getFunction());
                }

                proSubject = new ProhibitionSubject(evrEntity.getProcess().getProcessId(), ProhibitionSubjectType.P);
            }


            prohibitionsService.createProhibition(UUID.randomUUID().toString(),
                    opsArr, target.isIntersection(), resources, proSubject);
        }
    }

    private void doAssign(EvrAssignAction assignAction) throws InvalidEntityException, InvalidNodeTypeException, SQLException, InvalidEvrException, InvalidPropertyException, DatabaseException, IOException, ClassNotFoundException, NoProcessFoundException {
        EvrEntity child = assignAction.getChild();
        EvrEntity parent = assignAction.getParent();

        if(child.isFunction()) {
            child = evalFunction(child.getFunction());
        }

        if(parent.isFunction()) {
            parent = evalFunction(parent.getFunction());
        }

        HashSet<Node> childNodes = getNodes(child);
        HashSet<Node> parentnodes = getNodes(parent);

        for(Node childNode : childNodes) {
            for(Node parentNode : parentnodes) {
                System.out.println("Assigning " + childNode.getName() + " to " + parentNode.getName());
                //assignmentService.createAssignment(childNode.getId(), parentNode.getId());
            }
        }
    }

    private HashSet<Node> getNodes(EvrEntity evrEntity) throws InvalidEntityException, InvalidNodeTypeException, ClassNotFoundException, SQLException, IOException, DatabaseException, InvalidPropertyException {
        HashSet<Node> nodes = new HashSet<>();
        if(evrEntity.isList()) {
            List<EvrEntity> entityList = evrEntity.getEntityList();
            for(EvrEntity entity : entityList) {
                if(entity.isNode()) {
                    nodes.add(entity.getNode());
                } else if(entity.isEvrNode()) {
                    HashSet<Node> entityNodes = getNodes(entity.getName(), entity.getType(), entity.getProperties());
                    nodes.addAll(entityNodes);
                }
            }
        } else if(evrEntity.isEvrNode()) {
            HashSet<Node> entityNodes = getNodes(evrEntity.getName(), evrEntity.getType(), evrEntity.getProperties());
            nodes.addAll(entityNodes);
        } else if(evrEntity.isNode()) {
            nodes.add(evrEntity.getNode());
        }

        return nodes;
    }


    /**
     * grant each subject the ops on the target
     * @param grantAction
     */
    private void doGrant(EvrGrantAction grantAction) throws InvalidEntityException, InvalidNodeTypeException, SQLException, InvalidEvrException, InvalidPropertyException, DatabaseException, IOException, ClassNotFoundException, NoProcessFoundException {
        EvrSubject subject = grantAction.getSubject();
        List<EvrEntity> entities = subject.getEntities();

        EvrOpertations operations = grantAction.getOperations();
        HashSet<String> ops = operations.getOps();

        EvrTarget target = grantAction.getTarget();
        List<EvrEntity> containers = target.getContainers();

        for(EvrEntity subjectEntity : entities) {
            if(subjectEntity.isFunction()) {
                subjectEntity = evalFunction(subjectEntity.getFunction());
            }

            HashSet<Node> subjectNodes = getNodes(subjectEntity);

            //loop through the nodes that are included in the subject entity
            for(Node subjectNode : subjectNodes) {
                if(!subjectNode.getType().equals(NodeType.UA)) {//can only grant analytics for ua
                    continue;
                }

                for(EvrEntity targetContainer : containers) {
                    if(targetContainer.isFunction()) {
                        targetContainer = evalFunction(targetContainer.getFunction());
                    }

                    HashSet<Node> targetNodes = getNodes(targetContainer);

                    for(Node targetNode : targetNodes) {
                        System.out.println("Granting " + subjectNode.getName() + " " + ops + " on " + targetNode.getName());
                        //analyticsService.grantAccess(subjectNode.getId(), targetNode.getId(), new HashSet<>(ops), true);
                    }
                }
            }
        }
    }

    public void updateScript(String obligation) throws ClassNotFoundException, SQLException, DatabaseException, InvalidPropertyException, IOException {
        List<EvrScript> scripts = getEvrManager().getScripts();
        for(EvrScript script : scripts) {
            if(script.getScriptName().equals(obligation)) {
                script.setEnabled(!script.isEnabled());

                getDaoManager().getObligationsDAO().updateScript(obligation, script.isEnabled());
            }
        }
    }
}
