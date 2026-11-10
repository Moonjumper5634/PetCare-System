package ph.gov.santarosa.petcare.service;

import java.util.List;

/** The exact value sets the front end uses. The server accepts these and nothing else. */
public final class Values {
    public static final String OWNER = "owner", STAFF = "staff", ADMIN = "admin";

    public static final List<String> SEX = List.of("Male", "Female");
    public static final List<String> HEALTH = List.of("Apparently Healthy", "With Known Condition", "For Verification");
    public static final List<String> UPDATE_STATUS = List.of("Updated", "Not Updated", "Unknown");
    public static final List<String> MICROCHIP_STATUS = List.of("Not Microchipped", "Microchipped");
    public static final List<String> RESIDENCY = List.of("Verified at Barangay", "Submitted, Pending Verification", "Not Submitted");
    public static final List<String> HOUSING = List.of("Owned House", "Rented House/Apartment", "Condominium", "Living with Family");
    public static final List<String> RECOMMENDATION = List.of("Recommended", "Not Recommended", "Further Review");
    /** A request in one of these states is still open (not rejected, not completed). */
    public static final List<String> OPEN_REQUEST = List.of("Submitted", "For Review", "Approved");

    private Values() { }
}
