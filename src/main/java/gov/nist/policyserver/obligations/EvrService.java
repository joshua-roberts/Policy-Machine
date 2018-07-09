package gov.nist.policyserver.obligations;

import gov.nist.policyserver.obligations.exceptions.InvalidEntityException;
import gov.nist.policyserver.obligations.exceptions.InvalidEvrException;
import gov.nist.policyserver.obligations.model.EvrArg;
import gov.nist.policyserver.obligations.model.EvrEntity;
import gov.nist.policyserver.obligations.model.EvrFunction;
import gov.nist.policyserver.obligations.model.script.EvrScript;
import gov.nist.policyserver.obligations.model.script.rule.event.time.EvrTime;
import gov.nist.policyserver.exceptions.ConfigurationException;
import gov.nist.policyserver.exceptions.DatabaseException;
import gov.nist.policyserver.exceptions.InvalidPropertyException;
import gov.nist.policyserver.service.Service;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.sql.SQLException;
import java.util.HashSet;

class EvrService extends Service {
    private EvrManager getEvrManager() throws ClassNotFoundException, SQLException, IOException, DatabaseException {
        return getDaoManager().getObligationsDAO().getEvrManager();
    }

    public void evr(String source) throws InvalidEvrException, SQLException, ParserConfigurationException, InvalidEntityException, SAXException, DatabaseException, IOException, InvalidPropertyException, ClassNotFoundException, ConfigurationException {
        getEvrManager().evr(source);
    }

    String createScript(EvrScript script) throws DatabaseException, SQLException, IOException, ClassNotFoundException {
        //add script to memory
        getEvrManager().addScript(script);

        return getDaoManager().getObligationsDAO().createScript(script.getScriptName());
    }

    String createRule(String parentId, String parentLabel, String label) throws DatabaseException, SQLException, IOException, ClassNotFoundException {
        //create rule node
        return getDaoManager().getObligationsDAO().createRule(parentId, parentLabel, label);
    }

    String createSubject(String ruleId, String parentLabel) throws DatabaseException, SQLException, IOException, ClassNotFoundException {
        return getDaoManager().getObligationsDAO().createSubject(ruleId, parentLabel);
    }

    String createEntity(String parentId, String parentLabel, EvrEntity evrEntity) throws DatabaseException, InvalidEntityException, SQLException, IOException, ClassNotFoundException {
        return getDaoManager().getObligationsDAO().createEntity(parentId, parentLabel, evrEntity);
    }

    String createFunction(String parentId, String parentLabel, EvrFunction function) throws DatabaseException, SQLException, IOException, ClassNotFoundException {
        return getDaoManager().getObligationsDAO().createFunction(parentId, parentLabel, function);
    }

    String createProcess(String parentId, String parentLabel) throws DatabaseException, SQLException, IOException, ClassNotFoundException {
        return getDaoManager().getObligationsDAO().createProcess(parentId, parentLabel);
    }

    void updateProcess(String parentId, String process) throws DatabaseException, SQLException, IOException, ClassNotFoundException {
        getDaoManager().getObligationsDAO().createProcess(parentId, process);
    }

    String addFuntionArg(String functionId) throws DatabaseException, SQLException, IOException, ClassNotFoundException {
        return getDaoManager().getObligationsDAO().addFunctionArg(functionId);
    }

    void addFuntionArgValue(String functionId, EvrArg evrArg) throws DatabaseException, SQLException, IOException, ClassNotFoundException {
        getDaoManager().getObligationsDAO().addFunctionArgValue(functionId, evrArg);
    }

    void updateEntity(String entityId, EvrEntity evrEntity) throws DatabaseException, InvalidEntityException, SQLException, IOException, ClassNotFoundException {
        getDaoManager().getObligationsDAO().updateEntity(entityId, evrEntity);
    }

    String createPolicies(String ruleId, String parentLabel, boolean isOr) throws DatabaseException, SQLException, IOException, ClassNotFoundException {
        return getDaoManager().getObligationsDAO().createPolicies(ruleId, parentLabel, isOr);
    }

    void createOperations(String parentId, String parentType, HashSet<String> ops) throws DatabaseException, SQLException, IOException, ClassNotFoundException {
        getDaoManager().getObligationsDAO().createOperations(parentId, parentType, ops);
    }

    void createTime(String ruleId, EvrTime evrTime) throws DatabaseException, SQLException, IOException, ClassNotFoundException {
        getDaoManager().getObligationsDAO().createTime(ruleId, evrTime);
    }

    String createTarget(String ruleId, String parentLabel) throws DatabaseException, SQLException, IOException, ClassNotFoundException {
        return getDaoManager().getObligationsDAO().createTarget(ruleId, parentLabel);
    }

    String createCondition(String ruleId, boolean exists) throws DatabaseException, SQLException, IOException, ClassNotFoundException {
        return getDaoManager().getObligationsDAO().createCondition(ruleId, exists);
    }

    String createAssignAction(String parentId, String parentLabel) throws DatabaseException, SQLException, IOException, ClassNotFoundException {
        return getDaoManager().getObligationsDAO().createAssignAction(parentId, parentLabel);
    }

    String createAssignActionParam(String assignActionId, String param) throws DatabaseException, SQLException, IOException, ClassNotFoundException {
        return getDaoManager().getObligationsDAO().createAssignActionParam(assignActionId, param);
    }

    String createGrantAction(String parentId, String parentLabel) throws DatabaseException, SQLException, IOException, ClassNotFoundException {
        return getDaoManager().getObligationsDAO().createGrantAction(parentId, parentLabel);
    }

    String createCreateAction(String parentId, String parentLabel) throws DatabaseException, SQLException, IOException, ClassNotFoundException {
        return getDaoManager().getObligationsDAO().createCreateAction(parentId, parentLabel);
    }

    String createDenyAction(String parentId, String parentLabel) throws DatabaseException, SQLException, IOException, ClassNotFoundException {
        return getDaoManager().getObligationsDAO().createDenyAction(parentId, parentLabel);
    }

    String createDeleteAction(String parentId, String parentLabel) throws DatabaseException, SQLException, IOException, ClassNotFoundException {
        return getDaoManager().getObligationsDAO().createDeleteAction(parentId, parentLabel);
    }
}
