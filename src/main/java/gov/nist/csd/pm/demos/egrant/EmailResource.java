package gov.nist.csd.pm.demos.egrant;

import gov.nist.csd.pm.model.exceptions.*;
import gov.nist.csd.pm.pdp.analytics.PmAnalyticsEntry;
import gov.nist.csd.pm.model.graph.Node;

import gov.nist.csd.pm.pep.requests.EmailRequest;
import gov.nist.csd.pm.pep.response.ApiResponse;
import gov.nist.csd.pm.pdp.services.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/email")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class EmailResource {

    private NodeService nodeService      = new NodeService();
    private AssignmentService assignmentService      = new AssignmentService();
    private EmailService emailService      = new EmailService();
    private AnalyticsService analyticsService = new AnalyticsService();

    @GET
    public Response getEmails(@QueryParam("box") String box,
                              @QueryParam("session") String session,
                              @QueryParam("process") long process) throws InvalidNodeTypeException, InvalidPropertyException,
            SessionUserNotFoundException, ConfigurationException, SessionDoesNotExistException, ClassNotFoundException, SQLException, IOException, DatabaseException, UnexpectedNumberOfNodesException, NodeNotFoundException, NoUserParameterException {
        {

            List<Long> emailIDs = new ArrayList<>();
            Node user = analyticsService.getSessionUser(session);
            System.out.println(" user is " + user.getName());

            Map<String, String> properties = new HashMap<>();
            properties.put("inbox",user.getName());
            Node inbox = nodeService.getNode(null, null, properties);

            System.out.println(" name of the inbox " + inbox.getName() + " consists of ");
            List<PmAnalyticsEntry> emails = analyticsService.getAccessibleChildren(inbox.getID(),user.getID());
            for (PmAnalyticsEntry entry : emails) {
                System.out.println(" email id " + entry.getTarget().getName());
                emailIDs.add(entry.getTarget().getID());
            }
            return new ApiResponse(emailService.getEmails(emailIDs)).toResponse();
        }
    }
    @Path("/sendEmail")
    @POST
    public Response sendEmail(EmailRequest request,
                              @QueryParam("session") String session,
                              @QueryParam("process") long process) throws NullNameException, NodeIDExistsException, NodeNotFoundException, NodeNameExistsException, SQLException, DatabaseException, InvalidNodeTypeException, IOException, InvalidPropertyException, ClassNotFoundException, ConfigurationException, NullTypeException, SessionDoesNotExistException, SessionUserNotFoundException, UnexpectedNumberOfNodesException, InvalidAssignmentException, AssociationExistsException, AssignmentExistsException, PropertyNotFoundException, InvalidKeySpecException, NoSuchAlgorithmException, InvalidAssociationException, NoSubjectParameterException, MissingPermissionException, InvalidProhibitionSubjectTypeException, NodeNameExistsInNamespaceException {
    /*
        Steps
        1. Create OA Node and get the node_id back with email_node_id
        2. insert into email and attachment tables
        3. assign the email_node_id to outbox of the user
        4. assign attachment to email_node_id oa
        5. assign email_node_id to inbox of recepient
    */

        Node user = analyticsService.getSessionUser(session);

        Email email = new Email();
        email.setEmailNodeID(request.getEmailNodeID());
        email.setEmailSubject(request.getEmailSubject());
        email.setTimestamp(request.getTimestamp());
        email.setRecipient(request.getRecipient());
        email.setSender(request.getSender());
        email.setAttachments(request.getAttachments());
        email.setEmailBody(request.getEmailBody());
        emailService.sendEmail(email, user, session, process);

        return null;
    }
}