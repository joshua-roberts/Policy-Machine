package gov.nist.csd.pm.pdp.translator.algorithms;

import gov.nist.csd.pm.epp.obligations.EvrManager;
import gov.nist.csd.pm.model.exceptions.*;
import gov.nist.csd.pm.pdp.analytics.PmAnalyticsEntry;
import gov.nist.csd.pm.model.graph.Node;
import gov.nist.policyserver.model.graph.nodes.NodeType;
import gov.nist.csd.pm.pep.services.AnalyticsService;
import gov.nist.csd.pm.pep.services.NodeService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.*;

import static gov.nist.csd.pm.pip.DAOManager.getDaoManager;

public class PmManager {
    public static final String GET_ENTITY_ID = "getEntityId";
    public static final String GET_INTERSECTION = "getIntersection";
    public static final String GET_ACCESS_CHILDREN = "getAccessibleChildren";
    public static final String GET_ENTITY_NAME = "getNameOfEntityWithIdAndType";
    public static final String GET_PERMITTED_OPS = "getUserPermsOn";

    public static final String PATH_DELIM = "/";
    public static final String NAME_DELIM = "+";

    private BufferedReader in;
    private PrintWriter out;

    private Node             pmUser;
    private NodeService      nodeService;
    private AnalyticsService analyticsService;
    private EvrManager       evrManager;
    private long           process;

    public PmManager(String username, long process) throws NodeNotFoundException, ConfigurationException, DatabaseException, IOException, ClassNotFoundException, SQLException, InvalidPropertyException {
        this.nodeService = new NodeService();
        this.analyticsService = new AnalyticsService();
        this.pmUser = getPmUser(username);
        this.evrManager = getDaoManager().getObligationsDAO().getEvrManager();
        this.process = process;
    }

    public long getProcess() {
        return process;
    }

    public void setProcess(long process) {
        this.process = process;
    }

