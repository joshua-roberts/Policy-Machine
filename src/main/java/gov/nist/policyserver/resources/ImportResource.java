package gov.nist.policyserver.resources;

import gov.nist.policyserver.exceptions.*;
import gov.nist.policyserver.requests.ConnectRequest;
import gov.nist.policyserver.requests.ImportEntitiesRequest;
import gov.nist.policyserver.requests.ImportFilesRequest;
import gov.nist.policyserver.response.ApiResponse;
import gov.nist.policyserver.service.ImportService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.sql.SQLException;

import static gov.nist.policyserver.common.Constants.SUCCESS;

@Path("/import")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ImportResource {

    private ImportService importService = new ImportService();

    @Path("files")
    @POST
    public Response importFiles(ImportFilesRequest request) throws InvalidPropertyException, AssignmentExistsException, InvalidNodeTypeException, NodeNotFoundException, ClassNotFoundException, NodeIdExistsException, NodeNameExistsException, NodeNameExistsInNamespaceException, DatabaseException, ConfigurationException, SQLException, NullNameException, IOException, NullTypeException, InvalidAssignmentException, UnexpectedNumberOfNodesException, AssociationExistsException, NoBaseIdException {
        importService.importFiles(request.getFiles(), request.getSource());
        return new ApiResponse("Successfully imported " + request.getFiles().length + " files from " + request
                .getSource()).toResponse();
    }

    @Path("entities")
    @POST
    public Response importEntities(ImportEntitiesRequest request) throws InvalidPropertyException, AssignmentExistsException, InvalidNodeTypeException, NodeNotFoundException, ClassNotFoundException, NodeIdExistsException, NodeNameExistsException, NodeNameExistsInNamespaceException, DatabaseException, ConfigurationException, SQLException, NullNameException, IOException, NullTypeException, InvalidAssignmentException, UnexpectedNumberOfNodesException, AssociationExistsException, NoBaseIdException {
        importService.importEntities(request.getKind(), request.getEntities());

        return null;
    }

    @Path("sql")
    @POST
    public Response importData(@QueryParam("session") String session,
                               @QueryParam("process") long process,
                               ConnectRequest request) throws DatabaseException, ConfigurationException, InvalidNodeTypeException, InvalidPropertyException, AssignmentExistsException, NodeNotFoundException, NameInNamespaceNotFoundException, InvalidAssignmentException, SQLException, IOException, ClassNotFoundException, NodeIdExistsException, NodeNameExistsException, NodeNameExistsInNamespaceException, NullTypeException, NullNameException, UnexpectedNumberOfNodesException, AssociationExistsException, NoBaseIdException {
        String host = request.getHost();
        int port = request.getPort();
        String schema = request.getSchema();
        String username = request.getUsername();
        String password = request.getPassword();

        importService.importSql(host, port, schema, username, password);

        return new ApiResponse(SUCCESS).toResponse();
    }

}
