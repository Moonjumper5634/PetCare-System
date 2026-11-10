package ph.gov.santarosa.petcare.eligibility;

import java.math.BigDecimal;
import java.time.LocalDate;

/** The pet facts the decision tree reads. Plain data, so the engine never touches JPA. */
public record PetFacts(String registrationStatus, String breed, String color, LocalDate dob, BigDecimal weightKg,
                       String healthStatus, String rabiesStatus, LocalDate lastRabies, String dewormingStatus,
                       LocalDate lastDeworming, String proofOfResidency, String microchip, String microchipStatus) { }
