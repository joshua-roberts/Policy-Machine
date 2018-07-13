package gov.nist.policyserver.dao;

import gov.nist.policyserver.exceptions.InvalidPropertyException;
import gov.nist.policyserver.obligations.exceptions.InvalidEntityException;
import gov.nist.policyserver.obligations.model.EvrArg;
import gov.nist.policyserver.obligations.model.EvrEntity;
import gov.nist.policyserver.obligations.model.EvrFunction;
import gov.nist.policyserver.obligations.model.script.rule.event.time.EvrTime;
import gov.nist.policyserver.obligations.model.script.rule.event.time.EvrTimeElement;
import gov.nist.policyserver.exceptions.ConfigurationException;
import gov.nist.policyserver.exceptions.DatabaseException;
import gov.nist.policyserver.exceptions.PmException;
import gov.nist.policyserver.graph.PmGraph;
import gov.nist.policyserver.helpers.JsonHelper;
import gov.nist.policyserver.model.graph.nodes.Node;
import gov.nist.policyserver.model.graph.nodes.NodeType;
import gov.nist.policyserver.model.graph.nodes.Property;
import gov.nist.policyserver.model.graph.relationships.Assignment;
import gov.nist.policyserver.model.graph.relationships.Association;
import gov.nist.policyserver.model.prohibitions.Prohibition;
import gov.nist.policyserver.model.prohibitions.ProhibitionResource;
import gov.nist.policyserver.model.prohibitions.ProhibitionSubject;
import gov.nist.policyserver.model.prohibitions.ProhibitionSubjectType;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static gov.nist.policyserver.common.Constants.ERR_NEO;

/**
 * Helper class for Neo4j
 */
public class NeoDAO extends DAO {

    private static String PROHIBITION_LABEL = "prohibition";

    public NeoDAO() throws DatabaseException, SQLException, IOException, ClassNotFoundException, InvalidPropertyException {
        super();
    }

    /**
     * execute a cypher query
     *
     * @param cypher the query
     * @return the result of executing the query
     * @throws PmException
     */
    public ResultSet execute(String cypher) throws DatabaseException {
        try {
            PreparedStatement stmt = conn.prepareStatement(cypher);
            return stmt.executeQuery();
        }
        catch (SQLException e) {
            throw new DatabaseException(ERR_NEO, e.getMessage());
        }
    }

    @Override
    public void connect() throws DatabaseException {
        try {
            Driver driver = new org.neo4j.jdbc.Driver();
            DriverManager.registerDriver(driver);
            conn = DriverManager.getConnection("jdbc:neo4j:http://" + host + ":" + port + "", username, password);

            //load nodes into cache
            //warmUp();

            System.out.println("Connected to Neo4j");
        }
        catch (SQLException e) {
            throw new DatabaseException(ERR_NEO, e.getMessage());
        }
    }

    @Override
    public void buildGraph() throws DatabaseException, InvalidPropertyException {
        System.out.println("Building graph...");

        graph = new PmGraph();

        System.out.print("Getting nodes...");
        List<Node> nodes = getNodes();
        for(Node node : nodes){
            graph.addNode(node);
        }
        System.out.println("DONE");


        System.out.print("Getting assignments...");
        List<Assignment> assignments = getAssignments();
        for(Assignment assignment : assignments){
            graph.createAssignment(graph.getNode(assignment.getChild().getId()), graph.getNode(assignment.getParent().getId()));
        }
        System.out.println("DONE");

        System.out.print("Getting associations...");
        List<Association> associations = getAssociations();
        for(Association assoc : associations){
            graph.createAssociation(assoc.getChild(), assoc.getParent(), assoc.getOps(), assoc.isInherit());
        }
        System.out.println("DONE");
    }

    @Override
    public void buildProhibitions() throws DatabaseException {
        System.out.print("Building prohibitions...");

        String cypher = "match(d:D)<-[" + PROHIBITION_LABEL +"]-(s)\n" +
                "with d, s\n" +
                "match(d:D)-[" + PROHIBITION_LABEL +"]->(r)\n" +
                "return s, d, collect(r)";
        ResultSet rs = execute(cypher);
        try {
            while (rs.next()) {
                String json = rs.getString(1);
                ProhibitionSubject ps = JsonHelper.getProhibitionSubject(json);

                json = rs.getString(2);
                Prohibition prohibition = JsonHelper.getProhibition(json);

                json = rs.getString(3);
                List<ProhibitionResource> prs = JsonHelper.getProhibitionResources(json);

                prohibition.setResources(prs);
                prohibition.setSubject(ps);

                access.addProhibition(prohibition);
            }
        }catch(SQLException e){
            throw new DatabaseException(ERR_NEO, "Error getting prohibitions from nodes");
        }

        System.out.println("DONE");
    }

