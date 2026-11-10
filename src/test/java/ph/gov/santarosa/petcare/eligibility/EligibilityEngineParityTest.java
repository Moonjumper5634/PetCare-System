package ph.gov.santarosa.petcare.eligibility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

/**
 * Proves the Java engine gives the same answer as the front end's JavaScript decision tree.
 * eligibility-golden.json is produced by tools/generate-seed.js by running the JS engine itself on the seeded pets
 * plus edge cases (age boundaries, 12-month rabies interval, missing weight, every health/residency value, ...).
 * Compared per case: classification, eligible flag, and every criterion's label, value, result and basis.
 */
class EligibilityEngineParityTest {

    private static final EligibilityEngine ENGINE = new EligibilityEngine();

    @TestFactory
    List<DynamicTest> javaEngineMatchesJavaScriptEngine() throws Exception {
        JsonNode root;
        try (InputStream in = getClass().getResourceAsStream("/eligibility-golden.json")) {
            root = new ObjectMapper().readTree(in);
        }
        LocalDate today = LocalDate.parse(root.get("referenceDate").asText());
        List<DynamicTest> tests = new ArrayList<>();
        for (JsonNode c : root.get("cases")) {
            String name = c.get("case").asText() + " / " + c.get("service").asText();
            tests.add(DynamicTest.dynamicTest(name, () -> check(c, today)));
        }
        assertFalse(tests.isEmpty());
        return tests;
    }

    private static void check(JsonNode c, LocalDate today) {
        AssessmentResult actual = ENGINE.assess(facts(c.get("pet")), c.get("service").asText(), today);
        JsonNode expect = c.get("expect");
        assertEquals(expect.get("classification").asText(), actual.classification().label(), "classification");
        assertEquals(expect.get("eligible").asBoolean(), actual.eligible(), "eligible flag");
        JsonNode crit = expect.get("criteria");
        assertEquals(crit.size(), actual.criteria().size(), "number of criteria");
        for (int i = 0; i < crit.size(); i++) {
            CriterionResult a = actual.criteria().get(i);
            JsonNode e = crit.get(i);
            assertEquals(e.get("label").asText(), a.label(), "label #" + i);
            assertEquals(e.get("value").asText(), a.value(), "value of " + a.label());
            assertEquals(e.get("result").asText(), a.result().name(), "result of " + a.label());
            assertEquals(e.get("basis").asText(), a.basis(), "basis of " + a.label());
        }
        assertEquals(crit.size() + 2, actual.decisionPath().size(), "decision path = START + criteria + FINAL");
    }

    private static PetFacts facts(JsonNode p) {
        return new PetFacts(str(p, "registrationStatus"), str(p, "breed"), str(p, "color"), date(p, "dob"),
                p.hasNonNull("weight") ? new BigDecimal(p.get("weight").asText()) : null,
                str(p, "healthStatus"), str(p, "rabiesStatus"), date(p, "lastRabies"), str(p, "dewormingStatus"),
                date(p, "lastDeworming"), str(p, "proofOfResidency"), str(p, "microchip"), str(p, "microchipStatus"));
    }

    private static String str(JsonNode n, String f) { return n.hasNonNull(f) ? n.get(f).asText() : null; }
    private static LocalDate date(JsonNode n, String f) { return n.hasNonNull(f) ? LocalDate.parse(n.get(f).asText()) : null; }
}
