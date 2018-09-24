package gov.nist.csd.pm.pep.provider;

import gov.nist.csd.pm.model.exceptions.PmException;
import gov.nist.csd.pm.pep.response.ApiResponse;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class PmExceptionMapper implements ExceptionMapper<PmException> {
    @Override
    public Response toResponse(PmException e) {
        e.printStackTrace();
        return new ApiResponse(e).toResponse();
    }
}
