package gov.nist.csd.pm.pdp.services;

import gov.nist.csd.pm.common.constants.Operations;
import gov.nist.csd.pm.common.exceptions.*;
import gov.nist.csd.pm.decider.Decider;
import gov.nist.csd.pm.exceptions.PMException;
import gov.nist.csd.pm.prohibitions.model.Prohibition;

import java.util.List;

import static gov.nist.csd.pm.common.constants.Operations.CREATE_PROHIBITION;
import static gov.nist.csd.pm.common.constants.Operations.PROHIBIT_SUBJECT;

public class ProhibitionsService extends Service {

    public ProhibitionsService(long userID, long processID) throws PMGraphException {
        super(userID, processID);
    }

    public void createProhibition(Prohibition prohibition) throws PMException {
        String name = prohibition.getName();
        Prohibition.Subject subject = prohibition.getSubject();
        List<Prohibition.Node> nodes = prohibition.getNodes();

        //check that the prohibition name is not null or empty
        if(name == null || name.isEmpty()) {
            throw new IllegalArgumentException("a null name was provided when creating a prohibition");
        }

        //check the prohibitions doesn't already exist
        for(Prohibition p : getProhibitions()) {
            if(p.getName().equals(name)) {
                throw new PMProhibitionException(String.format("a prohibition with the name %s already exists", name));
            }
        }

        //check the user can create a prohibition on the subject and the nodes
        Decider decider = getDecider();
        if(subject.getSubjectType().equals(Prohibition.Subject.Type.USER) || subject.getSubjectType().equals(Prohibition.Subject.Type.USER_ATTRIBUTE)) {
            // first check that the subject exists
            if(!getGraphPAP().exists(subject.getSubjectID())) {
                throw new PMGraphException(String.format("node with ID %d and type %s does not exist", subject.getSubjectID(), subject.getSubjectType()));
            }
            if(!decider.hasPermissions(getUserID(), subject.getSubjectID(), CREATE_PROHIBITION)) {
                throw new PMAuthorizationException(String.format("unauthorized permissions on %s: %s", subject.getSubjectID(), PROHIBIT_SUBJECT));
            }
        }

        for(Prohibition.Node node : nodes) {
            if(!decider.hasPermissions(getUserID(), node.getID(), CREATE_PROHIBITION)) {
                throw new PMAuthorizationException(String.format("unauthorized permissions on %s: %s", node.getID(), Operations.PROHIBIT_RESOURCE));
            }
        }

        //create prohibition in PAP
        getProhibitionsPAP().createProhibition(prohibition);
    }

    public List<Prohibition> getProhibitions() throws PMException {
        return getProhibitionsPAP().getProhibitions();
    }

    /**
     * Get the prohibition with the given node from the PAP. An exception will be thrown if one does not exist.
     *
     * @param prohibitionName The name of the Prohibition to retrieve.
     * @return the prohibition with the given node.
     * @throws PMConfigurationException if there is an error with the PAP configuration.
     * @throws PMAuthorizationException if the current user is not authorized to carry out the action.
     * @throws PMDBException if the PAP accesses the database and an error occurs.
     * @throws PMGraphException if there is an error with the graph.
     * @throws PMProhibitionException if a prohibition with the given name does not exist.
     */
    public Prohibition getProhibition(String prohibitionName) throws PMException {
        Prohibition prohibition = getProhibitionsPAP().getProhibition(prohibitionName);
        if(prohibition == null) {
            throw new PMProhibitionException(String.format("prohibition with the name %s does not exist", prohibitionName));
        }

        return prohibition;
    }

    /**
     * Update the prohibition.  The prohibition is identified by the name.
     *
     * @param prohibition The prohibition to update.
     * @throws IllegalArgumentException if the given prohibition is null.
     * @throws IllegalArgumentException if the given prohibition's name is null.
     * @throws PMConfigurationException if there is an error with the PAP configuration.
     * @throws PMAuthorizationException if the current user is not authorized to carry out the action.
     * @throws PMDBException if the PAP accesses the database and an error occurs.
     * @throws PMGraphException if there is an error with the graph.
     * @throws PMProhibitionException if a prohibition with the given name does not exist.
     */
    public void updateProhibition(Prohibition prohibition) throws PMException {
        if(prohibition == null) {
            throw new IllegalArgumentException("the prohibition to update was null");
        } else if(prohibition.getName() == null || prohibition.getName().isEmpty()) {
            throw new IllegalArgumentException("cannot update a prohibition with a null name");
        }

        // TODO need to check if the user has permission to update the prohibition.
        // delete the prohibition
        deleteProhibition(prohibition.getName());

        //create prohibition in PAP
        createProhibition(prohibition);
    }

    public void deleteProhibition(String prohibitionName) throws PMException {
        //check that the prohibition exists
        getProhibition(prohibitionName);

        //delete prohibition in PAP
        getProhibitionsPAP().deleteProhibition(prohibitionName);
    }
}