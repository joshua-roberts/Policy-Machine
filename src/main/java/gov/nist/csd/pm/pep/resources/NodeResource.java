package gov.nist.csd.pm.pep.resources;

import gov.nist.csd.pm.model.exceptions.*;
import gov.nist.csd.pm.model.graph.Node;
import gov.nist.csd.pm.pdp.services.NodeService;
import gov.nist.csd.pm.pep.requests.CreateNodeRequest;
import gov.nist.csd.pm.pep.response.ApiResponse;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;

@Path("/nodes")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class NodeResource {

    private NodeService nodeService = new NodeService();

    @GET
    public Response getNodes(@Context UriInfo uriInfo,
                             @QueryParam("session") String session,
                             @QueryParam("process")
                                     long process) throws SQLException, SessionDoesNotExistException, IOException, ClassNotFoundException, InvalidPropertyException, SessionUserNotFoundException, DatabaseException, InvalidNodeTypeException {

        MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();
        HashMap<String, String> properties = new HashMap<>();
        for (String key : queryParameters.keySet()) {
            if (key.equalsIgnoreCase("name") || key.equalsIgnoreCase("type")) {
                continue;
            }

            String value = queryParameters.getFirst(key);
            properties.put(key, value);
        }

        HashSet<Node> nodes = nodeService.getNodes(queryParameters.getFirst("name"), queryParameters.getFirst("type"), properties, session, process);

        return new ApiResponse(nodes).toResponse();
    }

    @Path("/policies")
    @POST
    public Response createPolicy(CreateNodeRequest request,
                                 @QueryParam("session") String session,
                                 @QueryParam("process")
                                         long process) throws InvalidAssignmentException, UnexpectedNumberOfNodesException, ConfigurationException, InvalidNodeTypeException, SessionDoesNotExistException, ClassNotFoundException, AssociationExistsException, DatabaseException, NullNameException, NullTypeException, NodeNameExistsException, NodeIDExistsException, PropertyNotFoundException, SQLException, InvalidPropertyException, InvalidAssociationException, SessionUserNotFoundException, NodeNotFoundException, AssignmentExistsException, IOException {
        Node node = nodeService.createPolicy(request.getName(), request.getProperties(), session, process);

        return new ApiResponse(node).toResponse();
    }

    @Path("/{nodeID}")
    @GET
    public Response getNode(@PathParam("nodeID") long nodeID,
                            @QueryParam("content") boolean content,
                            @QueryParam("session") String session,
                            @QueryParam("process")
                                    long process) throws NodeNotFoundException, SessionUserNotFoundException, NoSubjectParameterException, MissingPermissionException, InvalidProhibitionSubjectTypeException, ConfigurationException, SessionDoesNotExistException, ClassNotFoundException, SQLException, IOException, DatabaseException, PropertyNotFoundException, InvalidPropertyException, NullNameException, InvalidEvrException, InvalidEntityException, ProhibitionResourceExistsException, ProhibitionDoesNotExistException, InvalidNodeTypeException, ProhibitionNameExistsException {
        Node node = nodeService.getNode(nodeID, session, process);
        return new ApiResponse(node).toResponse();
    }

    @Path("/{nodeID}")
    @DELETE
    public Response deleteNode(@PathParam("nodeID") long id,
                               @QueryParam("session") String session,
                               @QueryParam("process") long process)
            throws NodeNotFoundException, DatabaseException, ConfigurationException,
            SessionUserNotFoundException, NoSubjectParameterException,
            MissingPermissionException, InvalidProhibitionSubjectTypeException, SessionDoesNotExistException, SQLException, IOException, ClassNotFoundException, InvalidPropertyException {
        nodeService.deleteNode(id, session, process);
        return new ApiResponse(ApiResponse.DELETE_NODE_SUCCESS).toResponse();
    }

    @Path("{nodeID}/children")
    @POST
    public Response createNodeIn(@PathParam("nodeID") long nodeID,
                                 CreateNodeRequest request,
                                 @QueryParam("content") boolean content,
                                 @QueryParam("session") String session,
                                 @QueryParam("process") long process)
            throws NullNameException, NullTypeException,
            InvalidPropertyException, DatabaseException, InvalidNodeTypeException,
            NodeNameExistsException, ConfigurationException, NodeIDExistsException,
            NodeNotFoundException, InvalidAssignmentException, SessionDoesNotExistException, SessionUserNotFoundException, NoSubjectParameterException, MissingPermissionException, InvalidProhibitionSubjectTypeException, AssignmentExistsException, IOException, ClassNotFoundException, SQLException, UnexpectedNumberOfNodesException, AssociationExistsException, PropertyNotFoundException, InvalidKeySpecException, NoSuchAlgorithmException, InvalidAssociationException {
        Node node = nodeService.createNodeIn(nodeID, request.getName(), request.getType(), request.getProperties(), session, process);
        return new ApiResponse(node).toResponse();
    }


    @Path("{nodeID}/children")
    @GET
    public Response getNodeChildren(@PathParam("nodeID") long id,
                                    @QueryParam("type") String type,
                                    @QueryParam("session") String session,
                                    @QueryParam("process") long process)
            throws NodeNotFoundException, SessionUserNotFoundException, NoUserParameterException, ConfigurationException, SessionDoesNotExistException, ClassNotFoundException, SQLException, IOException, DatabaseException, InvalidPropertyException, InvalidNodeTypeException {
        return new ApiResponse(nodeService.getNodeChildren(id, type, session, process)).toResponse();
    }

    @Path("/{nodeID}/children")
    @DELETE
    public Response deleteNodeChildren(@PathParam("nodeID") long id,
                                       @QueryParam("type") String type,
                                       @QueryParam("session") String session,
                                       @QueryParam("process") long process)
            throws InvalidNodeTypeException, NodeNotFoundException, DatabaseException,
            ConfigurationException, SessionUserNotFoundException, NoSubjectParameterException,
            InvalidProhibitionSubjectTypeException, MissingPermissionException, SessionDoesNotExistException, SQLException, IOException, ClassNotFoundException, InvalidPropertyException {
        nodeService.deleteNodeChildren(id, type, session, process);
        return new ApiResponse(ApiResponse.DELETE_NODE_CHILDREN_SUCESS).toResponse();
    }

    @Path("/{nodeID}/parents")
    @GET
    public Response getNodeParents(@PathParam("nodeID") long id,
                                   @QueryParam("type") String type,
                                   @QueryParam("session") String session,
                                   @QueryParam("process") long process)
            throws InvalidNodeTypeException, NodeNotFoundException, SessionUserNotFoundException, NoSubjectParameterException, InvalidProhibitionSubjectTypeException, MissingPermissionException, ConfigurationException, SessionDoesNotExistException, ClassNotFoundException, SQLException, DatabaseException, IOException, InvalidPropertyException {
        return new ApiResponse(nodeService.getParentsOfType(id, type, session, process)).toResponse();
    }
}
