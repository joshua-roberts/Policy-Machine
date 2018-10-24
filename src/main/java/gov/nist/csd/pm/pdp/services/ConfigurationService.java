package gov.nist.csd.pm.pdp.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import gov.nist.csd.pm.model.Constants;
import gov.nist.csd.pm.model.exceptions.*;
import gov.nist.csd.pm.model.graph.Assignment;
import gov.nist.csd.pm.model.graph.Association;
import gov.nist.csd.pm.model.graph.OldNode;
import gov.nist.csd.pm.model.graph.nodes.NodeType;
import gov.nist.csd.pm.pip.PIP;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.*;
import java.util.*;

public class ConfigurationService extends Service{
    private NodeService nodeService;
    private AssignmentService assignmentService;
    private AssociationsService associationsService;

    public ConfigurationService() {
        nodeService = new NodeService();
        assignmentService = new AssignmentService();
        associationsService = new AssociationsService();
    }

    public void connect(String database, String host, int port, String schema, String username, String password) throws DatabaseException, ConfigurationException, SQLException, IOException, ClassNotFoundException, InvalidPropertyException {
        Properties props = new Properties();
        props.put("database", database);
        props.put("host", host);
        props.put("port", String.valueOf(port));
        props.put("username", username);
        props.put("password", password);
        props.put("schema", schema == null ? "" : schema);
        PIP.init(props);
    }

    public void setInterval(int interval) throws ConfigurationException {
        //DAO.setInterval(interval);
    }

