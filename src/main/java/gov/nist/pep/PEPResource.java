package gov.nist.pep;

import gov.nist.policyserver.analytics.PmAnalyticsEntry;
import gov.nist.policyserver.exceptions.*;
import gov.nist.policyserver.model.graph.nodes.Node;
import gov.nist.policyserver.model.graph.nodes.NodeType;
import gov.nist.policyserver.requests.AssignmentRequest;
import gov.nist.policyserver.requests.CreateNodeRequest;
import gov.nist.policyserver.requests.CreateSessionRequest;
import gov.nist.policyserver.requests.GrantRequest;
import gov.nist.policyserver.response.ApiResponse;
import gov.nist.policyserver.service.AnalyticsService;
import gov.nist.policyserver.service.AssignmentService;
import gov.nist.policyserver.service.NodeService;
import gov.nist.policyserver.service.SessionService;
import gov.nist.policyserver.translator.exceptions.PMAccessDeniedException;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static gov.nist.policyserver.common.Constants.ASSIGN_OBJECT_ATTRIBUTE;
import static gov.nist.policyserver.common.Constants.FILE_WRITE;

@Path("/pep")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PEPResource {

    NodeService       nodeService       = new NodeService();
    AnalyticsService  analyticsService  = new AnalyticsService();
    AssignmentService assignmentService = new AssignmentService();
    SessionService sessionService = new SessionService();

    @Path("/sessions")
    @POST
    public Response createSession(CreateSessionRequest request) throws InvalidAssignmentException, NoSuchAlgorithmException, ConfigurationException, InvalidNodeTypeException, PMAccessDeniedException, ClassNotFoundException, DatabaseException, NullNameException, NullTypeException, NodeNameExistsException, NodeIdExistsException, PropertyNotFoundException, SQLException, InvalidPropertyException, NodeNameExistsInNamespaceException, InvalidKeySpecException, NodeNotFoundException, AssignmentExistsException, IOException {
        return new ApiResponse(sessionService.createSession(request.getUsername(), request.getPassword())).toResponse();
    }


    @Path("/analytics/rows/{rowId}/columns")
    @GET
    public Response getRowAccessibleColumns(@PathParam("rowId") String rowId, @QueryParam("table") String table, @QueryParam("username") String username) throws InvalidNodeTypeException, InvalidPropertyException, NodeNotFoundException, NoUserParameterException, ConfigurationException, ClassNotFoundException, SQLException, IOException, DatabaseException {
        System.out.println(rowId + " " + table + " " + username);
        //get the user id
        HashSet<Node> nodes = nodeService.getNodes(null, username, NodeType.U.toString(), null);
        if(nodes.size() != 1) {
            throw new NodeNotFoundException(username);
        }
        Node userNode = nodes.iterator().next();

        //get the rowId
        nodes = nodeService.getNodes(table, String.valueOf(rowId), NodeType.OA.toString(), null);
        if(nodes.size() != 1) {
            throw new NodeNotFoundException(rowId);
        }
        Node rowNode = nodes.iterator().next();

        System.out.println(userNode.getId() + "&" + rowNode.getName() + "(" + rowNode.getId() + ")");

        //get the accessible children of the row with the given permission
        List<PmAnalyticsEntry> accessibleChildren = analyticsService.getAccessibleChildren(rowNode.getId(), userNode.getId());
        for(PmAnalyticsEntry entry : accessibleChildren) {
            System.out.println(entry.getTarget().getName() + ": " + entry.getOperations());
        }

        //for each column in the table, get the children, if a child is in the result of 1 add the column to a list
        HashSet<Node> columns = nodeService.getNodes(table, "Columns", NodeType.OA.toString(), null);
        if(nodes.size() != 1) {
            throw new NodeNotFoundException(table);
        }
        Node columnsNode = columns.iterator().next();

        //get the children of the columns node.  These are the columns of the table
        columns = nodeService.getChildrenOfType(columnsNode.getId(), NodeType.OA.toString());

        List<String> accesibleColumns = new ArrayList<>();
        for(Node column : columns) {
            HashSet<Node> children = nodeService.getChildrenOfType(column.getId(), NodeType.O.toString());
            for (PmAnalyticsEntry entry : accessibleChildren) {
                Node target = entry.getTarget();
                if (children.contains(target) && entry.getOperations().contains(FILE_WRITE)) {
                    accesibleColumns.add(column.getName());
                }
            }
        }

        return new ApiResponse(accesibleColumns).toResponse();
    }

    @Path("/permissions")
    @GET
    public Response checkPermissions(@QueryParam("username") String username, @QueryParam("property") String property, @QueryParam("value") String value, @QueryParam("permission") String requiredPermission) throws PmException, SQLException, IOException, ClassNotFoundException {
        HashSet<Node> nodes = nodeService.getNodes(null, username, NodeType.U.toString(), null);
        if(nodes.size() != 1) {
            throw new NodeNotFoundException(username);
        }
        Node userNode = nodes.iterator().next();

        //get the node(s) with the property (property=value)
        nodes = nodeService.getNodes(null, null, null, property, value);

        for(Node node : nodes) {
            PmAnalyticsEntry userAccessOn = analyticsService.getUserPermissionsOn(node.getId(), userNode.getId());
            HashSet<String> operations = userAccessOn.getOperations();
            if(!operations.contains(requiredPermission)) {
                return new ApiResponse(false).toResponse();
            }
        }

        return new ApiResponse(true).toResponse();
    }

    @Path("nodes")
    @GET
    public Response getNodes(@QueryParam("namespace") String namespace,
                             @QueryParam("name") String name,
                             @QueryParam("type") String type,
                             @QueryParam("key") String key,
                             @QueryParam("value") String value) throws InvalidNodeTypeException, InvalidPropertyException, ClassNotFoundException, SQLException, IOException, DatabaseException {
        return new ApiResponse(nodeService.getNodes(namespace, name, type, key, value)).toResponse();
    }

    @Path("nodes/{baseId}")
    @POST
    public Response createNode(@PathParam("baseId") long baseId, CreateNodeRequest request) throws NullNameException, NodeNameExistsInNamespaceException, NullTypeException, InvalidPropertyException, DatabaseException, InvalidNodeTypeException, NodeNameExistsException, ConfigurationException, NodeIdExistsException, NodeNotFoundException, AssignmentExistsException, InvalidKeySpecException, NoSuchAlgorithmException, InvalidAssignmentException, IOException, ClassNotFoundException, SQLException, UnexpectedNumberOfNodesException, AssociationExistsException, NoBaseIdException {
        Node node = nodeService.createNode(baseId, request.getId(), request.getName(), request.getType(), request.getProperties());

        return new ApiResponse("success").toResponse();
    }

    @Path("nodes/{id}/children")
    @GET
    public Response getChildren(@PathParam("id") long id) throws NodeNotFoundException, InvalidNodeTypeException, ClassNotFoundException, SQLException, DatabaseException, IOException, InvalidPropertyException {
        return new ApiResponse(nodeService.getChildrenOfType(id, null)).toResponse();
    }

    @Path("assignments")
    @POST
    public Response createAssignment(AssignmentRequest request) throws NodeNotFoundException,
            AssignmentExistsException, DatabaseException, InvalidAssignmentException, SQLException, IOException, ClassNotFoundException, InvalidPropertyException, UnexpectedNumberOfNodesException, AssociationExistsException, InvalidNodeTypeException {
        assignmentService.createAssignment(request.getChildId(), request.getParentId());
        return new ApiResponse("success").toResponse();
    }

    @Path("assignments")
    @DELETE
    public Response deleteAssignment(@QueryParam("childId") long childId,
                                     @QueryParam("parentId") long parentId) throws NodeNotFoundException, AssignmentDoesNotExistException, ConfigurationException, DatabaseException, NoSubjectParameterException, MissingPermissionException, InvalidProhibitionSubjectTypeException, SQLException, IOException, ClassNotFoundException, InvalidPropertyException {
        assignmentService.deleteAssignment(childId, parentId);
        return new ApiResponse(ApiResponse.DELETE_ASSIGNMENT_SUCCESS).toResponse();
    }

    @Path("nodes/{nodeId}")
    @DELETE
    public Response deleteNode(@PathParam("nodeId") long id)
            throws NodeNotFoundException, DatabaseException, SQLException, IOException, ClassNotFoundException, InvalidPropertyException {
        nodeService.deleteNode(id);
        return new ApiResponse(ApiResponse.DELETE_NODE_SUCCESS).toResponse();
    }

    @Path("/analytics/{var1:target}/users/{username}/permissions")
    @GET
    public Response getUserPermissionsOn(@PathParam("var1") PathSegment targetPs,
                                         @PathParam("username") String username) throws SQLException, IOException, UnexpectedNumberOfNodesException, ClassNotFoundException, InvalidPropertyException, DatabaseException, InvalidNodeTypeException, NodeNotFoundException, ConfigurationException, InvalidProhibitionSubjectTypeException, NoSubjectParameterException {
        //get the target node from matrix params
        MultivaluedMap<String, String> targetParams = targetPs.getMatrixParameters();

        String name = targetParams.getFirst("name");
        String type = targetParams.getFirst("type");
        String properties = targetParams.getFirst("properties");

        Node targetNode = nodeService.getNode(
                (!name.equalsIgnoreCase("null") && !name.isEmpty() ? name : null),
                (!type.equalsIgnoreCase("null") && !type.isEmpty() ? type : null),
                (!properties.equalsIgnoreCase("null") && !properties.isEmpty() ? properties : null));

        //get the user node
        Node userNode = nodeService.getNode(username, NodeType.U.toString(), null);

        return new ApiResponse(analyticsService.getUserPermissionsOn(targetNode.getId(), userNode.getId()).getOperations()).toResponse();
    }

    @Path("grant")
    @POST
    public Response grant(EGrantRequest request) throws NodeNotFoundException, ClassNotFoundException, IOException, SQLException, DatabaseException, InvalidNodeTypeException, InvalidPropertyException, UnexpectedNumberOfNodesException, NoSubjectParameterException, ConfigurationException, InvalidProhibitionSubjectTypeException, MissingPermissionException {
        String senderName = request.getSenderId();
        String recipientName = request.getRecipientName();
        long attachmentId = request.getAttachmentId();

        Node senderNode = nodeService.getNode(senderName, NodeType.U.toString(), null);

        //get the inbox of recipient
        Node inboxNode = nodeService.getNode(null, NodeType.OA.toString(), "inbox=" + recipientName);

        //get outbox of sender
        Node outboxNode = nodeService.getNode(null, NodeType.OA.toString(), "outbox=" + senderNode.getName());

        //assign attachment to inbox and outbox
        analyticsService.checkPermissions(senderNode, -1, inboxNode.getId(), ASSIGN_OBJECT_ATTRIBUTE);
        try {
            assignmentService.createAssignment(attachmentId, inboxNode.getId());
            assignmentService.createAssignment(attachmentId, outboxNode.getId());
        }
        catch (AssignmentExistsException | InvalidAssignmentException | AssociationExistsException e) {
            e.printStackTrace();
        }

        return new ApiResponse("Success").toResponse();
    }
}
