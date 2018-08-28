package gov.nist.csd.pm.pep.provider;

import gov.nist.csd.pm.pep.response.ApiResponse;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class WebApplicationExceptionMapper implements ExceptionMapper<WebApplicationException> {
    @Override
    public Response toResponse(WebApplicationException e) {
        e.printStackTrace();
        return new ApiResponse(e.getResponse().getStatus(), e.getMessage()).toResponse();
    }
}
