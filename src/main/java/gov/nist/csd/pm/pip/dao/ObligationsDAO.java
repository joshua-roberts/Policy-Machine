package gov.nist.csd.pm.pip.dao;

import gov.nist.csd.pm.model.exceptions.DatabaseException;
import gov.nist.csd.pm.model.exceptions.InvalidPropertyException;
import gov.nist.csd.pm.epp.obligations.EvrManager;
import gov.nist.csd.pm.model.exceptions.InvalidEntityException;
import gov.nist.csd.pm.model.obligations.EvrArg;
import gov.nist.csd.pm.model.obligations.EvrEntity;
import gov.nist.csd.pm.model.obligations.EvrFunction;
import gov.nist.csd.pm.model.obligations.script.rule.event.time.EvrTime;

import java.sql.SQLException;
import java.util.HashSet;

public interface ObligationsDAO {

    EvrManager getEvrManager();

    void buildObligations() throws DatabaseException, SQLException, InvalidPropertyException;

    String createScript(String scriptName) throws DatabaseException, SQLException;

    String createRule(String parentId, String parentLabel, String label) throws DatabaseException;

    String createSubject(String ruleId, String parentLabel) throws DatabaseException;

    String createPolicies(String ruleId, String parentLabel, boolean isOr) throws DatabaseException;

    void createTime(String ruleId, EvrTime evrTime) throws DatabaseException;

    String createTarget(String ruleId, String parentLabel) throws DatabaseException;

    String createEntity(String parentId, String parentLabel, EvrEntity entity) throws DatabaseException, InvalidEntityException;

    void updateEntity(String entityId, EvrEntity evrEntity) throws DatabaseException, InvalidEntityException;

    String createFunction(String parentId, String parentLabel, EvrFunction function) throws DatabaseException;

    String createProcess(String parentId, String parentLabel) throws DatabaseException;

    void updateProcess(String parentId, long process) throws DatabaseException;

    String addFunctionArg(String functionId) throws DatabaseException;

    void addFunctionArgValue(String functionId, EvrArg arg) throws DatabaseException;

    String createCondition(String ruleId, boolean exists) throws DatabaseException;

    String createAssignAction(String ruleId, String parentLabel) throws DatabaseException;

    String createAssignActionParam(String assignActionId, String param) throws DatabaseException;

    String createGrantAction(String ruleId, String parentLabel) throws DatabaseException;

    void createOperations(String parentId, String parentType, HashSet<String> ops) throws DatabaseException;

    String createCreateAction(String parentId, String parentLabel) throws DatabaseException;

    String createDenyAction(String parentId, String parentLabel) throws DatabaseException;

    String createDeleteAction(String parentId, String parentLabel) throws DatabaseException;

    void deleteObligations() throws DatabaseException;

    void updateScript(String obligation, boolean enabled) throws DatabaseException;
}
