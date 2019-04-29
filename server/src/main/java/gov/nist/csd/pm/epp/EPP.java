package gov.nist.csd.pm.epp;

import gov.nist.csd.pm.common.model.obligations.*;
import gov.nist.csd.pm.graph.model.nodes.Node;
import gov.nist.csd.pm.pap.PAP;
import gov.nist.csd.pm.pdp.PDP;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class EPP {

    private static EPP epp;

    public static void initPAP(PAP pap) {
        if(epp == null) {
            epp = new EPP();
        }
        epp.setPAP(pap);
    }

    public static void initPDP(PDP pdp) {
        if(epp == null) {
            epp = new EPP();
        }
        epp.setPDP(pdp);
    }

    public static EPP getEPP() {
        if (epp == null) {
            throw new IllegalStateException("PDP is not initialized.  Initialize the PDP with PDP.init(...).");
        }

        return epp;
    }

    private PAP pap;
    private PDP pdp;

    public EPP(){}

    public EPP(PAP pap, PDP pdp) {
        this.pap = pap;
        this.pdp = pdp;
    }

    public PAP getPAP() {
        return pap;
    }

    public void setPAP(PAP pap) {
        this.pap = pap;
    }

    public PDP getPDP() {
        return pdp;
    }

    public void setPDP(PDP pdp) {
        this.pdp = pdp;
    }

    /*public void processEvent(EventMatcher matcher, EventProcessor processor) {
        List<Obligation> obligations = getPAP().getObligationsPAP().getAll();
        for(Obligation obligation : obligations) {
            if(!matcher.matches(obligation)) {
                continue;
            }

            processor.process();
        }
    }*/

    /*public void processEvent(String event, Subject subject, PolicyClass policyClass, gov.nist.csd.pm.graph.model.nodes.Node target) {
        //get all rules with the same event
        List<Rule> rules = getRules(event);
        for(Rule rule : rules) {
            Event evrEvent = rule.getEvent();
            if(eventMatches(evrEvent, subject, policyClass, target)) {
                *//*EvrResponse response = rule.getResponse();
                EvrCondition condition = response.getCondition();
                if(checkCondition(condition)) {
                    this.curSubjects = procSubject.getEntities();

                    List<EvrAction> actions = response.getActions();
                    doActions(actions);
                }*//*
            }
        }
    }

    public List<Rule> getRules(String event) {
        List<Rule> retRules = new ArrayList<>();
        for(Obligation obligation : getPAP().getObligationsPAP().getAll()) {
            if(obligation.isEnabled()) {
                List<Rule> rules = obligation.getRules();
                for (Rule rule : rules) {
                    Event ruleEvent = rule.getEvent();

                    //check the event matches
                    List<String> operations = ruleEvent.getOperations();
                    if(operations == null || operations.contains(event)) {
                        retRules.add(rule);
                    }
                }
            }
        }

        return retRules;
    }

    private boolean eventMatches(Event event, Subject subject, String policyClass, Node target) {
        //check subject
        Subject eventSubject = event.getSubject();
        PolicyClass eventPc = event.getPolicyClass();
        EvrNode eventTarget = event.getTarget();

        return subjectMatches(subject, eventSubject) &&
                pcMatches(policyClass, eventPc) &&
                targetMatches(target, eventTarget);
    }

    private boolean subjectMatches(Subject procSubject, Subject evrSubject) {
        // check any user
        if((evrSubject.getAnyUser() == null && evrSubject.getUser() == null && evrSubject.getProcess() == null) ||
                evrSubject.getAnyUser().isEmpty() ||
                evrSubject.getAnyUser().contains(procSubject.getUser())) {
            return true;
        }

        // check user
        EvrNode procUser = procSubject.getUser();
        EvrNode evrUser = evrSubject.getUser();
        if(procUser != null && procUser.equals(evrUser)) {
            return true;
        }

        // check process
        return procSubject.getProcess().equals(evrSubject.getProcess());
    }

    private boolean pcMatches(String procPc, PolicyClass evrPc) {
        *//*if(evrPc == null) {
            return true;
        }

        if((evrPc.getAnyOf() == null || evrPc.getAnyOf().isEmpty()) &&
                evrPc.getEachOf() == null || evrPc.getEachOf().isEmpty()){
            return true;
        }

        *//*
        return true;
    }

    private boolean targetMatches(Node procTarget, Event event) {
        EvrNode target = event.getTarget();
        Containers containers = event.getContainers();

        Node evrTargetNode = null;

        if(target.getAny() == null) {
            // check that the nodes match
            HashSet<gov.nist.csd.pm.graph.model.nodes.Node> nodes =
                    nodeService.getNodes(null, evrTargetEntity.getName(), evrTargetEntity.getType(), evrTargetEntity.getProperties());
            if(nodes.size() != 1) {
                throw new InvalidEvrException("Target entity (" + evrTargetEntity.getName() + ") can only be one node");
            }

            evrTargetNode = nodes.iterator().next();
            if(!procTarget.equals(evrTargetNode)) {
                return false;
            }
        }

        if(!evrTarget.isAnyContainer()) {
            //make sure the entity is in at least one container
            for(EvrEntity evrEntity : evrTargetContainers) {
                HashSet<gov.nist.csd.pm.graph.model.nodes.Node> nodes = nodeService.getNodes(null, evrEntity.getName(), evrEntity.getType(), evrEntity.getProperties());
                for(gov.nist.csd.pm.graph.model.nodes.Node node : nodes) {
                    if(evrTargetNode != null) {
                        HashSet<gov.nist.csd.pm.graph.model.nodes.Node> ascendants = assignmentService.getAscendants(evrTargetNode.getId());
                        ascendants.add(evrTargetNode);
                        if(ascendants.contains(node)) {
                            return true;
                        }
                    } else {
                        //any object, check the processed target is in the container
                        HashSet<gov.nist.csd.pm.graph.model.nodes.Node> ascendants = assignmentService.getAscendants(node.getId());
                        ascendants.add(node);
                        if(ascendants.contains(procTarget)) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    private boolean checkProcess(EvrEntity procEntity, EvrEntity evrEntity) {
        return procEntity.isProcess() &&
                evrEntity.isProcess() &&
                procEntity.getProcess().equals(evrEntity.getProcess());
    }*/
}
