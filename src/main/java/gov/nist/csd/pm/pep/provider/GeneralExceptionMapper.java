package gov.nist.csd.pm.pep.provider;

import gov.nist.csd.pm.pep.response.ApiResponse;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class GeneralExceptionMapper implements ExceptionMapper<Exception> {
    @Override
    public Response toResponse(Exception e) {
        e.printStackTrace();
        return new ApiResponse(e).toResponse();
    }
}