    @Override
    public void buildObligations() throws DatabaseException {
        //load scripts from db and add to evrManager

    }

    private List<Node> getNodes() throws DatabaseException, InvalidPropertyException {
        String cypher = "match(n) where n:PC or n:OA or n:O or n:UA or n:U return n";
        ResultSet rs = execute(cypher);
        List<Node> nodes = getNodesFromResultSet(rs);
        for(Node node : nodes){
            node.setProperties(getNodeProps(node));
        }

        return nodes;
    }

    private List<Property> getNodeProps(Node node) throws DatabaseException, InvalidPropertyException {
        String cypher = "match(n:" + node.getType() + "{id:" + node.getId() + "}) return n";
        ResultSet rs = execute(cypher);
        try {
            List<Property> props = new ArrayList<>();
            while(rs.next()){
                String json = rs.getString(1);
                props.addAll(JsonHelper.getPropertiesFromJson(json));
            }
            return props;
        }
        catch (SQLException e) {
            throw new DatabaseException(ERR_NEO, e.getMessage());
        }
    }

    private List<Association> getAssociations() throws DatabaseException {
        List<Association> associations = new ArrayList<>();

        String cypher = "match(ua:UA)-[a:association]->(oa:OA) return ua,oa,a.operations,a.inherit;";
        ResultSet rs = execute(cypher);
        try {
            while (rs.next()) {
                Node startNode = JsonHelper.getNodeFromJson(rs.getString(1));
                Node endNode = JsonHelper.getNodeFromJson(rs.getString(2));
                HashSet<String> ops = JsonHelper.getStringSetFromJson(rs.getString(3));
                boolean inherit = Boolean.valueOf(rs.getString(4));
                Association assoc = new Association(startNode, endNode, ops, inherit);
                associations.add(assoc);
            }
            return associations;
        }
        catch (SQLException e) {
            throw new DatabaseException(ERR_NEO, e.getMessage());
        }
    }

    @Override
    public List<Assignment> getAssignments() throws DatabaseException {
        List<Assignment> assignments = new ArrayList<>();

        String cypher = "match(n)-[r:assigned_to]->(m) return n, r, m";
        ResultSet rs = execute(cypher);
        try {
            while (rs.next()) {
                Node startNode = JsonHelper.getNodeFromJson(rs.getString(1));
                Node endNode = JsonHelper.getNodeFromJson(rs.getString(3));
                assignments.add(new Assignment(startNode, endNode));
            }
            return assignments;
        }
        catch (SQLException e) {
            throw new DatabaseException(ERR_NEO, e.getMessage());
        }
    }

    private void warmUp() throws DatabaseException {
        execute("call apoc.warmup.run();");
    }

    @Override
    public Node createNode(long id, String name, NodeType type) throws DatabaseException {
        if(id == 0) {
            id = getMaxId() + 1;
        }
        String cypher = "CREATE " +
                "(n:" + type +
                "{" +
                "id: " + id + ", " +
                "name:'" + name + "'," +
                "type:'" + type + "'})";
        execute(cypher);

        return new Node(id, name, type);
    }

    public long getMaxId() throws DatabaseException {
        String cypher = "match(n) return max(n.id)";
        try {
            ResultSet rs = execute(cypher);
            rs.next();
            long maxId = rs.getLong(1);
            if(maxId == -1) {
                maxId = 1;
            }
            return maxId;
        }
        catch (SQLException e) {
            throw new DatabaseException(ERR_NEO, e.getMessage());
        }
    }

    @Override
    public void updateNode(long nodeId, String name) throws DatabaseException {
        if(name != null && !name.isEmpty()) {
            //update name
            String cypher = "merge (n {id:" + nodeId + "}) set n.name='" + name + "'";
            execute(cypher);
        }
    }

    @Override
    public void deleteNode(long nodeId) throws DatabaseException {
        //delete node
        String cypher = "MATCH (n) where n.id=" + nodeId + " DETACH DELETE n";
        execute(cypher);
    }

    @Override
    public void addNodeProperty(long nodeId, Property property) throws DatabaseException {
        String cypher = "match(n{id:" + nodeId + "}) set n." + property.getKey() + "='" + property.getValue() + "'";
        execute(cypher);
    }