    private Node getPmUser(String username) throws NodeNotFoundException, ClassNotFoundException, SQLException, IOException, DatabaseException {
        try {
            HashSet<Node> nodes = nodeService.getNodes(null, username, NodeType.USER.toString(), null);
            if(!nodes.isEmpty()) {
                return nodes.iterator().next();
            }else {
                throw new NodeNotFoundException(username);
            }
        }
        catch (InvalidNodeTypeException | InvalidPropertyException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Node getPmUser() {
        return pmUser;
    }

    public long getEntityId(String namespace, String name, NodeType type) throws InvalidNodeTypeException,
            NameInNamespaceNotFoundException, InvalidPropertyException, ClassNotFoundException, SQLException, DatabaseException, IOException {
        return nodeService.getNodeInNamespace(namespace, name, type).getID();
    }

    public List<Node> getAccessibleChildren(long id, String perm) throws NodeNotFoundException, NoUserParameterException, NoSubjectParameterException, InvalidProhibitionSubjectTypeException, ConfigurationException, ClassNotFoundException, SQLException, IOException, DatabaseException, InvalidPropertyException {
        List<PmAnalyticsEntry> accessibleChildren = analyticsService.getAccessibleChildren(id, pmUser.getID());
        List<Node> nodes = new ArrayList<>();
        for(PmAnalyticsEntry entry : accessibleChildren) {
            Node target = entry.getTarget();
            HashSet<String> prohibitedOps = getProhibitedOps(target.getID());
            if(entry.hasOp(perm) && !prohibitedOps.contains(perm)) {
                nodes.add(entry.getTarget());
            }
        }

        return nodes;
    }

    /**
     * Get the operations that are prohibited for the current user on a node.  Also, include the operations that are prohibited for the process.
     * @param id the ID of the node
     * @return The set of prohibited operations
     * @throws NoSubjectParameterException
     * @throws NodeNotFoundException
     * @throws InvalidProhibitionSubjectTypeException
     */
    private HashSet<String> getProhibitedOps(long id) throws NoSubjectParameterException, NodeNotFoundException, InvalidProhibitionSubjectTypeException, ConfigurationException, ClassNotFoundException, SQLException, IOException, DatabaseException, InvalidPropertyException {
        //get the prohibited ops for the user
        HashSet<String> prohibitedOps = analyticsService.getProhibitedOps(id, pmUser.getID(), "U");

        //get the prohibited ops for the process if it exists
        if(process != 0) {
            prohibitedOps.addAll(analyticsService.getProhibitedOps(id, Long.valueOf(process), "P"));
        }

        return prohibitedOps;
    }

    public Node getIntersection(long columnPmId, long rowPmId) throws NodeNotFoundException, InvalidNodeTypeException, ClassNotFoundException, SQLException, DatabaseException, IOException, InvalidPropertyException {
        HashSet<Node> columnChildren = nodeService.getChildrenOfType(columnPmId, NodeType.OBJECT.toString());
        HashSet<Node> rowChildren = nodeService.getChildrenOfType(rowPmId, NodeType.OBJECT.toString());
        columnChildren.retainAll(rowChildren);
        if(!columnChildren.isEmpty()) {
            return columnChildren.iterator().next();
        }else{
            return null;
        }
    }

    public boolean checkColumnAccess(String columnName, String tableName, String ... perms) throws PmException, SQLException, IOException, ClassNotFoundException {
        Node node = nodeService.getNodeInNamespace(tableName, columnName, NodeType.OBJECT_ATTRIBUTE);
        if(node == null) {
            throw new NodeNotFoundException("Could not find column object attribute for " + tableName);
        }

        List<String> permList = Arrays.asList(perms);

        PmAnalyticsEntry access = analyticsService.getUserPermissionsOn(node.getID(), pmUser.getID());
        HashSet<String> operations = access.getOperations();

        //get and remove all prohibited operations
        HashSet<String> prohibitedOps = getProhibitedOps(node.getID());
        operations.removeAll(prohibitedOps);

        return operations.containsAll(permList) || operations.contains("*");
    }

    public boolean checkRowAccess(String tableName, String ... perms) throws PmException, SQLException, IOException, ClassNotFoundException {
        HashSet<Node> nodes = nodeService.getNodes(tableName, "Rows", NodeType.OBJECT_ATTRIBUTE.toString(), null);
        if(nodes.size() != 1) {
            throw new NodeNotFoundException("Could not find row object attribute for table " + tableName);
        }

        List<String> permList = Arrays.asList(perms);

        Node node = nodes.iterator().next();
        PmAnalyticsEntry access = analyticsService.getUserPermissionsOn(node.getID(), pmUser.getID());
        HashSet<String> operations = access.getOperations();

        //get and remove all prohibited operations
        HashSet<String> prohibitedOps = getProhibitedOps(node.getID());
        operations.removeAll(prohibitedOps);

        return operations.containsAll(permList) || operations.contains("*");
    }

    /*public void processUpdate(String id, Update update, List<String> rows, DbManager dbManager) {
        new Thread(() -> {
            while(true) {
                if(!evrManager.isActiveSql(id)) {
                    try {
                        dbManager.setRows(rows);
                        evrManager.setDbManager(dbManager);

                        evrManager.processUpdate(pmUser, process, update);
                    }
                    catch (InvalidEntityException | SQLException | InvalidEvrException | DatabaseException | ConfigurationException | InvalidPropertyException | InvalidNodeTypeException | NodeNotFoundException | AssignmentExistsException | ProhibitionDoesNotExistException | InvalidProhibitionSubjectTypeException | ProhibitionResourceExistsException | ProhibitionNameExistsException | NullNameException e) {
                        e.printStackTrace();
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                    catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                    return;
                }
            }
        }).start();
    }

    public void processSelect(String id, Set<List<Column>> columnSet, List<CompositeRow> compositeRows, DbManager dbManager) {
        new Thread(() -> {
            while(true) {
                if(!evrManager.isActiveSql(id)) {
                    try {
                        //find row names from composite rows
                        HashMap<String, List<String>> tableRows = new HashMap<>();
                        for(CompositeRow row : compositeRows) {
                            List<SimpleRow> compositeRow = row.getCompositeRow();
                            for(SimpleRow simpleRow : compositeRow) {
                                List<String> existingRows = tableRows.get(simpleRow.getTableName());
                                if(existingRows == null) {
                                    existingRows = new ArrayList<>();
                                }
                                existingRows.add(simpleRow.getRowName());
                                tableRows.put(simpleRow.getTableName(),existingRows);
                            }
                        }

                        //get columns being read
                        HashSet<Column> columns = new HashSet<>();
                        for(List<Column> cols : columnSet) {
                            columns.addAll(cols);
                        }

                        dbManager.setTableRows(tableRows);
                        dbManager.setColumns(columns);
                        evrManager.setDbManager(dbManager);

                        evrManager.processSelect(pmUser, process);
                    }
                    catch (InvalidEntityException | SQLException | InvalidEvrException | DatabaseException | ConfigurationException | InvalidPropertyException | InvalidNodeTypeException | NodeNotFoundException | AssignmentExistsException | ProhibitionDoesNotExistException | ProhibitionResourceExistsException | InvalidProhibitionSubjectTypeException | ProhibitionNameExistsException | NullNameException | IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                    return;
                }
            }
        }).start();
    }*/

    public void addActiveSql(String id) {
        evrManager.addActiveSql(id);
    }

}
