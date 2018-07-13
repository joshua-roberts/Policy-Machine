package gov.nist.policyserver.service;

import gov.nist.policyserver.common.Constants;
import gov.nist.policyserver.exceptions.*;
import gov.nist.policyserver.model.graph.nodes.Node;
import gov.nist.policyserver.model.graph.nodes.NodeType;
import gov.nist.policyserver.model.graph.nodes.Property;
import gov.nist.policyserver.model.imports.ImportFile;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static gov.nist.policyserver.common.Constants.*;

public class ImportService {

    private NodeService nodeService;
    private AssignmentService assignmentService;

    public ImportService() {
        nodeService = new NodeService();
        assignmentService = new AssignmentService();
    }

    public void importFiles(ImportFile[] files, String storage) throws InvalidPropertyException,
            AssignmentExistsException, InvalidNodeTypeException, NodeNotFoundException, ClassNotFoundException, NodeIdExistsException, NodeNameExistsException, NodeNameExistsInNamespaceException, IOException, ConfigurationException, SQLException, NullNameException, DatabaseException, NullTypeException, InvalidAssignmentException, UnexpectedNumberOfNodesException, AssociationExistsException, NoBaseIdException {
        for(ImportFile importFile : files) {
            String name = importFile.getName();
            String bucket = importFile.getBucket();
            String contentType = importFile.getContentType();
            long size = importFile.getSize();

            Node bucketNode;
            try {
                bucketNode = nodeService.getNode(bucket, NodeType.OA.toString(), NAMESPACE_PROPERTY + "=" + bucket);
            }
            catch (UnexpectedNumberOfNodesException e) {
                //create bucket pc
                bucketNode = nodeService.createNode(NO_BASE_ID, NEW_NODE_ID, bucket, NodeType.PC.toString(),
                        new Property[]{
                                new Property(BUCKET_PROPERTY, bucket),
                                new Property(STORAGE_PROPERTY, storage)
                        });

                //create bucket OA
                bucketNode = nodeService.createNode(bucketNode.getId(), NEW_NODE_ID, bucket, NodeType.OA.toString(),
                        new Property[]{
                                new Property(NAMESPACE_PROPERTY, bucket),
                                new Property(STORAGE_PROPERTY, storage)
                        });

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
                    parentNode = nodeService.getNodeInNamespace(parentNamespace, parentName, NodeType.OA);
                }catch (Exception e){}

                try {
                    node = nodeService.getNodeInNamespace(namespace, fileName, NodeType.OA);
                }catch (Exception e) {}

                if(node == null) {
                    //create node
                    if(parentNode == null) {
                        //create pc
                        parentNode = nodeService.createNode(NO_BASE_ID, NEW_NODE_ID, parentName,
                                NodeType.OA.toString(), null);
                    }
                    nodeService.createNode(parentNode.getId(), NEW_NODE_ID, fileName,
                            (i == split.length-1) ? NodeType.O.toString() : NodeType.OA.toString(),
                            new Property[]{
                                    new Property(NAMESPACE_PROPERTY, namespace),
                                    new Property(STORAGE_PROPERTY, storage),
                                    new Property(BUCKET_PROPERTY, bucket),
                                    new Property(PATH_PROPERTY, name),
                                    new Property(CONTENT_TYPE_PROPERTY, contentType),
                                    new Property(SIZE_PROPERTY, String.valueOf(size)),
                            });
                    System.out.println("creating node " + fileName + " in namespace " + namespace);
                }
            }
        }
    }

    public void importEntities(String kind, HashMap<String, Object>[] entities) throws InvalidPropertyException, AssignmentExistsException, InvalidNodeTypeException, NodeNotFoundException, ClassNotFoundException, NodeIdExistsException, NodeNameExistsException, NodeNameExistsInNamespaceException, IOException, ConfigurationException, SQLException, NullNameException, DatabaseException, NullTypeException, InvalidAssignmentException, UnexpectedNumberOfNodesException, AssociationExistsException, NoBaseIdException {
        //create pc for kind
        Node node = nodeService.createNode(NO_BASE_ID, NEW_NODE_ID, kind, NodeType.PC.toString(), null);

        //create base OA for kind
        node = nodeService.createNode(node.getId(), NEW_NODE_ID, kind, NodeType.OA.toString(), null);

        //for each property, add object with property just the name NO VALUES

        for (HashMap<String, Object> map : entities) {
            long id = 0;
            for (String key : map.keySet()) {
                if(key.equalsIgnoreCase("id")) {
                    id = (long) map.get(key);
                }
            }

            //create row node
            Node entityNode = nodeService.createNode(node.getId(), NEW_NODE_ID, String.valueOf(id), NodeType.OA
                            .toString(),
                    new Property[]{
                            new Property("kind", kind),
                            new Property("id", String.valueOf(id))
                    }
            );


            for (String key : map.keySet()) {
                nodeService.createNode(entityNode.getId(), NEW_NODE_ID, key, NodeType.O.toString(), new
                        Property[]{new Property("value", key)});
            }
        }
    }

    public void importSql(String host, int port, String schema, String username, String password)
            throws DatabaseException, NodeNotFoundException, ConfigurationException, AssignmentExistsException,
            InvalidPropertyException, InvalidNodeTypeException, NameInNamespaceNotFoundException, InvalidAssignmentException, SQLException, IOException, ClassNotFoundException, NodeNameExistsException, NodeNameExistsInNamespaceException, NodeIdExistsException, NullTypeException, NullNameException, UnexpectedNumberOfNodesException, AssociationExistsException, NoBaseIdException {
        //create the schema policy class node
        Property[] properties = new Property[] {
                new Property(Constants.SCHEMA_COMP_PROPERTY, Constants.SCHEMA_COMP_SCHEMA_PROPERTY),
                new Property(Constants.DESCRIPTION_PROPERTY, "Policy Class for " + schema)
        };
        Node pcNode = nodeService.createNode(NO_BASE_ID, NEW_NODE_ID, schema, NodeType.PC.toString(), properties);

        properties = new Property[] {
                new Property(Constants.SCHEMA_COMP_PROPERTY, Constants.SCHEMA_COMP_SCHEMA_PROPERTY),
                new Property(Constants.DESCRIPTION_PROPERTY, "Base Object Attribute for " + schema)
        };
        // create the schema object attribute node
        Node schemaNode = nodeService.createNode(pcNode.getId(), NEW_NODE_ID, schema, NodeType.OA.toString(),
                properties);

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
                properties = new Property[] {
                        new Property(Constants.SCHEMA_NAME_PROPERTY, schema),
                        new Property(NAMESPACE_PROPERTY, schema),
                        new Property(Constants.SCHEMA_COMP_PROPERTY, Constants.SCHEMA_COMP_TABLE_PROPERTY)
                };
                Node tableNode = nodeService.createNode(schemaNode.getId(), NEW_NODE_ID, tableName, NodeType.OA.toString(),
                        properties);

                //create columns container
                properties = new Property[] {
                        new Property(NAMESPACE_PROPERTY, tableName),
                        new Property(DESCRIPTION_PROPERTY, "Column container for " + tableName)
                };
                Node columnsNode = nodeService.createNode(tableNode.getId(), NEW_NODE_ID, Constants
                                .COLUMN_CONTAINER_NAME, NodeType.OA.toString(), properties);
                //create columns
                Statement stmt1 = conn.createStatement();
                String colSql = "SELECT c.column_name FROM INFORMATION_SCHEMA.COLUMNS c WHERE c.table_name = '" + tableName + "' AND c.table_schema = '" + schema + "'";
                ResultSet rs1 = stmt1.executeQuery(colSql);
                String columnSql = "";
                while(rs1.next()){
                    String columnName = rs1.getString(1);
                    System.out.println("creating column " + columnName);

                    properties = new Property[]{
                            new Property(NAMESPACE_PROPERTY, tableName)
                    };
                    Node columnNode = nodeService.createNode(columnsNode.getId(), NEW_NODE_ID, columnName, NodeType.OA
                                    .toString
                                    (),
                            properties);

                    columnSql += columnName + ", ";
                }
                columnSql = columnSql.substring(0, columnSql.length()-2);

                //create rows
                if(!columnSql.isEmpty()){
                    //create rows containers
                    properties = new Property[] {
                            new Property(NAMESPACE_PROPERTY, tableName),
                            new Property(DESCRIPTION_PROPERTY, "Row container for " + tableName)
                    };
                    Node rowsNode = nodeService.createNode(tableNode.getId(), NEW_NODE_ID, Constants.ROW_CONTAINER_NAME,
                            NodeType.OA.toString(), properties);

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
                        properties = new Property[]{
                                new Property(NAMESPACE_PROPERTY, tableName),
                                new Property(Constants.SCHEMA_COMP_PROPERTY, Constants.SCHEMA_COMP_ROW_PROPERTY)
                        };
                        Node rowNode = nodeService.createNode(rowsNode.getId(), NEW_NODE_ID, rowName, NodeType.OA
                                        .toString(),
                                properties);

                        //create data objects, assign to row and column
                        for(int i = 1; i <= rs2MetaData.getColumnCount(); i++){
                            //get column
                            String columnName = rs2MetaData.getColumnName(i);
                            Node columnNode = nodeService.getNodeInNamespace(tableName, columnName, NodeType.OA);


                            //create data object node
                            String objectName = UUID.randomUUID().toString();
                            properties = new Property[]{
                                    new Property(NAMESPACE_PROPERTY, tableName),
                                    new Property(DESCRIPTION_PROPERTY, "Object in table=" + tableName + ", row=" + rowName + ", column=" + columnNode.getName())
                            };
                            Node objectNode = nodeService.createNode(rowNode.getId(), NEW_NODE_ID, objectName, NodeType.O.toString(),
                                    properties);

                            //assign object to row and column
                            assignmentService.createAssignment(objectNode.getId(), columnNode.getId());
                        }
                    }
                }
            }

        }catch(SQLException | ClassNotFoundException e){
            e.printStackTrace();
            throw new DatabaseException(PmException.CLIENT_ERROR, e.getMessage());
        }
    }
}
