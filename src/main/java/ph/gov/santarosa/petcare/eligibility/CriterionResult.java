package ph.gov.santarosa.petcare.eligibility;

/** One evaluated criterion: what was checked, the value found, PASS/FAIL/REVIEW and the plain-language basis. */
public record CriterionResult(String key, String label, String value, Result result, String basis) { }
