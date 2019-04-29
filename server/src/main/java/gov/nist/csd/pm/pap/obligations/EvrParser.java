package gov.nist.csd.pm.pap.obligations;

import gov.nist.csd.pm.common.model.obligations.*;
import gov.nist.csd.pm.common.model.obligations.actions.Action;
import gov.nist.csd.pm.common.model.obligations.actions.CreateAction;
import gov.nist.csd.pm.common.model.obligations.functions.Arg;
import gov.nist.csd.pm.common.model.obligations.functions.Function;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.*;

public class EvrParser {

    public static void main(String[] args) throws FileNotFoundException, EvrException {
        System.out.println(System.getProperty("user.dir"));
        Yaml yaml = new Yaml();

        File initialFile = new File("evr/test.yml");
        InputStream is = new FileInputStream(initialFile);

        Map<Object, Object> obligation = yaml.load(is);

        Obligation parse = EvrParser.parse(obligation);
        int x =0;
    }

    private static <T> T getObject(Object o, Class<T> type) throws EvrException {
        if(!type.isInstance(o)) {
            throw new EvrException("expected " + type + " got " + o.getClass() + " at \"" + o + "\"");
        }

        return type.cast(o);
    }

    public static Obligation parse(Map<Object, Object> map) throws EvrException {
        Obligation obligation = new Obligation();

        String label = getObject(map.get("label"), String.class);
        if (label == null) {
            label = UUID.randomUUID().toString();
        }
        obligation.setLabel(label);

        if(map.containsKey("rules")) {
            List<Rule> rules = parseRules(map.get("rules"));
            obligation.setRules(rules);
        }

        return obligation;
    }

    protected static List<Rule> parseRules(Object o) throws EvrException {
        List rulesList = getObject(o, List.class);
        List<Rule> rules = new ArrayList<>();

        for(Object rule : rulesList) {
            rules.add(parseRule(rule));
        }

        return rules;
    }

    protected static Rule parseRule(Object o) throws EvrException {
        if(!(o instanceof Map)) {
            throw new EvrException("rule should be a map, got " + o.getClass() + " in " + o);
        }

        Map map = (Map)o;
        Rule rule = new Rule();

        String label = getObject(map.get("label"), String.class);
        if (label == null) {
            label = UUID.randomUUID().toString();
        }
        rule.setLabel(label);

        if(!map.containsKey("event")) {
            throw new EvrException("no event provided at " + o);
        }
        Event event = parseEvent(map.get("event"));
        rule.setEvent(event);

        if(!map.containsKey("response")) {
            throw new EvrException("no response provided at " + o);
        }
        Response response = parseResponse(map.get("response"));
        rule.setResponse(response);

        return rule;
    }

    protected static Event parseEvent(Object o) throws EvrException {
        if(!(o instanceof Map)) {
            throw new EvrException("event should be a Map, got " + o.getClass() + " in " + o);
        }

        Map map = (Map)o;
        Event event = new Event();
        if(map.containsKey("subject")) {
            Subject subject = parseSubject(map.get("subject"));
            event.setSubject(subject);
        }

        if(map.containsKey("policyClass")) {
            PolicyClass policyClass = parsePolicyClass(map.get("policyClass"));
            event.setPolicyClass(policyClass);
        }

        if(map.containsKey("operations")) {
            List<String> operations = parseOperations(map.get("operations"));
            event.setOperations(operations);
        }

        if(map.containsKey("target")) {
            Target target = parseTarget(map.get("target"));
            event.setTarget(target);
        }

        return event;
    }

    /**
     * Target can be a list of string or a map - containers: list<String>
     * If null an empty target will be returned
     * @param o
     * @return
     */
    protected static Target parseTarget(Object o) throws EvrException {
        Target target = new Target();
        if(o == null) {
            return target;
        } else if(!(o instanceof List) && !(o instanceof Map)) {
            throw new EvrException("event target should be a Map or List, got " + o.getClass() +
                    " in " + o);
        }

        if (o instanceof List) {
            target.setPolicyElements(parsePolicyElements(o));
        } else {
            target.setContainers(parseContainers(o));
        }

        return target;
    }

    private static List<String> parseContainers(Object o) throws EvrException {
        Map map = getObject(o, Map.class);
        List list = getObject(map.get("containers"), List.class);
        List<String> containers = new ArrayList<>();

        // check that each element in the array is a string
        for(Object l : list) {
            containers.add(getObject(l, String.class));
        }

        return containers;
    }

    private static List<String> parsePolicyElements(Object o) throws EvrException {
        List list = (List)o;
        List<String> policyElements = new ArrayList<>();

        // check that each element in the array is a string
        for(Object l : list) {
            policyElements.add(getObject(l, String.class));
        }

        return policyElements;
    }

    /**
     * If the provided object is null then any operation will satisfy this event.
     * If the provided object is not a list an exception is thrown.
     * @param o
     * @return
     * @throws EvrException
     */
    protected static List<String> parseOperations(Object o) throws EvrException {
        if(o == null) {
            return new ArrayList<>();
        }

        List opsList = getObject(o, List.class);
        List<String> operations = new ArrayList<>();
        for(Object op : opsList) {
            operations.add(getObject(op, String.class));
        }

        return operations;
    }

