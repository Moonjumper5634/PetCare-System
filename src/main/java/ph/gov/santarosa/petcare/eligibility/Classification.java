package ph.gov.santarosa.petcare.eligibility;

import com.fasterxml.jackson.annotation.JsonValue;

/** Overall system recommendation. Serialised with the front end's labels ("FOR REVIEW"). */
public enum Classification {
    ELIGIBLE("ELIGIBLE"), FOR_REVIEW("FOR REVIEW"), NOT_ELIGIBLE("NOT ELIGIBLE");

    private final String label;
    Classification(String label) { this.label = label; }

    @JsonValue
    public String label() { return label; }

    public static Classification fromLabel(String label) {
        for (Classification c : values()) if (c.label.equals(label)) return c;
        throw new IllegalArgumentException("Unknown classification: " + label);
    }
}
