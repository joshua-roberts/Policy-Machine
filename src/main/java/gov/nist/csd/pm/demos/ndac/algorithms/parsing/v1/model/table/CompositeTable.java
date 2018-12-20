package gov.nist.csd.pm.demos.ndac.algorithms.parsing.v1.model.table;

import java.util.ArrayList;
import java.util.List;

/**
 * A table made up of simple tables
 */
public class CompositeTable {
    private List<NDACTable>    tables;

    public CompositeTable(){
        tables = new ArrayList<>();
    }

    public void addTable(NDACTable table){
        tables.add(table);
    }

    public List<NDACTable> getTables(){
        return tables;
    }

    public NDACTable getTable(String tableName){
        for(NDACTable t : tables){
            if(t.getTableName().equals(tableName)){
                return t;
            }
        }
        return null;
    }

    @Override
    public String toString(){
        String str = "";
        for(NDACTable t : tables){
            str += t + "\n";
        }
        return str;
    }
}
