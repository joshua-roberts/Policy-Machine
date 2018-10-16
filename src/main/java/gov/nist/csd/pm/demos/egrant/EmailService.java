package gov.nist.csd.pm.demos.egrant;

import gov.nist.csd.pm.model.exceptions.*;
import gov.nist.csd.pm.model.graph.Node;
import gov.nist.csd.pm.pdp.services.AssignmentService;
import gov.nist.csd.pm.pdp.services.NodeService;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static gov.nist.csd.pm.pip.dao.DAOManager.getDaoManager;

public class EmailService {

    private NodeService       nodeService       = new NodeService();
    private AssignmentService assignmentService = new AssignmentService();

    public List<Email> getEmails(List<Long> emailIDs) throws DatabaseException, ClassNotFoundException, SQLException, InvalidPropertyException, IOException {
        return getDaoManager().getApplicationDAO().getEmails(emailIDs);
    }

    public void sendEmail(Email email, Node user, String session , long process) throws DatabaseException, ClassNotFoundException, SQLException, InvalidPropertyException, IOException, InvalidNodeTypeException, UnexpectedNumberOfNodesException, NullNameException, NodeNameExistsException, NodeIDExistsException, ConfigurationException, NullTypeException, NodeNotFoundException, InvalidAssignmentException, AssociationExistsException, AssignmentExistsException, PropertyNotFoundException, InvalidKeySpecException, NoSuchAlgorithmException, InvalidAssociationException, NoSubjectParameterException, SessionDoesNotExistException, InvalidProhibitionSubjectTypeException, SessionUserNotFoundException, MissingPermissionException, NodeNameExistsInNamespaceException {
        Map<String, String> properties = new HashMap<>();
        properties.put("inbox", email.getRecipient());
        Node recipientInbox = nodeService.getNode(null, null, properties);
        
        properties.clear();
        properties.put("outbox",user.getName());
        Node senderOutbox = nodeService.getNode(null, null, properties);

        Node emailNodeID = nodeService.createNodeIn(email.getEmailNodeID(),email.getSender() + " to " + email.getRecipient() + email.getTimestamp(),"OA",null, session , process);

        System.out.println(emailNodeID.getID());

        email.setEmailNodeID(((int) emailNodeID.getID()));

        assignmentService.createAssignment(emailNodeID.getID(),senderOutbox.getID());
        assignmentService.createAssignment(emailNodeID.getID(),recipientInbox.getID());
        if (email.getAttachments() != null) {
            for (Integer attachmentID : email.getAttachments()) {
                assignmentService.createAssignment(attachmentID, emailNodeID.getID());
            }
        }
        getDaoManager().getApplicationDAO().saveEmail(email);
    }
}
