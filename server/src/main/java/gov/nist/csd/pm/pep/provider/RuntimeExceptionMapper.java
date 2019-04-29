package gov.nist.csd.pm.pep.provider;

import gov.nist.csd.pm.pep.response.ApiResponse;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class RuntimeExceptionMapper implements ExceptionMapper<RuntimeException> {
    @Override
    public Response toResponse(RuntimeException e) {
        e.printStackTrace();
        return ApiResponse.Builder
                .error(e.hashCode(), e.getMessage())
                .build();
    }
}

