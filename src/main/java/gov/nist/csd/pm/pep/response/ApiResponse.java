package gov.nist.csd.pm.pep.response;

import gov.nist.csd.pm.model.exceptions.PMException;
import gov.nist.csd.pm.model.graph.nodes.Node;
import gov.nist.csd.pm.model.prohibitions.Prohibition;
import gov.nist.csd.pm.pdp.services.ConfigurationService;

import javax.ws.rs.core.Response;

import java.util.Collection;

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
    public static final String DELETE_NODE_SUCCESS            = "OldNode successfully deleted";
    public static final String PUT_NODE_SUCCESS               = "OldNode was successfully updated";
    public static final String CREATE_PROHIBITION_SUCCESS       = "The Prohibition was successfully created";
    public static final String DELETE_PROHIBITION_SUCCESS       = "Prohibition was deleted successfully";
    public static final String ADD_PROHIBITION_RESOURCE_SUCCESS = "Resource was added to the prohibitions";
    public static final String REMOVE_PROHIBITION_RESOURCE_SUCCESS  = "The resource was successfully removed from the prohibitions";
    public static final String POST_PROHIBITION_SUBJECT_SUCCESS     = "The subject was successfully set for the prohibitions";
    public static final String ADD_PROHIBITION_OPS_SUCCESS          = "The operations were successfully added to the prohibitions";
    public static final String REMOVE_PROHIBITION_OP_SUCCESS        = "The operation was successfully removed from the prohibitions";
    public static final String DELETE_NODE_IN_NAMESPACE_SUCCESS     = "The node was successfully deleted";
    public static final String DELETE_SESSION_SUCCESS     = "Session was deleted";

    private static final String SUCCESS_MSG = "Success";
    private static final String ERROR_MSG = "Error";

    private static final int SUCCESS_CODE = 9000;

    public static class Builder {
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
            Builder res = new Builder(e.getErrorCode());
            res.message = e.getMessage();
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
         * Builder method to set the entity of this Builder to a Collection
         * @param c the collection to store in the Builder's entity field.
         * @return The current Builder with the given Collection as the entity
         */
        public Builder entity(Collection c) {
            this.entity = c;
            return this;
        }

        /**
         * Builder method to set the entity of this Builder to a PmException
         * @param e PmException to set as this Builder's entity
         * @return The current Builder with the given PMException as the entity
         */
        public Builder entity(PMException e) {
            this.entity = e;
            return this;
        }

        /**
         * Builder method to set the entity of this Builder to true or false
         * @param b The boolean value to set as this Builder's entity
         * @return The current Builder with the given boolean as the entity
         */
        public Builder entity(boolean b) {
            this.entity = b;
            return this;
        }

        /**
         * Builder method to set the entity of this Builder to the given int value
         * @param i The int value to set as this Builder's entity
         * @return The current Builder with the given int as the entity
         */
        public Builder entity(int i) {
            this.entity = i;
            return this;
        }

        /**
         * Builder method to set the entity of this Builder to the given String
         * @param s The String value to set as this Builder's entity
         * @return The current Builder with the given String as the entity
         */
        public Builder entity(String s) {
            this.entity = s;
            return this;
        }

        /**
         * Builder method to set the entity of this Builder to the given Table object
         * This method is only used in the ConfigurationService
         * @param t The Table object to set as this Builder's entity
         * @return The current Builder with the given Table as the entity
         */
        public Builder entity(ConfigurationService.Table t) {
            this.entity = t;
            return this;
        }

        /**
         * Builder method to set the entity of this Builder to the given JsonNode.
         * This method is only used in the ConfigurationService
         * @param jn The JsonNode object to set as this Builder's entity
         * @return The current Builder with the given JsonNode as the entity
         */
        public Builder entity(ConfigurationService.JsonNode jn) {
            this.entity = jn;
            return this;
        }

        /**
         * Builder method to set the entity of this Builder to the given OldNode
         * @param n The OldNode object to set as this Builder's entity
         * @return The current Builder with the given OldNode as the entity
         */
        public Builder entity(Node n) {
            this.entity = n;
            return this;
        }

        /**
         * Builder method to set the entity of this Builder to the given Prohibition
         * @param p The Prohibition object to set as this Builder's entity
         * @return The current Builder with the given Prohibition as the entity
         */
        public Builder entity(Prohibition p) {
            this.entity = p;
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
