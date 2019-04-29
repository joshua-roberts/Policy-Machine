package gov.nist.csd.pm.pap.obligations;

import gov.nist.csd.pm.common.model.obligations.Obligation;
import gov.nist.csd.pm.common.model.obligations.PolicyClass;
import gov.nist.csd.pm.common.model.obligations.Subject;
import gov.nist.csd.pm.common.model.obligations.Target;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class EvrParserTest {

    @Nested
    @DisplayName("label tests")
    class LabelTests {

        @Test
        @DisplayName("err: label as array")
        void Test1() throws EvrException {
            String s = "" +
                    "label:\n" +
                    "  - newObligation";
            Yaml yaml = new Yaml();
            Map<Object, Object> load = yaml.load(s);
            assertThrows(EvrException.class, () -> EvrParser.parse(load));
        }

        @Test
        @DisplayName("label as string")
        void Test2() throws EvrException {
            String s = "" +
                    "label: newObligation";
            Yaml yaml = new Yaml();
            Map<Object, Object> load = yaml.load(s);
            Obligation obligation = EvrParser.parse(load);
            assertEquals("newObligation", obligation.getLabel());
        }
    }

    @Nested
    @DisplayName("event subject tests")
    class SubjectTests {

        @Test
        @DisplayName("user with name")
        void Test1() throws EvrException {
            Map<String, Object> map = new HashMap<>();
            map.put("user", "USER_NAME");
            Subject subject = EvrParser.parseSubject(map);
            map.clear();
            assertEquals("USER_NAME", subject.getUser());
        }

        @Test
        @DisplayName("any user of array of users")
        void Test2() throws EvrException {
            Map<String, Object> map = new HashMap<>();
            map.put("anyUser", Arrays.asList("user 1", "user 2"));
            Subject subject = EvrParser.parseSubject(map);
            map.clear();
            assertEquals(Arrays.asList("user 1", "user 2"), subject.getAnyUser());
        }

        @Test
        @DisplayName("any user")
        void Test3() throws EvrException {
            Map<String, Object> map = new HashMap<>();
            map.put("anyUser", new ArrayList<>());
            Subject subject = EvrParser.parseSubject(map);
            map.clear();
            assertEquals(new ArrayList<>(), subject.getAnyUser());
        }

        @Test
        @DisplayName("process with ID")
        void Test4() throws EvrException {
            Map<String, Object> map = new HashMap<>();
            map.put("process", "PROCESS");
            Subject subject = EvrParser.parseSubject(map);
            map.clear();
            assertEquals("PROCESS", subject.getProcess().getValue());
        }

    }

    @Nested
    @DisplayName("event policy class tests")
    class PolicyClassTests {
        @Test
        void TestParsePolicyClass() throws EvrException {
            Map<Object, Object> map = new HashMap<>();
            map.put("anyOf", Arrays.asList("pc 1", "pc 2"));
            PolicyClass policyClass = EvrParser.parsePolicyClass(map);
            assertEquals(Arrays.asList("pc 1", "pc 2"), policyClass.getAnyOf());

            map = new HashMap<>();
            map.put("eachOf", Arrays.asList("pc 1", "pc 2"));
            policyClass = EvrParser.parsePolicyClass(map);
            assertEquals(Arrays.asList("pc 1", "pc 2"), policyClass.getEachOf());

            map = new HashMap<>();
            policyClass = EvrParser.parsePolicyClass(map);
            assertTrue(policyClass.getAnyOf().isEmpty());
            assertTrue(policyClass.getEachOf().isEmpty());
        }
    }

    @Nested
    @DisplayName("event operations tests")
    class OperationsTests {
        @Test
        void TestParseOperations() throws EvrException {
            List<String> ops = EvrParser.parseOperations(Arrays.asList("read", "write"));
            assertEquals(Arrays.asList("read", "write"), ops);
        }
    }

    @Nested
    @DisplayName("event target tests")
    class TargetTests {
        @Test
        void TestParseTarget() throws EvrException {
            List elements = Arrays.asList("el1", "el2");
            Target target = EvrParser.parseTarget(elements);
            assertEquals(elements, target.getPolicyElements());

            target = EvrParser.parseTarget(null);
            assertTrue(target.getContainers().isEmpty());
            assertTrue(target.getPolicyElements().isEmpty());

            Map<Object, Object> map = new HashMap<>();
            map.put("containers", Arrays.asList("cont 1", "cont 2"));
            target = EvrParser.parseTarget(map);
            assertEquals(Arrays.asList("cont 1", "cont 2"), target.getContainers());
        }
    }
}