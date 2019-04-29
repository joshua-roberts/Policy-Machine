package gov.nist.csd.pm.pep.response;

import gov.nist.csd.pm.common.exceptions.Errors;
import gov.nist.csd.pm.exceptions.PMException;

import javax.ws.rs.core.Response;

import java.io.Serializable;

public class ApiResponse {
    private int code;
    private String  message;
    private Object  entity;

    public static final String DELETE_ASSIGNMENT_SUCCESS      = "assignment deleted";
    public static final String CREATE_ASSIGNMENT_SUCCESS      = "assignment created";
    public static final String CREATE_ASSOCIATION_SUCCESS     = "association created";
    public static final String UPDATE_ASSOCIATION_SUCCESS     = "association updated";
    public static final String DELETE_NODE_SUCCESS            = "node deleted";
    public static final String UPDATE_NODE_SUCCESS               = "node updated";
    public static final String CREATE_PROHIBITION_SUCCESS       = "prohibition created";
    public static final String UPDATE_PROHIBITION_SUCCESS       = "prohibition updated";
    public static final String DELETE_PROHIBITION_SUCCESS       = "prohibition deleted";
    public static final String DELETE_SESSION_SUCCESS     = "Session was deleted";

    private static final String SUCCESS_MSG = "Success";

    private static final int SUCCESS_CODE = 9000;

    public static class Builder implements Serializable {
        private int code;
        private String  message;
        private Object  entity;

        /**
         * Create a new builder with the given response code
         * @param code The code to give this builder.
         */
        public Builder(int code) {
            this.code = code;
        }

        /**
         * This method returns a Builder that has the success response code and message.
         * @return a Builder with success response code and default message.
         */
        public static Builder success() {
            Builder builder = new Builder(SUCCESS_CODE);
            builder.message = SUCCESS_MSG;
            return builder;
        }

        /**
         * This method returns a Builder that has the given error response code and message.
         * @param e The exception that represents the error.  The exception's error code and message will be
         *          added to the Builder.
         * @return a Builder with an error response code and default message.
         */
        public static Builder error(Errors err, PMException e) {
            Builder res = new Builder(err.getCode());
            res.message = err.getMessage();
            res.entity = e.getMessage();
            return res;
        }

        /**
         * Initialize a new Builder with an error code and message.
         * @param code The error code.
         * @param message The error message.
         * @return a new Builder instance.
         */
        public static Builder error(int code, String message) {
            return new Builder(code).message(message);
        }

        /**
         * Builder method to set the message of a Builder
         * @param message the message to give this Builder
         * @return the current Builder object with the given message
         */
        public Builder message(String message) {
            this.message = message;
            return this;
        }

        /**
         * Builder method to set the code of a Builder
         * @param code the code to give this Builder
         * @return the current Builder object with the given code
         */
        public Builder code(int code) {
            this.code = code;
            return this;
        }

        /**
         * Builder method to set the entity of this Builder to the given Object
         * @param o The Object to set as this Builder's entity
         * @return the current Builder with the given Object as the entity
         */
        public Builder entity(Object o) {
            this.entity = o;
            return this;
        }

        /**
         * Build a new JAX-RS response from the current builder.
         * @return the JAX-RS response constructed from the builder.
         */
        public Response build() {
            ApiResponse response = new ApiResponse();
            response.setCode(this.code);
            response.setMessage(this.message);
            response.setEntity(this.entity);

            // convert the ApiResponse to a JAX-RS response object
            // The JAX-RS response code will always be 200, The ApiResponse will have an error code if there is one.
            return Response.ok()
                    .entity(this)
                    .build();
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Object getEntity() {
            return entity;
        }

        public void setEntity(Object entity) {
            this.entity = entity;
        }
    }

    private ApiResponse() {}

    public int getCode() {
        return code;
    }

    private void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    private void setMessage(String message) {
        this.message = message;
    }

    public Object getEntity() {
        return entity;
    }

    private void setEntity(Object entity) {
        this.entity = entity;
    }
}
