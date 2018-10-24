package gov.nist.csd.pm.demos.ndac.translator.algorithms;

import gov.nist.csd.pm.model.exceptions.*;
import gov.nist.csd.pm.model.graph.OldNode;
import gov.nist.csd.pm.model.graph.nodes.NodeType;
import gov.nist.csd.pm.demos.ndac.translator.exceptions.PMAccessDeniedException;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.insert.Insert;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class InsertAlgorithm extends Algorithm{
    private Insert insert;

    public InsertAlgorithm(String id, Insert insert, PmManager pm, DbManager db) {
        super(id, pm, db);
        this.insert = insert;
    }

    @Override
    public String run() throws PMException, SQLException, IOException, ClassNotFoundException {
        //Check user can create an object attribute in the table oa
        Table table = insert.getTable();

        //get the columns the user has analytics to
        long columnsContId = pmManager.getEntityId(table.getName(), "Columns", NodeType.OA);
        List<OldNode> accColumns = pmManager.getAccessibleChildren(columnsContId, table.getName());
        List<String> accColumnNames = new ArrayList<>();
        for(OldNode node : accColumns) {
            accColumnNames.add(node.getName());
        }

        //check user can create a row in the table and assign the row to the table
        //schema_comp = table
        //namespace = tableName
        boolean access = false;
        try {
            access = pmManager.checkRowAccess(table.getName(), CREATE_OBJECT_ATTRIBUTE, ASSIGN_OBJECT_ATTRIBUTE);
        }
        catch (PMException | SQLException | ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
        if(!access) {
            throw new PMAccessDeniedException(table.getName());
        }

        //check user can create object in columns
        List<Column> targetColumns = insert.getColumns();
        for(Column column : targetColumns) {
            access = pmManager.checkColumnAccess(column.getColumnName(), table.getName(), CREATE_NODE, ASSIGN);
            if(!access) {
                throw new PMAccessDeniedException(column.getColumnName());
            }
        }


        //check user can assign object to OA in row
        // can we assume that if they can create the row they can assign to that row?

        //return the original sql if passed all checks
        return insert.toString();
    }

    /*Insert_Row_in_EmployeeTable(row, name, phone, ssn, salary)
    { CreateOAinOA(row, EmployeeTable)
        CreateOinOA(name, row)
        Assign(name, Name)
        CreateOinOA(phone, row)
        Assign(phone, Phone)
        CreateOinOA(ssn, row)
        Assign(ssn, SSN)
        CreateOinOA(salary, row)
        Assign(salary, Salary)
    }*/

    private boolean createOAinOA(long chidId, long parentId) {
        //create node
        //check can assign OA to OA
        return false;
    }
    private boolean createOinOA(long chidId, long parentId) {
        return false;
    }
    private boolean assignOAtoOA(long chidId, long parentId) {
        return false;
    }
    private boolean assignOtoOA(long chidId, long parentId) {
        return false;
    }
}
