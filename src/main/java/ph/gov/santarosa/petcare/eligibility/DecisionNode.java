package ph.gov.santarosa.petcare.eligibility;

/** A node of the decision tree the UI draws. type is "start", "node" or "final". */
public record DecisionNode(String type, String label, String value, Result result, String basis) { }
