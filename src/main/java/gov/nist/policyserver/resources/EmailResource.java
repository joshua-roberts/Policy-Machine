package gov.nist.policyserver.resources;

import gov.nist.policyserver.exceptions.*;
import gov.nist.policyserver.analytics.PmAnalyticsEntry;
import gov.nist.policyserver.model.applications.Email;
import gov.nist.policyserver.model.graph.nodes.Node;

import gov.nist.policyserver.requests.EmailRequest;
import gov.nist.policyserver.response.ApiResponse;
import gov.nist.policyserver.service.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
                             @QueryParam("session") String session) throws InvalidNodeTypeException, InvalidPropertyException,
            SessionUserNotFoundException, ConfigurationException, SessionDoesNotExistException, ClassNotFoundException, SQLException, IOException, DatabaseException, UnexpectedNumberOfNodesException, NodeNotFoundException, NoUserParameterException {
        {

            List<Long> emailIds = new ArrayList<>();
            Node user = analyticsService.getSessionUser(session);
            Node inbox = nodeService.getNode(null, user.getName() + " " + box, null, null);
            List<PmAnalyticsEntry> entries = analyticsService.getAccessibleChildren(inbox.getId(), user.getId());
            for (PmAnalyticsEntry entry : entries) {
                emailIds.add(entry.getTarget().getId());
            }
            return new ApiResponse(emailService.getEmails(emailIds)).toResponse();
        }
    }
    @Path("/sendEmail")
    @POST
    public Response sendEmail(EmailRequest request, @QueryParam("session") String session) throws NullNameException, NodeIdExistsException, NodeNotFoundException, NodeNameExistsException, SQLException, DatabaseException, InvalidNodeTypeException, IOException, InvalidPropertyException, ClassNotFoundException, ConfigurationException, NullTypeException, SessionDoesNotExistException, SessionUserNotFoundException, UnexpectedNumberOfNodesException, InvalidAssignmentException, AssociationExistsException, AssignmentExistsException, PropertyNotFoundException {
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
        email.setEmailNodeId(request.getEmailNodeId());
        email.setEmailSubject(request.getEmailSubject());
        email.setTimestamp(request.getTimestamp());
        email.setRecipient(request.getRecipient());
        email.setSender(request.getSender());
        email.setAttachments(request.getAttachments());
        email.setEmailBody(request.getEmailBody());
        emailService.sendEmail(email,user);

        return null;
    }
    }