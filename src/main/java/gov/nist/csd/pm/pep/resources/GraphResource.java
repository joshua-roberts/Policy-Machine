package gov.nist.csd.pm.pep.resources;

import gov.nist.csd.pm.common.exceptions.*;
import gov.nist.csd.pm.common.model.graph.nodes.Node;
import gov.nist.csd.pm.common.model.graph.nodes.NodeType;
import gov.nist.csd.pm.pdp.services.GraphService;
import gov.nist.csd.pm.pep.requests.CreateNodeRequest;
import gov.nist.csd.pm.pep.requests.UpdateNodeRequest;
import gov.nist.csd.pm.pep.response.ApiResponse;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@Path("/graph")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class GraphResource {

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

        GraphService graphService = new GraphService(session, process);
        HashSet<Node> nodes = graphService.search(queryParameters.getFirst("name"), queryParameters.getFirst("type"), properties);

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
        GraphService graphService = new GraphService(session, process);

        //get the request parameters
        long parentID = request.getParentID();
        String name = request.getName();
        NodeType type = NodeType.toNodeType(request.getType());
        HashMap<String, String> properties = request.getProperties();

        if (name == null) {
            throw new IllegalArgumentException("the node name cannot be null");
        }else if (type == null) {
            throw new IllegalArgumentException("the node type cannot be null");
        } else if(parentID == 0 && !type.equals(NodeType.PC)) {
            throw new IllegalArgumentException("need to provide a parentID to create the new node in");
        }

        //send the node to be created to the pdp
        Node newNode = graphService.createNode(parentID, new Node(name, type, properties));

        return ApiResponse.Builder
                .success()
                .entity(newNode)
                .build();
    }

    @Path("/{nodeID}")
    @GET
    public Response getNode(@PathParam("nodeID") long nodeID,
                            @QueryParam("session") String session,
                            @QueryParam("process") long process) throws PMException {
        GraphService graphService = new GraphService(session, process);
        Node node = graphService.getNode(nodeID);
        return ApiResponse.Builder
                .success()
                .entity(node)
                .build();
    }

    @Path("/{nodeID}")
    @PUT
    public Response updateNode(@PathParam("nodeID") long nodeID,
                               UpdateNodeRequest request,
                               @QueryParam("session") String session,
                               @QueryParam("process") long process) throws PMException {
        GraphService graphService = new GraphService(session, process);
        graphService.updateNode(new Node().id(nodeID).name(request.getName()).properties(request.getProperties()));
        return ApiResponse.Builder
                .success()
                .entity(ApiResponse.UPDATE_NODE_SUCCESS)
                .build();
    }

    @Path("/{nodeID}")
    @DELETE
    public Response deleteNode(@PathParam("nodeID") long id,
                               @QueryParam("session") String session,
                               @QueryParam("process") long process) throws PMException {
        GraphService graphService = new GraphService(session, process);
        graphService.deleteNode(id);
        return ApiResponse.Builder
                .success()
                .build();
    }

    @Path("/{nodeID}/children")
    @GET
    public Response getNodeChildren(@PathParam("nodeID") long id,
                                    @QueryParam("type") String type,
                                    @QueryParam("session") String session,
                                    @QueryParam("process") long process) throws PMException {
        GraphService graphService = new GraphService(session, process);
        HashSet<Node> children = graphService.getChildren(id);
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
        GraphService graphService = new GraphService(session, process);
        HashSet<Node> parents = graphService.getParents(id);
        return ApiResponse.Builder
                .success()
                .entity(parents)
                .build();
    }

    @Path("/{var1:child}/assignments/{var2:parent}")
    @POST
    public Response createAssignment(@PathParam("var1") PathSegment childPs,
                                     @PathParam("var2") PathSegment parentPs,
                                     @QueryParam("session") String session,
                                     @QueryParam("process") long process) throws PMException {
        GraphService graphService = new GraphService(session, process);

        //get the assignment parameters from the url
        MultivaluedMap<String, String> childParams = childPs.getMatrixParameters();
        long childID = Long.valueOf(childParams.getFirst("id"));
        NodeType childType = NodeType.toNodeType(childParams.getFirst("type"));
        MultivaluedMap<String, String> parentParams = parentPs.getMatrixParameters();
        long parentID = Long.valueOf(parentParams.getFirst("id"));
        NodeType parentType = NodeType.toNodeType(parentParams.getFirst("type"));

        //if either of the types are null throw an exception
        if(childType == null || parentType == null) {
            throw new PMException(Errors.ERR_NULL_TYPE, "the types of the child and parent node are required when creating an assignment");
        }

        //tell pdp to assign the child node to the parent
        graphService.assign(childID, childType, parentID, parentType);

        return ApiResponse.Builder
                .success()
                .message(ApiResponse.CREATE_ASSIGNMENT_SUCCESS)
                .build();
    }

    @Path("/{var1:child}/assignments/{var2:parent}")
    @DELETE
    public Response deleteAssignment(@PathParam("var1") PathSegment childPs,
                                     @PathParam("var2") PathSegment parentPs,
                                     @QueryParam("session") String session,
                                     @QueryParam("process") long process) throws PMException {
        GraphService graphService = new GraphService(session, process);

        //get the assignment parameters from the url
        MultivaluedMap<String, String> childParams = childPs.getMatrixParameters();
        long childID = Long.valueOf(childParams.getFirst("id"));
        NodeType childType = NodeType.toNodeType(childParams.getFirst("type"));
        MultivaluedMap<String, String> parentParams = parentPs.getMatrixParameters();
        long parentID = Long.valueOf(parentParams.getFirst("id"));
        NodeType parentType = NodeType.toNodeType(parentParams.getFirst("type"));

        //if either of the types are null throw an exception
        if(childType == null || parentType == null) {
            throw new PMException(Errors.ERR_NULL_TYPE, "the types of the child and parent node are required when deleting an assignment");
        }

        //delete assignment
        graphService.deassign(childID, childType, parentID, parentType);

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
        GraphService graphService = new GraphService(session, process);

        HashMap<Long, HashSet<String>> associations = new HashMap<>();
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

    @Path("/{uaID}/associations/{var1:target}")
    @POST
    public Response createAssociation(@PathParam("uaID") long uaID,
                                      @PathParam("var1") PathSegment targetPs,
                                      HashSet<String> operations,
                                      @QueryParam("session") String session,
                                      @QueryParam("process") long process) throws PMException {
        GraphService graphService = new GraphService(session, process);

        //get the target parameters from the url
        MultivaluedMap<String, String> parentParams = targetPs.getMatrixParameters();
        long targetID = Long.valueOf(parentParams.getFirst("id"));
        NodeType targetType = NodeType.toNodeType(parentParams.getFirst("type"));

        graphService.associate(uaID, targetID, targetType, operations);
        return ApiResponse.Builder
                .success()
                .message(ApiResponse.CREATE_ASSOCIATION_SUCCESS)
                .build();
    }


    @Path("/{uaID}/associations/{var1:target}")
    @PUT
    public Response updateAssociation(@PathParam("uaID") long uaID,
                                      @PathParam("var1") PathSegment targetPs,
                                      HashSet<String> operations,
                                      @QueryParam("session") String session,
                                      @QueryParam("process") long process) throws PMException {
        GraphService graphService = new GraphService(session, process);

        //get the target parameters from the url
        MultivaluedMap<String, String> parentParams = targetPs.getMatrixParameters();
        long targetID = Long.valueOf(parentParams.getFirst("id"));
        NodeType targetType = NodeType.toNodeType(parentParams.getFirst("type"));

        graphService.associate(uaID, targetID, targetType, operations);
        return ApiResponse.Builder
                .success()
                .message(ApiResponse.UPDATE_ASSOCIATION_SUCCESS)
                .build();
    }

    @Path("/{uaID}/associations/{var1:target}")
    @DELETE
    public Response deleteAssociation(@PathParam("uaID") long uaID,
                                      @PathParam("var1") PathSegment targetPs,
                                      @QueryParam("session") String session,
                                      @QueryParam("process") long process) throws PMException {
        GraphService graphService = new GraphService(session, process);

        //get the target parameters from the url
        MultivaluedMap<String, String> parentParams = targetPs.getMatrixParameters();
        long targetID = Long.valueOf(parentParams.getFirst("id"));
        NodeType targetType = NodeType.toNodeType(parentParams.getFirst("type"));

        graphService.dissociate(uaID, targetID, targetType);
        return ApiResponse.Builder
                .success()
                .message(ApiResponse.UPDATE_ASSOCIATION_SUCCESS)
                .build();
    }
}
