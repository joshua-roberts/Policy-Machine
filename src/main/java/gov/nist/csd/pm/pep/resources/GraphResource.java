package gov.nist.csd.pm.pep.resources;

import gov.nist.csd.pm.exceptions.PMException;
import gov.nist.csd.pm.graph.model.nodes.Node;
import gov.nist.csd.pm.graph.model.nodes.NodeType;
import gov.nist.csd.pm.pdp.services.GraphService;
import gov.nist.csd.pm.pdp.services.SessionsService;
import gov.nist.csd.pm.pep.requests.CreateNodeRequest;
import gov.nist.csd.pm.pep.response.ApiResponse;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static gov.nist.csd.pm.graph.model.nodes.NodeType.UA;

@Path("/graph")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class GraphResource {

    SessionsService sessionsService = new SessionsService();

    @Path("/nodes")
    @GET
    public Response getNodes(@Context UriInfo uriInfo,
                             @QueryParam("session") String session,
                             @QueryParam("process") long process) throws PMException {
        MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();
        Map<String, String> properties = new HashMap<>();
        for (String key : queryParameters.keySet()) {
            if (key.equalsIgnoreCase("name") ||
                    key.equalsIgnoreCase("type") ||
                    key.equalsIgnoreCase("session") ||
                    key.equalsIgnoreCase("process")) {
                continue;
            }

            String value = queryParameters.getFirst(key);
            properties.put(key, value);
        }

        GraphService graphService = new GraphService(sessionsService.getSessionUserID(session), process);
        Set<Node> nodes = graphService.search(queryParameters.getFirst("name"), queryParameters.getFirst("type"), properties);

        return ApiResponse.Builder
                .success()
                .entity(nodes)
                .build();
    }

    @Path("/nodes")
    @POST
    public Response createNode(CreateNodeRequest request,
                               @QueryParam("session") String session,
                               @QueryParam("process") long process) throws PMException {
        GraphService graphService = new GraphService(sessionsService.getSessionUserID(session), process);

        //get the request parameters
        long parentID = request.getParentID();
        String name = request.getName();
        NodeType type = NodeType.toNodeType(request.getType());
        Map<String, String> properties = request.getProperties();

        if (name == null) {
            throw new IllegalArgumentException("the node name cannot be null");
        }else if (type == null) {
            throw new IllegalArgumentException("the node type cannot be null");
        } else if(parentID == 0 && !type.equals(NodeType.PC)) {
            throw new IllegalArgumentException("need to provide a parentID to create the new node in");
        }

        //send the request to the pdp
        Node node = graphService.createNode(parentID, name, type, properties);

        return ApiResponse.Builder
                .success()
                .entity(node)
                .build();
    }

    @Path("/{nodeID}")
    @GET
    public Response getNode(@PathParam("nodeID") long nodeID,
                            @QueryParam("session") String session,
                            @QueryParam("process") long process) throws PMException {
        GraphService graphService = new GraphService(sessionsService.getSessionUserID(session), process);
        Node node = graphService.getNode(nodeID);
        return ApiResponse.Builder
                .success()
                .entity(node)
                .build();
    }

    @Path("/{nodeID}")
    @PUT
    public Response updateNode(@PathParam("nodeID") long nodeID,
                               CreateNodeRequest request,
                               @QueryParam("session") String session,
                               @QueryParam("process") long process) throws PMException {
        GraphService graphService = new GraphService(sessionsService.getSessionUserID(session), process);
        graphService.updateNode(nodeID, request.getName(), request.getProperties());
        return ApiResponse.Builder
                .success()
                .message(ApiResponse.UPDATE_NODE_SUCCESS)
                .build();
    }

    @Path("/{nodeID}")
    @DELETE
    public Response deleteNode(@PathParam("nodeID") long id,
                               @QueryParam("session") String session,
                               @QueryParam("process") long process) throws PMException {
        GraphService graphService = new GraphService(sessionsService.getSessionUserID(session), process);
        graphService.deleteNode(id);
        return ApiResponse.Builder
                .success()
                .message(ApiResponse.DELETE_NODE_SUCCESS)
                .build();
    }

    @Path("/{nodeID}/children")
    @GET
    public Response getNodeChildren(@PathParam("nodeID") long id,
                                    @QueryParam("type") String type,
                                    @QueryParam("session") String session,
                                    @QueryParam("process") long process) throws PMException {
        GraphService graphService = new GraphService(sessionsService.getSessionUserID(session), process);
        Set<Node> children = graphService.getChildren(id);
        return ApiResponse.Builder
                .success()
                .entity(children)
                .build();
    }

    @Path("/{nodeID}/parents")
    @GET
    public Response getNodeParents(@PathParam("nodeID") long id,
                                   @QueryParam("type") String type,
                                   @QueryParam("session") String session,
                                   @QueryParam("process") long process) throws PMException {
        GraphService graphService = new GraphService(sessionsService.getSessionUserID(session), process);
        Set<Node> parents = graphService.getParents(id);
        return ApiResponse.Builder
                .success()
                .entity(parents)
                .build();
    }

    @Path("/{childID}/assignments/{parentID}")
    @POST
    public Response createAssignment(@PathParam("childID") long childID,
                                     @PathParam("parentID") long parentID,
                                     @QueryParam("session") String session,
                                     @QueryParam("process") long process) throws PMException {
        GraphService graphService = new GraphService(sessionsService.getSessionUserID(session), process);

        //tell pdp to assign the child node to the parent
        graphService.assign(childID, parentID);

        return ApiResponse.Builder
                .success()
                .message(ApiResponse.CREATE_ASSIGNMENT_SUCCESS)
                .build();
    }

    @Path("/{childID}/assignments/{parentID}")
    @DELETE
    public Response deleteAssignment(@PathParam("childID") long childID,
                                     @PathParam("parentID") long parentID,
                                     @QueryParam("session") String session,
                                     @QueryParam("process") long process) throws PMException {
        GraphService graphService = new GraphService(sessionsService.getSessionUserID(session), process);

        //delete assignment
        graphService.deassign(childID, parentID);

        return ApiResponse.Builder
                .success()
                .message(ApiResponse.DELETE_ASSIGNMENT_SUCCESS)
                .build();
    }

    @Path("/{nodeID}/associations")
    @GET
    public Response getAssociations(@PathParam("nodeID") long nodeID,
                                    @QueryParam("type") String type,
                                    @QueryParam("session") String session,
                                    @QueryParam("process") long process) throws PMException {
        GraphService graphService = new GraphService(sessionsService.getSessionUserID(session), process);

        HashMap<Long, Set<String>> associations = new HashMap<>();
        // if the type is source, return the source associations
        // else return the target associations
        if (type.equals("source")) {
            associations.putAll(graphService.getSourceAssociations(nodeID));
        } else if (type.equals("target")) {
            associations.putAll(graphService.getTargetAssociations(nodeID));
        }

        return ApiResponse.Builder
                .success()
                .entity(associations)
                .build();
    }

    @Path("/{uaID}/associations/{targetID}")
    @POST
    public Response createAssociation(@PathParam("uaID") long uaID,
                                      @PathParam("targetID") long targetID,
                                      Set<String> operations,
                                      @QueryParam("session") String session,
                                      @QueryParam("process") long process) throws PMException {
        GraphService graphService = new GraphService(sessionsService.getSessionUserID(session), process);
        graphService.associate(uaID, targetID, operations);
        return ApiResponse.Builder
                .success()
                .message(ApiResponse.CREATE_ASSOCIATION_SUCCESS)
                .build();
    }


    @Path("/{uaID}/associations/{targetID}")
    @PUT
    public Response updateAssociation(@PathParam("uaID") long uaID,
                                      @PathParam("targetID") long targetID,
                                      Set<String> operations,
                                      @QueryParam("session") String session,
                                      @QueryParam("process") long process) throws PMException {
        GraphService graphService = new GraphService(sessionsService.getSessionUserID(session), process);
        graphService.associate(uaID, targetID, operations);
        return ApiResponse.Builder
                .success()
                .message(ApiResponse.UPDATE_ASSOCIATION_SUCCESS)
                .build();
    }

    @Path("/{uaID}/associations/{targetID}")
    @DELETE
    public Response deleteAssociation(@PathParam("uaID") long uaID,
                                      @PathParam("targetID") long targetID,
                                      @QueryParam("session") String session,
                                      @QueryParam("process") long process) throws PMException {
        GraphService graphService = new GraphService(sessionsService.getSessionUserID(session), process);
        graphService.dissociate(uaID, targetID);
        return ApiResponse.Builder
                .success()
                .message(ApiResponse.UPDATE_ASSOCIATION_SUCCESS)
                .build();
    }
}
