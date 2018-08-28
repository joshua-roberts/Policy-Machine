package gov.nist.csd.pm.pep.resources;

import gov.nist.csd.pm.model.exceptions.*;
import gov.nist.csd.pm.pep.requests.AssignmentRequest;
import gov.nist.csd.pm.pep.response.ApiResponse;
import gov.nist.csd.pm.pep.services.AssignmentService;

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

    @GET
    public Response isAssigned(@QueryParam("childId") long childId,
                               @QueryParam("parentId") long parentId,
                               @QueryParam("session") String session,
                               @QueryParam("process") long process) throws NodeNotFoundException, ClassNotFoundException, SQLException, IOException, DatabaseException, InvalidPropertyException {
        return new ApiResponse(assignmentService.isAssigned(childId, parentId)).toResponse();
    }

    @POST
    public Response createAssignment(AssignmentRequest request,
                                     @QueryParam("session") String session,
                                     @QueryParam("process") long process) throws InvalidPropertyException, AssignmentExistsException, DatabaseException, SessionDoesNotExistException, NodeNotFoundException, ClassNotFoundException, NoSubjectParameterException, MissingPermissionException, InvalidAssignmentException, ConfigurationException, UnexpectedNumberOfNodesException, AssociationExistsException, SQLException, IOException, SessionUserNotFoundException, InvalidNodeTypeException, InvalidProhibitionSubjectTypeException, PropertyNotFoundException {
        assignmentService.createAssignment(session, process, request.getChildId(), request.getParentId(), true);

        return new ApiResponse(ApiResponse.CREATE_ASSIGNMENT_SUCCESS).toResponse();
    }

    @DELETE
    public Response deleteAssignment(@QueryParam("childId") long childId,
                                     @QueryParam("parentId") long parentId,
                                     @QueryParam("session") String session,
                                     @QueryParam("process") long process) throws IOException, ConfigurationException, SessionDoesNotExistException, SessionUserNotFoundException, SQLException, MissingPermissionException, DatabaseException, NoSubjectParameterException, InvalidProhibitionSubjectTypeException, InvalidPropertyException, ClassNotFoundException, AssignmentDoesNotExistException, NodeNotFoundException {
        assignmentService.deleteAssignment(session, process, childId, parentId);

        return new ApiResponse(ApiResponse.DELETE_ASSIGNMENT_SUCCESS).toResponse();
    }
}
