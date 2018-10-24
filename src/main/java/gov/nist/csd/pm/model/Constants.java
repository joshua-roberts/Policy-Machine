package gov.nist.csd.pm.model;

public class Constants {


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

    public static final boolean INHERIT_DEFAULT      = true;
    public static final String SUPER_USER_NAME       = "super";
    public static final int NEW_NODE_ID              = 0;
    public static final int NO_BASE_ID               = 0;
    public static final String COMMA_DELIMETER       = ",\\s*";

    public static final int NUM_META_NODES = 3;
}
