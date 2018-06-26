package gov.nist.policyserver.obligations;


import gov.nist.policyserver.obligations.exceptions.InvalidEntityException;
import gov.nist.policyserver.obligations.exceptions.InvalidEvrException;
import gov.nist.policyserver.exceptions.ConfigurationException;
import gov.nist.policyserver.exceptions.DatabaseException;
import gov.nist.policyserver.exceptions.InvalidPropertyException;
import gov.nist.policyserver.response.ApiResponse;
import org.xml.sax.SAXException;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.sql.SQLException;

@Path("/obligations")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class EvrResource {
    private EvrService evrService = new EvrService();

    public EvrResource() throws DatabaseException, IOException, ClassNotFoundException, SQLException {
    }

    @POST
    public Response createEvr(EvrRequest request) throws IOException, SAXException, InvalidEvrException, InvalidEntityException, SQLException, DatabaseException, ParserConfigurationException, ConfigurationException, InvalidPropertyException, ClassNotFoundException {
        evrService.evr(request.getSource());
        return new ApiResponse("success").toResponse();
    }

    /*@Path("/{scriptName}")
    @PUT
    public Response updateEvr(EvrRequest request, @PathParam("scriptName") String scriptName) {
        return new ApiResponse(evrService.update(request.getScriptName(), request.getSource())).toResponse();
    }

    @Path("/{scriptName}")
    @POST
    public Response enableEvr(@PathParam("scriptName") String scriptName) {
        evrService.enableEvr(scriptName);
        return new ApiResponse("Script " + scriptName + " was successfully enabled").toResponse();
    }*/

}
