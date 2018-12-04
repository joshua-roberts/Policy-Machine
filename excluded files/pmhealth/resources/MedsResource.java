package gov.nist.csd.pm.demos.ndac.pmhealth.resources;


import gov.nist.csd.pm.demos.ndac.pmhealth.dao.DAO;
import gov.nist.csd.pm.pep.response.ApiResponse;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.SQLException;

@Path("/demos/pmhealth/medicines")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MedsResource {
    private DAO dao;
    public MedsResource() throws PropertyVetoException, SQLException, ClassNotFoundException {
        dao = DAO.getDao();
    }

    @GET
    public Response getAllMeds(@QueryParam("user") String user) throws SQLException, IOException {
        return ApiResponse.Builder.success().entity(dao.getAllMeds(user)).build();
    }
}
