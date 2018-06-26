package gov.nist.policyserver.obligations;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import gov.nist.policyserver.obligations.exceptions.InvalidEntityException;
import gov.nist.policyserver.obligations.exceptions.InvalidEvrException;
import gov.nist.policyserver.obligations.model.*;
import gov.nist.policyserver.obligations.model.script.EvrScript;
import gov.nist.policyserver.obligations.model.script.rule.event.*;
import gov.nist.policyserver.obligations.model.script.rule.event.time.EvrEvent;
import gov.nist.policyserver.obligations.model.script.rule.event.time.EvrTime;
import gov.nist.policyserver.obligations.model.script.rule.event.time.EvrTimeElement;
import gov.nist.policyserver.obligations.model.script.rule.response.*;
import gov.nist.policyserver.exceptions.ConfigurationException;
import gov.nist.policyserver.exceptions.DatabaseException;
import gov.nist.policyserver.exceptions.InvalidPropertyException;
import gov.nist.policyserver.model.graph.nodes.Property;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import static gov.nist.policyserver.obligations.EvrKeywords.*;

public class EvrParser {
    private Element    root;
    private EvrService evrService;

    EvrParser() throws DatabaseException, IOException, ClassNotFoundException, SQLException {
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
                case OP_SPEC_TAG:
                    EvrOpSpec opSpec = parseOpSpec(ruleId, "event", childNode);
                    event.setOpSpec(opSpec);
                    break;
                case PC_SPEC_TAG:
                    EvrPcSpec pcSpec = parsePcSpec(ruleId, "event", childNode);
                    event.setPcSpec(pcSpec);
                    break;
                case TARGET_TAG:
                    EvrTarget targetSpec = parseTarget(ruleId, "event", childNode);
                    event.setTarget(targetSpec);
                    break;
                case TIME_TAG:
                    EvrTime evrTime = parseTime(ruleId, childNode);
                    event.setTime(evrTime);
                    break;
            }
        }

        return event;
    }

    private EvrTime parseTime(String ruleId, Node timeNode) throws DatabaseException, SQLException, IOException, ClassNotFoundException {
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

    private EvrTarget parseTarget(String parentId, String parentLabel, Node targetNode) throws InvalidPropertyException, InvalidEvrException, DatabaseException, ConfigurationException, InvalidEntityException, SQLException, IOException, ClassNotFoundException {
        System.out.println("parsing target spec...");
        EvrTarget targetSpec = new EvrTarget();

        //check if intersection
        NamedNodeMap attributes = targetNode.getAttributes();
        Node interNode = attributes.getNamedItem(INTERSECTION_ATTRIBUTE);
        boolean intersection = false;
        if(interNode != null) {
            intersection = Boolean.valueOf(interNode.getNodeValue());
        }
        targetSpec.setIntersection(intersection);

        //create target
        String targetId = evrService.createTarget(parentId, parentLabel);

        List<Node> childNodes = getChildNodes(targetNode);
        for(Node node : childNodes) {
            switch (node.getNodeName()) {
                case ENTITY_TAG:
                    System.out.println("target spec entity...");
                    EvrEntity evrEntity = parseEntity(targetId, "target_object", node);
                    targetSpec.setEntity(evrEntity);
                    break;
                case CONTAINER_TAG:
                    List<Node> contChildNodes = getChildNodes(node);
                    for (Node contChildNode : contChildNodes) {
                        switch (contChildNode.getNodeName()) {
                            case ENTITY_TAG:
                                System.out.println("target spec container...");
                                evrEntity = parseEntity(targetId, "target_containers", contChildNode);
                                targetSpec.addContainer(evrEntity);
                                break;
                        }
                    }
                    break;
            }
        }
        return targetSpec;
    }

    private EvrPcSpec parsePcSpec(String parentId, String parentLabel, Node pcSpecNode) throws InvalidPropertyException, InvalidEvrException, DatabaseException, ConfigurationException, InvalidEntityException, SQLException, IOException, ClassNotFoundException {
        System.out.println("parsing pc spec...");

        EvrPcSpec pcSpec = new EvrPcSpec();

        //child tags are either OR or AND.  If there are no child nodes
        //the default is OR
        List<Node> childNodes = getChildNodes(pcSpecNode);
        for(Node node : childNodes) {
            if(node.getNodeName().equals(AND_TAG)) {
                pcSpec.setOr(false);
                pcSpecNode = node;
            } else if(node.getNodeName().equals(OR_TAG)) {
                pcSpecNode = node;
            }
        }

        //create subject node
        String pcSpecId = evrService.createPcSpec(parentId, parentLabel, pcSpec.isOr());

        //get actual entity nodes
        childNodes = getChildNodes(pcSpecNode);
        for(Node child : childNodes) {
            if(child.getNodeName().equals(ENTITY_TAG)) {
                EvrEntity evrEntity = parseEntity(pcSpecId, "pc_spec", child);
                pcSpec.addEntity(evrEntity);
            }
        }

        return pcSpec;
    }

    private EvrOpSpec parseOpSpec(String parentId, String parentLabel, Node opSpecNode) throws DatabaseException, ConfigurationException, SQLException, IOException, ClassNotFoundException {
        System.out.println("parsing op spec...");

        HashSet<String> ops = new HashSet<>();

        List<Node> childNodes = getChildNodes(opSpecNode);
        for(Node node : childNodes) {
            if(node.getNodeName().equals(OP_TAG)) {
                if(node.getTextContent().length() > 0) {
                    ops.add(node.getTextContent());
                }
            }
        }

        System.out.println("\tops: " + ops);

        //create opspec in db
        evrService.createOpSpec(parentId, parentLabel, ops);

        return new EvrOpSpec(ops);
    }

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

    private EvrProcess parseProcess(String parentId, Node processNode) throws InvalidEvrException, InvalidPropertyException, DatabaseException, ConfigurationException, InvalidEntityException, SQLException, IOException, ClassNotFoundException {
        System.out.println("parsing process...");

        EvrProcess evrProcess = new EvrProcess();

        String processId = evrService.createProcess(parentId, "subject");

        List<Node> childNodes = getChildNodes(processNode);
        if(childNodes.isEmpty()) {
            evrProcess = new EvrProcess(processNode.getTextContent());

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
            List<Property> propList = new ArrayList<>();
            if(properties != null) {
                String[] propArr = properties.getNodeValue().split(",");
                for(String prop : propArr) {
                    String[] pieces = prop.split("=");
                    propList.add(new Property(pieces[0], pieces[1]));
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
            System.out.println("\tproperties=" + propList);

            evrEntity = new EvrEntity(entityName, entityType, propList, bComp);

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
                case OP_SPEC_TAG:
                    EvrOpSpec evrOpSpec = parseOpSpec(denyActionId, "deny_action", node);
                    action.setOpSpec(evrOpSpec);
                    break;
                case TARGET_TAG:
                    EvrTarget evrTarget = parseTarget(denyActionId, "deny_action", node);
                    action.setTarget(evrTarget);
                    break;
            }
        }

        return action;
    }

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
                case OP_SPEC_TAG:
                    EvrOpSpec evrOpSpec = parseOpSpec(grantActionId, "grant_action", node);
                    action.setOpSpec(evrOpSpec);
                    break;
                case TARGET_TAG:
                    EvrTarget evrTarget = parseTarget(grantActionId, "grant_action", node);
                    action.setTarget(evrTarget);
                    break;
            }
        }

        return action;
    }

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