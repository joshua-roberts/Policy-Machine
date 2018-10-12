package gov.nist.csd.pm.pdp.services;

import gov.nist.csd.pm.model.Constants;
import gov.nist.csd.pm.model.exceptions.*;
import gov.nist.csd.pm.model.graph.Node;
import gov.nist.csd.pm.demos.cloud.ImportFile;
import gov.nist.csd.pm.model.graph.NodeType;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.*;
import java.util.*;

import static gov.nist.csd.pm.model.Constants.*;
import static gov.nist.csd.pm.model.graph.NodeType.OA;
import static gov.nist.csd.pm.model.graph.NodeType.PC;
import static gov.nist.csd.pm.pip.dao.DAOManager.getDaoManager;

public class ImportService {

    private NodeService        nodeService;
    private AssignmentService  assignmentService;
    private AssociationsService associationsService;

    public ImportService() {
        nodeService = new NodeService();
        assignmentService = new AssignmentService();
        associationsService = new AssociationsService();
    }

    public void importFiles(ImportFile[] files, String storage, String session, long process) throws InvalidPropertyException,
            AssignmentExistsException, InvalidNodeTypeException, NodeNotFoundException, ClassNotFoundException, NodeIDExistsException, NodeNameExistsException, NodeNameExistsInNamespaceException, IOException, ConfigurationException, SQLException, NullNameException, DatabaseException, NullTypeException, InvalidAssignmentException, UnexpectedNumberOfNodesException, AssociationExistsException, NoBaseIDException, PropertyNotFoundException, NoSubjectParameterException, SessionDoesNotExistException, InvalidProhibitionSubjectTypeException, SessionUserNotFoundException, MissingPermissionException, InvalidAssociationException, InvalidKeySpecException, NoSuchAlgorithmException {
        for(ImportFile importFile : files) {
            String name = importFile.getPath();
            String bucket = importFile.getBucket();
            String contentType = importFile.getContentType();
            long size = importFile.getSize();

            Node bucketNode;
            try {
                Map<String, String> searchProps = new HashMap<>();
                searchProps.put(NAMESPACE_PROPERTY, bucket);
                bucketNode = nodeService.getNode(bucket, OA.toString(),searchProps);
            }
            catch (UnexpectedNumberOfNodesException e) {
                //create bucket pc
                Map<String, String> properties = new HashMap<>();
                properties.put(BUCKET_PROPERTY, bucket);
                properties.put(STORAGE_PROPERTY, storage);
                bucketNode = nodeService.createPolicy(bucket, properties, session, process);

                //create bucket OA
                properties.clear();
                properties.put(NAMESPACE_PROPERTY, bucket);
                properties.put(STORAGE_PROPERTY, storage);
                bucketNode = nodeService.createNodeIn(bucketNode.getID(), bucket, OA.toString(), properties, session, process);

            }


            String[] split = name.split("/");
            for(int i = 0; i < split.length; i++) {
                String fileName = split[i];

                String parentName;
                String parentNamespace;

                //get namespace
                String namespace;
                if(i == 0) {
                    namespace = bucket;
                } else {
                    namespace = split[i-1].split(":")[0];
                }

                if(i == 0) {
                    parentName = bucket;
                    parentNamespace = bucket;
                } else if(i == 1) {
                    parentName = split[i-1];
                    parentNamespace = bucket;
                } else {
                    parentName = split[i-1];
                    parentNamespace = split[i-2];
                }

                Node node = null;
                Node parentNode = null;
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
                        parentNode = nodeService.createNodeIn(bucketNode.getID(), parentName,
                                OA.toString(), null, session, process);
                    }
                    Map<String, String> properties = new HashMap<>();
                    properties.put(NAMESPACE_PROPERTY, namespace);
                    properties.put(STORAGE_PROPERTY, storage);
                    properties.put(BUCKET_PROPERTY, bucket);
                    properties.put(PATH_PROPERTY, name);
                    properties.put(CONTENT_TYPE_PROPERTY, contentType);
                    properties.put(SIZE_PROPERTY, String.valueOf(size));
                    nodeService.createNodeIn(parentNode.getID(), fileName,
                            (i == split.length-1) ? NodeType.O.toString() : OA.toString(), properties, session, process);
                    System.out.println("creating node " + fileName + " in namespace " + namespace);
                }
            }
        }
    }

    public void importEntities(String kind, HashMap<String, Object>[] entities) throws InvalidPropertyException, AssignmentExistsException, InvalidNodeTypeException, NodeNotFoundException, ClassNotFoundException, NodeIDExistsException, NodeNameExistsException, NodeNameExistsInNamespaceException, IOException, ConfigurationException, SQLException, NullNameException, DatabaseException, NullTypeException, InvalidAssignmentException, UnexpectedNumberOfNodesException, AssociationExistsException, NoBaseIDException, PropertyNotFoundException, InvalidAssociationException, InvalidKeySpecException, NoSuchAlgorithmException {
        //create pc for kind
        Node pcNode = nodeService.createNode(NEW_NODE_ID, kind, PC.toString(), null);

        //create base OA for kind
        Node oaNode = nodeService.createNode(NEW_NODE_ID, kind, OA.toString(), null);

        assignmentService.createAssignment(oaNode.getID(), pcNode.getID());

        //for each property, add object with property just the name NO VALUES

        for (HashMap<String, Object> map : entities) {
            long id = 0;
            for (String key : map.keySet()) {
                if(key.equalsIgnoreCase("id")) {
                    id = (long) map.get(key);
                }
            }

            //create row node
            Map<String, String> properties = new HashMap<>();
            properties.put("kind", kind);
                    properties.put("id", String.valueOf(id));
            Node entityNode = nodeService.createNode(NEW_NODE_ID, String.valueOf(id), OA.toString(), properties);
            assignmentService.createAssignment(oaNode.getID(), entityNode.getID());


            for (String key : map.keySet()) {
                properties.clear();
                properties.put("value", key);
                Node node = nodeService.createNode(NEW_NODE_ID, key, NodeType.O.toString(), properties);
                assignmentService.createAssignment(node.getID(), entityNode.getID());
            }
        }
    }

    public void importSql(String host, int port, String schema, String username, String password, String session, long process)
            throws DatabaseException, NodeNotFoundException, ConfigurationException, AssignmentExistsException,
            InvalidPropertyException, InvalidNodeTypeException, NameInNamespaceNotFoundException, InvalidAssignmentException, SQLException, IOException, ClassNotFoundException, NodeNameExistsException, NodeNameExistsInNamespaceException, NodeIDExistsException, NullTypeException, NullNameException, UnexpectedNumberOfNodesException, AssociationExistsException, PropertyNotFoundException, InvalidAssociationException, SessionDoesNotExistException, SessionUserNotFoundException, NoSubjectParameterException, InvalidProhibitionSubjectTypeException, InvalidKeySpecException, NoSuchAlgorithmException, MissingPermissionException {
        //create the schema policy class node
        Map<String, String> properties = new HashMap<>();
                properties.put(Constants.SCHEMA_COMP_PROPERTY, Constants.SCHEMA_COMP_SCHEMA_PROPERTY);
                properties.put(Constants.DESCRIPTION_PROPERTY, "Policy Class for " + schema);

        Node pcNode = nodeService.createPolicy(schema, properties, session, process);

        properties.clear();
                properties.put(Constants.SCHEMA_COMP_PROPERTY, Constants.SCHEMA_COMP_SCHEMA_PROPERTY);
                properties.put(Constants.DESCRIPTION_PROPERTY, "Base Object Attribute for " + schema);
        // create the schema object attribute node
        Node schemaNode = nodeService.createNodeIn(pcNode.getID(), schema, OA.toString(), properties, session, process);

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + schema, username, password);
            Statement stmt = conn.createStatement();
            stmt.execute("use " + schema);
            ResultSet rs = stmt.executeQuery("show full tables where Table_Type = 'BASE TABLE'");
            while(rs.next()){
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
                while(rs3.next()){
                    keys.add(rs3.getString(1));
                }

                //create table node
                properties.clear();
                        properties.put(Constants.SCHEMA_NAME_PROPERTY, schema);
                        properties.put(NAMESPACE_PROPERTY, schema);
                        properties.put(Constants.SCHEMA_COMP_PROPERTY, Constants.SCHEMA_COMP_TABLE_PROPERTY);
                Node tableNode = nodeService.createNodeIn(schemaNode.getID(), tableName,
                        OA.toString(), properties, session, process);

                //create columns container
                properties.clear();
                        properties.put(NAMESPACE_PROPERTY, tableName);
                        properties.put(DESCRIPTION_PROPERTY, "Column container for " + tableName);
                Node columnsNode = nodeService.createNodeIn(tableNode.getID(), Constants
                                .COLUMN_CONTAINER_NAME, OA.toString(), properties, session, process);
                //create columns
                Statement stmt1 = conn.createStatement();
                String colSql = "SELECT c.column_name FROM INFORMATION_SCHEMA.COLUMNS c WHERE c.table_name = '" + tableName + "' AND c.table_schema = '" + schema + "'";
                ResultSet rs1 = stmt1.executeQuery(colSql);
                String columnSql = "";
                HashMap<String, Node> columnNodes = new HashMap<>();
                while(rs1.next()){
                    String columnName = rs1.getString(1);
                    System.out.println("creating column " + columnName);

                    properties.clear();
                            properties.put(NAMESPACE_PROPERTY, tableName);
                    Node columnNode = nodeService.createNodeIn(columnsNode.getID(), columnName, OA.toString(),
                            properties, session, process);
                    columnNodes.put(columnName, columnNode);

                    columnSql += columnName + ", ";
                }
                columnSql = columnSql.substring(0, columnSql.length()-2);

                //create rows
                if(!columnSql.isEmpty()){
                    //create rows containers
                    properties.clear();
                            properties.put(NAMESPACE_PROPERTY, tableName);
                            properties.put(DESCRIPTION_PROPERTY, "Row container for " + tableName);
                    Node rowsNode = nodeService.createNodeIn(tableNode.getID(), Constants.ROW_CONTAINER_NAME,
                            OA.toString(), properties, session, process);

                    //get data from table
                    String sql = "select " + columnSql + " from " + tableName;
                    Statement stmt2 = conn.createStatement();
                    ResultSet rs2 = stmt2.executeQuery(sql);
                    ResultSetMetaData rs2MetaData = rs2.getMetaData();
                    int numCols = rs2MetaData.getColumnCount();
                    while(rs2.next()){
                        //creating rows
                        String rowName = "";
                        for(int i = 1; i <= numCols; i++){
                            String columnName = rs2MetaData.getColumnName(i);
                            if(keys.contains(columnName)){
                                String value = String.valueOf(rs2.getObject(i));
                                if(rowName.isEmpty()){
                                    rowName += value;
                                }else{
                                    rowName += "+" + value;
                                }
                            }
                        }
                        System.out.println("creating row " + rowName);


                        //create row node
                        properties.clear();
                                properties.put(NAMESPACE_PROPERTY, tableName);
                                properties.put(Constants.SCHEMA_COMP_PROPERTY, Constants.SCHEMA_COMP_ROW_PROPERTY);
                        Node rowNode = nodeService.createNode(NEW_NODE_ID, rowName, OA.toString(), properties);
                        /*Node rowNode = nodeService.createNode(rowsNode.getID(), NEW_NODE_ID, rowName, NodeType.OA
                                        .toString(),
                                properties);*/
                        assignmentService.createAssignment(rowNode, rowsNode);

                        //create data objects, assign to row and column
                        for(int i = 1; i <= rs2MetaData.getColumnCount(); i++){
                            //get column
                            String columnName = rs2MetaData.getColumnName(i);
                            Node columnNode = columnNodes.get(columnName);//nodeService.getNodeInNamespace(tableName, columnName, NodeType.OA, session, process);


                            //create data object node
                            String objectName = rowName + "_" + columnName;
                            properties.clear();
                                    properties.put(NAMESPACE_PROPERTY, tableName);
                                    properties.put(DESCRIPTION_PROPERTY, "Object in table=" + tableName + ", row=" + rowName + ", column=" + columnNode.getName());
                            /*Node objectNode = nodeService.createNode(rowNode.getID(), NEW_NODE_ID, objectName, NodeType.O.toString(),
                                    properties);*/
                            Node objectNode = nodeService.createNode(NEW_NODE_ID, objectName, NodeType.O.toString(), properties);
                            assignmentService.createAssignment(objectNode, rowNode);

                            //assign object to row and column
                            assignmentService.createAssignment(objectNode, columnNode);
                        }
                    }
                }
            }

        }catch(SQLException | ClassNotFoundException e){
            e.printStackTrace();
            throw new DatabaseException(PmException.CLIENT_ERROR, e.getMessage());
        }
        catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    void createAssociation(long uaId, long targetID, HashSet<String> ops) throws ClassNotFoundException, SQLException, DatabaseException, InvalidPropertyException, IOException {
        //create association in database
        getDaoManager().getAssociationsDAO().createAssociation(uaId, targetID, ops);

        //create association in nodes
        getDaoManager().getGraphDAO().getGraph().createAssociation(uaId, targetID, ops);
    }

    private Node getSuperUA() throws ClassNotFoundException, SQLException, DatabaseException, IOException, InvalidPropertyException, InvalidNodeTypeException, PropertyNotFoundException, NodeNotFoundException {
        HashSet<Node> nodesOfType = getDaoManager().getGraphDAO().getGraph().getNodesOfType(NodeType.UA);
        for(Node node : nodesOfType) {
            if(node.getName().equals(SUPER_KEYWORD)) {
                if(node.hasPropertyKey(NAMESPACE_PROPERTY) && node.getProperty(NAMESPACE_PROPERTY).equals(SUPER_KEYWORD)) {
                    return node;
                }
            }
        }

        throw new NodeNotFoundException(SUPER_KEYWORD);
    }
}
