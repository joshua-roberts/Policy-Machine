package gov.nist.csd.pm.pep.resources;

import gov.nist.csd.pm.model.exceptions.*;
import gov.nist.csd.pm.pep.requests.AssignmentRequest;
import gov.nist.csd.pm.pep.response.ApiResponse;
import gov.nist.csd.pm.pdp.services.AssignmentService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.io.IOException;
import java.sql.SQLException;

@Path("/assignments")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AssignmentResource {

    private AssignmentService assignmentService = new AssignmentService();

    @POST
    public Response createAssignment(AssignmentRequest request,
                                     @QueryParam("session") String session,
                                     @QueryParam("process") long process) throws InvalidPropertyException, AssignmentExistsException, DatabaseException, SessionDoesNotExistException, NodeNotFoundException, ClassNotFoundException, NoSubjectParameterException, MissingPermissionException, InvalidAssignmentException, ConfigurationException, UnexpectedNumberOfNodesException, AssociationExistsException, SQLException, IOException, SessionUserNotFoundException, InvalidNodeTypeException, InvalidProhibitionSubjectTypeException, PropertyNotFoundException, InvalidAssociationException {
        assignmentService.createAssignment(request.getChildID(), request.getParentID(), session, process);

        return new ApiResponse(ApiResponse.CREATE_ASSIGNMENT_SUCCESS).toResponse();
    }

    @DELETE
    public Response deleteAssignment(@QueryParam("childID") long childID,
                                     @QueryParam("parentID") long parentID,
                                     @QueryParam("session") String session,
                                     @QueryParam("process") long process) throws IOException, ConfigurationException, SessionDoesNotExistException, SessionUserNotFoundException, SQLException, MissingPermissionException, DatabaseException, NoSubjectParameterException, InvalidProhibitionSubjectTypeException, InvalidPropertyException, ClassNotFoundException, AssignmentDoesNotExistException, NodeNotFoundException {
        assignmentService.deleteAssignment(session, process, childID, parentID);

        return new ApiResponse(ApiResponse.DELETE_ASSIGNMENT_SUCCESS).toResponse();
    }
}
