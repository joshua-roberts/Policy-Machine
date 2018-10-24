package gov.nist.csd.pm.pep.resources;

import gov.nist.csd.pm.model.exceptions.*;
import gov.nist.csd.pm.model.graph.nodes.Node;
import gov.nist.csd.pm.model.graph.nodes.NodeType;
import gov.nist.csd.pm.model.graph.relationships.NGACAssignment;
import gov.nist.csd.pm.pdp.PDP;
import gov.nist.csd.pm.pep.requests.AssignmentRequest;
import gov.nist.csd.pm.pep.response.ApiResponse;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/assignments")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AssignmentResource {

    @POST
    public Response createAssignment(AssignmentRequest request,
                                     @QueryParam("session") String session,
                                     @QueryParam("process") long process) throws PMException {
        PDP pdp = new PDP(session, process);

        //check parameters
        long childID = request.getChildID();
        if(pdp.exists(childID)) {
            throw new NodeNotFoundException(childID);
        }
        long parentID = request.getParentID();
        if(pdp.exists(parentID)) {
            throw new NodeNotFoundException(parentID);
        }

        NodeType childType = NodeType.toNodeType(request.getChildType());
        NodeType parentType = NodeType.toNodeType(request.getParentType());

        //check if the assignment is valid
        NGACAssignment.checkAssignment(childType, parentType);

        //create node contexts for the child node and the parent node
        Node childCtx = new Node(childID, childType);
        Node parentCtx = new Node(parentID, parentType);

        //assign the child node to the parent
        pdp.assign(childCtx, parentCtx);

        return ApiResponse.Builder
                .success()
                .message(ApiResponse.CREATE_ASSIGNMENT_SUCCESS)
                .build();
    }

    @DELETE
    public Response deleteAssignment(@QueryParam("childID") long childID,
                                     @QueryParam("parentID") long parentID,
                                     @QueryParam("session") String session,
                                     @QueryParam("process") long process) throws PMException {
        PDP pdp = new PDP(session, process);

        //check nodes exist
        if(pdp.exists(childID)) {
            throw new NodeNotFoundException(childID);
        }
        if(pdp.exists(parentID)) {
            throw new NodeNotFoundException(parentID);
        }
        
        Node childNode = pdp.getNode(childID);
        Node parentNode = pdp.getNode(parentID);

        //create node contexts for the child node and the parent node
        Node childCtx = new Node(childID, childNode.getType());
        Node parentCtx = new Node(parentID, parentNode.getType());

        //deassign the node from the parent
        pdp.assign(childCtx, parentCtx);

        return ApiResponse.Builder
                .success()
                .message(ApiResponse.DELETE_ASSIGNMENT_SUCCESS)
                .build();
    }
}
