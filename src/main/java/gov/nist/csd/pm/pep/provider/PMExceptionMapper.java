package gov.nist.csd.pm.pep.provider;

import gov.nist.csd.pm.common.exceptions.*;
import gov.nist.csd.pm.exceptions.PMException;
import gov.nist.csd.pm.pep.response.ApiResponse;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class PMExceptionMapper implements ExceptionMapper<PMException> {
    @Override
    public Response toResponse(PMException e) {
        e.printStackTrace();

        Errors err = Errors.toException(e);

        return ApiResponse.Builder
                .error(err, e)
                .build();
    }
}
