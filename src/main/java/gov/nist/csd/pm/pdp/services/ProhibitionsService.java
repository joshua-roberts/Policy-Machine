package gov.nist.csd.pm.pdp.services;

import gov.nist.csd.pm.common.constants.Operations;
import gov.nist.csd.pm.common.exceptions.*;
import gov.nist.csd.pm.common.model.prohibitions.*;
import gov.nist.csd.pm.pdp.engine.Decider;

import java.util.List;

import static gov.nist.csd.pm.common.constants.Operations.ANY_OPERATIONS;
import static gov.nist.csd.pm.common.constants.Operations.PROHIBIT_SUBJECT;

public class ProhibitionsService extends Service implements ProhibitionsDAO {

    public ProhibitionsService(String sessionID, long processID) throws PMException {
        super(sessionID, processID);
    }

    @Override
    public void createProhibition(Prohibition prohibition) throws PMException {
        String name = prohibition.getName();
        ProhibitionSubject subject = prohibition.getSubject();
        List<ProhibitionNode> nodes = prohibition.getNodes();

        //check that the prohibition name is not null or empty
        if(name == null || name.isEmpty()) {
            throw new PMException(Errors.ERR_NULL_NAME, "a null name was provided when creating a prohibition");
        }

        //check the prohibitions doesn't already exist
        for(Prohibition p : getProhibitions()) {
            if(p.getName().equals(name)) {
                throw new PMException(Errors.ERR_PROHIBITION_NAME_EXISTS, String.format("a prohibition with the name %s already exists", name));
            }
        }

        //check the user can create a prohibition on the subject and the nodes
        Decider decider = newPolicyDecider();
        if(subject.getSubjectType().equals(ProhibitionSubjectType.U)) {
            if(!decider.hasPermissions(getSessionUserID(), getProcessID(), subject.getSubjectID(), ANY_OPERATIONS)) {
                throw new PMException(Errors.ERR_MISSING_PERMISSIONS, String.format("Missing permissions on %d: %s", subject.getSubjectID(), PROHIBIT_SUBJECT));
            }
        }

        for(ProhibitionNode node : nodes) {
            if(!decider.hasPermissions(getSessionUserID(), getProcessID(), node.getID(), ANY_OPERATIONS)) {
                throw new PMException(Errors.ERR_MISSING_PERMISSIONS, String.format("Missing permissions on %d: %s", node.getID(), Operations.PROHIBIT_RESOURCE));

            }
        }

        //create prohibition in PAP
        getProhibitionsDB().createProhibition(prohibition);
        getProhibitionsMem().createProhibition(prohibition);
    }

    @Override
    public List<Prohibition> getProhibitions() throws PMException {
        return getProhibitionsMem().getProhibitions();
    }

    @Override
    public Prohibition getProhibition(String prohibitionName) throws PMException {
        List<Prohibition> prohibitions = getProhibitions();
        for(Prohibition prohibition : prohibitions) {
            if(prohibition.getName().equals(prohibitionName)) {
                return prohibition;
            }
        }
        throw new PMException(Errors.ERR_PROHIBITION_DOES_NOT_EXIST, String.format("prohibition with the name %s does not exist", prohibitionName));
    }

    @Override
    public void updateProhibition(Prohibition prohibition) throws PMException {
        //create prohibition in PAP
        getProhibitionsDB().updateProhibition(prohibition);
        getProhibitionsMem().updateProhibition(prohibition);
    }

    @Override
    public void deleteProhibition(String prohibitionName) throws PMException {
        //check that the prohibition exists
        getProhibition(prohibitionName);

        //delete prohibition in PAP
        getProhibitionsMem().deleteProhibition(prohibitionName);
        getProhibitionsDB().deleteProhibition(prohibitionName);
    }
}