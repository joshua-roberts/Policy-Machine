package gov.nist.csd.pm.epp.obligations;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.sql.SQLException;
import java.util.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import gov.nist.csd.pm.model.exceptions.InvalidEntityException;
import gov.nist.csd.pm.model.exceptions.InvalidEvrException;
import gov.nist.csd.pm.model.obligations.*;
import gov.nist.csd.pm.model.obligations.script.EvrScript;
import gov.nist.csd.pm.model.obligations.script.rule.event.*;
import gov.nist.csd.pm.model.obligations.script.rule.event.time.EvrEvent;
import gov.nist.csd.pm.model.obligations.script.rule.event.time.EvrTime;
import gov.nist.csd.pm.model.obligations.script.rule.event.time.EvrTimeElement;
import gov.nist.csd.pm.model.obligations.script.rule.response.*;
import gov.nist.csd.pm.model.exceptions.ConfigurationException;
import gov.nist.csd.pm.model.exceptions.DatabaseException;
import gov.nist.csd.pm.model.exceptions.InvalidPropertyException;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import static gov.nist.csd.pm.epp.obligations.EvrKeywords.*;

public class EvrParser {
    private Element    root;
    private EvrService evrService;

    EvrParser() {
        evrService = new EvrService();
    }

    EvrParser(File file) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(file);
        doc.getDocumentElement().normalize();
        root = doc.getDocumentElement();
    }

    EvrScript parse() throws DatabaseException, InvalidEvrException, InvalidEntityException, SQLException, InvalidPropertyException, ConfigurationException, IOException, ClassNotFoundException {
        return parseScript();
    }

    EvrScript parse(String xml) throws IOException, SAXException, ParserConfigurationException, DatabaseException, InvalidEvrException, InvalidEntityException, SQLException, InvalidPropertyException, ConfigurationException, ClassNotFoundException {
        init(xml);
        return parseScript();
    }

    private void init(String xml) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        InputSource is = new InputSource();
        is.setCharacterStream(new StringReader(xml));

        Document doc = db.parse(is);
        doc.getDocumentElement().normalize();
        root = doc.getDocumentElement();
    }

    /**
     * Parse the root node of the xml script
     * Allowed child tags: label, rules
     * @return
     * @throws InvalidEvrException
     * @throws InvalidPropertyException
     * @throws DatabaseException
     * @throws ConfigurationException
     * @throws SQLException
     * @throws InvalidEntityException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private EvrScript parseScript() throws InvalidEvrException, InvalidPropertyException, DatabaseException, ConfigurationException, SQLException, InvalidEntityException, IOException, ClassNotFoundException {
        System.out.println("parsing script");

        //get label
        String name = getLabel(root);
        System.out.println("label: " + name);

        //initialize script object
        EvrScript script = new EvrScript(name);

        //create script in db
        String scriptId = evrService.createScript(script);

        //parse rules and add
        List<EvrRule> evrRules = parseRules(scriptId);
        script.setRules(evrRules);

        return script;
    }

    /**
     * Get the label for this script.
     * @param node
     * @return
     */
    private String getLabel(Node node) {
        String name = null;

        List<Node> childNodes = getChildNodes(node);
        for(Node childNode : childNodes) {
            String tag = childNode.getNodeName();
            switch(tag) {
                case LABEL_TAG:
                    name = childNode.getTextContent();
                    break;
            }
        }

        if(name == null) {
            name = UUID.randomUUID().toString().toUpperCase();
        }

        return name;
    }

    /**
     * Parse the rules for this script
     * Allowed child tags: rule
     * @param scriptId
     * @return
     * @throws InvalidPropertyException
     * @throws InvalidEvrException
     * @throws DatabaseException
     * @throws ConfigurationException
     * @throws InvalidEntityException
     * @throws SQLException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private List<EvrRule> parseRules(String scriptId) throws InvalidPropertyException, InvalidEvrException, DatabaseException, ConfigurationException, InvalidEntityException, SQLException, IOException, ClassNotFoundException {
        System.out.println("parsing rules...");
        List<EvrRule> rules = new ArrayList<>();

        Node rulesNode = null;
        List<Node> childNodes = getChildNodes(root);
        for(Node childNode : childNodes) {
            switch(childNode.getNodeName()) {
                case RULES_TAG:
                    rulesNode = childNode;
                    break;
            }
        }

        if(rulesNode == null) {
            return rules;
        }

        childNodes = getChildNodes(rulesNode);
        for(Node childNode : childNodes) {
            switch(childNode.getNodeName()) {
                case RULE_TAG:
                    EvrRule evrRule = parseRule(scriptId, "rules", childNode);
                    rules.add(evrRule);
                    break;
            }
        }

        return rules;
    }

    /**
     * Parse a rule
     * Allowed child tags: event, response
     * @param parentId
     * @param parentLabel
     * @param ruleNode
     * @return
     * @throws InvalidPropertyException
     * @throws InvalidEvrException
     * @throws DatabaseException
     * @throws ConfigurationException
     * @throws InvalidEntityException
     * @throws SQLException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private EvrRule parseRule(String parentId, String parentLabel, Node ruleNode) throws InvalidPropertyException, InvalidEvrException, DatabaseException, ConfigurationException, InvalidEntityException, SQLException, IOException, ClassNotFoundException {
        System.out.println("parsing rule...");
        EvrRule rule = new EvrRule();

        String label = getLabel(ruleNode);
        System.out.println("label: " + label);
        rule.setLabel(label);

        //create rule node in database
        String ruleId = evrService.createRule(parentId, parentLabel, label);

        List<Node> childNodes = getChildNodes(ruleNode);
        for(Node childNode : childNodes) {
            switch(childNode.getNodeName()) {
                case EVENT_TAG:
                    EvrEvent event = parseEvent(ruleId, childNode);
                    rule.setEvent(event);
                    break;
                case RESPONSE_TAG:
                    EvrResponse response = parseResponse(ruleId, childNode);
                    rule.setResponse(response);
                    break;
            }
        }

        return rule;
    }

    /**
     * Parse an event
     * Allowed child tags: subject, operations, policies, target, time
     * @param ruleId
     * @param eventNode
     * @return
     * @throws InvalidPropertyException
     * @throws InvalidEvrException
     * @throws DatabaseException
     * @throws ConfigurationException
     * @throws InvalidEntityException
     * @throws SQLException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private EvrEvent parseEvent(String ruleId, Node eventNode) throws InvalidPropertyException, InvalidEvrException, DatabaseException, ConfigurationException, InvalidEntityException, SQLException, IOException, ClassNotFoundException {
        System.out.println("parsing event...");
        EvrEvent event = new EvrEvent();

        List<Node> childNodes = getChildNodes(eventNode);
        for(Node childNode : childNodes) {
            switch(childNode.getNodeName()) {
                case SUBJECT_TAG:
                    EvrSubject subject = parseSubject(ruleId, "event", childNode);
                    event.setSubject(subject);
                    break;
                case OPERATIONS_TAG:
                    EvrOpertations operations = parseOperations(ruleId, "event", childNode);
                    event.setEvrOperations(operations);
                    break;
                case POLICIES_TAG:
                    EvrPolicies policies = parsePolicies(ruleId, "event", childNode);
                    event.setEvrPolicies(policies);
                    break;
                case TARGET_TAG:
                    EvrTarget target = parseTarget(ruleId, "event", childNode);
                    event.setTarget(target);
                    break;
                case TIME_TAG:
                    EvrTime evrTime = parseTime(ruleId, childNode);
                    event.setTime(evrTime);
                    break;
            }
        }

        return event;
    }

    /**
     * Parse a time event.
     * Allowed child tags: dow, day, month, year, hour
     * @param ruleId
     * @param timeNode
     * @return
     * @throws DatabaseException
     * @throws SQLException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private EvrTime parseTime(String ruleId, Node timeNode) throws DatabaseException, SQLException, IOException, ClassNotFoundException, InvalidPropertyException {
        System.out.println("parsing time event...");

        EvrTime evrTime = new EvrTime();

        List<Node> childNodes = getChildNodes(timeNode);
        for(Node node : childNodes) {
            EvrTimeElement element = parseTimeElement(node);
            switch (node.getNodeName()) {
                case DOW_TAG:
                    evrTime.setDow(element);
                    break;
                case DAY_TAG:
                    evrTime.setDay(element);
                    break;
                case MONTH_TAG:
                    evrTime.setMonth(element);
                    break;
                case YEAR_TAG:
                    evrTime.setYear(element);
                    break;
                case HOUR_TAG:
                    evrTime.setHour(element);
                    break;
            }
        }

        //create time in db
        evrService.createTime(ruleId, evrTime);

        return evrTime;
    }

    /**
     * Parse a time element. Day of the week (dow) is numbered 1-7 (1 = monday, 7 = sunday).  Each element can be a
     * list of values or a range
     * @param node
     * @return
     */
    private EvrTimeElement parseTimeElement(Node node) {
        System.out.println("parsing time element...");

        EvrTimeElement element = new EvrTimeElement();

        List<Node> childNodes = getChildNodes(node);
        if(childNodes.isEmpty()) {
            if(node.getChildNodes().getLength() > 0) {
                String[] split = node.getTextContent().split(",\\s*");
                List<Integer> values = new ArrayList<>();
                for(String s : split) {
                    values.add(new Integer(s));
                }
                element.setValues(values);
            }
        }else {
            //range
            int start = 0;
            int end = 0;
            for(Node child : childNodes) {
                switch (child.getNodeName()) {
                    case START_TAG:
                        start = Integer.valueOf(child.getTextContent());
                        break;
                    case END_TAG:
                        end = Integer.valueOf(child.getTextContent());
                        break;
                }
            }
            element.setRange(start, end);
        }

        return element;
    }

    /**
     * Parse a target.  This can be in an event or actions. The entity tag are the objects
     * (i.e. 'any object' = <entity/). The containers tag, contains one or more entities, and an intersection
     * attribute that denotes whether or not to take the intersection of the containers
     * Allowed child tags: entity, containers
     * @param parentId
     * @param parentLabel
     * @param targetNode
     * @return
     * @throws InvalidPropertyException
     * @throws InvalidEvrException
     * @throws DatabaseException
     * @throws ConfigurationException
     * @throws InvalidEntityException
     * @throws SQLException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private EvrTarget parseTarget(String parentId, String parentLabel, Node targetNode) throws InvalidPropertyException, InvalidEvrException, DatabaseException, ConfigurationException, InvalidEntityException, SQLException, IOException, ClassNotFoundException {
        System.out.println("parsing target...");
        EvrTarget target = new EvrTarget();

        //check if intersection
        NamedNodeMap attributes = targetNode.getAttributes();
        Node interNode = attributes.getNamedItem(INTERSECTION_ATTRIBUTE);
        boolean intersection = false;
        if(interNode != null) {
            intersection = Boolean.valueOf(interNode.getNodeValue());
        }
        target.setIntersection(intersection);

        //create target
        String targetId = evrService.createTarget(parentId, parentLabel);

        List<Node> childNodes = getChildNodes(targetNode);
        for(Node node : childNodes) {
            switch (node.getNodeName()) {
                case ENTITY_TAG:
                    System.out.println("target entity...");
                    EvrEntity evrEntity = parseEntity(targetId, "target_object", node);
                    target.setEntity(evrEntity);
                    break;
                case CONTAINERS_TAG:
                    List<Node> contChildNodes = getChildNodes(node);
                    for (Node contChildNode : contChildNodes) {
                        switch (contChildNode.getNodeName()) {
                            case ENTITY_TAG:
                                System.out.println("target container...");
                                evrEntity = parseEntity(targetId, "target_containers", contChildNode);
                                target.addContainer(evrEntity);
                                break;
                        }
                    }
                    break;
            }
        }
        return target;
    }

    /**
     * Parse the policies tag.  There can be one or more entity tags under the policies tag.  The default behavior is
     * to apply an OR operation to the set of policies.  This can be set by surrounding the set of entities in an
     * AND or OR tag.
     * Allowed child tags: and, or, entity
     * @param parentId
     * @param parentLabel
     * @param policyNode
     * @return
     * @throws InvalidPropertyException
     * @throws InvalidEvrException
     * @throws DatabaseException
     * @throws ConfigurationException
     * @throws InvalidEntityException
     * @throws SQLException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private EvrPolicies parsePolicies(String parentId, String parentLabel, Node policyNode) throws 
            InvalidPropertyException, InvalidEvrException, DatabaseException, ConfigurationException, InvalidEntityException, SQLException, IOException, ClassNotFoundException {
        System.out.println("parsing pc...");

        EvrPolicies policies = new EvrPolicies();

        //child tags are either OR or AND.  If there are no child nodes
        //the default is OR
        List<Node> childNodes = getChildNodes(policyNode);
        for(Node node : childNodes) {
            if(node.getNodeName().equals(AND_TAG)) {
                policies.setOr(false);
                policyNode = node;
            } else if(node.getNodeName().equals(OR_TAG)) {
                policyNode = node;
            }
        }

        //create subject node
        String policiesId = evrService.createPolicies(parentId, parentLabel, policies.isOr());

        //get actual entity nodes
        childNodes = getChildNodes(policyNode);
        for(Node child : childNodes) {
            if(child.getNodeName().equals(ENTITY_TAG)) {
                EvrEntity evrEntity = parseEntity(policiesId, POLICIES_TAG, child);
                policies.addEntity(evrEntity);
            }
        }

        return policies;
    }

    /**
     * Parse operations.  This is just a set of operation tags
     * Allowed child tags: operation
     * @param parentId
     * @param parentLabel
     * @param operationsNode
     * @return
     * @throws DatabaseException
     * @throws ConfigurationException
     * @throws SQLException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private EvrOpertations parseOperations(String parentId, String parentLabel, Node operationsNode) throws DatabaseException, ConfigurationException, SQLException, IOException, ClassNotFoundException, InvalidPropertyException {
        System.out.println("parsing op...");

        HashSet<String> ops = new HashSet<>();

        List<Node> childNodes = getChildNodes(operationsNode);
        for(Node node : childNodes) {
            switch (node.getNodeName()) {
                case OPERATION_TAG:
                    if(node.getTextContent().length() > 0) {
                        ops.add(node.getTextContent());
                    }
            }
        }

        System.out.println("\tops: " + ops);

        //create opspec in db
        evrService.createOperations(parentId, parentLabel, ops);

        return new EvrOpertations(ops);
    }

    /**
     * Parse a subject. The subject can be an entity (user or user attribute), a function, or a process
     * Allowed child tags: entity, function, process
     * @param parentId
     * @param parentLabel
     * @param subjectNode
     * @return
     * @throws InvalidPropertyException
     * @throws InvalidEvrException
     * @throws DatabaseException
     * @throws ConfigurationException
     * @throws InvalidEntityException
     * @throws SQLException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private EvrSubject parseSubject(String parentId, String parentLabel, Node subjectNode) throws InvalidPropertyException, InvalidEvrException, DatabaseException, ConfigurationException, InvalidEntityException, SQLException, IOException, ClassNotFoundException {
        System.out.println("parsing subject...");

        EvrSubject subject = new EvrSubject();

        //create subject node
        String subjectId = evrService.createSubject(parentId, parentLabel);

        List<Node> childNodes = getChildNodes(subjectNode);
        //if childnodes is not empty then add to the list, else do nothing. An empty list means any user
        if(!childNodes.isEmpty()) {
            for(Node node : childNodes) {
                if(node.getNodeName().equals(ENTITY_TAG)) {
                    EvrEntity evrEntity = parseEntity(subjectId, "subject", node);
                    //if the entity is any entity dont add to subject
                    if(!evrEntity.isAny()) {
                        subject.addEntity(evrEntity);
                    }
                } else if(node.getNodeName().equals(FUNCTION_TAG)) {
                    EvrFunction evrFunction = parseFunction(subjectId, "subject", node);
                    subject.addEntity(new EvrEntity(evrFunction));
                } else if(node.getNodeName().equals(PROCESS_TAG)) {
                    EvrProcess evrProcess = parseProcess(subjectId, node);
                    subject.addEntity(new EvrEntity(evrProcess));
                }
            }
        }

        return subject;
    }

    /**
     * Parse a process. The process can be explicitly set or the result of calling a function
     * Allowed child tags: function
     * @param parentId
     * @param processNode
     * @return
     * @throws InvalidEvrException
     * @throws InvalidPropertyException
     * @throws DatabaseException
     * @throws ConfigurationException
     * @throws InvalidEntityException
     * @throws SQLException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private EvrProcess parseProcess(String parentId, Node processNode) throws InvalidEvrException, InvalidPropertyException, DatabaseException, ConfigurationException, InvalidEntityException, SQLException, IOException, ClassNotFoundException {
        System.out.println("parsing process...");

        EvrProcess evrProcess = new EvrProcess();

        String processId = evrService.createProcess(parentId, "subject");

        List<Node> childNodes = getChildNodes(processNode);
        if(childNodes.isEmpty()) {
            evrProcess = new EvrProcess(Long.valueOf(processNode.getTextContent()));

            evrService.updateProcess(processId, evrProcess.getProcessId());
        }else {
            //arg is a function
            for(Node argChild : childNodes) {
                if(argChild.getNodeName().equals(FUNCTION_TAG)) {
                    EvrFunction evrFunction = parseFunction(processId, "process", argChild);
                    evrProcess = new EvrProcess(evrFunction);
                }
            }
        }

        return evrProcess;
    }

    /**
     * Parse a function.  A function takes zero or more arguments. An argument could either be text, a function, or
     * an entity
     * Allowed child tags: arg
     * @param parentId
     * @param parentLabel
     * @param functionNode
     * @return
     * @throws InvalidEvrException
     * @throws InvalidPropertyException
     * @throws DatabaseException
     * @throws ConfigurationException
     * @throws InvalidEntityException
     * @throws SQLException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private EvrFunction parseFunction(String parentId, String parentLabel, Node functionNode) throws InvalidEvrException, InvalidPropertyException, DatabaseException, ConfigurationException, InvalidEntityException, SQLException, IOException, ClassNotFoundException {
        NamedNodeMap attributes = functionNode.getAttributes();
        Node name = attributes.getNamedItem(NAME_ATTRIBUTE);

        EvrFunction evrFunction = new EvrFunction(name.getNodeValue());

        //create function node
        String functionId = evrService.createFunction(parentId, parentLabel, evrFunction);

        List<Node> childNodes = getChildNodes(functionNode);
        //loop through arguments
        for(Node node : childNodes) {
            List<Node> argChildren = getChildNodes(node);
            if(argChildren.isEmpty()) {
                EvrArg evrArg = new EvrArg(node.getTextContent());

                //arg is a value
                evrFunction.addArg(evrArg);

                //add arg in db
                evrService.addFuntionArgValue(functionId, evrArg);
            } else {
                //arg is a function or entity
                for(Node argChild : argChildren) {

                    //create arg in db
                    String argId = evrService.addFuntionArg(functionId);

                    switch (argChild.getNodeName()) {
                        case FUNCTION_TAG:
                            EvrFunction evrFunctionArg = parseFunction(argId, "arg", argChild);
                            evrFunction.addArg(new EvrArg(evrFunctionArg));
                            break;
                        case ENTITY_TAG:
                            EvrEntity entity = parseEntity(argId, "arg", argChild);
                            evrFunction.addArg(new EvrArg(entity));
                            break;
                    }
                }
            }
        }

        return evrFunction;
    }

    /**
     * Parse an entity.  Each entity can have a name, type, properties, and complement tags.  An entity can also be a
     * function.
     * Allowed child tags: function
     * @param parentId
     * @param parentLabel
     * @param entityNode
     * @return
     * @throws InvalidPropertyException
     * @throws InvalidEvrException
     * @throws DatabaseException
     * @throws ConfigurationException
     * @throws InvalidEntityException
     * @throws SQLException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private EvrEntity parseEntity(String parentId, String parentLabel, Node entityNode) throws InvalidPropertyException, InvalidEvrException, DatabaseException, ConfigurationException, InvalidEntityException, SQLException, IOException, ClassNotFoundException {
        System.out.println("parsing entity...");

        NamedNodeMap attributes = entityNode.getAttributes();
        Node name = attributes.getNamedItem(NAME_ATTRIBUTE);
        Node type = attributes.getNamedItem(TYPE_ATTRIBUTE);
        Node properties = attributes.getNamedItem(PROPERTIES_ATTRIBUTE);
        Node compliment = attributes.getNamedItem(COMP_ATTRIBUTE);

        EvrEntity evrEntity = new EvrEntity();

        // create the entity node
        String entityId = evrService.createEntity(parentId, parentLabel, evrEntity);

        List<Node> childNodes = getChildNodes(entityNode);
        if(childNodes.isEmpty()) {
            //node
            HashMap<String, String> propsMap = new HashMap<>();
            if(properties != null) {
                String[] propArr = properties.getNodeValue().split(",");
                for(String prop : propArr) {
                    String[] pieces = prop.split("=");
                    propsMap.put(pieces[0], pieces[1]);
                }
            }

            String entityName = null;
            if(name != null) {
                entityName = name.getNodeValue();
            }

            String entityType = null;
            if(type != null) {
                entityType = type.getNodeValue();
            }

            boolean bComp = false;
            if(compliment != null) {
                bComp = Boolean.valueOf(compliment.getNodeValue());
            }

            System.out.println("\tname=" + entityName);
            System.out.println("\ttype=" + entityType);
            System.out.println("\tproperties=" + propsMap);

            evrEntity = new EvrEntity(entityName, entityType, propsMap, bComp);

            evrService.updateEntity(entityId, evrEntity);
        } else {
            //function
            for(Node node : childNodes) {
                if(node.getNodeName().equals(FUNCTION_TAG)) {
                    evrEntity = new EvrEntity(parseFunction(entityId, "entity", node));
                }
            }
        }

        return evrEntity;
    }

    /**
     * Parse the response of a rule.  Each response can have a condition, and one or more actions
     * Allowed child tags: condition, assign, grant, create, deny, delete
     * @param ruleId
     * @param responseNode
     * @return
     * @throws InvalidPropertyException
     * @throws InvalidEvrException
     * @throws DatabaseException
     * @throws ConfigurationException
     * @throws InvalidEntityException
     * @throws SQLException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private EvrResponse parseResponse(String ruleId, Node responseNode) throws InvalidPropertyException, InvalidEvrException, DatabaseException, ConfigurationException, InvalidEntityException, SQLException, IOException, ClassNotFoundException {
        System.out.println("parsing response...");

        EvrResponse response = new EvrResponse();

        List<Node> childNodes = getChildNodes(responseNode);
        for(Node node : childNodes) {
            switch(node.getNodeName()) {
                case CONDITION_TAG:
                    EvrCondition condition = parseCondition(ruleId, node);
                    response.setCondition(condition);
                    break;
                case ASSIGN_TAG:
                    EvrAssignAction evrAssignAction = parseAssign(ruleId, "response", node);
                    response.addAction(evrAssignAction);
                    break;
                case GRANT_TAG:
                    EvrGrantAction evrGrantAction = parseGrant(ruleId, "response", node);
                    response.addAction(evrGrantAction);
                    break;
                case CREATE_TAG:
                    EvrCreateAction evrCreateAction = parseCreate(ruleId, "response", node);
                    response.addAction(evrCreateAction);
                    break;
                case DENY_TAG:
                    EvrDenyAction evrDenyAction = parseDeny(ruleId, "response", node);
                    response.addAction(evrDenyAction);
                    break;
                case DELETE_TAG:
                    EvrDeleteAction evrDeleteAction = parseDelete(ruleId, "response", node);
                    response.addAction(evrDeleteAction);
                    break;
            }
        }
        return response;
    }

    /**
     * Parse a delete action. A delete action can delete an assignment, deny, or rule
     * Allowed child tags: assign, deny, rule
     * @param parentId
     * @param parentLabel
     * @param deleteNode
     * @return
     * @throws InvalidPropertyException
     * @throws InvalidEvrException
     * @throws DatabaseException
     * @throws ConfigurationException
     * @throws InvalidEntityException
     * @throws SQLException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private EvrDeleteAction parseDelete(String parentId, String parentLabel, Node deleteNode) throws InvalidPropertyException, InvalidEvrException, DatabaseException, ConfigurationException, InvalidEntityException, SQLException, IOException, ClassNotFoundException {
        System.out.println("parsing deny action...");

        EvrDeleteAction action = new EvrDeleteAction();

        //create delete action in db
        String deleteActionId = evrService.createDeleteAction(parentId, parentLabel);

        List<Node> childNodes = getChildNodes(deleteNode);
        for(Node node : childNodes) {
            switch(node.getNodeName()) {
                case ASSIGN_TAG:
                    EvrAssignAction evrAssignAction = parseAssign(deleteActionId, "delete_action", node);
                    action.setEvrAction(evrAssignAction);
                    break;
                case DENY_TAG:
                    EvrDenyAction evrDenyAction = parseDeny(deleteActionId, "delete_action", node);
                    action.setEvrAction(evrDenyAction);
                    break;
                case RULE_TAG:
                    EvrRule evrRule = parseRule(deleteActionId, "delete_action", node);
                    action.setEvrRule(evrRule);
                    break;
            }
        }

        return action;
    }

    /**
     * Parse a deny action. Deny the operations for the subject on the target
     * Allowed child tags: subject, operations, target
     * @param parentId
     * @param parentLabel
     * @param denyNode
     * @return
     * @throws DatabaseException
     * @throws ConfigurationException
     * @throws InvalidPropertyException
     * @throws InvalidEntityException
     * @throws InvalidEvrException
     * @throws SQLException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private EvrDenyAction parseDeny(String parentId, String parentLabel, Node denyNode) throws DatabaseException, ConfigurationException, InvalidPropertyException, InvalidEntityException, InvalidEvrException, SQLException, IOException, ClassNotFoundException {
        System.out.println("parsing deny action...");

        EvrDenyAction action = new EvrDenyAction();

        //create deny in db
        String denyActionId = evrService.createDenyAction(parentId, parentLabel);

        List<Node> childNodes = getChildNodes(denyNode);
        for(Node node : childNodes) {
            switch (node.getNodeName()) {
                case SUBJECT_TAG:
                    EvrSubject evrSubject = parseSubject(denyActionId, "deny_action", node);
                    action.setSubject(evrSubject);
                    break;
                case OPERATIONS_TAG:
                    EvrOpertations evrOpertations = parseOperations(denyActionId, "deny_action", node);
                    action.setEvrOperations(evrOpertations);
                    break;
                case TARGET_TAG:
                    EvrTarget evrTarget = parseTarget(denyActionId, "deny_action", node);
                    action.setTarget(evrTarget);
                    break;
            }
        }

        return action;
    }

    /**
     * Parse create action, which can create an entity, a target, or another rule.
     * Allowed child tags: entity, target, rule
     * @param parentId
     * @param parentLabel
     * @param createNode
     * @return
     * @throws InvalidPropertyException
     * @throws InvalidEvrException
     * @throws DatabaseException
     * @throws ConfigurationException
     * @throws InvalidEntityException
     * @throws SQLException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private EvrCreateAction parseCreate(String parentId, String parentLabel, Node createNode) throws InvalidPropertyException, InvalidEvrException, DatabaseException, ConfigurationException, InvalidEntityException, SQLException, IOException, ClassNotFoundException {
        System.out.println("parsing create action...");

        EvrCreateAction action = new EvrCreateAction();

        //create create in db
        String createActionId = evrService.createCreateAction(parentId, parentLabel);

        List<Node> childNodes = getChildNodes(createNode);
        for(Node node : childNodes) {
            switch (node.getNodeName()) {
                case ENTITY_TAG:
                    EvrEntity entity = parseEntity(createActionId, "create_action", node);
                    action.setEntity(entity);
                    break;
                case TARGET_TAG:
                    EvrTarget evrTarget = parseTarget(createActionId, "create_action", node);
                    action.setTarget(evrTarget);
                    break;
                case RULE_TAG:
                    EvrRule evrRule = parseRule(createActionId, "create_action", node);
                    action.setRule(evrRule);
                    break;
            }
        }

        return action;
    }

    /**
     * Parse a grant action.  Grant the subject the operations on the target
     * @param parentId
     * @param parentLabel
     * @param grantNode
     * @return
     * @throws InvalidEvrException
     * @throws InvalidPropertyException
     * @throws DatabaseException
     * @throws ConfigurationException
     * @throws InvalidEntityException
     * @throws SQLException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private EvrGrantAction parseGrant(String parentId, String parentLabel, Node grantNode) throws InvalidEvrException, InvalidPropertyException, DatabaseException, ConfigurationException, InvalidEntityException, SQLException, IOException, ClassNotFoundException {
        System.out.println("parsing grant action...");

        EvrGrantAction action = new EvrGrantAction();

        //create grant in db
        String grantActionId = evrService.createGrantAction(parentId, parentLabel);

        List<Node> childNodes = getChildNodes(grantNode);
        for(Node node : childNodes) {
            switch (node.getNodeName()) {
                case SUBJECT_TAG:
                    EvrSubject evrSubject = parseSubject(grantActionId, "grant_action", node);
                    action.setSubject(evrSubject);
                    break;
                case OPERATIONS_TAG:
                    EvrOpertations evrOpertations = parseOperations(grantActionId, "grant_action", node);
                    action.setEvrOperations(evrOpertations);
                    break;
                case TARGET_TAG:
                    EvrTarget evrTarget = parseTarget(grantActionId, "grant_action", node);
                    action.setTarget(evrTarget);
                    break;
            }
        }

        return action;
    }

    /**
     * Parse an assign action. Assign the child to the parent.  Either can be an entity or a function.
     * Allowed child tags: child, parent
     * @param parentId
     * @param parentLabel
     * @param assignNode
     * @return
     * @throws InvalidPropertyException
     * @throws InvalidEvrException
     * @throws DatabaseException
     * @throws ConfigurationException
     * @throws InvalidEntityException
     * @throws SQLException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private EvrAssignAction parseAssign(String parentId, String parentLabel, Node assignNode) throws InvalidPropertyException, InvalidEvrException, DatabaseException, ConfigurationException, InvalidEntityException, SQLException, IOException, ClassNotFoundException {
        System.out.println("parsing assign action...");

        EvrAssignAction action = new EvrAssignAction();

        //create assign action
        String assignActionId = evrService.createAssignAction(parentId, parentLabel);

        List<Node> childNodes = getChildNodes(assignNode);
        for(Node node : childNodes) {
            List<Node> childChildNodes = getChildNodes(node);
            if(childChildNodes.isEmpty()) {
                continue;
            }

            Node childNode = childChildNodes.get(0);
            EvrEntity evrEntity = new EvrEntity();

            switch (node.getNodeName()) {
                case CHILD_TAG:
                    String aaChildId = evrService.createAssignActionParam(assignActionId, "child");

                    switch (childNode.getNodeName()) {
                        case ENTITY_TAG:
                            evrEntity = parseEntity(aaChildId, "assign_action_child", childNode);
                            break;
                        case FUNCTION_TAG:
                            evrEntity = new EvrEntity(parseFunction(aaChildId, "assign_action_child", childNode));
                            break;
                    }
                    action.setChild(evrEntity);
                    break;
                case PARENT_TAG:
                    String aaParentId = evrService.createAssignActionParam(assignActionId, "parent");

                    switch (childNode.getNodeName()) {
                        case ENTITY_TAG:
                            evrEntity = parseEntity(aaParentId, "assign_action_parent", childNode);
                            break;
                        case FUNCTION_TAG:
                            evrEntity = new EvrEntity(parseFunction(aaParentId, "assign_action_parent", childNode));
                            break;
                    }
                    action.setParent(evrEntity);
                    break;
            }
        }

        return action;
    }

    /**
     * Parse the condition for the response.  The condition is optional, but if present and returns true, the
     * response actions will be executed.
     * @param ruleId
     * @param conditionNode
     * @return
     * @throws DatabaseException
     * @throws ConfigurationException
     * @throws InvalidPropertyException
     * @throws InvalidEntityException
     * @throws InvalidEvrException
     * @throws SQLException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private EvrCondition parseCondition(String ruleId, Node conditionNode) throws DatabaseException, ConfigurationException, InvalidPropertyException, InvalidEntityException, InvalidEvrException, SQLException, IOException, ClassNotFoundException {
        NamedNodeMap attributes = conditionNode.getAttributes();
        Node existsNode = attributes.getNamedItem(EXISTS_ATTRIBUTE);
        boolean exists = (existsNode == null) ? true : Boolean.valueOf(existsNode.getNodeValue());

        EvrCondition condition = new EvrCondition();
        condition.setExists(exists);

        String conditionId = evrService.createCondition(ruleId, exists);

        List<Node> childNodes = getChildNodes(conditionNode);
        for(Node node : childNodes) {
            switch (node.getNodeName()) {
                case ENTITY_TAG:
                    EvrEntity entity = parseEntity(conditionId, "condition", node);
                    condition.setEntity(entity);
                    break;
                case FUNCTION_TAG:
                    EvrFunction evrFunction = parseFunction(conditionId, "condition", node);
                    condition.setEntity(new EvrEntity(evrFunction));
                    break;
            }
        }

        return condition;
    }

    private List<Node> getChildNodes(Node node) {
        List<Node> nodes = new ArrayList<>();
        NodeList childNodes = node.getChildNodes();
        for(int i = 0; i < childNodes.getLength(); i++) {
            if(!childNodes.item(i).getNodeName().startsWith("#")) {
                nodes.add(childNodes.item(i));
            }
        }

        return nodes;
    }
}