    @Override
    public void deleteNodeProperty(long nodeId, String key) throws DatabaseException {
        String cypher = "match(n{id:" + nodeId + "}) remove n." + key;
        execute(cypher);
    }

    @Override
    public void updateNodeProperty(long nodeId, String key, String value) throws DatabaseException {
        String cypher = "match(n{id:" + nodeId + "}) set n." + key + " = '" + value + "'";
        execute(cypher);
    }

    protected String setToCypherArray(HashSet<String> set) {
        String str = "[";
        for (String op : set) {
            op = "'" + op + "'";
            if (str.length()==1) {
                str += op;
            }
            else {
                str += "," + op;
            }
        }
        str += "]";
        return str;
    }

    private List<Node> getNodesFromResultSet(ResultSet rs) throws DatabaseException {
        List<Node> nodes = new ArrayList<>();

        try {
            while (rs.next()) {
                Node node = JsonHelper.getNodeFromJson(rs.getString(1));
                nodes.add(node);
            }
        }
        catch (SQLException e) {
            throw new DatabaseException(ERR_NEO, e.getMessage());
        }

        return nodes;
    }

    @Override
    public void createAssignment(long childId, long parentId) throws DatabaseException {
        String cypher = "MATCH (a {id:" + childId + "}), (b {id:" + parentId + "}) " +
                "CREATE (a)-[:assigned_to]->(b)";
        execute(cypher);
    }

    @Override
    public void deleteAssignment(long childId, long parentId) throws DatabaseException {
        String cypher = "match (a{id:" + childId + "})-[r:assigned_to]->(b{id:" + parentId + "}) delete r";
        execute(cypher);
    }

    @Override
    public void createAssociation(long uaId, long targetId, HashSet<String> operations, boolean inherit) throws DatabaseException {
        String ops = setToCypherArray(operations);
        String cypher = "MATCH (ua:UA{id:" + uaId + "}), (oa:OA {id:" + targetId + "}) " +
                "CREATE (ua)-[:association{label:'ar', inherit:'" + inherit + "', operations:" + ops + "}]->(oa)";
        execute(cypher);
    }

    @Override
    public void updateAssociation(long uaId, long targetId, boolean inherit, HashSet<String> ops) throws DatabaseException {
        String strOps = setToCypherArray(ops);
        String cypher = "MATCH (ua:UA {id:" + uaId + "})-[r:association]->(oa:OA{id:" + targetId + "}) " +
                "SET r.operations=" + strOps;
        execute(cypher);
    }

    @Override
    public void deleteAssociation(long uaId, long targetId) throws DatabaseException {
        String cypher = "match (a{id:" + uaId + "})-[r:association]->(b{id:" + targetId + "}) delete r";
        execute(cypher);
    }

    @Override
    public void createProhibition(String prohibitionName, HashSet<String> operations, boolean intersection, ProhibitionResource[] resources, ProhibitionSubject subject) throws DatabaseException {
        String cypher = "create (:" + PROHIBITION_LABEL + "{" +
                "name: '" + prohibitionName + "', " +
                "operations: " + setToCypherArray(operations) +
                ", intersection: " + intersection +
                "})";
        execute(cypher);

        for(ProhibitionResource pr : resources){
            addResourceToProhibition(prohibitionName, pr.getResourceId(), pr.isComplement());
        }

        setProhibitionSubject(prohibitionName, subject.getSubjectId(), subject.getSubjectType());
    }

    @Override
    public void deleteProhibition(String prohibitionName) throws DatabaseException {
        String cypher = "match(p:" + PROHIBITION_LABEL +") detach delete p";
        execute(cypher);
    }

    @Override
    public void addResourceToProhibition(String prohibitionName, long resourceId, boolean complement) throws DatabaseException {
        String cypher = "match(p:" + PROHIBITION_LABEL + "{name:'" + prohibitionName + "'}), (n{id:" + resourceId +"}) create (p)-[:" + PROHIBITION_LABEL +"{complement: " + complement + "}]->(n)";
        execute(cypher);
    }

    @Override
    public void deleteProhibitionResource(String prohibitionName, long resourceId) throws DatabaseException {
        String cypher = "match(n{id:" + resourceId + "})<-[r:" + PROHIBITION_LABEL +"]-(p:" + PROHIBITION_LABEL +"{name:'" + prohibitionName + "'}) delete r";
        execute(cypher);
    }

