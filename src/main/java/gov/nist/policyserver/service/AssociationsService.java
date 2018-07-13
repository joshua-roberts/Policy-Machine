package gov.nist.policyserver.service;

import gov.nist.policyserver.exceptions.*;
import gov.nist.policyserver.model.graph.nodes.Node;
import gov.nist.policyserver.model.graph.relationships.Association;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;

public class AssociationsService extends Service{

    public void createAssociation(long uaId, long targetId, HashSet<String> ops, boolean inherit) throws NodeNotFoundException, DatabaseException, AssociationExistsException, SQLException, IOException, ClassNotFoundException, InvalidPropertyException {
        // TODO check types are valid for association

        //check that the target and user attribute nodes exist
        Node target = getGraph().getNode(targetId);
        if(target == null){
            throw new NodeNotFoundException(targetId);
        }

        Node ua = getGraph().getNode(uaId);
        if(ua == null){
            throw new NodeNotFoundException(uaId);
        }

        Association association = getGraph().getAssociation(uaId, targetId);
        if(association != null) {
            throw new AssociationExistsException(uaId, targetId);
        }

        //create association in database
       getDaoManager().getAssociationsDAO().createAssociation(uaId, targetId, ops, inherit);

        //create association in nodes
        getGraph().createAssociation(uaId, targetId, ops, inherit);
    }

    public void updateAssociation(long targetId, long uaId, HashSet<String> ops, boolean inherit) throws NodeNotFoundException, AssociationDoesNotExistException, DatabaseException, SQLException, IOException, ClassNotFoundException, InvalidPropertyException {
        //check that the target and user attribute nodes exist
        Node target = getGraph().getNode(targetId);
        if(target == null){
            throw new NodeNotFoundException(targetId);
        }
        Node ua = getGraph().getNode(uaId);
        if(ua == null){
            throw new NodeNotFoundException(uaId);
        }

        //check ua -> oa exists
        Association assoc = getAssociation(uaId, targetId);
        if (assoc == null) {
            throw new AssociationDoesNotExistException(uaId, targetId);
        }

        //update association in database
        getDaoManager().getAssociationsDAO().updateAssociation(uaId, targetId, inherit, ops);

        //update association in nodes
        getGraph().updateAssociation(uaId, targetId, ops, inherit);
    }

    private Association getAssociation(long uaId, long targetId) throws AssociationDoesNotExistException, ClassNotFoundException, SQLException, DatabaseException, IOException, InvalidPropertyException {
        Association association = getGraph().getAssociation(uaId, targetId);
        if(association == null){
            throw new AssociationDoesNotExistException(uaId, targetId);
        }

        return association;
    }

    public void deleteAssociation(long targetId, long uaId) throws NoUserParameterException, NodeNotFoundException, AssociationDoesNotExistException, ConfigurationException, DatabaseException, SQLException, IOException, ClassNotFoundException, InvalidPropertyException {
        //check the user attribute id is present
        if(uaId == 0){
            throw new NoUserParameterException();
        }

        //check that the nodes exist
        Node target = getGraph().getNode(targetId);
        if(target == null){
            throw new NodeNotFoundException(targetId);
        }
        Node ua = getGraph().getNode(uaId);
        if(ua == null){
            throw new NodeNotFoundException(uaId);
        }

        //check ua -> oa exists
        Association assoc = getAssociation(uaId, targetId);
        if (assoc == null) {
            throw new AssociationDoesNotExistException(uaId, targetId);
        }

        //delete the association in database
        getDaoManager().getAssociationsDAO().deleteAssociation(uaId, targetId);

        //delete the association in nodes
        getGraph().deleteAssociation(uaId, targetId);
    }

    public List<Association> getTargetAssociations(long targetId) throws NodeNotFoundException, ClassNotFoundException, SQLException, DatabaseException, IOException, InvalidPropertyException {
        Node target = getGraph().getNode(targetId);
        if(target == null){
            throw new NodeNotFoundException(targetId);
        }
        return getGraph().getTargetAssociations(targetId);
    }

    public List<Association> getSubjectAssociations(long subjectId) throws NodeNotFoundException, ClassNotFoundException, SQLException, DatabaseException, IOException, InvalidPropertyException {
        Node target = getGraph().getNode(subjectId);
        if(target == null){
            throw new NodeNotFoundException(subjectId);
        }
        return getGraph().getUattrAssociations(subjectId);
    }

    public List<Association> getAssociations() throws ClassNotFoundException, SQLException, DatabaseException, IOException, InvalidPropertyException {
        return getGraph().getAssociations();
    }
}
