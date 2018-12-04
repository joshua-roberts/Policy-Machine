package gov.nist.csd.pm.demos.ndac.pmhealth.resources;

import gov.nist.csd.pm.demos.ndac.pmhealth.dao.DAO;
import gov.nist.csd.pm.demos.ndac.pmhealth.requests.TestRequest;
import gov.nist.csd.pm.pep.response.ApiResponse;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.SQLException;

@Path("/demos/pmhealth/test")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TestResource {
    private DAO dao;
    public TestResource() throws PropertyVetoException, SQLException, ClassNotFoundException {
        dao = DAO.getDao();
    }

    @POST
    public Response getLinks(TestRequest request) throws SQLException, IOException {
        return ApiResponse.Builder.success().entity(dao.executeTest(request.getSql())).build();
    }
}
