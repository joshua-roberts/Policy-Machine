package gov.nist.csd.pm.demos.ndac.pep;

import gov.nist.csd.pm.demos.ndac.translator.TranslatorService;
import gov.nist.csd.pm.demos.ndac.translator.exceptions.PolicyMachineException;
import gov.nist.csd.pm.model.exceptions.InvalidEntityException;
import gov.nist.csd.pm.model.exceptions.*;
import gov.nist.csd.pm.pep.response.ApiResponse;
import gov.nist.csd.pm.pep.response.TranslateResponse;
import net.sf.jsqlparser.JSQLParserException;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.sql.SQLException;

@Path("translate")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TranslatorResource {
    private TranslatorService translatorService = new TranslatorService();

    public TranslatorResource() {
    }

    @POST
    public Response translate(TranslateRequest request, @QueryParam("session") String session) throws ClassNotFoundException, SQLException, JSQLParserException, IOException, PMException, InvalidEntityException {
        TranslateResponse translation = translatorService.translate(request.getSql(), session, request.getProcess(),
                request.getHost(), request.getPort(), request.getDbUsername(),
                request.getDbPassword(), request.getDatabase());
        return ApiResponse.Builder.success().entity(translation).build();
    }
}
