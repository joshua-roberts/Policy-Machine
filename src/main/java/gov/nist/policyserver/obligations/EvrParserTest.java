package gov.nist.policyserver.obligations;

import gov.nist.policyserver.obligations.exceptions.EvrRuleDoesNotExist;
import gov.nist.policyserver.obligations.model.EvrEntity;
import gov.nist.policyserver.obligations.model.EvrRule;
import gov.nist.policyserver.obligations.model.script.EvrScript;
import gov.nist.policyserver.obligations.model.script.rule.event.*;
import gov.nist.policyserver.obligations.model.script.rule.event.time.EvrEvent;
import gov.nist.policyserver.obligations.model.script.rule.event.time.EvrTime;
import gov.nist.policyserver.obligations.model.script.rule.event.time.EvrTimeElement;
import gov.nist.policyserver.exceptions.InvalidPropertyException;
import gov.nist.policyserver.model.graph.nodes.Property;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.*;

import static org.junit.Assert.*;


public class EvrParserTest {

    private static EvrScript script;

    @BeforeClass
    public static void init() {
        try {
            File inputFile = new File("scripts/test.obligations");

            EvrParser evrParser = new EvrParser(inputFile);
            script = evrParser.parse();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test1() {
        try {
            EvrRule rule = script.getRule("test1");

            EvrEvent event = rule.getEvent();

            //test subject
            EvrSubject subject = event.getSubject();
            List<EvrEntity> entities = subject.getEntities();
            assertTrue("The subject should be one specific user", entities.size() == 1);

            EvrEntity evrEntity = entities.get(0);
            assertTrue("Subject should be a node", evrEntity.isNode());
            assertEquals("Subject entity name does not match", "testUser", evrEntity.getName());
            assertEquals("Subject entity type does not match", "U",  evrEntity.getType());

            //test pc spec
            EvrPolicies policies = event.getPolicies();
            List<EvrEntity> pcs = policies.getPcs();
            assertTrue("There should only be one policy class in this policies", pcs.size() == 1);

            evrEntity = pcs.get(0);
            assertTrue("policies should be a node", evrEntity.isNode());
            assertEquals("policies entity name does not match", "RBAC", evrEntity.getName());
            assertEquals("policies entity type does not match", "PC", evrEntity.getType());

            //test op spec
            EvrOpertations operations = event.getOperations();
            HashSet<String> ops = operations.getOps();
            assertTrue("There should only be one op in the operations", ops.size() == 1);

            String op = ops.iterator().next();
            assertEquals("operations op name does not match", "Object read", op);

            //test target
            EvrTarget target = event.getTarget();

            //test target entity
            EvrEntity targetEntity = target.getEntity();
            assertNotNull("The target entity should not be null", targetEntity);
            assertTrue("Target entity should be a node", targetEntity.isNode());
            assertEquals(targetEntity.getName(), "object123");
            assertEquals(targetEntity.getType(), "O");

            try {
                List<Property> expected = new ArrayList<>(Arrays.asList(new Property("prop1", "value1"), new Property("prop2", "value2")));
                List<Property> actual = targetEntity.getProperties();
                assertEquals("Target entity does not have the correct properties", expected, actual);
            }
            catch (InvalidPropertyException e) {
                e.printStackTrace();
                fail(e.getMessage());
            }

            //test target container
            List<EvrEntity> targetContainers = target.getContainers();
            assertTrue("There shouls only be one target container", targetContainers.size() == 1);

            EvrEntity entity = targetContainers.get(0);
            assertTrue("Target entity should be a node", entity.isNode());
            assertEquals(entity.getName(), "Oattr123");
            assertEquals(entity.getType(), "OA");

            try {
                List<Property> expected = new ArrayList<>(Collections.singletonList(new Property("prop1", "value1")));
                List<Property> actual = entity.getProperties();
                assertEquals("Target container does not have the correct properties", expected, actual);
            }
            catch (InvalidPropertyException e) {
                e.printStackTrace();
                fail(e.getMessage());
            }
        }
        catch (EvrRuleDoesNotExist evrRuleDoesNotExist) {
            evrRuleDoesNotExist.printStackTrace();
            fail(evrRuleDoesNotExist.getMessage());
        }
    }

    @Test
    public void test2() {
        try {
            EvrRule rule = script.getRule("test2");

            EvrEvent event = rule.getEvent();

            //test subject
            EvrSubject subject = event.getSubject();
            assertTrue("The subject should be any user", subject.isAny());

            //test pc spec
            EvrPolicies policies = event.getPolicies();
            assertTrue("The policies should be any pc", policies.isAny());

            //test op spec
            EvrOpertations operations = event.getOperations();
            assertTrue("operations should be any op", operations.isAny());

            //test target
            EvrTarget target = event.getTarget();

            //test target entity
            EvrEntity targetEntity = target.getEntity();
            assertNotNull("The target entity should not be null", targetEntity);
            assertTrue("Target entity should be a node", targetEntity.isNode());
            assertEquals(targetEntity.getName(), null);
            assertEquals(targetEntity.getType(), null);

            try {
                List<Property> expected = new ArrayList<>(Arrays.asList(new Property("prop1", "value1"), new Property("prop2", "value2")));
                List<Property> actual = targetEntity.getProperties();
                assertEquals("Target entity does not have the correct properties", expected, actual);
            }
            catch (InvalidPropertyException e) {
                e.printStackTrace();
                fail(e.getMessage());
            }

            //test target container
            List<EvrEntity> targetContainers = target.getContainers();
            assertTrue("There shouls only be one target container", targetContainers.size() == 1);

            EvrEntity entity = targetContainers.get(0);
            assertTrue("Target entity should be a class", entity.isClass());
            assertEquals(entity.getName(), "Object");
            assertEquals(entity.getType(), "class");
        }
        catch (EvrRuleDoesNotExist evrRuleDoesNotExist) {
            evrRuleDoesNotExist.printStackTrace();
            fail(evrRuleDoesNotExist.getMessage());
        }
    }

    @Test
    public void test3() {
        try{
            EvrRule rule = script.getRule("test3");

            EvrEvent event = rule.getEvent();

            //test subject
            EvrSubject subject = event.getSubject();
            List<EvrEntity> entities = subject.getEntities();
            assertTrue("The subject should be one user attribute", entities.size() == 1);

            EvrEntity evrEntity = entities.get(0);
            assertTrue("Subject should be a node", evrEntity.isNode());
            assertEquals("Subject entity name does not match", "ua1", evrEntity.getName());
            assertEquals("Subject entity type does not match", "UA",  evrEntity.getType());

            //test pc spec
            EvrPolicies policies = event.getPolicies();
            List<EvrEntity> pcs = policies.getPcs();
            assertTrue("There should only be 4 policy classes in this policies", pcs.size() == 4);
            assertTrue("The policies should be or", policies.isOr());

            for(EvrEntity entity : pcs) {
                assertTrue("policies should be a node", entity.isNode());
                assertNotNull("policies pc name should not be null", entity.getName());
                assertEquals("policies entity type does not match", "PC", entity.getType());
            }

            //test op spec
            EvrOpertations operations = event.getOperations();
            HashSet<String> ops = operations.getOps();
            assertTrue("There should only be two ops in the operations", ops.size() == 2);

            String op = ops.iterator().next();
            assertEquals("operations op name does not match", "Object read", op);
            op = ops.iterator().next();
            assertEquals("operations op name does not match", "Object write", op);

            //test target
            EvrTarget target = event.getTarget();

            //test target entity
            EvrEntity targetEntity = target.getEntity();
            assertNotNull("The target entity should not be null", targetEntity);
            assertTrue("Target entity should be any entity", targetEntity.isAny());
            assertTrue("Target entity should be any entity", target.isAnyEntity());

            //test target container
            List<EvrEntity> targetContainers = target.getContainers();
            assertTrue("There should be two target entities", targetContainers.size() == 2);
            for(EvrEntity entity : targetContainers) {
                assertNotNull("The target container name should not be null", entity.getName());
            }
        }
        catch (EvrRuleDoesNotExist evrRuleDoesNotExist) {
            evrRuleDoesNotExist.printStackTrace();
            fail(evrRuleDoesNotExist.getMessage());
        }
    }

    @Test
    public void test4() {
        try{
            EvrRule rule = script.getRule("test4");

            EvrEvent event = rule.getEvent();

            //test subject
            EvrSubject subject = event.getSubject();
            List<EvrEntity> entities = subject.getEntities();
            assertTrue("The subject should be 4 entities", entities.size() == 4);

            for(EvrEntity entity : entities) {
                assertTrue("Subject entity name should not be null", entity.getName() != null);
            }

            //test pc spec
            EvrPolicies policies = event.getPolicies();
            List<EvrEntity> pcs = policies.getPcs();
            assertTrue("There should only be 4 policy classes in this policies", pcs.size() == 4);
            assertFalse("The policies should be or", policies.isOr());

            for(EvrEntity entity : pcs) {
                assertTrue("policies should be a node", entity.isNode());
                assertNotNull("policies pc name should not be null", entity.getName());
                assertEquals("policies entity type does not match", "PC", entity.getType());
            }

            //test op spec
            //test op spec
            EvrOpertations operations = event.getOperations();
            assertTrue("operations should be any op", operations.isAny());

            //test target
            EvrTarget target = event.getTarget();

            //test target entity
            EvrEntity targetEntity = target.getEntity();
            assertNotNull("The target entity should not be null", targetEntity);
            assertTrue("Target entity should be any entity", targetEntity.isAny());
            assertTrue("Target entity should be any entity", target.isAnyEntity());

            //test target container
            List<EvrEntity> targetContainers = target.getContainers();
            assertTrue("There should be two target entities", targetContainers.size() == 2);
            for(EvrEntity entity : targetContainers) {
                assertNotNull("The target container name should not be null", entity.getName());
                assertEquals("The target container should be a class", "class", entity.getType());
            }
        }
        catch (EvrRuleDoesNotExist evrRuleDoesNotExist) {
            evrRuleDoesNotExist.printStackTrace();
            fail(evrRuleDoesNotExist.getMessage());
        }
    }

    @Test
    public void test5() {
        try{
            EvrRule rule = script.getRule("test5");

            EvrEvent event = rule.getEvent();

            //test subject
            EvrSubject subject = event.getSubject();
            List<EvrEntity> entities = subject.getEntities();
            assertTrue("The subject should a process from a function", entities.size() == 1);

            EvrEntity subjectEntity = entities.get(0);
            assertTrue(subjectEntity.isProcess());
            assertTrue(subjectEntity.getProcess().isFunction());
            assertEquals("current_process", subjectEntity.getProcess().getFunction().getFunctionName());

            //test pc spec
            EvrPolicies policies = event.getPolicies();
            List<EvrEntity> pcs = policies.getPcs();
            assertTrue("There should only be 4 policy classes in this policies", pcs.size() == 4);
            assertTrue("The policies should be or", policies.isOr());

            for(EvrEntity entity : pcs) {
                assertTrue("policies should be a node", entity.isNode());
                assertNotNull("policies pc name should not be null", entity.getName());
                assertEquals("policies entity type does not match", "PC", entity.getType());
            }

            //test op spec
            //test op spec
            EvrOpertations operations = event.getOperations();
            assertTrue("operations should be any op", operations.isAny());

            //test target
            EvrTarget target = event.getTarget();

            //test target entity
            EvrEntity targetEntity = target.getEntity();
            assertNotNull("The target entity should not be null", targetEntity);
            assertTrue("Target entity should be any entity", targetEntity.isAny());
            assertTrue("Target entity should be any entity", target.isAnyEntity());

            //test target container
            List<EvrEntity> targetContainers = target.getContainers();
            assertTrue("There should be two target entities", targetContainers.size() == 2);
            for(EvrEntity entity : targetContainers) {
                assertNotNull("The target container name should not be null", entity.getName());
                assertEquals("The target container should be an OA", "OA", entity.getType());
            }
        }
        catch (EvrRuleDoesNotExist evrRuleDoesNotExist) {
            evrRuleDoesNotExist.printStackTrace();
            fail(evrRuleDoesNotExist.getMessage());
        }
    }

    @Test
    public void test6() {
        try{
            EvrRule rule = script.getRule("Time Rule");

            EvrEvent event = rule.getEvent();

            //test time
            EvrTime time = event.getTime();
            EvrTimeElement element = time.getDow();
            assertEquals(5, element.getValues().size());

            element = time.getDay();
            assertTrue(element.getValues().isEmpty());

            element = time.getMonth();
            assertTrue(element.getValues().isEmpty());

            element = time.getYear();
            assertEquals(1, element.getValues().size());

            element = time.getHour();
            assertTrue(element.isRange());
            assertEquals(9, element.getRange().getStart());
            assertEquals(17, element.getRange().getEnd());
        }
        catch (EvrRuleDoesNotExist evrRuleDoesNotExist) {
            evrRuleDoesNotExist.printStackTrace();
            fail(evrRuleDoesNotExist.getMessage());
        }
    }
}