    @Override
    public void setProhibitionIntersection(String prohibitionName, boolean intersection) throws DatabaseException {
        String cypher = "match(d:" + PROHIBITION_LABEL +"{name:'" + prohibitionName + "'}) set d.intersection = " + intersection;
        execute(cypher);
    }

    @Override
    public void setProhibitionSubject(String prohibitionName, long subjectId, ProhibitionSubjectType subjectType) throws DatabaseException {
        String cypher;
        if(subjectType.equals(ProhibitionSubjectType.P)) {
            cypher = "match(p:" + PROHIBITION_LABEL + "{name:'" + prohibitionName + "'}) create (p)<-[:" + PROHIBITION_LABEL + "]-(:PP{subjectId:" + subjectId + ", subjectType:'" + subjectType + "'})";
        } else {
            cypher = "match(p:" + PROHIBITION_LABEL + "{name:'" + prohibitionName + "'}), (n{id:" + subjectId + ", type:'" + subjectType + "'}) create (p)<-[:" + PROHIBITION_LABEL + "]-(n)";
        }
        execute(cypher);
    }

    @Override
    public void setProhibitionOperations(String prohibitionName, HashSet<String> operations) throws DatabaseException {
        String opStr = setToCypherArray(operations);
        String cypher = "match(p:" + PROHIBITION_LABEL +"{name:'" + prohibitionName + "'}) set p.operations = " + opStr;
        execute(cypher);
    }

    @Override
    public void reset() throws DatabaseException {
        String cypher = "match(n) detach delete n";
        execute(cypher);
    }

    private String getEvrId() {
        return UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
    }

    @Override
    public String createScript(String scriptName) throws DatabaseException, SQLException {
        String id = getEvrId();

        //check script node exists
        String cypher = "match(n:obligations:scripts) return n";
        ResultSet rs = execute(cypher);
        if(!rs.next()) {
            //create scripts node
            cypher = "create(:obligations:scripts{name:'scripts'})";
            execute(cypher);
        }

        //create script node in scripts
        cypher = "match(n:obligations:scripts) " +
                "create (n)<-[:script]-(m:obligations:script{evr_id:'" + id + "', name:'" + scriptName + "'})";
        execute(cypher);

        //create rules node in script
        cypher = "match(n:obligations:script{evr_id:'" + id + "'}) " +
                "create (n)<-[:rules]-(m:obligations:rules{evr_id:'" + id + "', name:'rules'})";
        execute(cypher);

        return id;
    }

    @Override
    public String createRule(String parentId, String parentLabel, String label) throws DatabaseException {
        String ruleId = getEvrId();

        //add rule to rules
        String cypher = "match(n:obligations:" + parentLabel + "{evr_id:'" + parentId + "'}) " +
                "create (n)<-[:rule]-(:obligations:rule{evr_id:'" + ruleId + "', label:'" + label + "', name:'rule'})";
        execute(cypher);

        //create event node
        cypher = "match(n:obligations:rule{evr_id:'" + ruleId + "'}) " +
                "create (n)<-[:event]-(:obligations:event{evr_id:'" + ruleId + "', name:'event'})";
        execute(cypher);

        //create response node
        cypher = "match(n:obligations:rule{evr_id:'" + ruleId + "'}) " +
                "create (n)<-[:response]-(:obligations:response{evr_id:'" + ruleId + "', name:'response'})";
        execute(cypher);

        return ruleId;
    }

    @Override
    public String createSubject(String ruleId, String parentLabel) throws DatabaseException {
        String subjectId = getEvrId();

        String cypher = "match(n:obligations:" + parentLabel + "{evr_id:'" + ruleId + "'}) " +
                "create (n)<-[:subject]-(:obligations:subject{evr_id:'" + subjectId + "', name:'subject'})";
        execute(cypher);

        return subjectId;
    }

    @Override
    public String createPolicies(String ruleId, String parentLabel) throws DatabaseException {
        String policiesId = getEvrId();

        String cypher = "match(n:obligations:" + parentLabel + "{evr_id:'" + ruleId + "'}) " +
                "create (n)<-[:policies]-(:obligations:policies{evr_id:'" + policiesId + "', name:'policies'})";
        execute(cypher);

        return policiesId;
    }

