package gov.nist.policyserver.dao.sql;

import gov.nist.policyserver.dao.ObligationsDAO;
import gov.nist.policyserver.exceptions.DatabaseException;
import gov.nist.policyserver.obligations.EvrManager;
import gov.nist.policyserver.obligations.exceptions.InvalidEntityException;
import gov.nist.policyserver.obligations.model.EvrArg;
import gov.nist.policyserver.obligations.model.EvrEntity;
import gov.nist.policyserver.obligations.model.EvrFunction;
import gov.nist.policyserver.obligations.model.script.rule.event.time.EvrTime;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;

public class SqlObligationsDAO implements ObligationsDAO {
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
    public String createPcSpec(String ruleId, String parentLabel, boolean isOr) throws DatabaseException {
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
    public void updateProcess(String parentId, String process) throws DatabaseException {

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
    public void createOpSpec(String parentId, String parentType, HashSet<String> ops) throws DatabaseException {

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
}
