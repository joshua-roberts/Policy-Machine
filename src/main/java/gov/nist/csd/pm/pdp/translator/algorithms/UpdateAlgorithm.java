package gov.nist.csd.pm.pdp.translator.algorithms;

import gov.nist.csd.pm.model.exceptions.InvalidEntityException;
import gov.nist.csd.pm.model.exceptions.*;
import gov.nist.csd.pm.model.graph.NodeType;
import gov.nist.csd.pm.pdp.translator.exceptions.PMAccessDeniedException;
import gov.nist.csd.pm.pdp.translator.exceptions.PolicyMachineException;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.util.SelectUtils;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static gov.nist.csd.pm.model.Constants.FILE_READ;
import static gov.nist.csd.pm.model.Constants.FILE_WRITE;

public class UpdateAlgorithm extends Algorithm {
    private Update update;
    private Select select;

    public UpdateAlgorithm(String id, Update update, PmManager pm, DbManager db){
        super(id, pm, db);
        this.update = update;
    }

    @Override
    public String run() throws SQLException, IOException, PolicyMachineException, PMAccessDeniedException, JSQLParserException, NodeNotFoundException, InvalidNodeTypeException, NoUserParameterException, NameInNamespaceNotFoundException, InvalidPropertyException, InvalidEntityException, NoSubjectParameterException, InvalidProhibitionSubjectTypeException, ConfigurationException, DatabaseException, ClassNotFoundException, UnexpectedNumberOfNodesException {
        //determine the rows that are going to be updated
        List<String> rows = getTargetRows();

        //check each row that the user has write analytics to each row,column
        checkRows(rows);

        //process the statement as an event
        //pmManager.processUpdate(id, update, rows, dbManager);

        //return update statement
        return update.toString();
    }

    public List<String> getTargetRows() throws SQLException {
        String tableName = update.getTable().getName();
        List<String> keys = getKeys(tableName);

        //build select
        buildSelect(tableName, keys, update.getWhere());

        //return rows
        return getRows(select);
    }

    private List<String> getRows(Select select) throws SQLException {
        List<String> rows = new ArrayList<>();

        ResultSet rs = dbManager.getConnection().createStatement().executeQuery(select.toString());
        ResultSetMetaData meta = rs.getMetaData();
        int numCols = meta.getColumnCount();
        while (rs.next()) {
            String rowName = "";
            for (int i = 1; i <= numCols; i++) {
                String value = rs.getString(i);
                if(i == 1){
                    rowName += value;
                }else{
                    rowName += PmManager.NAME_DELIM + value;
                }
            }
            rows.add(rowName);
        }

        return rows;
    }

    private void  buildSelect(String tableName, List<String> keys, Expression where){
        Select select = SelectUtils.buildSelectFromTable(new Table(tableName));
        PlainSelect ps = (PlainSelect) select.getSelectBody();

        List<SelectItem> selectItems = new ArrayList<>();
        for(String key : keys){
            selectItems.add(new SelectExpressionItem(new Column(key)));
        }

        ps.setSelectItems(selectItems);
        ps.setWhere(where);

        this.select = select;
    }

    private void checkRows(List<String> rows) throws IOException, PolicyMachineException, PMAccessDeniedException, JSQLParserException, InvalidNodeTypeException, NodeNotFoundException, NoUserParameterException, NameInNamespaceNotFoundException, InvalidPropertyException, NoSubjectParameterException, InvalidProhibitionSubjectTypeException, ConfigurationException, ClassNotFoundException, DatabaseException, SQLException, UnexpectedNumberOfNodesException {
        List<String> reqColumns = update.getColumns().stream().map(
                Column::getColumnName).collect(Collectors.toList());

        for (String rowName : rows) {
            long rowPmId = pmManager.getEntityId(update.getTable().getName(), rowName, NodeType.OA);

            //iterate through the requested columns and find the intersection of the current row and current column
            for (String columnName : reqColumns) {
                long columnPmId = pmManager.getEntityId(update.getTable().getName(), columnName, NodeType.OA);
                //if the intersection (an object) is in the accessible children add the COLUMN to a list
                //else if not in accChildren, check if its in where clause

                if(!checkColumn(columnPmId, rowPmId, FILE_WRITE)){
                    throw new PMAccessDeniedException(columnName);
                }
            }


            //check there are no inaccessible columns in where clause for this row
            //get all columns in where clause
            //check each column with current row

            HashSet<Column> whereColumns = getWhereColumns(update.getWhere());
            for(Column column : whereColumns){
                if(!reqColumns.contains(column.getColumnName())){
                    long columnPmId = pmManager.getEntityId(update.getTable().getName(), column.getColumnName(), NodeType.OA);

                    //if the intersection (an object) is in the accessible children add the COLUMN to a list
                    //else if not in accChildren, check if its in where clause
                    //if a column is in a where clause, the iser needs to be able to read that column
                    if(!checkColumn(columnPmId, rowPmId, FILE_READ)){
                        throw new PMAccessDeniedException(column.toString());
                    }
                }
            }
        }
    }
}