    @Override
    public void createTime(String ruleId, EvrTime evrTime) throws DatabaseException {
        String timeId = getEvrId();

        String cypher = "match(n:obligations:event{evr_id:'" + ruleId + "'}) " +
                "create (n)<-[:time]-(:obligations:time{evr_id:'" + timeId + "'})";
        execute(cypher);

        EvrTimeElement dow = evrTime.getDow();
        cypher = "match(n:obligations:time{evr_id:'" + timeId + "'}) " +
                "create (n)<-[:time_dow]-(:obligations:time_dow{" +
                "evr_id:'" + timeId + "'" +
                (dow.isRange() ? (", start: " + dow.getRange().getStart() +
                        ", end: " + dow.getRange().getEnd()) : ", range: " + dow.getValues()) +
                "})";
        execute(cypher);

        EvrTimeElement day = evrTime.getDay();
        cypher = "match(n:obligations:time{evr_id:'" + timeId + "'}) " +
                "create (n)<-[:time_day]-(:obligations:time_day{" +
                "evr_id:'" + timeId + "'" +
                (day.isRange() ? (", start: " + day.getRange().getStart() +
                        ", end: " + day.getRange().getEnd()) : ", range: " + day.getValues()) +
                "})";
        execute(cypher);

        EvrTimeElement month = evrTime.getDay();
        cypher = "match(n:obligations:time{evr_id:'" + timeId + "'}) " +
                "create (n)<-[:time_month]-(:obligations:time_month{" +
                "evr_id:'" + timeId + "'" +
                (month.isRange() ? (", start: " + month.getRange().getStart() +
                        ", end: " + month.getRange().getEnd()) : ", range: " + month.getValues()) +
                "})";
        execute(cypher);

        EvrTimeElement year = evrTime.getDay();
        cypher = "match(n:obligations:time{evr_id:'" + timeId + "'}) " +
                "create (n)<-[:time_year]-(:obligations:time_year{" +
                "evr_id:'" + timeId + "'" +
                (year.isRange() ? (", start: " + year.getRange().getStart() +
                        ", end: " + year.getRange().getEnd()) : ", range: " + year.getValues()) +
                "})";
        execute(cypher);

        EvrTimeElement hour = evrTime.getDay();
        cypher = "match(n:obligations:time{evr_id:'" + timeId + "'}) " +
                "create (n)<-[:time_hour]-(:obligations:time_hour{" +
                "evr_id:'" + timeId + "'" +
                (hour.isRange() ? (", start: " + hour.getRange().getStart() +
                        ", end: " + hour.getRange().getEnd()) : ", range: " + hour.getValues()) +
                "})";
        execute(cypher);
    }

    @Override
    public String createTarget(String ruleId, String parentLabel) throws DatabaseException {
        String targetId = getEvrId();

        //create target node in event
        String cypher = "match(n:obligations:" + parentLabel + "{evr_id:'" + ruleId + "'}) " +
                "create (n)<-[:target]-(:obligations:target{evr_id:'" + targetId + "', name:'target'})";
        execute(cypher);

        //create targetObjects node in target node
        cypher = "match(n:obligations:target{evr_id:'" + targetId + "'}) " +
                "create (n)<-[:target_objects]-(:obligations:target_objects{evr_id:'" + targetId + "', name:'target_objects'})";
        execute(cypher);

        //create targetContainers node in target node
        cypher = "match(n:obligations:target{evr_id:'" + targetId + "'}) " +
                "create (n)<-[:target_containers]-(:obligations:target_containers{evr_id:'" + targetId + "', name:'target_containers'})";
        execute(cypher);

        return targetId;
    }

    @Override
    public String createEntity(String parentId, String parentLabel, EvrEntity entity) throws DatabaseException {
        String entityId = getEvrId();

        //create the entity node
        String cypher = "match(n:obligations:" + parentLabel + "{evr_id:'" + parentId + "'}) " +
                "create (n)<-[:entity]-(:obligations:entity{evr_id:'" + entityId + "', name:'entity'})";
        execute(cypher);

        return entityId;
    }

    @Override
    public void updateEntity(String entityId, EvrEntity entity) throws DatabaseException, InvalidEntityException {
        if (!entity.isList() && !entity.isFunction() && !entity.isProcess()) {
            //entity is a leaf -- base case
            //create entity and assign it to the parent
            if (entity.isNode()) {
                String cypher = "match(n:obligations:entity{evr_id:'" + entityId + "'}) set n.node=" + entity.getNode().getId();
                execute(cypher);
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
                execute(cypher);
            }
        }
    }

