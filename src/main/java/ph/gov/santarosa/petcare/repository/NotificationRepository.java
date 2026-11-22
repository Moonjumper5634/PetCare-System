package ph.gov.santarosa.petcare.repository;

import java.util.List;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import ph.gov.santarosa.petcare.domain.Notification;

public interface NotificationRepository extends JpaRepository<Notification, String> {
    List<Notification> findByUserIdOrderByCreatedAtDesc(String userId);
    long countByUserIdAndReadFalse(String userId);
    List<Notification> findTop50ByEmailStatusAndEmailAttemptsLessThanOrderByCreatedAtAsc(String status, int attempts);
    boolean existsByUserIdAndTitleAndCreatedAtBetween(String userId, String title, LocalDateTime from, LocalDateTime to);
}
