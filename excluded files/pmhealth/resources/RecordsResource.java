package gov.nist.csd.pm.demos.ndac.pmhealth.resources;

import gov.nist.csd.pm.demos.ndac.pmhealth.dao.DAO;
import gov.nist.csd.pm.demos.ndac.pmhealth.model.Record;
import gov.nist.csd.pm.demos.ndac.pmhealth.model.Visit;
import gov.nist.csd.pm.pep.response.ApiResponse;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.SQLException;

@Path("/demos/pmhealth/records")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RecordsResource {
    private DAO dao;
    public RecordsResource() throws PropertyVetoException, SQLException, ClassNotFoundException {
        dao = DAO.getDao();
    }

    @GET
    public Response getRecords(@QueryParam("user") String user) throws SQLException, IOException {
        return ApiResponse.Builder.success().entity(dao.getRecords(user)).build();
    }

    @Path("/{patientId}")
    @GET
    public Response getRecord(@PathParam("patientId") int patientId, @QueryParam("user") String user) throws SQLException, IOException {
        return ApiResponse.Builder.success().entity(dao.getRecord(patientId, user)).build();
    }

    @Path("/{patientId}")
    @PUT
    public Response updateRecord(@PathParam("patientId") int patientId, Record data, @QueryParam("user") String user) throws SQLException, IOException {
        dao.updateRecord(patientId, data, user);
        return ApiResponse.Builder.success().entity("success").build();
    }

    /**
     * Get the vitals that were most recently entered for the patient
     * @param patientId the ID of the patient
     * @return the Vitals
     * @throws SQLException
     */
    @Path("/{patientId}/vitals")
    @GET
    public Response getLastVitals(@PathParam("patientId") int patientId, @QueryParam("user") String user) throws SQLException, IOException {
        return ApiResponse.Builder.success().entity(dao.getLastVitals(patientId, user)).build();
    }

    @Path("/{patientId}/visits")
    @GET
    public Response getVisits(@PathParam("patientId") int patientId, @QueryParam("user") String user) throws SQLException, IOException {
        return ApiResponse.Builder.success().entity(dao.getVisits(patientId, user)).build();
    }

    @Path("/{patientId}/visits")
    @POST
    public Response createVisit(@PathParam("patientId") int patientId, @QueryParam("user") String user) throws SQLException {
        return ApiResponse.Builder.success().entity(dao.createVisit(patientId, user)).build();
    }

    @Path("/{patientId}/visits/{visitId}")
    @PUT
    public Response updateVisit(@PathParam("patientId") int patientId, @PathParam("visitId") int visitId, Visit data, @QueryParam("user") String user) throws SQLException, IOException {
        return ApiResponse.Builder.success().entity(dao.updateVisit(patientId, visitId, data, user)).build();
    }

    @Path("/{patientId}/medicines")
    @GET
    public Response getMedicines(@PathParam("patientId") int patientId, @QueryParam("user") String user) throws SQLException {
        return ApiResponse.Builder.success().entity(dao.getMedicines(patientId, user)).build();
    }

    @Path("/{patientId}/user")
    @GET
    public Response getPatientUsername(@PathParam("patientId") long patientId) throws IOException, SQLException {
        return ApiResponse.Builder.success().entity(dao.getPatientUsername(patientId)).build();
    }

    @Path("/visits/{visitId}/notes")
    @GET
    public Response getVisitNotes(@PathParam("visitId") int visitId) throws IOException, SQLException {
        return ApiResponse.Builder.success().entity(dao.getVisitNotesId(visitId)).build();
    }
}
