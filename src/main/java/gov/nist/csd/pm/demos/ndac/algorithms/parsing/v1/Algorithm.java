package gov.nist.csd.pm.demos.ndac.algorithms.parsing.v1;

import gov.nist.csd.pm.common.exceptions.*;
import gov.nist.csd.pm.common.model.graph.Graph;
import gov.nist.csd.pm.common.model.graph.Search;
import gov.nist.csd.pm.common.model.graph.nodes.Node;
import gov.nist.csd.pm.common.model.graph.nodes.NodeType;
import gov.nist.csd.pm.common.model.prohibitions.Prohibition;
import gov.nist.csd.pm.pap.db.sql.SQLConnection;
import gov.nist.csd.pm.pdp.engine.Decider;
import gov.nist.csd.pm.pdp.engine.PReviewDecider;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.*;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

import static gov.nist.csd.pm.common.constants.Properties.NAMESPACE_PROPERTY;


public abstract class Algorithm {

    protected static final String NAME_DELIM = "+";

    protected Context ctx;

    public Algorithm(Context ctx) {
        this.ctx = ctx;
    }

    public abstract String run() throws SQLException, IOException, PMException, JSQLParserException, InvalidEntityException, ClassNotFoundException;

    protected List<String> getKeys(String tableName) throws InvalidProhibitionSubjectTypeException, SessionDoesNotExistException, InvalidNodeTypeException, LoadConfigException, DatabaseException {
        HashMap<String, String> props = new HashMap<>();
        props.put(NAMESPACE_PROPERTY, tableName);
        props.put("pk", "true");
        props.put("schema_comp", "column");
        HashSet<Node> nodes = ctx.getSearch().search(null, NodeType.OA.toString(), props);

        List<String> keys = new ArrayList<>();
        for(Node node : nodes) {
            keys.add(node.getName());
        }

        return keys;
    }

    protected HashSet<Column> getWhereColumns(Expression where) throws JSQLParserException {
        final HashSet<Column> visitedColumns = new HashSet<>();

        if(where == null){
            return visitedColumns;
        }

        Expression ex = CCJSqlParserUtil.parseCondExpression(where.toString());
        ex.accept(new ExpressionVisitorAdapter() {
            @Override
            public void visit(Column column) {
                visitedColumns.add(column);
            }

            @Override
            public void visit(SubSelect subSelect) {
                PlainSelect select = (PlainSelect) subSelect.getSelectBody();

                for (SelectItem s : select.getSelectItems()) {
                    s.accept(new SelectItemVisitor() {
                        @Override
                        public void visit(AllColumns allColumns) {

                        }

                        @Override
                        public void visit(AllTableColumns allTableColumns) {

                        }

                        @Override
                        public void visit(SelectExpressionItem selectExpressionItem) {
                            selectExpressionItem.getExpression().accept(new ExpressionVisitorAdapter(){
                                public void visit(Column c){
                                    visitedColumns.add(c);
                                }
                            });
                        }
                    });
                }

                try {
                    Expression ex = CCJSqlParserUtil.parseCondExpression(select.getWhere().toString());
                    ex.accept(this);
                } catch (JSQLParserException e) {
                    e.printStackTrace();
                }

            }
        });

        return visitedColumns;
    }

    public Connection getConnection() {
        return ctx.getConnection().getConnection();
    }


    public boolean checkColumn(long columnPmId, long rowPmId, String perm) throws DatabaseException, SessionDoesNotExistException, NodeNotFoundException, LoadConfigException, InvalidProhibitionSubjectTypeException, MissingPermissionException {
        System.out.println("checking column with ID " + columnPmId);
        System.out.println("checking row with ID " + rowPmId);
        Decider decider = new PReviewDecider(ctx.getGraph(), ctx.getProhibitions());
        HashSet<Node> children = decider.getChildren(ctx.getUserID(), ctx.getProcessID(), rowPmId, perm);
        System.out.println("accessible row children: " + children);
        Node intersection = getIntersection(columnPmId, rowPmId);
        System.out.println("intersection: " + intersection);
        return children.contains(intersection);
    }

    public Node getIntersection(long columnPmId, long rowPmId) throws NodeNotFoundException, DatabaseException, SessionDoesNotExistException, LoadConfigException, MissingPermissionException, InvalidProhibitionSubjectTypeException {
        HashSet<Node> columnChildren = ctx.getGraph().getChildren(columnPmId);
        HashSet<Node> rowChildren = ctx.getGraph().getChildren(rowPmId);
        columnChildren.retainAll(rowChildren);
        if(!columnChildren.isEmpty()) {
            return columnChildren.iterator().next();
        }else{
            return null;
        }
    }

    public boolean checkColumnAccess(String columnName, String tableName, String ... perms) throws InvalidProhibitionSubjectTypeException, SessionDoesNotExistException, InvalidNodeTypeException, LoadConfigException, DatabaseException, NodeNotFoundException, MissingPermissionException {
        Map<String, String> properties = new HashMap<>();
        properties.put(NAMESPACE_PROPERTY, tableName);
        HashSet<Node> nodes = ctx.getSearch().search(columnName, NodeType.OA.toString(), properties);
        if(nodes.size() != 1) {
            throw new NodeNotFoundException("Could not find column object attribute for " + tableName);
        }
        Node node = nodes.iterator().next();

        Decider decider = new PReviewDecider(ctx.getGraph(), ctx.getProhibitions());
        return decider.hasPermissions(ctx.getUserID(), ctx.getProcessID(), node.getID(), perms);
    }

    public boolean checkRowAccess(String tableName, String ... perms) throws InvalidProhibitionSubjectTypeException, SessionDoesNotExistException, InvalidNodeTypeException, LoadConfigException, DatabaseException, NodeNotFoundException, MissingPermissionException {
        Map<String, String> properties = new HashMap<>();
        properties.put(NAMESPACE_PROPERTY, tableName);
        HashSet<Node> nodes = ctx.getSearch().search("Rows", NodeType.OA.toString(), properties);
        if(nodes.size() != 1) {
            throw new NodeNotFoundException("Could not find row object attribute for " + tableName);
        }
        Node node = nodes.iterator().next();

        Decider decider = new PReviewDecider(ctx.getGraph(), ctx.getProhibitions());
        return decider.hasPermissions(ctx.getUserID(), ctx.getProcessID(), node.getID(), perms);
    }


    static class Context {
        private SQLConnection conn;
        private Graph graph;
        private Search search;
        private List<Prohibition> prohibitions;
        private long userID;
        private long processID;

        public Context(SQLConnection conn, Graph graph, Search search, List<Prohibition> prohibitions, long userID, long processID) {
            this.conn = conn;
            this.graph = graph;
            this.search = search;
            this.prohibitions = prohibitions;
            this.processID = processID;
            this.userID = userID;
        }

        public SQLConnection getConnection() {
            return conn;
        }

        public Graph getGraph() {
            return graph;
        }

        public Search getSearch() {
            return search;
        }

        public List<Prohibition> getProhibitions() {
            return prohibitions;
        }

        public long getProcessID() {
            return processID;
        }

        public long getUserID() {
            return userID;
        }
    }

}