    @Override
    public String createFunction(String parentId, String parentLabel, EvrFunction function) throws DatabaseException {
        String functionId = getEvrId();

        String cypher = "match(n:obligations:" + parentLabel + "{evr_id:'" + parentId + "'}) " +
                "create (n)<-[:function]-(:obligations:function{evr_id:'" + functionId + "', name:'" + function.getFunctionName() + "'})";
        execute(cypher);

        return functionId;
    }

    @Override
    public void addFunctionArg(String functionId, EvrArg evrArg) throws DatabaseException {
        String cypher = "match(n:obligations:function{evr_id:'" + functionId + "'}) " +
                "create (n)<-[:arg]-(:obligations:arg{evr_id:'" + functionId + "', value:'" + evrArg.getValue() + "', name:'arg'})";
        execute(cypher);
    }

    @Override
    public String createCondition(String ruleId, boolean exists) throws DatabaseException {
        String conditionId = getEvrId();

        //create the entity node
        String cypher = "match(n:obligations:rule{evr_id:'" + ruleId + "'}) " +
                "create (n)<-[:condition]-(:obligations:condition{evr_id:'" + conditionId + "', name:'condition', exists:" + exists + "})";
        execute(cypher);

        return conditionId;
    }

    @Override
    public String createAssignAction(String parentId, String parentLabel) throws DatabaseException {
        String assignActionId = getEvrId();

        //create the entity node
        String cypher = "match(n:obligations:" + parentLabel + "{evr_id:'" + parentId + "'}) " +
                "create (n)<-[:assign_action]-(:obligations:assign_action{evr_id:'" + assignActionId + "', name:'assign_action'})";
        execute(cypher);

        return assignActionId;
    }

    @Override
    public String createAssignActionParam(String assignActionId, String param) throws DatabaseException {
        String paramId = getEvrId();

        //create the entity node
        String cypher = "match(n:obligations:assign_action{evr_id:'" + assignActionId + "'}) " +
                "create (n)<-[:assign_action_" + param + "]-" +
                "(:obligations:assign_action_" + param + "{evr_id:'" + assignActionId + "', name:'assign_action_" + param + "'})";
        execute(cypher);

        return paramId;
    }

    @Override
    public String createGrantAction(String parentId, String parentLabel) throws DatabaseException {
        String grantActionId = getEvrId();

        //create the entity node
        String cypher = "match(n:obligations:" + parentLabel + "{evr_id:'" + parentId + "'}) " +
                "create (n)<-[:grant_action]-(:obligations:grant_action{evr_id:'" + grantActionId + "', name:'grant_action'})";
        execute(cypher);

        return grantActionId;
    }

    @Override
    public void createOperations(String parentId, String parentType, List<String> ops) throws DatabaseException {
        HashSet<String> opSet = new HashSet<>(ops);

        String cypher = "match(n:obligations:" + parentType + "{evr_id:'" + parentId + "'}) " +
                "create (n)<-[:operations]-(:obligations:operations{evr_id:'" + parentId + "', name:'operations', ops:" + setToCypherArray(opSet) + "})";
        execute(cypher);
    }

    @Override
    public String createCreateAction(String parentId, String parentLabel) throws DatabaseException {
        String createActionId = getEvrId();

        //create the entity node
        String cypher = "match(n:obligations:" + parentLabel + "{evr_id:'" + parentId + "'}) " +
                "create (n)<-[:create_action]-" +
                "(:obligations:create_action{evr_id:'" + createActionId + "', name:'create_action'})";
        execute(cypher);

        return createActionId;
    }

    @Override
    public String createDenyAction(String parentId, String parentLabel) throws DatabaseException {
        String denyActionId = getEvrId();

        //create the entity node
        String cypher = "match(n:obligations:" + parentLabel + "{evr_id:'" + parentId + "'}) " +
                "create (n)<-[:deny_action]-" +
                "(:obligations:deny_action{evr_id:'" + denyActionId + "', name:'deny_action'})";
        execute(cypher);

        return denyActionId;
    }

    @Override
    public String createDeleteAction(String parentId, String parentLabel) throws DatabaseException {
        String deleteActionId = getEvrId();

        //create the entity node
        String cypher = "match(n:obligations:" + parentLabel + "{evr_id:'" + parentId + "'}) " +
                "create (n)<-[:delete_action]-" +
                "(:obligations:delete_action{evr_id:'" + deleteActionId + "', name:'delete_action'})";
        execute(cypher);

        return deleteActionId;
    }
}
