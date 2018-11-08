package gov.nist.csd.pm.pep.provider;

import gov.nist.csd.pm.model.exceptions.PMException;
import gov.nist.csd.pm.pep.response.ApiResponse;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class PmExceptionMapper implements ExceptionMapper<PMException> {
    @Override
    public Response toResponse(PMException e) {
        e.printStackTrace();
        return ApiResponse.Builder
                .error(e)
                .build();
    }
}
