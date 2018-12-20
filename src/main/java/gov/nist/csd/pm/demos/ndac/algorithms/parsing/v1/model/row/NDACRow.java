package gov.nist.csd.pm.demos.ndac.algorithms.parsing.v1.model.row;

public class NDACRow {
    private String rowName;
    private String tableName;

    public NDACRow(String tableName, String rowName) {
        this.rowName = rowName;
        this.tableName = tableName;
    }

    public String getRowName() {
        return rowName;
    }

    public void setRowName(String rowName) {
        this.rowName = rowName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String toString() {
        return this.tableName + "." + this.rowName;
    }
}
