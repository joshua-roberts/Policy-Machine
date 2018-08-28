package gov.nist.csd.pm.pep.resources;

import gov.nist.csd.pm.model.exceptions.*;
import gov.nist.csd.pm.model.graph.Node;
import gov.nist.csd.pm.model.exceptions.InvalidEntityException;
import gov.nist.csd.pm.model.exceptions.InvalidEvrException;
import gov.nist.csd.pm.pep.requests.CreateNodeRequest;
import gov.nist.csd.pm.pep.response.ApiResponse;
import gov.nist.csd.pm.pep.services.NodeService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.util.HashSet;

@Path("/nodes")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class NodeResource {

    private NodeService      nodeService      = new NodeService();

    @GET
    public Response getNodes(@QueryParam("namespace") String namespace,
                             @QueryParam("name") String name,
                             @QueryParam("type") String type,
                             @QueryParam("key") String key,
                             @QueryParam("value") String value,
                             @QueryParam("session") String session,
                             @QueryParam("process") long process)
            throws InvalidNodeTypeException, InvalidPropertyException,
            SessionUserNotFoundException, SessionDoesNotExistException, ClassNotFoundException, SQLException, IOException, DatabaseException {

        HashSet<Node> nodes = nodeService.getNodes(namespace, name, type, key, value, session, process);

        return new ApiResponse(nodes).toResponse();
    }

    @Path("/{var1:target}")
    @GET
    public Response getNode(@PathParam("var1") PathSegment targetPs,
                                       @QueryParam("session") String session,
                                       @QueryParam("process") long process) throws InvalidNodeTypeException, InvalidPropertyException, UnexpectedNumberOfNodesException, NodeNotFoundException, ConfigurationException, InvalidProhibitionSubjectTypeException, NoSubjectParameterException, ClassNotFoundException, SQLException, DatabaseException, IOException, SessionDoesNotExistException, SessionUserNotFoundException, MissingPermissionException {
        //get the target node from matrix params
        MultivaluedMap<String, String> targetParams = targetPs.getMatrixParameters();
        Node targetNode = nodeService.getNode(
                targetParams.getFirst("name"),
                targetParams.getFirst("type"),
                targetParams.getFirst("properties"),
                session,
                process);

        return new ApiResponse(targetNode).toResponse();
    }

    @POST
    public Response createNode(CreateNodeRequest request,
                               @QueryParam("session") String session,
                               @QueryParam("process") long process)
            throws NullNameException, NullTypeException,
            InvalidPropertyException, DatabaseException, InvalidNodeTypeException,
            NodeNameExistsException, ConfigurationException, NodeIdExistsException,
            NodeNotFoundException, InvalidAssignmentException, AssignmentExistsException,
            IOException, ClassNotFoundException, SQLException,
            UnexpectedNumberOfNodesException, AssociationExistsException,
            SessionDoesNotExistException, SessionUserNotFoundException, PropertyNotFoundException {
        Node node = nodeService.createNode(request.getName(), request.getType(), request.getProperties(), session, process);

        return new ApiResponse(node).toResponse();
    }

    @Path("/{nodeId}")
    @GET
    public Response getNode(@PathParam("nodeId") long nodeId,
                            @QueryParam("content") boolean content,
                            @QueryParam("session") String session,
                            @QueryParam("process") long process) throws NodeNotFoundException, SessionUserNotFoundException, NoSubjectParameterException, MissingPermissionException, InvalidProhibitionSubjectTypeException, ConfigurationException, SessionDoesNotExistException, ClassNotFoundException, SQLException, IOException, DatabaseException, PropertyNotFoundException, InvalidPropertyException, NullNameException, InvalidEvrException, InvalidEntityException, ProhibitionResourceExistsException, ProhibitionDoesNotExistException, InvalidNodeTypeException, ProhibitionNameExistsException {
        Node node = nodeService.getNode(nodeId, content, session, process);
        return new ApiResponse(node).toResponse();
    }
    


    @Path("/{nodeId}")
    @PUT
    public Response updateNode(@PathParam("nodeId") long nodeId,
                               CreateNodeRequest request,
                               @QueryParam("content") boolean content,
                               @QueryParam("session") String session,
                               @QueryParam("process") long process)
            throws NodeNotFoundException, DatabaseException, ConfigurationException,
            SessionUserNotFoundException, NoSubjectParameterException, MissingPermissionException,
            InvalidProhibitionSubjectTypeException, InvalidPropertyException, PropertyNotFoundException, SessionDoesNotExistException, SQLException, IOException, ClassNotFoundException, InvalidKeySpecException, NoSuchAlgorithmException {
        Node node = nodeService.updateNode(nodeId, request.getName(), request.getProperties(), request.getContent(), session, process);
        return new ApiResponse(node).toResponse();
    }

    @Path("/{nodeId}")
    @DELETE
    public Response deleteNode(@PathParam("nodeId") long id,
                               @QueryParam("session") String session,
                               @QueryParam("process") long process)
            throws NodeNotFoundException, DatabaseException, ConfigurationException,
            SessionUserNotFoundException, NoSubjectParameterException,
            MissingPermissionException, InvalidProhibitionSubjectTypeException, SessionDoesNotExistException, SQLException, IOException, ClassNotFoundException, InvalidPropertyException {
        nodeService.deleteNode(id, session, process);
        return new ApiResponse(ApiResponse.DELETE_NODE_SUCCESS).toResponse();
    }

    @Path("/{nodeId}/properties/{key}")
    @DELETE
    public Response deleteNodeProperty(@PathParam("nodeId") long id,
                                       @PathParam("key") String key,
                                       @QueryParam("session") String session,
                                       @QueryParam("process") long process)
            throws DatabaseException, NodeNotFoundException, PropertyNotFoundException, ConfigurationException, NoSubjectParameterException, MissingPermissionException, InvalidProhibitionSubjectTypeException, SessionUserNotFoundException, SessionDoesNotExistException, SQLException, IOException, ClassNotFoundException, InvalidPropertyException {
        nodeService.deleteNodeProperty(id, key, session, process);
        return new ApiResponse(ApiResponse.DELETE_NODE_PROPERTY_SUCCESS).toResponse();
    }

    @Path("{nodeId}/children")
    @POST
    public Response createNodeIn(@PathParam("nodeId") long nodeId,
                                 CreateNodeRequest request,
                                 @QueryParam("content") boolean content,
                                 @QueryParam("session") String session,
                                 @QueryParam("process") long process)
            throws NullNameException, NullTypeException,
            InvalidPropertyException, DatabaseException, InvalidNodeTypeException,
            NodeNameExistsException, ConfigurationException, NodeIdExistsException,
            NodeNotFoundException, InvalidAssignmentException, SessionDoesNotExistException, SessionUserNotFoundException, NoSubjectParameterException, MissingPermissionException, InvalidProhibitionSubjectTypeException, AssignmentExistsException, IOException, ClassNotFoundException, SQLException, UnexpectedNumberOfNodesException, AssociationExistsException, PropertyNotFoundException, InvalidKeySpecException, NoSuchAlgorithmException {
        Node node = nodeService.createNodeIn(nodeId,request.getName(), request.getType(), request.getProperties(), request.getContent(), session, process);
        return new ApiResponse(node).toResponse();
    }



    @Path("{nodeId}/children")
    @GET
    public Response getNodeChildren(@PathParam("nodeId") long id,
                                    @QueryParam("type") String type,
                                    @QueryParam("session") String session,
                                    @QueryParam("process") long process)
            throws NodeNotFoundException, SessionUserNotFoundException, NoUserParameterException, ConfigurationException, SessionDoesNotExistException, ClassNotFoundException, SQLException, IOException, DatabaseException, InvalidPropertyException, InvalidNodeTypeException {
        return new ApiResponse(nodeService.getNodeChildren(id, type, session, process)).toResponse();
    }

    @Path("/{nodeId}/children")
    @DELETE
    public Response deleteNodeChildren(@PathParam("nodeId") long id,
                                       @QueryParam("type") String type,
                                       @QueryParam("session") String session,
                                       @QueryParam("process") long process)
            throws InvalidNodeTypeException, NodeNotFoundException, DatabaseException,
            ConfigurationException, SessionUserNotFoundException, NoSubjectParameterException,
            InvalidProhibitionSubjectTypeException, MissingPermissionException, SessionDoesNotExistException, SQLException, IOException, ClassNotFoundException, InvalidPropertyException {
        nodeService.deleteNodeChildren(id, type, session, process);
        return new ApiResponse(ApiResponse.DELETE_NODE_CHILDREN_SUCESS).toResponse();
    }

    @Path("/{nodeId}/parents")
    @GET
    public Response getNodeParents(@PathParam("nodeId") long id,
                                   @QueryParam("type") String type,
                                   @QueryParam("session") String session,
                                   @QueryParam("process") long process)
            throws InvalidNodeTypeException, NodeNotFoundException, SessionUserNotFoundException, NoSubjectParameterException, InvalidProhibitionSubjectTypeException, MissingPermissionException, ConfigurationException, SessionDoesNotExistException, ClassNotFoundException, SQLException, DatabaseException, IOException, InvalidPropertyException {
        return new ApiResponse(nodeService.getParentsOfType(id, type, session, process)).toResponse();
    }
}
