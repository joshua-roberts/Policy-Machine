package gov.nist.csd.pm.pdp.services;

import gov.nist.csd.pm.model.exceptions.*;
import gov.nist.csd.pm.model.graph.Node;
import gov.nist.csd.pm.model.prohibitions.Prohibition;
import gov.nist.csd.pm.model.prohibitions.ProhibitionResource;
import gov.nist.csd.pm.model.prohibitions.ProhibitionSubject;
import gov.nist.csd.pm.model.prohibitions.ProhibitionSubjectType;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

public class ProhibitionsService extends Service{

    public Prohibition createProhibition(String name, String[] operations, boolean intersection,
                                         ProhibitionResource[] resources, ProhibitionSubject denySubject)
            throws ProhibitionNameExistsException, DatabaseException, NullNameException, SQLException, IOException, ClassNotFoundException, InvalidPropertyException {
        if(name == null || name.isEmpty()) {
            throw new NullNameException();
        }

        //check the prohibitions doesn't already exist
        Prohibition prohibition = getAnalytics().getProhibition(name);
        if(prohibition != null){
            throw new ProhibitionNameExistsException(name);
        }

        HashSet<String> hsOps = new HashSet<>(Arrays.asList(operations));

        //create prohibition in database
        getDaoManager().getProhibitionsDAO().createProhibition(name, hsOps, intersection, resources, denySubject);

        List<ProhibitionResource> resourcesList = new ArrayList<>();
        Collections.addAll(resourcesList, resources);

        //add prohibition to nodes
        prohibition = new Prohibition(denySubject, resourcesList, name, hsOps, intersection);
        getAnalytics().addProhibition(prohibition);

        return prohibition;
    }

    public Prohibition getProhibition(String prohibitionName)
            throws ProhibitionDoesNotExistException, ClassNotFoundException, SQLException, DatabaseException, IOException, InvalidPropertyException {
        Prohibition prohibition = getAnalytics().getProhibition(prohibitionName);
        if(prohibition == null){
            throw new ProhibitionDoesNotExistException(prohibitionName);
        }

        return prohibition;
    }

    public void deleteProhibition(String prohibitionName)
            throws ProhibitionDoesNotExistException, DatabaseException, SQLException, IOException, ClassNotFoundException, InvalidPropertyException {
        //check that the prohibition exists
        getProhibition(prohibitionName);

        //delete prohibition in database
        getDaoManager().getProhibitionsDAO().deleteProhibition(prohibitionName);

        //delete prohibition in memory
        getAnalytics().removeProhibition(prohibitionName);
    }

    private void addResourceToProhibition(String prohibitionName, long resourceID, boolean complement)
            throws DatabaseException, ProhibitionDoesNotExistException, NodeNotFoundException, ProhibitionResourceExistsException, ConfigurationException, SQLException, IOException, ClassNotFoundException, InvalidPropertyException {
        //check if the prohibition exists
        Prohibition prohibition = getProhibition(prohibitionName);

        //check if the resource exists
        Node node = getGraph().getNode(resourceID);
        if(node == null){
            throw new NodeNotFoundException(resourceID);
        }

        //check if resource exists in prohibition
        ProhibitionResource pr = new ProhibitionResource(resourceID, complement);
        if(prohibition.getResources().contains(pr)){
            throw new ProhibitionResourceExistsException(prohibitionName, resourceID);
        }
        //add resource to prohibition in database
        getDaoManager().getProhibitionsDAO().addResourceToProhibition(prohibitionName, resourceID, complement);

        //add resource to prohibition in memory
        prohibition.addResource(pr);

    }

    private void setProhibitionSubject(String prohibitionName, long subjectID, String subjectType)
            throws InvalidProhibitionSubjectTypeException, DatabaseException, ProhibitionDoesNotExistException, ConfigurationException, SQLException, IOException, ClassNotFoundException, InvalidPropertyException {
        ProhibitionSubjectType subType = ProhibitionSubjectType.toProhibitionSubjectType(subjectType);

        //check if prohibition exists
        Prohibition prohibition = getProhibition(prohibitionName);

        //set the prohibition subject in the database
        getDaoManager().getProhibitionsDAO().setProhibitionSubject(prohibitionName, subjectID, subType);

        //set the prohibition subject in memory
        prohibition.setSubject(new ProhibitionSubject(subjectID, subType));
    }

    public Prohibition updateProhibition(String name, boolean intersection, String[] operations, ProhibitionResource[] resources, ProhibitionSubject subject) throws ProhibitionDoesNotExistException, ConfigurationException, DatabaseException, InvalidProhibitionSubjectTypeException, NodeNotFoundException, ProhibitionResourceExistsException, SQLException, IOException, ClassNotFoundException, InvalidPropertyException {
        //check the prohibition exists
        Prohibition prohibition = getProhibition(name);

        //set intersection
        getDaoManager().getProhibitionsDAO().setProhibitionIntersection(name, intersection);
        prohibition.setIntersection(intersection);

        //set operations
        HashSet<String> opSet = new HashSet<>(Arrays.asList(operations));
        getDaoManager().getProhibitionsDAO().setProhibitionOperations(prohibition.getName(), opSet);
        prohibition.setOperations(opSet);

        //set resources
        List<ProhibitionResource> oldResources = prohibition.getResources();

        //delete from database
        for(ProhibitionResource prohibitionResource : oldResources) {
            getDaoManager().getProhibitionsDAO().deleteProhibitionResource(name, prohibitionResource.getResourceID());
        }
        //remove from memory
        prohibition.clearResources();


        //add new resources
        for(ProhibitionResource prohibitionResource : resources) {
            addResourceToProhibition(prohibition.getName(), prohibitionResource.getResourceID(), prohibitionResource.isComplement());
        }

        //set subject
        setProhibitionSubject(prohibition.getName(), subject.getSubjectID(), subject.getSubjectType().toString());

        return getProhibition(name);
    }

    public List<Prohibition> getProhibitions(long subjectID, long resourceID) throws ClassNotFoundException, SQLException, DatabaseException, IOException, InvalidPropertyException {
        List<Prohibition> prohibitions = getAnalytics().getProhibitions();
        if(subjectID != 0) {
            prohibitions.removeIf(prohibition -> prohibition.getSubject().getSubjectID() != subjectID);
        }

        if(resourceID != 0) {
            prohibitions.removeIf(prohibition -> {
                List<ProhibitionResource> resources = prohibition.getResources();
                for(ProhibitionResource resource : resources) {
                    if(resource.getResourceID() == resourceID) {
                        return true;
                    }
                }
                return false;
            });
        }

        return prohibitions;
    }
}
