package ph.gov.santarosa.petcare.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ph.gov.santarosa.petcare.domain.IdSequence;
import ph.gov.santarosa.petcare.repository.IdSequenceRepository;

/**
 * Database version of the front end's nextId(kind): "pet" -> P-1050, "req" -> REQ-3303, "reg" -> REG-1003,
 * "adopt" -> ADOPT-1001, "user" -> U-4001, "notif" -> N-1, "audit" -> A-1.
 * The sequence row is locked (SELECT ... FOR UPDATE) for the length of the caller's transaction.
 */
@Service
public class IdGenerator {

    private final IdSequenceRepository sequences;

    public IdGenerator(IdSequenceRepository sequences) { this.sequences = sequences; }

    @Transactional
    public String next(String kind) {
        IdSequence s = sequences.lockByKind(kind)
                .orElseThrow(() -> new IllegalArgumentException("Unknown id kind: " + kind));
        long n = s.getNextValue();
        s.setNextValue(n + 1);
        return s.getPrefix() + n;
    }
}
