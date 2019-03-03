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

        Errors err;
        if(e instanceof PMAuthenticationException) {
            err = Errors.ERR_AUTHENTICATION;
        } else if(e instanceof PMAuthorizationException) {
            err = Errors.ERR_AUTHORIZATION;
        } else if(e instanceof PMDBException) {
            err = Errors.ERR_DB;
        } else if(e instanceof PMProhibitionException) {
            err = Errors.ERR_PROHIBITION;
        } else if(e instanceof PMGraphException) {
            err = Errors.ERR_GRAPH;
        } else if(e instanceof PMConfigurationException) {
            err = Errors.ERR_CONFIG;
        } else {
            err = Errors.ERR_PM;
        }

        return ApiResponse.Builder
                .error(err, e)
                .build();
    }
}
