package gov.nist.csd.pm.demos.nlpm;

import gov.nist.csd.pm.model.graph.OldNode;

import java.util.List;

public class PDLParser {

    public static final String GRANT = "grant";
    public static final String USER_FUNC = "user";
    public static final String USER_ATTR_FUNC = "user_attr";
    public static final String OBJECT_FUNC = "object";
    public static final String OBJECT_ATTR_FUNC = "object_attr";
    public static final String POLICY_FUNC = "policy";

    public static final int GRANT_LENGTH = 7;
    public static final int SUBJECT_LENGTH = 2;

    private String[] commands;
    public PDLParser(String[] commands) {
        this.commands = commands;
    }

    public void parse() throws MalformedPDLException {
        for(String command : commands) {
            if (command.startsWith("GRANT")) {
                parseGrant(command);
            }
        }
    }

    public void parseGrant(String command) throws MalformedPDLException{
        System.out.println("parsing... " + command);
        String[] pieces = command.split("\\s+(?=[^])}]*([\\[({]|$))");
        if (pieces.length != GRANT_LENGTH) {
            throw new MalformedPDLException(command);
        }
        String subject = pieces[1];
        parseSubjetFunc(subject);

        String ops = pieces[2];
        String oa = pieces[4];
        String policy = pieces[6];
    }

    private List<OldNode> parseSubjetFunc(String subjectFunc) throws MalformedPDLException {
        System.out.println(subjectFunc);
        String[] pieces = subjectFunc.split("\\(|\\)");
        if(pieces.length != SUBJECT_LENGTH) {
            throw new MalformedPDLException(subjectFunc);
        }
        return null;
    }

    public static void main(String[] args) {
        String[] commands = {
                "GRANT user('u1') operations(read, write) ON object_attr('oa1', 'prop1=value1, prop2=value2') UNDER policy('pc1')"/*,
                "GRANT user('u1') operations(read, write) ON object_attr('oa2', {prop1: value1}) UNDER policy('pc2')"*/
        };

        PDLParser parser = new PDLParser(commands);
        try {
            parser.parse();
        }
        catch (MalformedPDLException e) {
            e.printStackTrace();
        }
    }
}
