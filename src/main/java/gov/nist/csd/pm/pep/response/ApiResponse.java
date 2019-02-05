package gov.nist.csd.pm.pep.response;

import gov.nist.csd.pm.common.exceptions.PMException;

import javax.ws.rs.core.Response;

import java.io.Serializable;

public class ApiResponse {
    private int code;
    private String  message;
    private Object  entity;

    public static final String DELETE_ASSIGNMENT_SUCCESS      = "Assignment was successfully deleted";
    public static final String POST_NODE_PROPERTY_SUCCESS     = "The property was successfully added to the node";
    public static final String DELETE_NODE_PROPERTY_SUCCESS   = "The property was successfully deleted";
    public static final String DELETE_NODE_CHILDREN_SUCESS    = "The children of the node were all deleted";
    public static final String DELETE_ASSOCIATION_ASSOCIATION = "Successfully deleted Association";
    public static final String CREATE_ASSIGNMENT_SUCCESS      = "Assignment was successfully created";
    public static final String CREATE_ASSOCIATION_SUCCESS     = "Association was successfully created";
    public static final String UPDATE_ASSOCIATION_SUCCESS     = "Successfully updated the association";
    public static final String DELETE_NODE_SUCCESS            = "Node successfully deleted";
    public static final String UPDATE_NODE_SUCCESS               = "Node was successfully updated";
    public static final String CREATE_PROHIBITION_SUCCESS       = "The Prohibition was successfully created";
    public static final String UPDATE_PROHIBITION_SUCCESS       = "The Prohibition was successfully updated";
    public static final String DELETE_PROHIBITION_SUCCESS       = "Prohibition was deleted successfully";
    public static final String ADD_PROHIBITION_RESOURCE_SUCCESS = "Resource was added to the prohibitions";
    public static final String REMOVE_PROHIBITION_RESOURCE_SUCCESS  = "The resource was successfully removed from the prohibitions";
    public static final String POST_PROHIBITION_SUBJECT_SUCCESS     = "The subject was successfully set for the prohibitions";
    public static final String ADD_PROHIBITION_OPS_SUCCESS          = "The operations were successfully added to the prohibitions";
    public static final String REMOVE_PROHIBITION_OP_SUCCESS        = "The operation was successfully removed from the prohibitions";
    public static final String DELETE_NODE_IN_NAMESPACE_SUCCESS     = "The node was successfully deleted";
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
         * @return A Builder with success response code and default message.
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
         * @return A Builder with an error response code and default message.
         */
        public static Builder error(PMException e) {
            Builder res = new Builder(e.getError().getCode());
            res.message = e.getError().getMessage();
            res.entity = e.getDetailedMessage();
            return res;
        }

        /**
         * 
         * @param code
         * @param message
         * @return
         */
        public static Builder error(int code, String message) {
            return new Builder(code).message(message);
        }

        /**
         * Builder method to set the message of a Builder
         * @param message the message to give this Builder
         * @return The current Builder object with the given message
         */
        public Builder message(String message) {
            this.message = message;
            return this;
        }

        /**
         * Builder method to set the code of a Builder
         * @param code the code to give this Builder
         * @return The current Builder object with the given code
         */
        public Builder code(int code) {
            this.code = code;
            return this;
        }

        /**
         * Builder method to set the entity of this Builder to the given Object
         * @param o The Object to set as this Builder's entity
         * @return The current Builder with the given Object as the entity
         */
        public Builder entity(Object o) {
            this.entity = o;
            return this;
        }

        /**
         * Build a new JAX-RS response from the current builder.
         * @return The JAX-RS response constructed from the builder.
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
