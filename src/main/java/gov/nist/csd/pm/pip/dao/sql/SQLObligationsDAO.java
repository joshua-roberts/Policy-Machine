package gov.nist.csd.pm.pip.dao.sql;

import gov.nist.csd.pm.model.exceptions.DatabaseException;
import gov.nist.csd.pm.pip.dao.ObligationsDAO;
import gov.nist.csd.pm.epp.obligations.EvrManager;
import gov.nist.csd.pm.model.exceptions.InvalidEntityException;
import gov.nist.csd.pm.model.obligations.EvrArg;
import gov.nist.csd.pm.model.obligations.EvrEntity;
import gov.nist.csd.pm.model.obligations.EvrFunction;
import gov.nist.csd.pm.model.obligations.script.rule.event.time.EvrTime;
import gov.nist.csd.pm.pip.model.DatabaseContext;

import java.sql.SQLException;
import java.util.HashSet;

public class SQLObligationsDAO implements ObligationsDAO {
    private SQLConnection mysql;

    public SQLObligationsDAO(DatabaseContext ctx) throws DatabaseException {
        mysql = new SQLConnection(ctx.getHost(), ctx.getPort(), ctx.getUsername(), ctx.getPassword(), ctx.getSchema());
    }

    @Override
    public EvrManager getEvrManager() {
        return null;
    }

    @Override
    public void buildObligations() throws DatabaseException, SQLException {

    }

    @Override
    public String createScript(String scriptName) throws DatabaseException, SQLException {
        return null;
    }

    @Override
    public String createRule(String parentId, String parentLabel, String label) throws DatabaseException {
        return null;
    }

    @Override
    public String createSubject(String ruleId, String parentLabel) throws DatabaseException {
        return null;
    }

    @Override
    public String createPolicies(String ruleId, String parentLabel, boolean isOr) throws DatabaseException {
        return null;
    }

    @Override
    public void createTime(String ruleId, EvrTime evrTime) throws DatabaseException {

    }

    @Override
    public String createTarget(String ruleId, String parentLabel) throws DatabaseException {
        return null;
    }

    @Override
    public String createEntity(String parentId, String parentLabel, EvrEntity entity) throws DatabaseException, InvalidEntityException {
        return null;
    }

    @Override
    public void updateEntity(String entityId, EvrEntity evrEntity) throws DatabaseException, InvalidEntityException {

    }

    @Override
    public String createFunction(String parentId, String parentLabel, EvrFunction function) throws DatabaseException {
        return null;
    }

    @Override
    public String createProcess(String parentId, String parentLabel) throws DatabaseException {
        return null;
    }


    @Override
    public void updateProcess(String parentId, long process) throws DatabaseException {

    }

    @Override
    public String addFunctionArg(String functionId) throws DatabaseException {
        return null;
    }

    @Override
    public void addFunctionArgValue(String functionId, EvrArg arg) throws DatabaseException {

    }

    @Override
    public String createCondition(String ruleId, boolean exists) throws DatabaseException {
        return null;
    }

    @Override
    public String createAssignAction(String ruleId, String parentLabel) throws DatabaseException {
        return null;
    }

    @Override
    public String createAssignActionParam(String assignActionId, String param) throws DatabaseException {
        return null;
    }

    @Override
    public String createGrantAction(String ruleId, String parentLabel) throws DatabaseException {
        return null;
    }

    @Override
    public void createOperations(String parentId, String parentType, HashSet<String> ops) throws DatabaseException {

    }

    @Override
    public String createCreateAction(String parentId, String parentLabel) throws DatabaseException {
        return null;
    }

    @Override
    public String createDenyAction(String parentId, String parentLabel) throws DatabaseException {
        return null;
    }

    @Override
    public String createDeleteAction(String parentId, String parentLabel) throws DatabaseException {
        return null;
    }

    @Override
    public void deleteObligations() throws DatabaseException {

    }

    @Override
    public void updateScript(String obligation, boolean enabled) throws DatabaseException {

    }
}
