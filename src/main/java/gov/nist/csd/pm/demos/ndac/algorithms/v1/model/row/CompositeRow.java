package gov.nist.csd.pm.demos.ndac.algorithms.v1.model.row;

import java.util.ArrayList;
import java.util.List;

public class CompositeRow {
    private List<NDACRow> compositeRow;

    public CompositeRow(){
        compositeRow = new ArrayList<>();
    }

    public void addToRow(NDACRow row){
        compositeRow.add(row);
    }

    public List<NDACRow> getCompositeRow(){
        return compositeRow;
    }

    @Override
    public String toString(){
        String s = "";
        for(NDACRow r : compositeRow){
            if(s.isEmpty()){
                s += r + "\t";
            }else {
                s += r + "\t";
            }
        }
        return s;
    }
}
