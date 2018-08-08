package gov.nist.policyserver.service;

import gov.nist.policyserver.exceptions.*;
import gov.nist.policyserver.model.applications.Email;
import gov.nist.policyserver.model.graph.nodes.Node;
import gov.nist.policyserver.model.graph.nodes.Property;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static gov.nist.policyserver.dao.DAOManager.getDaoManager;

public class EmailService {

    private NodeService nodeService      = new NodeService();
    private AssignmentService assignmentService      = new AssignmentService();

    public List<Email> getEmails(List<Long> emailIds) throws DatabaseException, ClassNotFoundException, SQLException, InvalidPropertyException, IOException {
        return getDaoManager().getApplicationDAO().getEmails(emailIds);
    }

    public void sendEmail(Email email, Node user) throws DatabaseException, ClassNotFoundException, SQLException, InvalidPropertyException, IOException, InvalidNodeTypeException, UnexpectedNumberOfNodesException, NullNameException, NodeNameExistsException, NodeIdExistsException, ConfigurationException, NullTypeException, NodeNotFoundException, InvalidAssignmentException, AssociationExistsException, AssignmentExistsException, PropertyNotFoundException, InvalidKeySpecException, NoSuchAlgorithmException {
        Property p = new Property("inbox",email.getRecipient());
        List<Property> propList = new ArrayList<Property>();
        propList.add(p);
        Node recepientInbox = nodeService.getNode(null, null, null, propList);
        p = new Property("outbox",user.getName());
        propList = new ArrayList<Property>();
        propList.add(p);
        Node senderOutbox = nodeService.getNode(null, null, null, propList);
        Node emailNodeId = nodeService.createNode(email.getEmailNodeId(),email.getSender() + " to " + email.getRecipient() + email.getTimestamp(),"OA",null);
        System.out.println(emailNodeId.getId());
        email.setEmailNodeId(((int) emailNodeId.getId()));
        assignmentService.createAssignment(emailNodeId.getId(),senderOutbox.getId());
        assignmentService.createAssignment(emailNodeId.getId(),recepientInbox.getId());
        if (email.getAttachments() != null) {
            for (Integer attachmentId : email.getAttachments()) {
                assignmentService.createAssignment(attachmentId, emailNodeId.getId());
            }
        }
        getDaoManager().getApplicationDAO().saveEmail(email);
    }
}
