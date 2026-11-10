package ph.gov.santarosa.petcare.support;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import ph.gov.santarosa.petcare.domain.*;
import ph.gov.santarosa.petcare.eligibility.EligibilityService;
import ph.gov.santarosa.petcare.repository.*;
import ph.gov.santarosa.petcare.service.*;
import org.springframework.security.crypto.password.PasswordEncoder;

/** Wires the real services to in-memory repositories and loads a small, known data set. */
public final class Kit {

    public static final LocalDate TODAY = LocalDate.of(2026, 8, 17);
    private static final ZoneId MANILA = ZoneId.of("Asia/Manila");

    /** A clock the test can move, to check lockout and code expiry. */
    public static final class MutableClock extends Clock {
        private Instant now;
        public MutableClock(Instant start) { this.now = start; }
        public void advanceMinutes(long m) { now = now.plusSeconds(m * 60); }
        @Override public ZoneId getZone() { return MANILA; }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return now; }
    }

    /** Not real bcrypt (that library is not needed to test the rules): a reversible marker. */
    public static final class FakeEncoder implements PasswordEncoder {
        @Override public String encode(CharSequence raw) { return "enc:" + raw; }
        @Override public boolean matches(CharSequence raw, String encoded) { return encoded != null && encoded.equals("enc:" + raw); }
    }

    public static final class CapturingSender implements CodeSender {
        public final List<String> sent = new ArrayList<>();
        public String lastCode;
        @Override public void send(AppUser user, String purpose, String code) { sent.add(purpose + ":" + user.getEmail()); lastCode = code; }
    }

    public final AppUserRepository users = FakeRepo.create(AppUserRepository.class, AppUser.class);
    public final PetRepository pets = FakeRepo.create(PetRepository.class, Pet.class);
    public final PetRegistrationRepository registrations = FakeRepo.create(PetRegistrationRepository.class, PetRegistration.class);
    public final ServiceRequestRepository requests = FakeRepo.create(ServiceRequestRepository.class, ServiceRequest.class);
    public final EligibilityAssessmentRepository assessments = FakeRepo.create(EligibilityAssessmentRepository.class, EligibilityAssessment.class);
    public final EligibilityAssessmentCriterionRepository assessmentCriteria = FakeRepo.create(EligibilityAssessmentCriterionRepository.class, EligibilityAssessmentCriterion.class);
    public final ServiceRecordRepository records = FakeRepo.create(ServiceRecordRepository.class, ServiceRecord.class);
    public final ShelterPetRepository shelterPets = FakeRepo.create(ShelterPetRepository.class, ShelterPet.class);
    public final AdoptionApplicationRepository adoptions = FakeRepo.create(AdoptionApplicationRepository.class, AdoptionApplication.class);
    public final AdoptionInterviewRepository interviews = FakeRepo.create(AdoptionInterviewRepository.class, AdoptionInterview.class);
    public final NotificationRepository notifications = FakeRepo.create(NotificationRepository.class, Notification.class);
    public final AuditLogRepository audit = FakeRepo.create(AuditLogRepository.class, AuditLog.class);
    public final EligibilityCriterionRepository criteria = FakeRepo.create(EligibilityCriterionRepository.class, EligibilityCriterion.class);
    public final VeterinaryServiceRepository services = FakeRepo.create(VeterinaryServiceRepository.class, VeterinaryService.class);
    public final IdSequenceRepository sequences = FakeRepo.create(IdSequenceRepository.class, IdSequence.class);

    public final Clock appClock = Clock.fixed(TODAY.atStartOfDay(MANILA).toInstant(), MANILA);
    public final MutableClock securityClock = new MutableClock(LocalDateTime.of(2026, 8, 17, 9, 0).atZone(MANILA).toInstant());
    public final PasswordEncoder encoder = new FakeEncoder();
    public final CapturingSender sender = new CapturingSender();

    public final IdGenerator ids = new IdGenerator(sequences);
    public final Notifier notifier = new Notifier(notifications, audit, ids, appClock);
    public final EligibilityService eligibility = new EligibilityService(pets, criteria, services, appClock);
    public final PetWorkflowService petFlow = new PetWorkflowService(pets, registrations, users, ids, notifier, appClock);
    public final RequestWorkflowService requestFlow = new RequestWorkflowService(requests, pets, services, assessments,
            assessmentCriteria, records, eligibility, ids, notifier, appClock);
    public final AdoptionWorkflowService adoptionFlow = new AdoptionWorkflowService(adoptions, interviews, shelterPets, ids, notifier, appClock);
    public final AuthService authFlow = new AuthService(users, encoder, ids, notifier, sender, securityClock, true);
    public final AdminService adminFlow = new AdminService(users, criteria, encoder, ids, notifier, authFlow);
    public final StateAssembler assembler = new StateAssembler(users, pets, registrations, requests, assessments, assessmentCriteria,
            records, adoptions, interviews, shelterPets, notifications, audit, criteria, services, appClock);

    // The cast of characters
    public AppUser maria, juan, rowena, jose;
    public Pet buddy, otherDog, pendingDog;
    public ShelterPet luna;

    public Kit() {
        for (String[] s : new String[][] { { "user", "U-", "5000" }, { "pet", "P-", "2000" }, { "reg", "REG-", "2000" },
                { "req", "REQ-", "4000" }, { "adopt", "ADOPT-", "6000" }, { "notif", "N-", "1" }, { "audit", "A-", "1" } }) {
            IdSequence q = new IdSequence();
            q.setKind(s[0]); q.setPrefix(s[1]); q.setNextValue(Long.parseLong(s[2]));
            sequences.save(q);
        }
        String[][] svc = { { "Anti-Rabies Vaccination", "registered,minAge,recentVax,medical" }, { "Microchipping", "registered,minAge6,noChip,info" },
                { "Kapon / Spay-Neuter", "registered,ageRange,weight,healthy,rabies,deworm,residency" } };
        int order = 1;
        for (String[] s : svc) {
            VeterinaryService v = new VeterinaryService(); v.setName(s[0]); v.setSortOrder(order++); services.save(v);
            int i = 1;
            for (String key : s[1].split(",")) {
                EligibilityCriterion c = new EligibilityCriterion();
                c.setServiceName(s[0]); c.setCriterionKey(key); c.setLabel(key); c.setDescription("d"); c.setEnabled(true); c.setSortOrder(i++);
                criteria.save(c);
            }
        }
        maria = user("U-1001", "owner", "Maria Santos", "maria@test.ph", "Resident#2026");
        juan = user("U-1002", "owner", "Juan Dela Cruz", "juan@test.ph", "Resident#2026");
        rowena = user("U-2001", "staff", "Dr. Rowena Tan", "rowena@test.ph", "Staff#2026");
        jose = user("U-3001", "admin", "Jose Dizon", "jose@test.ph", "Admin#2026");
        buddy = pet("P-1042", maria, "Buddy", "Verified", LocalDate.of(2024, 5, 10), "9.5");
        otherDog = pet("P-1050", juan, "Rocky", "Verified", LocalDate.of(2024, 5, 10), "9.5");
        pendingDog = pet("P-1051", maria, "Pending", "Pending", LocalDate.of(2025, 5, 10), "6");
        luna = new ShelterPet();
        luna.setId("S-201"); luna.setName("Luna"); luna.setSpecies("Dog"); luna.setSex("Female"); luna.setStatus("Available");
        shelterPets.save(luna);
    }

    public AppUser user(String id, String role, String name, String email, String password) {
        AppUser u = new AppUser();
        u.setId(id); u.setRole(role); u.setFullName(name); u.setEmail(email); u.setStatus("Active");
        u.setPasswordHash(encoder.encode(password));
        u.setMustChangePassword(false); u.setFailedLoginAttempts(0); u.setVerificationAttempts(0);
        u.setBarangay("Balibago");
        users.save(u);
        return u;
    }

    public Pet pet(String id, AppUser owner, String name, String registration, LocalDate dob, String weight) {
        Pet p = new Pet();
        p.setId(id); p.setOwnerId(owner.getId()); p.setName(name); p.setSpecies("Dog"); p.setBreed("Aspin"); p.setSex("Male");
        p.setDob(dob); p.setColor("Brown"); p.setBarangay("Balibago"); p.setRegistrationStatus(registration);
        p.setRegistrationSource("Owner"); p.setWeightKg(new BigDecimal(weight)); p.setHealthStatus("Apparently Healthy");
        p.setRabiesStatus("Updated"); p.setLastRabies(LocalDate.of(2026, 2, 10)); p.setDewormingStatus("Updated");
        p.setLastDeworming(LocalDate.of(2026, 7, 1)); p.setProofOfResidency("Verified at Barangay");
        p.setMicrochipStatus("Not Microchipped");
        pets.save(p);
        return p;
    }

    /** A valid registration form; tests change one field at a time. */
    public PetWorkflowService.RegisterPet newPetForm(String ownerId) {
        return new PetWorkflowService.RegisterPet(ownerId, "Coco", "Dog", "Aspin", "Female", "2025-03-01", "White", "Balibago",
                new BigDecimal("8.5"), "Apparently Healthy", null, "Updated", "2026-05-01", "Updated", "2026-06-01",
                "Not Microchipped", null, "Submitted, Pending Verification");
    }

    public static Instant at(int y, int mo, int d, int h, int mi) { return LocalDateTime.of(y, mo, d, h, mi).toInstant(ZoneOffset.ofHours(8)); }
}
