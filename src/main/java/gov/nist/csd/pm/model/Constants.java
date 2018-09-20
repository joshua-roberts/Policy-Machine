package gov.nist.csd.pm.model;

public class Constants {
    //operations
    public static final String FILE_WRITE = "file write";
    public static final String FILE_READ = "file read";
    public static final String ASSIGN_OBJECT_ATTRIBUTE = "assign object attribute";
    public static final String ASSIGN_OBJECT_ATTRIBUTE_TO = "assign object attribute to";
    public static final String ASSIGN_OBJECT = "assign object";
    public static final String ASSIGN_OBJECT_TO = "assign object to";
    public static final String CREATE_NODE = "create node";
    public static final String DELETE_NODE = "delete node";
    public static final String UPDATE_NODE = "update node";
    public static final String ASSIGN_TO = "assign to";
    public static final String ASSIGN = "assign";
    public static final String CREATE_OBJECT = "create object";
    public static final String CREATE_OBJECT_ATTRIBUTE = "create object attribute";
    public static final String CREATE_USER_ATTRIBUTE = "create user attribute";
    public static final String DEASSIGN = "deassign";
    public static final String DEASSIGN_FROM = "deassign from";
    public static final String CREATE_ASSOCIATION = "create association";
    public static final String UPDATE_ASSOCIATION = "update association";
    public static final String DELETE_ASSOCIATION = "delete association";
    public static final String GET_ASSOCIATIONS = "get associations";
    public static final String ALL_OPERATIONS = "*";
    public static final String ANY_OPERATIONS = "any";
    public static final String GET_PERMISSIONS = "get permissions";
    public static final String GET_ACCESSIBLE_CHILDREN = "get accessible children";
    public static final String GET_PROHIBITED_OPS = "get prohibited ops";
    public static final String GET_ACCESSIBLE_NODES = "get accessible nodes";

    //connector
    public static final String CONNECTOR_NAME = "PM";
    public static final String CONNECTOR_NAMESPACE = "connector";

    //super pc
    public static final String SUPER_PC_NAME = "Super PC";

    public static final long CONNECTOR_ID = 1L;
    public static final long NO_USER      = -1;
    public static final String NEO4J      = "neo4j";

    //obligations
    public static final String PM_UNKNOWN = "k";
    public static final String PM_RULE = "rule";
    public static final String PM_LABEL = "l";
    public static final String PM_EVENT_CREATE = "create";
    public static final String PM_EVENT_DELETE = "delete";
    // Events.
    public static final String PM_EVENT_OBJECT_CREATE = "object create";
    public static final String PM_EVENT_OBJECT_DELETE  = "object delete";
    public static final String PM_EVENT_OBJECT_READ    = "object read";
    public static final String PM_EVENT_OBJECT_WRITE   = "object write";
    public static final String PM_EVENT_USER_CREATE    = "user create";
    public static final String PM_EVENT_SESSION_CREATE = "session create";
    public static final String PM_EVENT_SESSION_DELETE = "session delete";
    public static final String PM_EVENT_OBJECT_SEND    = "object send";

    public static final int HASH_LENGTH = 163;
    public static final String SUPER_KEYWORD      = "super";
    public static final String ALL_OPS      = "*";
    public static final String PASSWORD_PROPERTY        = "password";
    public static final String DESCRIPTION_PROPERTY     = "description";
    public static final String NAMESPACE_PROPERTY       = "namespace";
    public static final String SOURCE_PROPERTY       = "source";
    public static final String STORAGE_PROPERTY       = "storage";
    public static final String GCS_STORAGE       = "google";
    public static final String AWS_STORAGE       = "amazon";
    public static final String LOCAL_STORAGE       = "local";
    public static final String CONTENT_TYPE_PROPERTY       = "content_type";
    public static final String SIZE_PROPERTY       = "size";
    public static final String PATH_PROPERTY       = "path";
    public static final String BUCKET_PROPERTY       = "bucket";
    public static final String COLUMN_INDEX_PROPERTY    = "column_index";
    public static final String ORDER_BY_PROPERTY        = "order_by";
    public static final String ROW_INDEX_PROPERTY       = "row_index";
    public static final String SESSION_USER_ID_PROPERTY = "user_id";
    public static final String SCHEMA_COMP_PROPERTY        = "schema_comp";
    public static final String SCHEMA_COMP_SCHEMA_PROPERTY = "schema";
    public static final String SCHEMA_COMP_TABLE_PROPERTY  = "table";
    public static final String SCHEMA_COMP_ROW_PROPERTY    = "row";
    public static final String SCHEMA_COMP_COLUMN_PROPERTY = "col";
    public static final String SCHEMA_COMP_CELL_PROPERTY = "cell";
    public static final String SCHEMA_NAME_PROPERTY  = "schema";
    public static final String COLUMN_CONTAINER_NAME = "Columns";
    public static final String ROW_CONTAINER_NAME    = "Rows";
    public static final boolean INHERIT_DEFAULT      = true;
    public static final String SUPER_USER_NAME       = "super";
    public static final int NEW_NODE_ID              = 0;
    public static final int NO_BASE_ID               = 0;
    public static final String COMMA_DELIMETER       = ",\\s*";

    public static final int NUM_META_NODES = 3;
}
