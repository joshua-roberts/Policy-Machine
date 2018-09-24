package gov.nist.csd.pm.pep.resources;

import gov.nist.csd.pm.model.exceptions.InvalidEntityException;
import gov.nist.csd.pm.model.exceptions.*;
import gov.nist.csd.pm.pdp.services.TranslatorService;
import gov.nist.csd.pm.pep.requests.TranslateRequest;
import gov.nist.csd.pm.pep.response.ApiResponse;
import gov.nist.csd.pm.pdp.translator.exceptions.PolicyMachineException;
import net.sf.jsqlparser.JSQLParserException;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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
    public Response translate(TranslateRequest request) throws ClassNotFoundException, SQLException, JSQLParserException, IOException, PmException, PolicyMachineException, InvalidEntityException {
        return new ApiResponse(translatorService.translate(request.getSql(), request.getUsername(), request.getProcess(),
                request.getHost(), request.getPort(), request.getDbUsername(),
                request.getDbPassword(), request.getDatabase())).toResponse();
    }
}
