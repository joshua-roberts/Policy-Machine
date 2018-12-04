package gov.nist.csd.pm.demos.ndac.pmhealth.resources;

import gov.nist.csd.pm.demos.ndac.pmhealth.dao.DAO;
import gov.nist.csd.pm.pep.response.ApiResponse;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.SQLException;

@Path("/demos/pmhealth/patients")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PatientsResource {

    private DAO dao;
    public PatientsResource() throws PropertyVetoException, SQLException, ClassNotFoundException {
        dao = DAO.getDao();
    }

    @Path("/{username}")
    @GET
    public Response getUserPatientId(@PathParam("username") String username) throws IOException, SQLException {
        return ApiResponse.Builder.success().entity(dao.getPatient(username)).build();
    }
}
