package gov.nist.policyserver.obligations;


import gov.nist.policyserver.obligations.exceptions.InvalidEntityException;
import gov.nist.policyserver.obligations.exceptions.InvalidEvrException;
import gov.nist.policyserver.exceptions.ConfigurationException;
import gov.nist.policyserver.exceptions.DatabaseException;
import gov.nist.policyserver.exceptions.InvalidPropertyException;
import gov.nist.policyserver.obligations.model.script.EvrScript;
import gov.nist.policyserver.response.ApiResponse;
import org.xml.sax.SAXException;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Path("/obligations")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class EvrResource {
    private EvrService evrService = new EvrService();

    public EvrResource() {
    }

    @POST
    public Response createEvr(EvrRequest request) throws IOException, SAXException, InvalidEvrException, InvalidEntityException, SQLException, DatabaseException, ParserConfigurationException, ConfigurationException, InvalidPropertyException, ClassNotFoundException {
        evrService.evr(request.getSource());
        return new ApiResponse("success").toResponse();
    }

    @GET
    public Response getObligations() throws ClassNotFoundException, SQLException, IOException, DatabaseException, InvalidPropertyException {
        List<EvrScript> scripts = evrService.getDaoManager().getObligationsDAO().getEvrManager().getScripts();
        List<EvrScript> retScripts = new ArrayList<>();
        for(EvrScript script : scripts) {
            retScripts.add(new EvrScript(script.getScriptName(), script.isEnabled()));
        }

        return new ApiResponse(retScripts).toResponse();
    }

    @DELETE
    public Response deleteObligations() throws ClassNotFoundException, SQLException, IOException, DatabaseException, InvalidPropertyException {
        evrService.deleteObligations();

        return new ApiResponse("Success").toResponse();
    }

    @Path("/{obligation}/enabled")
    @PUT
    public Response updateObligationEnabled(@PathParam("obligation") String obligation) throws ClassNotFoundException, SQLException, InvalidPropertyException, IOException, DatabaseException {
        evrService.updateScript(obligation);

        return new ApiResponse("Success").toResponse();
    }

}
