package gov.nist.csd.pm.demos.ndac.algorithms.parsing.v1.model.table;

import gov.nist.csd.pm.demos.ndac.algorithms.parsing.v1.model.row.NDACRow;
import net.sf.jsqlparser.schema.Column;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple table that contains all of the information(columns, keys, rows)
 */
public class NDACTable {
    private String        tableName;
    private List<Column>  columns;
    private List<String>  keys;
    private List<NDACRow> rows;

    public NDACTable(String tableName){
        this.tableName = tableName;
        columns = new ArrayList<>();
        keys = new ArrayList<>();
        rows = new ArrayList<>();
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public List<Column> getColumns() {
        return columns;
    }

    public void setColumns(List<Column> columns) {
        if(columns == null) {
            columns = new ArrayList<>();
        }
        this.columns = columns;
    }

    public void addColumn(Column column){
        this.columns.add(column);
    }

    public List<String> getKeys() {
        return keys;
    }

    public void setKeys(List<String> keys) {
        this.keys = keys;
    }

    public void addKey(String key) {
        this.keys.add(key);
    }

    public List<NDACRow> getRows() {
        return rows;
    }

    public void setRows(List<NDACRow> rows) {
        this.rows = rows;
    }

    public void addRow(NDACRow row){
        this.rows.add(row);
    }

    @Override
    public String toString(){
        String rows = "";
        for(NDACRow row : this.rows) {
            rows += row + "\n";
        }
        String str = "====================" +
                "\ntableName:\t" + tableName +
                "\nkeys:\t\t" + keys +
                "\ncolumns:\t" + columns +
                "\nrows {" +
                "\n" + rows +
                "\n}\n====================";
        return str;
    }
}
