package gov.nist.csd.pm.common.exceptions;

/**
 * Every error has an error code and general message
 */
public enum Errors {
    ERR_ASSIGNMENT_DOES_NOT_EXIST(6001, "assignment does not exist"),
    ERR_PROHIBITION_NAME_EXISTS(6002, "prohibition name already exists"),
    ERR_PROHIBITION_RESOURCE_EXISTS(6003, "node is already assigned to the prohibition"),
    ERR_NO_ID(6004, "an ID was expected but none was provided"),
    ERR_ASSIGNMENT_EXISTS(6005, "assignment already exists"),
    ERR_INVALID_PROPERTY(6006, "not a valid property"),
    ERR_INVALID_PROHIBITION_SUBJECT_TYPE(6007, "invalid prohibition subject type"),
    ERR_NODE_NOT_FOUND(6008, "node not found"),
    ERR_NO_USER_PARAMETER(6009, "no user provided"),
    ERR_ASSOCIATION_DOES_NOT_EXIST(6010, "association does not exist"),
    ERR_NULL_NAME(6011, "null name"),
    ERR_PROHIBITION_RESOURCE_DOES_NOT_EXIST(6012, "node in prohibition does not exist"),
    ERR_NO_SUBJECT_PARAMETER(6013, "no subject was provided"),
    ERR_CONFIGURATION(6014, "configuration error"),
    ERR_PROPERTY_NOT_FOUND(6015, "property not found"),
    ERR_NAME_IN_NAMESPACE_NOT_FOUND(6016, "node name not in namespace"),
    ERR_NODE_NAME_EXISTS(6017, "a node already exists with the provided name"),
    ERR_INVALID_NODETYPE(6018, "invalid node type"),
    ERR_PROHIBITION_SUBJECT_DOES_NOT_EXIST(6019, "the subject of the prohibition does not exist"),
    ERR_NULL_TYPE(6020, "null type"),
    ERR_NODE_NAME_EXISTS_IN_NAMESPACE(6021, "a node already exists with the name in the namesapce"),
    ERR_PROHIBITION_DOES_NOT_EXIST(6022, "prohibition does not exist"),
    ERR_SESSION_DOES_NOT_EXIST(6023, "session does not exist"),
    ERR_ACCESS_DENIED(6024, "access denied"),
    ERR_NODE_ID_EXISTS(6025, "a node with the given ID already exists"),
    ERR_ASSOCIATION_EXISTS(6026, "association already exists"),
    ERR_MISSING_PERMISSIONS(6027, "missing permissions"),
    ERR_SESSION_USER_NOT_FOUND(6028, "session user not found"),
    ERR_UNEXPECTED_NUMBER_OF_NODES(6029, "unexpected number of nodes returned"),
    ERR_INVALID_ASSIGNMENT(6030, "invalid assignment"),
    ERR_NO_BASE_ID(6031, "no base ID"),
    ERR_NO_PROCESS(6032, "no process ID"),
    ERR_INVALID_ASSOCIATION(6034, ""),
    ERR_NODE_EXISTS(6035, "node exists"),
    ERR_POLICY_NAME_EXISTS(6036, "a policy name with the given name already exists"),
    ERR_INVALID_CREDENTIALS(6037, "invalid credentials"),
    ERR_LOADER(6038, "loading exception"),
    ERR_NULL_NODE_CTX(6039, "null node context provided"),
    ERR_NOT_IMPLEMENTED(6041, "not implemented"),
    ERR_LOADING_DB_CONFIG_PROPS(6042, "error loading database configuration"),
    ERR_HASHING_USER_PSWD(6043, "error hashing user password"),
    ERR_NULL_OPERATIONS(6044, "null operations"),
    ERR_AUTH(6045, "authentication error"),
    ERR_NULL_SESSION(6046, "null session ID"),
    ERR_NO_REP(6047, "policy class has no rep node"),
    ERR_DB(7000, "database error");
    private int code;
    private String message;

    Errors(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