    /**
     * If the given is object is null, an empty PolicyClass object is returned indicating any policy class
     * Only one of anyOf or eachOf is allowed
     * @param o
     * @return
     * @throws EvrException
     */
    protected static PolicyClass parsePolicyClass(Object o) throws EvrException {
        PolicyClass policyClass = new PolicyClass();
        if(o == null) {
            return policyClass;
        }

        Map map = (Map)o;
        if(map.size() > 1) {
            throw new EvrException("expected one of (anyOf, eachOf), got " + map.keySet());
        }

        if(map.containsKey("anyOf")) {
            List<String> pcs = new ArrayList<>();
            List list = getObject(map.get("anyOf"), List.class);
            for(Object obj : list) {
                pcs.add((String) obj);
            }
            policyClass.setAnyOf(pcs);
        } else if(map.containsKey("eachOf")) {
            List<String> pcs = new ArrayList<>();
            List list = getObject(map.get("eachOf"), List.class);
            for(Object obj : list) {
                pcs.add((String) obj);
            }
            policyClass.setAnyOf(pcs);
        }

        return policyClass;
    }

    protected static Subject parseSubject(Object o) throws EvrException {
        if(!(o instanceof Map)) {
            throw new EvrException("event subject should be a Map, got " + o.getClass());
        }

        Map map = (Map)o;
        if(map.size() != 1) {
            throw new EvrException("only one element is expected for an event subject, got " + map);
        }

        if(map.containsKey("user")) {
            return parseSubjectUser(map.get("user"));
        } else if(map.containsKey("anyUser")) {
            return parseSubjectAnyUser(map.get("anyUser"));
        } else if(map.containsKey("process")) {
            return parseProcess(map.get("process"));
        }

        throw new EvrException("invalid subject specification");
    }

    private static Subject parseProcess(Object o) throws EvrException {
        String process = getObject(o, String.class);
        return new Subject(new EvrProcess(process));
    }

    private static Subject parseSubjectAnyUser(Object o) throws EvrException {
        List list = getObject(o, List.class);
        List<String> anyUser = new ArrayList<>();
        for(Object obj : list) {
            anyUser.add(getObject(obj, String.class));
        }

        return new Subject(anyUser);
    }

    private static Subject parseSubjectUser(Object o) throws EvrException {
        return new Subject(getObject(o, String.class));
    }

    protected static Response parseResponse(Object o) throws EvrException {
        Response response = new Response();
        if(o == null) {
            return response;
        }

        Map responseMap = getObject(o, Map.class);
        o = responseMap.get("actions");
        List actionsList = getObject(o, List.class);

        for(Object a : actionsList) {
            Map actionMap = getObject(a, Map.class);
            if(actionMap.containsKey("create")) {
                Action action = parseCreateAction(actionMap.get("create"));
                response.addAction(action);
            }
        }

        return response;
    }

    private static Action parseCreateAction(Object o) throws EvrException {
        if(o == null) {
            throw new EvrException("create action cannot be null or empty");
        }

        CreateAction action;
        Map createActionMap = getObject(o, Map.class);
        if(createActionMap.containsKey("function")) {
            System.out.println("found function: " + createActionMap.get("function"));
            Map funcMap = getObject(createActionMap.get("function"), Map.class);
            action = new CreateAction(parseFunction(funcMap));
        } else {
            String name = (String) createActionMap.get("name");
            if (name == null || name.isEmpty()) {
                throw new EvrException("name cannot be null in create action");
            }

            String type = (String) createActionMap.get("type");
            if (type == null || type.isEmpty()) {
                throw new EvrException("type cannot be null in create action");
            }

            Object propsObj = createActionMap.get("properties");
            Map<String, String> properties = new HashMap<>();
            if(propsObj != null) {
                Map propsMap = getObject(propsObj, Map.class);
                for(Object p : propsMap.keySet()) {
                    properties.put(getObject(p, String.class), getObject(propsMap.get(p), String.class));
                }
            }

            action = new CreateAction(name, type, properties);
        }

        if(createActionMap.containsKey("containers")) {
            System.out.println("assigning node to " + createActionMap.get("containers"));
        }

        return action;
    }

    private static Function parseFunction(Map funcMap) throws EvrException {
        String funcName = getObject(funcMap.get("name"), String.class);
        List funcArgList = getObject(funcMap.get("args"), List.class);
        List<Arg> argList = new ArrayList<>();
        for(Object l : funcArgList) {
            if(l instanceof String) {
                argList.add(new Arg(getObject(l, String.class)));
            } else if(l instanceof Map) {
                Map map = getObject(l, Map.class);
                if(map.containsKey("function")) {
                    argList.add(new Arg(parseFunction(map)));
                }
            } else {
                throw new EvrException("invalid function definition " + funcMap);
            }
        }

        return new Function(funcName, argList);
    }
}
