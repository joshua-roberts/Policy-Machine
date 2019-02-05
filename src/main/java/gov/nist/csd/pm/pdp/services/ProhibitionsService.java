package gov.nist.csd.pm.pdp.services;

import gov.nist.csd.pm.common.constants.Operations;
import gov.nist.csd.pm.common.exceptions.*;
import gov.nist.csd.pm.common.model.prohibitions.*;
import gov.nist.csd.pm.pdp.engine.Decider;

import java.util.List;

import static gov.nist.csd.pm.common.constants.Operations.CREATE_PROHIBITION;
import static gov.nist.csd.pm.common.constants.Operations.PROHIBIT_SUBJECT;

public class ProhibitionsService extends Service implements ProhibitionsDAO {

    public ProhibitionsService(long userID, long processID) throws PMException {
        super(userID, processID);
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
        Decider decider = getDecider();
        if(subject.getSubjectType().equals(ProhibitionSubjectType.U) || subject.getSubjectType().equals(ProhibitionSubjectType.UA)) {
            // first check that the subject exists
            if(!getGraphPAP().exists(subject.getSubjectID())) {
                throw new PMException(Errors.ERR_NODE_NOT_FOUND, String.format("node with ID %d and type %s does not exist", subject.getSubjectID(), subject.getSubjectType()));
            }
            if(!decider.hasPermissions(getUserID(), getProcessID(), subject.getSubjectID(), CREATE_PROHIBITION)) {
                throw new PMException(Errors.ERR_MISSING_PERMISSIONS, String.format("Missing permissions on %d: %s", subject.getSubjectID(), PROHIBIT_SUBJECT));
            }
        }

        for(ProhibitionNode node : nodes) {
            if(!decider.hasPermissions(getUserID(), getProcessID(), node.getID(), CREATE_PROHIBITION)) {
                throw new PMException(Errors.ERR_MISSING_PERMISSIONS, String.format("Missing permissions on %d: %s", node.getID(), Operations.PROHIBIT_RESOURCE));
            }
        }

        //create prohibition in PAP
        getProhibitionsPAP().createProhibition(prohibition);
    }

    @Override
    public List<Prohibition> getProhibitions() throws PMException {
        return getProhibitionsPAP().getProhibitions();
    }

    @Override
    public Prohibition getProhibition(String prohibitionName) throws PMException {
        Prohibition prohibition = getProhibitionsPAP().getProhibition(prohibitionName);
        if(prohibition == null) {
            throw new PMException(Errors.ERR_PROHIBITION_DOES_NOT_EXIST, String.format("prohibition with the name %s does not exist", prohibitionName));
        }

        return prohibition;
    }

    @Override
    public void updateProhibition(Prohibition prohibition) throws PMException {
        // TODO need to check if the user has permission to update the prohibition.
        // delete the prohibition
        deleteProhibition(prohibition.getName());

        //create prohibition in PAP
        createProhibition(prohibition);
    }

    @Override
    public void deleteProhibition(String prohibitionName) throws PMException {
        //check that the prohibition exists
        getProhibition(prohibitionName);

        //delete prohibition in PAP
        getProhibitionsPAP().deleteProhibition(prohibitionName);
    }
}