package ph.gov.santarosa.petcare.eligibility;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Decision Tree-Based Service Eligibility Classification (rule-based, not machine learning).
 *
 * Faithful port of assessEligibility() in the front end (src/services/eligibilityService.js).
 * Each criterion yields PASS, FAIL or REVIEW with the value used and a plain-language basis:
 *   NOT ELIGIBLE - at least one criterion FAILED
 *   FOR REVIEW   - no FAIL, but at least one criterion needs CVO verification
 *   ELIGIBLE     - every criterion PASSED
 *
 * Difference from the front end: the caller passes the criterion keys that are ENABLED (read from the
 * eligibility_criterion table), so the Administrator's Enable/Disable toggle now changes the outcome.
 * With every criterion enabled the output is identical to the JavaScript engine; EligibilityEngineParityTest
 * proves it on 144 generated cases.
 *
 * Pure Java on purpose: no Spring, no JPA, no clock. The reference date is passed in.
 */
public final class EligibilityEngine {

    @FunctionalInterface
    private interface Rule { CriterionResult evaluate(PetFacts pet, LocalDate today); }

    private static final DateTimeFormatter UI_DATE = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US);

    /** service -> (criterion key -> rule), each map in the front end's evaluation order. */
    private static final Map<String, Map<String, Rule>> RULES = new LinkedHashMap<>();

    static {
        Map<String, Rule> rabies = new LinkedHashMap<>();
        rabies.put("registered", (p, t) -> registered("registered", "Pet Registration", p));
        rabies.put("minAge", (p, t) -> {
            boolean pass = monthsBetween(p.dob(), t) >= 3;
            return new CriterionResult("minAge", "Minimum Applicable Age", ageString(p.dob(), t), pass(pass),
                    pass ? "Pet is at least 3 months old." : "Pet is younger than the minimum applicable age of 3 months.");
        });
        rabies.put("recentVax", (p, t) -> {
            Result r; String v;
            if (p.lastRabies() == null) { r = Result.PASS; v = "No prior record on file"; }
            else { r = monthsBetween(p.lastRabies(), t) >= 12 ? Result.PASS : Result.FAIL; v = fmtDate(p.lastRabies()); }
            return new CriterionResult("recentVax", "Recent Anti-Rabies Vaccination", v, r,
                    r == Result.PASS ? "No anti-rabies vaccination recorded within the past 12 months."
                                     : "A recent anti-rabies vaccination record exists within the configured restriction period.");
        });
        rabies.put("medical", (p, t) -> new CriterionResult("medical", "Medical Restriction", "Not on file", Result.REVIEW,
                "CVO Verification \u2014 no source confirms this rule; final medical clearance is verified by attending CVO staff at the time of service."));
        RULES.put(ServiceNames.ANTI_RABIES, rabies);

        Map<String, Rule> chip = new LinkedHashMap<>();
        chip.put("registered", (p, t) -> registered("registered", "Pet Registration", p));
        chip.put("minAge6", (p, t) -> {
            boolean pass = monthsBetween(p.dob(), t) >= 6;
            return new CriterionResult("minAge6", "Minimum Age (6 Months and Above)", ageString(p.dob(), t), pass(pass),
                    pass ? "Dog is 6 months old or above, per the CVO Microchipping campaign requirement."
                         : "Dog is younger than the required minimum age of 6 months.");
        });
        chip.put("noChip", (p, t) -> {
            boolean pass = !"Microchipped".equals(p.microchipStatus()) && isBlank(p.microchip());
            return new CriterionResult("noChip", "Existing Microchip", orElse(p.microchip(), "None on file"), pass(pass),
                    pass ? "No existing microchip number is on file." : "A microchip number is already recorded for this pet.");
        });
        chip.put("info", (p, t) -> {
            boolean ok = !isBlank(p.breed()) && !isBlank(p.color());
            return new CriterionResult("info", "Required Pet Information", ok ? "Complete" : "Incomplete",
                    ok ? Result.PASS : Result.REVIEW,
                    ok ? "Required pet information (breed, physical description) is complete."
                       : "Required pet information is incomplete and needs CVO verification.");
        });
        RULES.put(ServiceNames.MICROCHIPPING, chip);

        Map<String, Rule> kapon = new LinkedHashMap<>();
        kapon.put("registered", (p, t) -> registered("registered", "Pet Registered and Verified", p));
        kapon.put("ageRange", (p, t) -> {
            int m = monthsBetween(p.dob(), t);
            boolean pass = m >= 5 && m <= 48;
            return new CriterionResult("ageRange", "Age within CVO Kapon Range (5 Months \u2013 4 Years)", ageString(p.dob(), t), pass(pass),
                    pass ? "Dog's age falls within the 5-month to 4-year Kapon eligibility range."
                         : "Dog's age falls outside the 5-month to 4-year Kapon eligibility range.");
        });
        kapon.put("weight", (p, t) -> {
            Result r; String v;
            if (p.weightKg() == null) { r = Result.REVIEW; v = "Not on file"; }
            else { r = p.weightKg().compareTo(new BigDecimal("3")) >= 0 ? Result.PASS : Result.FAIL; v = plain(p.weightKg()) + " kg"; }
            return new CriterionResult("weight", "Weight Requirement Satisfied", v, r,
                    r == Result.REVIEW ? "Weight is not on file and requires CVO verification."
                    : r == Result.PASS ? "Recorded weight meets the sample CVO Kapon weight requirement (minimum 3 kg \u2014 verify exact figure against the official announcement)."
                    : "Recorded weight does not meet the sample CVO Kapon weight requirement (minimum 3 kg \u2014 verify exact figure against the official announcement).");
        });
        kapon.put("healthy", (p, t) -> {
            Result r = "Apparently Healthy".equals(p.healthStatus()) ? Result.PASS
                     : "With Known Condition".equals(p.healthStatus()) ? Result.FAIL : Result.REVIEW;
            return new CriterionResult("healthy", "Apparently Healthy", orElse(p.healthStatus(), "Not on file"), r,
                    r == Result.PASS ? "Pet profile records the dog as apparently healthy."
                    : r == Result.FAIL ? "Pet profile records a known health condition that requires veterinary clearance."
                    : "Health status is marked For Verification and requires a CVO physical exam.");
        });
        kapon.put("rabies", (p, t) -> {
            Result r; String v;
            if ("Updated".equals(p.rabiesStatus()) && p.lastRabies() != null) { r = Result.PASS; v = fmtDate(p.lastRabies()); }
            else if ("Not Updated".equals(p.rabiesStatus())) { r = Result.FAIL; v = "Not Updated"; }
            else { r = Result.REVIEW; v = "Unknown / not on file"; }
            return new CriterionResult("rabies", "Anti-Rabies Updated", v, r,
                    r == Result.PASS ? "Anti-rabies vaccination is on file and updated."
                    : r == Result.FAIL ? "Anti-rabies vaccination record is on file but is not updated."
                    : "Anti-rabies vaccination status is unknown and requires CVO verification.");
        });
        kapon.put("deworm", (p, t) -> {
            Result r; String v;
            if ("Updated".equals(p.dewormingStatus()) && p.lastDeworming() != null) { r = Result.PASS; v = fmtDate(p.lastDeworming()); }
            else if ("Not Updated".equals(p.dewormingStatus())) { r = Result.FAIL; v = "Not Updated"; }
            else { r = Result.REVIEW; v = "Unknown / not on file"; }
            return new CriterionResult("deworm", "Deworming Updated", v, r,
                    r == Result.PASS ? "Deworming record is on file and updated."
                    : r == Result.FAIL ? "Deworming record is on file but is not updated."
                    : "Deworming status is unknown and requires CVO verification. Deworming is tracked as a health/requirement record, not a standalone service.");
        });
        kapon.put("residency", (p, t) -> {
            Result r = "Verified at Barangay".equals(p.proofOfResidency()) ? Result.PASS
                     : "Not Submitted".equals(p.proofOfResidency()) ? Result.FAIL : Result.REVIEW;
            return new CriterionResult("residency", "Owner Proof of Residency Verified", orElse(p.proofOfResidency(), "Not on file"), r,
                    r == Result.PASS ? "Owner's proof of residency has been verified at the barangay."
                    : r == Result.FAIL ? "Owner has not submitted proof of residency."
                    : "Owner's proof of residency has been submitted but is still pending verification.");
        });
        RULES.put(ServiceNames.KAPON, kapon);
    }

    /** Criterion keys of a service in evaluation order (all enabled). */
    public List<String> defaultKeys(String service) {
        Map<String, Rule> rules = RULES.get(service);
        if (rules == null) throw new IllegalArgumentException("Unknown service: " + service);
        return new ArrayList<>(rules.keySet());
    }

    /** Assess with every criterion enabled (same as the front end). */
    public AssessmentResult assess(PetFacts pet, String service, LocalDate today) {
        return assess(pet, service, defaultKeys(service), today);
    }

    /**
     * @param enabledKeys criterion keys to evaluate, in order (from eligibility_criterion, enabled only)
     * @param today       reference date used for ages and the 12-month rabies interval
     */
    public AssessmentResult assess(PetFacts pet, String service, List<String> enabledKeys, LocalDate today) {
        Map<String, Rule> rules = RULES.get(service);
        if (rules == null) throw new IllegalArgumentException("Unknown service: " + service);

        List<CriterionResult> criteria = new ArrayList<>();
        for (String key : enabledKeys) {
            Rule rule = rules.get(key);
            if (rule != null) criteria.add(rule.evaluate(pet, today));
            else criteria.add(new CriterionResult(key, key, "Not evaluated", Result.REVIEW,
                    "No automatic rule exists for this criterion yet; it needs CVO verification."));
        }
        Classification c = classify(criteria);
        return new AssessmentResult(c == Classification.ELIGIBLE, c, List.copyOf(criteria), decisionPath(criteria, c), today);
    }

    /** NOT ELIGIBLE if any FAIL; else FOR REVIEW if any REVIEW (or nothing was assessed); else ELIGIBLE. */
    public static Classification classify(List<CriterionResult> criteria) {
        if (criteria.isEmpty()) return Classification.FOR_REVIEW;   // every criterion disabled: never auto-approve
        if (criteria.stream().anyMatch(c -> c.result() == Result.FAIL)) return Classification.NOT_ELIGIBLE;
        if (criteria.stream().anyMatch(c -> c.result() == Result.REVIEW)) return Classification.FOR_REVIEW;
        return Classification.ELIGIBLE;
    }

    public static List<DecisionNode> decisionPath(List<CriterionResult> criteria, Classification classification) {
        List<DecisionNode> path = new ArrayList<>();
        path.add(new DecisionNode("start", "START", "Eligibility Assessment Initiated", null, null));
        for (CriterionResult c : criteria) path.add(new DecisionNode("node", c.label(), c.value(), c.result(), c.basis()));
        path.add(new DecisionNode("final", "FINAL CLASSIFICATION", classification.label(), null, null));
        return List.copyOf(path);
    }

    /* ---------------- helpers (ports of formatting.js) ---------------- */

    private static CriterionResult registered(String key, String label, PetFacts p) {
        boolean pass = "Verified".equals(p.registrationStatus());
        return new CriterionResult(key, label, p.registrationStatus(), pass(pass),
                pass ? "Pet registration is verified." : "Pet registration is not yet verified by CVO staff.");
    }

    private static Result pass(boolean ok) { return ok ? Result.PASS : Result.FAIL; }

    /** Whole months from {@code d} to {@code ref}, minus one if the day-of-month has not been reached. */
    public static int monthsBetween(LocalDate d, LocalDate ref) {
        return (ref.getYear() - d.getYear()) * 12 + (ref.getMonthValue() - d.getMonthValue())
                + (ref.getDayOfMonth() >= d.getDayOfMonth() ? 0 : -1);
    }

    /** "Newborn", "7m", "1y", "1y 3m" - same strings as the UI. */
    public static String ageString(LocalDate dob, LocalDate ref) {
        int m = monthsBetween(dob, ref);
        if (m < 1) return "Newborn";
        if (m < 12) return m + "m";
        int y = m / 12, rem = m % 12;
        return rem != 0 ? y + "y " + rem + "m" : y + "y";
    }

    /** "Feb 10, 2026" (en-US short month). */
    public static String fmtDate(LocalDate d) { return d == null ? "\u2014" : UI_DATE.format(d); }

    /** 7.00 -> "7", 9.50 -> "9.5", 10 -> "10" (how JavaScript prints a number). */
    private static String plain(BigDecimal n) { return n.stripTrailingZeros().toPlainString(); }

    private static boolean isBlank(String s) { return s == null || s.isEmpty(); }
    private static String orElse(String s, String fallback) { return isBlank(s) ? fallback : s; }
}