    public void importData(String host, int port, String schema, String username, String password, String session, long process)
            throws DatabaseException, NodeNotFoundException, ConfigurationException, AssignmentExistsException,
            InvalidPropertyException, InvalidNodeTypeException, NameInNamespaceNotFoundException, InvalidAssignmentException, SQLException, IOException, ClassNotFoundException, UnexpectedNumberOfNodesException, AssociationExistsException, PropertyNotFoundException, SessionDoesNotExistException, SessionUserNotFoundException, InvalidAssociationException {
        //create the schema policy class node
        Map<String, String> properties = new HashMap<>();
        properties.put(Constants.SCHEMA_COMP_PROPERTY, SCHEMA_COMP_SCHEMA_PROPERTY);
        properties.put(Constants.DESCRIPTION_PROPERTY, "Policy Class for " + schema);
        OldNode pcNode = createNode(schema, NodeType.PC.toString(), properties);

        properties.clear();
        properties.put(Constants.SCHEMA_COMP_PROPERTY, Constants.SCHEMA_COMP_SCHEMA_PROPERTY);
        properties.put(Constants.DESCRIPTION_PROPERTY, "Base Object Attribute for " + schema);
        // create the schema object attribute node
        OldNode schemaNode = createNode(schema, NodeType.OA.toString(), properties);

        //assign oa node to pc node
        assignmentService.createAssignment(schemaNode.getID(), pcNode.getID());

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + schema, username, password)) {
                Statement stmt = conn.createStatement();
                stmt.execute("use " + schema);
                ResultSet rs = stmt.executeQuery("show full tables where Table_Type = 'BASE TABLE'");
                while (rs.next()) {
                    List<String> keys = new ArrayList<>();

                    String tableName = rs.getString(1);

                    //get primary keys to make name
                    PreparedStatement ps2 = conn.prepareStatement("SELECT k.COLUMN_NAME\n" +
                            "FROM information_schema.table_constraints t\n" +
                            "LEFT JOIN information_schema.key_column_usage k\n" +
                            "USING(constraint_name,table_schema,table_name)\n" +
                            "WHERE t.constraint_type='PRIMARY KEY'\n" +
                            "    AND t.table_schema=DATABASE()\n" +
                            "    AND t.table_name='" + tableName + "' order by ordinal_position;");
                    ResultSet rs3 = ps2.executeQuery();
                    while (rs3.next()) {
                        keys.add(rs3.getString(1));
                    }

                    //create table node
                    properties.clear();
                    properties.put(Constants.SCHEMA_NAME_PROPERTY, schema);
                    properties.put(NAMESPACE_PROPERTY, tableName);
                    properties.put(Constants.SCHEMA_COMP_PROPERTY, Constants.SCHEMA_COMP_TABLE_PROPERTY);
                    OldNode tableNode = createNode(tableName, NodeType.OA.toString(), properties);

                    //assign table node to policy class node
                    assignmentService.createAssignment(tableNode.getID(), schemaNode.getID());

                    //create columns container
                    properties.clear();
                    properties.put(NAMESPACE_PROPERTY, tableName);
                    properties.put(DESCRIPTION_PROPERTY, "Column container for " + tableName);
                    OldNode columnsNode = createNode(Constants.COLUMN_CONTAINER_NAME, NodeType.OA.toString(), properties);
                    assignmentService.createAssignment(columnsNode.getID(), tableNode.getID());

                    //create columns
                    Statement stmt1 = conn.createStatement();
                    String colSql = "SELECT c.column_name FROM INFORMATION_SCHEMA.COLUMNS c WHERE c.table_name = '" + tableName + "' AND c.table_schema = '" + schema + "'";
                    ResultSet rs1 = stmt1.executeQuery(colSql);
                    String columnSql = "";
                    while (rs1.next()) {
                        String columnName = rs1.getString(1);
                        System.out.println("creating column " + columnName);

                        properties.clear();
                        properties.put(NAMESPACE_PROPERTY, tableName);
                        OldNode columnNode = createNode(columnName, NodeType.OA.toString(), properties);

                        //assign column node to table
                        assignmentService.createAssignment(columnNode.getID(), columnsNode.getID());

                        columnSql += columnName + ", ";
                    }
                    columnSql = columnSql.substring(0, columnSql.length() - 2);

                    //create rows
                    if (!columnSql.isEmpty()) {
                        //create rows containers
                        properties.clear();
                        properties.put(NAMESPACE_PROPERTY, tableName);
                        properties.put(DESCRIPTION_PROPERTY, "Row container for " + tableName);
                        OldNode rowsNode = createNode(Constants.ROW_CONTAINER_NAME, NodeType.OA.toString(), properties);
                        assignmentService.createAssignment(rowsNode.getID(), tableNode.getID());

                        //get data from table
                        String sql = "select " + columnSql + " from " + tableName;
                        Statement stmt2 = conn.createStatement();
                        ResultSet rs2 = stmt2.executeQuery(sql);
                        ResultSetMetaData rs2MetaData = rs2.getMetaData();
                        int numCols = rs2MetaData.getColumnCount();
                        while (rs2.next()) {
                            //creating rows
                            String rowName = "";
                            for (int i = 1; i <= numCols; i++) {
                                String columnName = rs2MetaData.getColumnName(i);
                                if (keys.contains(columnName)) {
                                    String value = String.valueOf(rs2.getObject(i));
                                    if (rowName.isEmpty()) {
                                        rowName += value;
                                    }
                                    else {
                                        rowName += "+" + value;
                                    }
                                }
                            }
                            System.out.println("creating row " + rowName);


                            //create row node
                            properties.clear();
                            properties.put(NAMESPACE_PROPERTY, tableName);
                            properties.put(Constants.SCHEMA_COMP_PROPERTY, Constants.SCHEMA_COMP_ROW_PROPERTY);
                            OldNode rowNode = createNode(rowName, NodeType.OA.toString(), properties);

                            //assign row node to table
                            assignmentService.createAssignment(rowNode.getID(), rowsNode.getID());

                            //create data objects, assign to row and column
                            for (int i = 1; i <= rs2MetaData.getColumnCount(); i++) {
                                //get column
                                String columnName = rs2MetaData.getColumnName(i);
                                Map<String, String> searchProps = new HashMap<>();
                                searchProps.put(NAMESPACE_PROPERTY, tableName);
                                OldNode columnNode = nodeService.getNode(columnName, NodeType.OA.toString(), searchProps);


                                //create data object node
                                String objectName = UUID.randomUUID().toString();
                                properties.clear();
                                properties.put(NAMESPACE_PROPERTY, tableName);
                                properties.put(DESCRIPTION_PROPERTY, "Object in table=" + tableName + ", row=" + rowName + ", column=" + columnNode
                                        .getName());
                                OldNode objectNode = createNode(objectName, NodeType.O.toString(), properties);

                                //assign object to row and column
                                assignmentService.createAssignment(objectNode.getID(), rowNode.getID());
                                assignmentService.createAssignment(objectNode.getID(), columnNode.getID());
                            }
                        }
                    }
                }
            }

        }catch(SQLException | ClassNotFoundException e){
            e.printStackTrace();
            throw new DatabaseException(PMException.CLIENT_ERROR, e.getMessage());
        }
    }

    private OldNode createNode(String name, String type, Map<String, String> properties) throws DatabaseException, InvalidPropertyException, ConfigurationException, InvalidNodeTypeException, SQLException, IOException, ClassNotFoundException {
        //create node in database
        NodeType nt = NodeType.toNodeType(type);
        OldNode newNode = getDaoManager().getNodesDAO().createNode(0, name, nt, properties);

        //add the node to the nodes
        getGraph().addNode(newNode);

        return newNode;
    }

    public Table getData(String host, int port, String username, String password, String database, String tableName, String session, long process) throws InvalidNodeTypeException, InvalidPropertyException, PropertyNotFoundException, NodeNotFoundException, NameInNamespaceNotFoundException, IOException, DatabaseException, SessionDoesNotExistException, SessionUserNotFoundException, UnexpectedNumberOfNodesException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Map<String, String> searchProps;
            Table table;
            List<String> keys;
            HashSet<OldNode> rowNodes;
            String select;
            Statement stmt;
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, username, password)) {

                //get table node
                searchProps = new HashMap<>();
                searchProps.put(NAMESPACE_PROPERTY, tableName);
                OldNode tableNode = nodeService.getNode(tableName, NodeType.OA.toString(), searchProps);

                table = new Table();
                table.setNode(tableNode);

                HashSet<OldNode> children = nodeService.getChildrenOfType(tableNode.getID(), NodeType.OA.toString());

                OldNode columnsNode = null;
                OldNode rowsNode = null;
                for (OldNode node : children) {
                    if (node.getName().equals(Constants.COLUMN_CONTAINER_NAME)) {
                        columnsNode = node;
                    }
                    else if (node.getName().equals(Constants.ROW_CONTAINER_NAME)) {
                        rowsNode = node;
                    }
                }

                if (columnsNode == null || rowsNode == null) {
                    throw new NodeNotFoundException(columnsNode == null ? Constants.COLUMN_CONTAINER_NAME : Constants.ROW_CONTAINER_NAME);
                }

                //get column Nodes
                HashSet<OldNode> columnNodes = nodeService.getChildrenOfType(columnsNode.getID(), NodeType.OA.toString());

                List<Column> columns = new ArrayList<>();
                String cols = "";
                for (OldNode col : columnNodes) {
                    Column column = new Column(col, col.getName());
                    columns.add(column);

                    if (cols.isEmpty()) {
                        cols += col.getName();
                    }
                    else {
                        cols += "," + col.getName();
                    }
                }

                table.setColumns(columns);

                //get table keys
                keys = new ArrayList<>();
                PreparedStatement ps2 = conn.prepareStatement("SELECT k.COLUMN_NAME\n" +
                        "FROM information_schema.table_constraints t\n" +
                        "LEFT JOIN information_schema.key_column_usage k\n" +
                        "USING(constraint_name,table_schema,table_name)\n" +
                        "WHERE t.constraint_type='PRIMARY KEY'\n" +
                        "    AND t.table_schema=DATABASE()\n" +
                        "    AND t.table_name='" + tableName + "';");
                ResultSet rs3 = ps2.executeQuery();
                while (rs3.next()) {
                    keys.add(rs3.getString(1));
                }

                //get all row nodes
                rowNodes = nodeService.getChildrenOfType(rowsNode.getID(), NodeType.OA.toString());

                //get row values
                select = "select " + cols + " from " + tableName;
                stmt = conn.createStatement();
            }
            List<Row> rows;
            try (ResultSet rs = stmt.executeQuery(select)) {
                rows = new ArrayList<>();
                int rowIndex = 0;
                int numCols = rs.getMetaData().getColumnCount();
                while (rs.next()) {
                    Row row = new Row();
                    String rowName = "";
                    List<Object> rowValues = new ArrayList<>();
                    for (int i = 1; i <= numCols; i++) {
                        //add row value
                        String value = String.valueOf(rs.getObject(i));
                        rowValues.add(rs.getObject(i));

                        //get column name
                        String columnName = rs.getMetaData().getColumnName(i);

                        //construct rowName
                        if (keys.contains(columnName)) {
                            if (rowName.isEmpty()) {
                                rowName += value;
                            }
                            else {
                                rowName += "+" + value;
                            }
                        }
                    }
                    row.setRowValues(rowValues);

                    //get row node
                    OldNode rowNode = null;
                    for (OldNode rN : rowNodes) {
                        if (rN.getName().equals(rowName)) {
                            rowNode = rN;
                        }
                    }

                    if (rowNode == null) {
                        throw new NodeNotFoundException(rowName);
                    }

                    row.setNode(rowNode);

                    //now, get the objects that intersect the current row and columns
                    List<OldNode> rowNodesList = new ArrayList<>();
                    for (int i = 1; i <= numCols; i++) {
                        //get column name
                        String columnName = rs.getMetaData().getColumnName(i);

                        //get columnNode
                        searchProps.clear();
                        searchProps.put(NAMESPACE_PROPERTY, tableName);
                        OldNode columnNode = nodeService.getNode(columnName, NodeType.OA.toString(), searchProps);

                        HashSet<OldNode> colChildren = nodeService.getChildrenOfType(columnNode.getID(), NodeType.O.toString());
                        HashSet<OldNode> rowChildren = nodeService.getChildrenOfType(rowNode.getID(), NodeType.O.toString());

                        colChildren.retainAll(rowChildren);

                        rowNodesList.add(colChildren.iterator().next());
                    }

                    row.setRowNodes(rowNodesList);

                    rows.add(row);
                }
            }
            table.setRows(rows);

            return table;
        }
        catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void uploadFiles(String[] files, String session, long process) throws NullNameException, NodeIDExistsException, NodeNameExistsInNamespaceException, NodeNameExistsException, NoSuchAlgorithmException, AssignmentExistsException, DatabaseException, InvalidNodeTypeException, InvalidPropertyException, InvalidKeySpecException, ConfigurationException, NullTypeException, NodeNotFoundException, InvalidAssignmentException, IOException, ClassNotFoundException, SQLException, UnexpectedNumberOfNodesException, AssociationExistsException, NoBaseIDException, PropertyNotFoundException, InvalidAssociationException, SessionDoesNotExistException, SessionUserNotFoundException, NoSubjectParameterException, MissingPermissionException, InvalidProhibitionSubjectTypeException, PolicyClassNameExistsException {
        for(String file : files) {
            String[] split = file.split("/");
            for(int i = 0; i < split.length; i++) {
                String fileStr = split[i];
                String[] filePieces = fileStr.split(":");
                String fileName = filePieces[0];
                String fileID = filePieces[1];

                //get namespace
                String namespace;
                if(i == 0) {
                    namespace = fileName;
                } else {
                    namespace = split[i-1].split(":")[0];
                }

                String parentName = "";
                String parentNamespace = "";
                if(i == 1) {
                    parentName = split[i-1].split(":")[0];
                    parentNamespace = split[i-1].split(":")[0];
                } else if (i > 1){
                    parentName = split[i-1].split(":")[0];
                    parentNamespace = split[i-2].split(":")[0];
                }

                OldNode node = null;
                OldNode parentNode = null;
                try {
                    Map<String, String> searchProps = new HashMap<>();
                    searchProps.put(NAMESPACE_PROPERTY, parentNamespace);
                    parentNode = nodeService.getNode(parentName, NodeType.OA.toString(), searchProps);
                }catch (Exception e){}

                try {
                    Map<String, String> searchProps = new HashMap<>();
                    searchProps.put(NAMESPACE_PROPERTY, namespace);
                    node = nodeService.getNode(fileName, NodeType.OA.toString(), searchProps);
                }catch (Exception e) {}

                if(node == null) {
                    //create node
                    if(parentNode == null) {
                        //create pc
                        parentNode = nodeService.createPolicy(fileName, null, session, process);
                    }
                    Map<String, String> properties = new HashMap<>();
                    properties.put(NAMESPACE_PROPERTY, namespace);
                    properties.put("storage", "google");
                    properties.put("uuid", fileID);
                    nodeService.createNodeIn(parentNode.getID(), fileName,
                            file.endsWith("/") ? NodeType.OA.toString() : NodeType.O.toString(),
                            properties, session, process);
                    System.out.println("creating node " + fileName + " (" + fileID + ") in namespace " + namespace);
                }
            }
        }
    }

    class JsonGraph {
        HashSet<OldNode>         nodes;
        HashSet<JsonAssignment>  assignments;
        HashSet<JsonAssociation> associations;

        public JsonGraph(HashSet<OldNode> nodes, HashSet<JsonAssignment> assignments, HashSet<JsonAssociation> associations) {
            this.nodes = nodes;
            this.assignments = assignments;
            this.associations = associations;
        }

        public HashSet<OldNode> getNodes() {
            return nodes;
        }

        public HashSet<JsonAssignment> getAssignments() {
            return assignments;
        }

        public HashSet<JsonAssociation> getAssociations() {
            return associations;
        }
    }

    class JsonAssignment {
        long child;
        long parent;

        public JsonAssignment(long child, long parent) {
            this.child = child;
            this.parent = parent;
        }

        public long getChild() {
            return child;
        }

        public long getParent() {
            return parent;
        }
    }

    class JsonAssociation {
        long ua;
        long target;
        HashSet<String> ops;

        public JsonAssociation(long ua, long target, HashSet<String> ops) {
            this.ua = ua;
            this.target = target;
            this.ops = ops;
        }

        public long getUa() {
            return ua;
        }

        public long getTarget() {
            return target;
        }

        public HashSet<String> getOps() {
            return ops;
        }
    }

    public String save() throws ClassNotFoundException, SQLException, DatabaseException, IOException, InvalidPropertyException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        HashSet<OldNode> nodes = getGraph().getNodes();

        HashSet<Assignment> assignments = getGraph().getAssignments();
        HashSet<JsonAssignment> jsonAssignments = new HashSet<>();
        for(Assignment assignment : assignments) {
            jsonAssignments.add(new JsonAssignment(assignment.getChild().getID(), assignment.getParent().getID()));
        }

        List<Association> associations = getGraph().getAssociations();
        HashSet<JsonAssociation> jsonAssociations = new HashSet<>();
        for(Association association : associations) {
            jsonAssociations.add(new JsonAssociation(association.getChild().getID(),
                    association.getParent().getID(), association.getOps()));
        }

        return gson.toJson(new JsonGraph(nodes, jsonAssignments, jsonAssociations));
    }

    public void load(String config) throws InvalidPropertyException, DatabaseException, ClassNotFoundException, IOException, SQLException, InvalidKeySpecException, NoSuchAlgorithmException {
        JsonGraph graph = new Gson().fromJson(config, JsonGraph.class);

        HashSet<OldNode> nodes = graph.getNodes();
        HashMap<Long, OldNode> nodesMap = new HashMap<>();
        for(OldNode node : nodes) {
            Map<String, String> properties = node.getProperties();

            //if a password is present encrypt it if not already
            // a password is considered not encrypted if the length is less then 163 (HASH_LENGTH)
            if (properties != null &&
                    properties.get(PASSWORD_PROPERTY) != null &&
                    properties.get(PASSWORD_PROPERTY).length() < HASH_LENGTH) {
                properties.put(PASSWORD_PROPERTY, generatePasswordHash(properties.get(PASSWORD_PROPERTY)));
            }

            getDaoManager().getNodesDAO().createNode(node.getID(), node.getName(), node.getType(), properties);
            nodesMap.put(node.getID(), node);
        }

        HashSet<JsonAssignment> assignments = graph.getAssignments();
        for(JsonAssignment assignment : assignments) {
            // child - assigned to -> parent
            if(assignment != null &&
                    nodesMap.get(assignment.getChild()) != null &&
                    nodesMap.get(assignment.getParent()) != null) {
                System.out.println(assignment.getChild() + "-->" + assignment.getParent());
                getDaoManager().getAssignmentsDAO().createAssignment(nodesMap.get(assignment.getChild()), nodesMap.get(assignment.getParent()));
            }
        }

        HashSet<JsonAssociation> associations = graph.getAssociations();
        for(JsonAssociation association : associations) {
            if(nodesMap.get(association.getUa()) != null &&
                    nodesMap.get(association.getTarget()) != null) {
                System.out.println(association.getUa() + "-->" + association.getTarget() + association.getOps());
                getDaoManager().getAssociationsDAO()
                        .createAssociation(association.getUa(), association.getTarget(), association.getOps());
            }
        }
    }

    public void reset() throws SQLException, IOException, ClassNotFoundException, DatabaseException, InvalidPropertyException {
        getDaoManager().getGraphDAO().reset();
        getDaoManager().getGraphDAO().buildGraph();
    }

    public JsonNode getJsonGraph(String session, long process) throws InvalidNodeTypeException, InvalidPropertyException, NodeNotFoundException, ClassNotFoundException, SQLException, IOException, DatabaseException, SessionDoesNotExistException, SessionUserNotFoundException {
        Set<OldNode> cNodes = nodeService.getNodes("PM", "OA", null);
        OldNode cNode = cNodes.iterator().next();
        JsonNode root = new JsonNode((int)cNode.getID(), cNode.getName(), "C", cNode.getProperties(), getJsonNodes(cNode.getID()));
        return root;
    }
    public JsonNode getUserGraph(String session, long process) throws InvalidNodeTypeException, InvalidPropertyException, NodeNotFoundException, ClassNotFoundException, SQLException, IOException, DatabaseException, SessionDoesNotExistException, SessionUserNotFoundException {
        System.out.println("in get user graph");
        Set<OldNode> cNodes = nodeService.getNodes("PM", "OA", null);
        OldNode cNode = cNodes.iterator().next();
        JsonNode root = new JsonNode((int)cNode.getID(), cNode.getName(), "C", cNode.getProperties(), getJsonUserNodes(cNode.getID()));
        return root;
    }
    public JsonNode getObjGraph(String session, long process) throws InvalidNodeTypeException, InvalidPropertyException, NodeNotFoundException, ClassNotFoundException, SQLException, IOException, DatabaseException, SessionDoesNotExistException, SessionUserNotFoundException {
        Set<OldNode> cNodes = nodeService.getNodes("PM", "OA", null);
        OldNode cNode = cNodes.iterator().next();
        JsonNode root = new JsonNode((int)cNode.getID(), cNode.getName(), "C", cNode.getProperties(), getJsonObjNodes(cNode.getID()));
        return root;
    }

    List<JsonNode> getJsonUserNodes(long id) throws NodeNotFoundException, InvalidNodeTypeException, ClassNotFoundException, SQLException, DatabaseException, IOException, InvalidPropertyException {
        System.out.println("in get user nodes");
        List<JsonNode> jsonNodes = new ArrayList<>();
        HashSet<OldNode> children = nodeService.getChildrenOfType(id, "PC");
        children.addAll(nodeService.getChildrenOfType(id, "U"));
        children.addAll(nodeService.getChildrenOfType(id, "UA"));
        for(OldNode node : children) {
            jsonNodes.add(new JsonNode(
                    node.getID(),
                    node.getName(),
                    node.getType().toString(),
                    node.getProperties(),
                    getJsonNodes(node.getID())));
        }

        if(jsonNodes.isEmpty()){
            return null;
        }else {
            return jsonNodes;
        }
    }

    List<JsonNode> getJsonObjNodes(long id) throws NodeNotFoundException, InvalidNodeTypeException, ClassNotFoundException, SQLException, DatabaseException, IOException, InvalidPropertyException {
        List<JsonNode> jsonNodes = new ArrayList<>();
        HashSet<OldNode> children = nodeService.getChildrenOfType(id, "PC");
        children.addAll(nodeService.getChildrenOfType(id, "O"));
        children.addAll(nodeService.getChildrenOfType(id, "OA"));
        for(OldNode node : children) {
            jsonNodes.add(new JsonNode(
                    node.getID(),
                    node.getName(),
                    node.getType().toString(),
                    node.getProperties(),
                    getJsonNodes(node.getID())));
        }

        if(jsonNodes.isEmpty()){
            return null;
        }else {
            return jsonNodes;
        }
    }

    class graph {
        HashSet<OldNode>    nodes;
        HashSet<Assignment> links;

        public graph(HashSet<OldNode> nodes, HashSet<Assignment> links) {
            this.nodes = nodes;
            this.links = links;
        }

        public HashSet<OldNode> getNodes() {
            return nodes;
        }

        public void setNodes(HashSet<OldNode> nodes) {
            this.nodes = nodes;
        }

        public HashSet<Assignment> getLinks() {
            return links;
        }

        public void setLinks(HashSet<Assignment> links) {
            this.links = links;
        }
    }

    List<JsonNode> getJsonNodes(long id) throws NodeNotFoundException, InvalidNodeTypeException, ClassNotFoundException, SQLException, DatabaseException, IOException, InvalidPropertyException {
        List<JsonNode> jsonNodes = new ArrayList<>();
        HashSet<OldNode> children = nodeService.getChildrenOfType(id, null);
        for(OldNode node : children) {
            jsonNodes.add(new JsonNode(
                    node.getID(),
                    node.getName(),
                    node.getType().toString(),
                    node.getProperties(),
                    getJsonNodes(node.getID())));
        }

        if(jsonNodes.isEmpty()){
            return null;
        }else {
            return jsonNodes;
        }
    }

    public class JsonNode {
        String     id;
        long       nodeID;
        String     name;
        String     type;
        Map<String, String> properties;
        List<JsonNode>          children;

        public JsonNode(long id, String name, String type, Map<String, String> properties, List<JsonNode> children) {
            this.id = Integer.toString((int)id);
            this.nodeID = id;
            this.name = name;
            this.type = type;
            this.properties = properties;
            this.children = children;
        }

        public String getID() {
            return id;
        }

        public void setID(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Map<String, String> getProperties() {
            return properties;
        }

        public void setProperties(Map<String, String> properties) {
            this.properties = properties;
        }

        public List<JsonNode> getChildren() {
            return children;
        }

        public void setChildren(List<JsonNode> children) {
            this.children = children;
        }
    }

    public class Table{
        OldNode      node;
        List<Column> columns;
        List<Row>    rows;

        public OldNode getNode() {
            return node;
        }

        public void setNode(OldNode node) {
            this.node = node;
        }

        public List<Column> getColumns() {
            return columns;
        }

        public void setColumns(List<Column> columns) {
            this.columns = columns;
        }

        public List<Row> getRows() {
            return rows;
        }

        public void setRows(List<Row> rows) {
            this.rows = rows;
        }
    }

    class Column{
        OldNode node;
        String  column;

        public Column(OldNode node, String column){
            this.node = node;
            this.column = column;
        }

        public OldNode getNode() {
            return node;
        }

        public void setNode(OldNode node) {
            this.node = node;
        }

        public String getColumn() {
            return column;
        }

        public void setColumn(String column) {
            this.column = column;
        }
    }

    class Row{
        OldNode       node;
        List<OldNode> rowNodes;
        List<Object>  rowValues;

        public OldNode getNode() {
            return node;
        }

        public void setNode(OldNode node) {
            this.node = node;
        }

        public List<OldNode> getRowNodes() {
            return rowNodes;
        }

        public void setRowNodes(List<OldNode> rowNodes) {
            this.rowNodes = rowNodes;
        }

        public List<Object> getRowValues() {
            return rowValues;
        }

        public void setRowValues(List<Object> rowValues) {
            this.rowValues = rowValues;
        }
    }
}