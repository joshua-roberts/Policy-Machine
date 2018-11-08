package gov.nist.csd.pm.pdp.services;

import gov.nist.csd.pm.model.exceptions.*;
import gov.nist.csd.pm.model.prohibitions.*;
import gov.nist.csd.pm.pdp.engine.PolicyDecider;

import java.util.List;

import static gov.nist.csd.pm.model.constants.Operations.ANY_OPERATIONS;
import static gov.nist.csd.pm.model.constants.Operations.PROHIBIT_SUBJECT;

public class ProhibitionsService extends Service implements ProhibitionsDAO {

    public ProhibitionsService(String sessionID, long processID) {
        super(sessionID, processID);
    }

    @Override
    public void createProhibition(Prohibition prohibition) throws DatabaseException, NullNameException, ProhibitionNameExistsException, LoadConfigException, LoaderException, SessionDoesNotExistException, NodeNotFoundException, MissingPermissionException, InvalidProhibitionSubjectTypeException {
        String name = prohibition.getName();
        ProhibitionSubject subject = prohibition.getSubject();
        List<ProhibitionNode> nodes = prohibition.getNodes();

        //check that the prohibition name is not null or empty
        if(name == null || name.isEmpty()) {
            throw new NullNameException();
        }

        //check the prohibitions doesn't already exist
        for(Prohibition p : getProhibitions()) {
            if(p.getName().equals(name)) {
                throw new ProhibitionNameExistsException(name);
            }
        }

        //check the user can create a prohibition on the subject and the nodes
        PolicyDecider decider = newPolicyDecider();
        if(subject.getSubjectType().equals(ProhibitionSubjectType.U)) {
            if(!decider.hasPermissions(getSessionUserID(), getProcessID(), subject.getSubjectID(), ANY_OPERATIONS)) {
                throw new MissingPermissionException(subject.getSubjectID(), PROHIBIT_SUBJECT);
            }
        }

        for(ProhibitionNode res : nodes) {
            if(!decider.hasPermissions(getSessionUserID(), getProcessID(), res.getID(), ANY_OPERATIONS)) {
                throw new MissingPermissionException(res.getID(), PROHIBIT_SUBJECT);
            }
        }

        //create prohibition in PAP
        getProhibitionsDB().createProhibition(prohibition);
        getProhibitionsMem().createProhibition(prohibition);
    }

    @Override
    public List<Prohibition> getProhibitions() throws LoadConfigException, DatabaseException, LoaderException, InvalidProhibitionSubjectTypeException {
        return getProhibitionsMem().getProhibitions();
    }

    @Override
    public Prohibition getProhibition(String prohibitionName)
            throws ProhibitionDoesNotExistException, DatabaseException, LoadConfigException, LoaderException, InvalidProhibitionSubjectTypeException {
        List<Prohibition> prohibitions = getProhibitions();
        for(Prohibition prohibition : prohibitions) {
            if(prohibition.getName().equals(prohibitionName)) {
                return prohibition;
            }
        }
        throw new ProhibitionDoesNotExistException(prohibitionName);
    }

    @Override
    public void updateProhibition(Prohibition prohibition) throws DatabaseException, LoadConfigException, InvalidProhibitionSubjectTypeException, LoaderException {
        //create prohibition in PAP
        getProhibitionsDB().updateProhibition(prohibition);
        getProhibitionsMem().updateProhibition(prohibition);
    }

    @Override
    public void deleteProhibition(String prohibitionName)
            throws DatabaseException, LoadConfigException, InvalidProhibitionSubjectTypeException, LoaderException, ProhibitionDoesNotExistException {
        //check that the prohibition exists
        getProhibition(prohibitionName);

        //delete prohibition in PAP
        getProhibitionsMem().deleteProhibition(prohibitionName);
        getProhibitionsDB().deleteProhibition(prohibitionName);
    }
}