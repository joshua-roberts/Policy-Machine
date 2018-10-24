package gov.nist.csd.pm.pdp.services;

import gov.nist.csd.pm.model.exceptions.*;
import gov.nist.csd.pm.model.graph.Association;
import gov.nist.csd.pm.model.graph.OldNode;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;

public class AssociationsService extends Service{

    public void createAssociation(long uaID, long targetID, HashSet<String> ops) throws NodeNotFoundException, DatabaseException, AssociationExistsException, SQLException, IOException, ClassNotFoundException, InvalidPropertyException, InvalidAssociationException {
        //check that the target and user attribute nodes exist
        OldNode target = getGraph().getNode(targetID);
        if(target == null){
            throw new NodeNotFoundException(targetID);
        }
        OldNode ua = getGraph().getNode(uaID);
        if(ua == null){
            throw new NodeNotFoundException(uaID);
        }

        Association.checkAssociation(ua.getType(), target.getType());

        Association association = getGraph().getAssociation(uaID, targetID);
        if(association != null) {
            throw new AssociationExistsException(uaID, targetID);
        }

        //create association in database
       getDaoManager().getAssociationsDAO().createAssociation(uaID, targetID, ops);

        //create association in nodes
        getGraph().createAssociation(uaID, targetID, ops);
    }

    public void updateAssociation(long targetID, long uaID, HashSet<String> ops) throws NodeNotFoundException, AssociationDoesNotExistException, DatabaseException, SQLException, IOException, ClassNotFoundException, InvalidPropertyException {
        //check that the target and user attribute nodes exist
        OldNode target = getGraph().getNode(targetID);
        if(target == null){
            throw new NodeNotFoundException(targetID);
        }
        OldNode ua = getGraph().getNode(uaID);
        if(ua == null){
            throw new NodeNotFoundException(uaID);
        }

        //check ua -> oa exists
        Association assoc = getAssociation(uaID, targetID);
        if (assoc == null) {
            throw new AssociationDoesNotExistException(uaID, targetID);
        }

        //update association in database
        getDaoManager().getAssociationsDAO().updateAssociation(uaID, targetID, ops);

        //update association in nodes
        getGraph().updateAssociation(uaID, targetID, ops);
    }

    private Association getAssociation(long uaID, long targetID) throws AssociationDoesNotExistException, ClassNotFoundException, SQLException, DatabaseException, IOException, InvalidPropertyException {
        Association association = getGraph().getAssociation(uaID, targetID);
        if(association == null){
            throw new AssociationDoesNotExistException(uaID, targetID);
        }

        return association;
    }

    public void deleteAssociation(long targetID, long uaID) throws NoUserParameterException, NodeNotFoundException, AssociationDoesNotExistException, ConfigurationException, DatabaseException, SQLException, IOException, ClassNotFoundException, InvalidPropertyException {
        //check that the nodes exist
        OldNode target = getGraph().getNode(targetID);
        if(target == null){
            throw new NodeNotFoundException(targetID);
        }
        OldNode ua = getGraph().getNode(uaID);
        if(ua == null){
            throw new NodeNotFoundException(uaID);
        }

        //check ua -> oa exists
        if(isAssociated(ua, target)) {
            throw new AssociationDoesNotExistException(uaID, targetID);
        }

        //delete the association in database
        getDaoManager().getAssociationsDAO().deleteAssociation(uaID, targetID);

        //delete the association in nodes
        getGraph().deleteAssociation(uaID, targetID);
    }

    private boolean isAssociated(OldNode ua, OldNode target) throws ClassNotFoundException, SQLException, DatabaseException, IOException, InvalidPropertyException {
        return getGraph().isAssociated(ua, target);
    }

    public List<Association> getTargetAssociations(long targetID) throws NodeNotFoundException, ClassNotFoundException, SQLException, DatabaseException, IOException, InvalidPropertyException {
        OldNode target = getGraph().getNode(targetID);
        if(target == null){
            throw new NodeNotFoundException(targetID);
        }
        return getGraph().getTargetAssociations(targetID);
    }

    public List<Association> getSubjectAssociations(long subjectID) throws NodeNotFoundException, ClassNotFoundException, SQLException, DatabaseException, IOException, InvalidPropertyException {
        OldNode target = getGraph().getNode(subjectID);
        if(target == null){
            throw new NodeNotFoundException(subjectID);
        }
        return getGraph().getUattrAssociations(subjectID);
    }

    public List<Association> getAssociations() throws ClassNotFoundException, SQLException, DatabaseException, IOException, InvalidPropertyException {
        return getGraph().getAssociations();
    }
}
