package gov.nist.policyserver.resources;

import gov.nist.policyserver.exceptions.*;
import gov.nist.policyserver.analytics.PmAnalyticsEntry;
import gov.nist.policyserver.model.graph.nodes.Node;
import gov.nist.policyserver.requests.CreateNodeRequest;
import gov.nist.policyserver.response.ApiResponse;
import gov.nist.policyserver.service.AnalyticsService;
import gov.nist.policyserver.service.NodeService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;

import static gov.nist.policyserver.common.Constants.*;

@Path("/nodes")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class NodeResource {

    private NodeService      nodeService      = new NodeService();
    private AnalyticsService analyticsService = new AnalyticsService();

    @GET
    public Response getNodes(@QueryParam("namespace") String namespace,
                             @QueryParam("name") String name,
                             @QueryParam("type") String type,
                             @QueryParam("key") String key,
                             @QueryParam("value") String value,
                             @QueryParam("session") String session,
                             @QueryParam("process") long process)
            throws InvalidNodeTypeException, InvalidPropertyException,
            SessionUserNotFoundException, ConfigurationException, SessionDoesNotExistException, ClassNotFoundException, SQLException, IOException, DatabaseException {
        //PERMISSION CHECK
        //get user from username
        Node user = analyticsService.getSessionUser(session);

        //get the nodes that are accessible to the user
        List<PmAnalyticsEntry> accessibleNodes = analyticsService.getAccessibleNodes(user);
        HashSet<Node> nodes = new HashSet<>();
        for(PmAnalyticsEntry entry : accessibleNodes) {
            nodes.add(entry.getTarget());
        }

        return new ApiResponse(nodeService.getNodes(nodes, namespace, name, type, key, value)).toResponse();
    }

    @POST
    public Response createNode(CreateNodeRequest request,
                               @QueryParam("session") String session,
                               @QueryParam("process") long process)
            throws NullNameException, NodeNameExistsInNamespaceException, NullTypeException,
            InvalidPropertyException, DatabaseException, InvalidNodeTypeException,
            NodeNameExistsException, ConfigurationException, NodeIdExistsException,
            NodeNotFoundException, InvalidAssignmentException, AssignmentExistsException, IOException, ClassNotFoundException, SQLException {

        return new ApiResponse(nodeService.createNode(0, request.getId(), request.getName(), request.getType(), request.getProperties())).toResponse();
    }

    @Path("/{nodeId}")
    @GET
    public Response getNode(@PathParam("nodeId") long id,
                            @QueryParam("session") String session,
                            @QueryParam("process") long process) throws NodeNotFoundException, SessionUserNotFoundException, NoSubjectParameterException, MissingPermissionException, InvalidProhibitionSubjectTypeException, ConfigurationException, SessionDoesNotExistException, ClassNotFoundException, SQLException, IOException, DatabaseException {
        //PERMISSION CHECK
        //get user from username
        Node user = analyticsService.getSessionUser(session);

        //check user can analytics the node
        analyticsService.checkPermissions(user, process, id, ANY_OPERATIONS);

        return new ApiResponse(nodeService.getNode(id)).toResponse();
    }

    @Path("/{nodeId}")
    @PUT
    public Response updateNode(@PathParam("nodeId") long id,
                               CreateNodeRequest request,
                               @QueryParam("session") String session,
                               @QueryParam("process") long process)
            throws NodeNotFoundException, DatabaseException, ConfigurationException,
            SessionUserNotFoundException, NoSubjectParameterException, MissingPermissionException,
            InvalidProhibitionSubjectTypeException, InvalidPropertyException, PropertyNotFoundException, SessionDoesNotExistException, SQLException, IOException, ClassNotFoundException {
        //PERMISSION CHECK
        //get user from username
        Node user = analyticsService.getSessionUser(session);

        //check user can update the node
        analyticsService.checkPermissions(user, process, id, UPDATE_NODE);

        return new ApiResponse(nodeService.updateNode(id, request.getName(), request.getProperties())).toResponse();
    }

    @Path("/{nodeId}")
    @DELETE
    public Response deleteNode(@PathParam("nodeId") long id,
                               @QueryParam("session") String session,
                               @QueryParam("process") long process)
            throws NodeNotFoundException, DatabaseException, ConfigurationException,
            SessionUserNotFoundException, NoSubjectParameterException,
            MissingPermissionException, InvalidProhibitionSubjectTypeException, SessionDoesNotExistException, SQLException, IOException, ClassNotFoundException {
        //PERMISSION CHECK
        //get user from username
        Node user = analyticsService.getSessionUser(session);

        //check user can delete the node
        analyticsService.checkPermissions(user, process, id, DELETE_NODE);

        nodeService.deleteNode(id);
        return new ApiResponse(ApiResponse.DELETE_NODE_SUCCESS).toResponse();
    }

    @Path("/{nodeId}/properties/{key}")
    @DELETE
    public Response deleteNodeProperty(@PathParam("nodeId") long id,
                                       @PathParam("key") String key,
                                       @QueryParam("session") String session,
                                       @QueryParam("process") long process)
            throws DatabaseException, NodeNotFoundException, PropertyNotFoundException, ConfigurationException, NoSubjectParameterException, MissingPermissionException, InvalidProhibitionSubjectTypeException, SessionUserNotFoundException, SessionDoesNotExistException, SQLException, IOException, ClassNotFoundException {
        //PERMISSION CHECK
        //get user from username
        Node user = analyticsService.getSessionUser(session);

        //check user can delete the node
        analyticsService.checkPermissions(user, process, id, UPDATE_NODE);

        nodeService.deleteNodeProperty(id, key);
        return new ApiResponse(ApiResponse.DELETE_NODE_PROPERTY_SUCCESS).toResponse();
    }

    @Path("{nodeId}/children")
    @POST
    public Response createNodeIn(@PathParam("nodeId") long nodeId,
                                 CreateNodeRequest request,
                                 @QueryParam("session") String session,
                                 @QueryParam("process") long process)
            throws NullNameException, NodeNameExistsInNamespaceException, NullTypeException,
            InvalidPropertyException, DatabaseException, InvalidNodeTypeException,
            NodeNameExistsException, ConfigurationException, NodeIdExistsException,
            NodeNotFoundException, InvalidAssignmentException, SessionDoesNotExistException, SessionUserNotFoundException, NoSubjectParameterException, MissingPermissionException, InvalidProhibitionSubjectTypeException, AssignmentExistsException, IOException, ClassNotFoundException, SQLException {
        Node user = analyticsService.getSessionUser(session);

        //permissions check
        analyticsService.checkPermissions(user, process, nodeId, ASSIGN_TO);

        return new ApiResponse(nodeService.createNode(nodeId, request.getId(), request.getName(), request.getType(), request.getProperties())).toResponse();
    }

    @Path("{nodeId}/children")
    @GET
    public Response getNodeChildren(@PathParam("nodeId") long id,
                                    @QueryParam("type") String type,
                                    @QueryParam("session") String session,
                                    @QueryParam("process") long process)
            throws NodeNotFoundException, SessionUserNotFoundException, NoUserParameterException, ConfigurationException, SessionDoesNotExistException, ClassNotFoundException, SQLException, IOException, DatabaseException {
        //PERMISSION CHECK
        //get user from username
        Node user = analyticsService.getSessionUser(session);

        List<PmAnalyticsEntry> accessibleChildren = analyticsService.getAccessibleChildren(id, user.getId());

        HashSet<Node> nodes = new HashSet<>();
        for(PmAnalyticsEntry entry : accessibleChildren) {
            if(type == null || type.equals(entry.getTarget().getType().toString())) {
                nodes.add(entry.getTarget());
            }
        }

        return new ApiResponse(nodes).toResponse();
    }

    @Path("/{nodeId}/children")
    @DELETE
    public Response deleteNodeChildren(@PathParam("nodeId") long id,
                                       @QueryParam("type") String type,
                                       @QueryParam("session") String session,
                                       @QueryParam("process") long process)
            throws InvalidNodeTypeException, NodeNotFoundException, DatabaseException,
            ConfigurationException, SessionUserNotFoundException, NoSubjectParameterException,
            InvalidProhibitionSubjectTypeException, MissingPermissionException, SessionDoesNotExistException, SQLException, IOException, ClassNotFoundException {
        //PERMISSION CHECK
        //get user from username
        Node user = analyticsService.getSessionUser(session);

        HashSet<Node> children = nodeService.getChildrenOfType(id, type);

        for(Node node : children) {
            PmAnalyticsEntry perms = analyticsService.getUserPermissionsOn(node.getId(), user.getId());

            //check the user can delete the node
            if(!perms.getOperations().contains(DELETE_NODE)) {
                throw new MissingPermissionException("Can not delete child of " + id + " with id " + perms.getTarget().getId());
            }
        }

        nodeService.deleteNodeChildren(id, type);
        return new ApiResponse(ApiResponse.DELETE_NODE_CHILDREN_SUCESS).toResponse();
    }

    @Path("/{nodeId}/parents")
    @GET
    public Response getNodeParents(@PathParam("nodeId") long id,
                                   @QueryParam("type") String type,
                                   @QueryParam("session") String session,
                                   @QueryParam("process") long process)
            throws InvalidNodeTypeException, NodeNotFoundException, SessionUserNotFoundException, NoSubjectParameterException, InvalidProhibitionSubjectTypeException, MissingPermissionException, ConfigurationException, SessionDoesNotExistException, ClassNotFoundException, SQLException, DatabaseException, IOException {
        //PERMISSION CHECK
        //get user from username
        Node user = analyticsService.getSessionUser(session);

        HashSet<Node> parents = nodeService.getParentsOfType(id, type);
        for (Node node : parents) {
            PmAnalyticsEntry perms = analyticsService.getUserPermissionsOn(node.getId(), user.getId());
            if(perms.getOperations().isEmpty()) {
                throw new MissingPermissionException("Can not analytics parent of " + id);
            }
        }

        return new ApiResponse(nodeService.getParentsOfType(id, type)).toResponse();
    }
}
