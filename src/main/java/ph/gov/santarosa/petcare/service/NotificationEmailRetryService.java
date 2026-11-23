package ph.gov.santarosa.petcare.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ph.gov.santarosa.petcare.repository.NotificationRepository;

/** Retries temporary email-provider failures while keeping the in-app notification immediately available. */
@Service
public class NotificationEmailRetryService {
    private static final int MAX_ATTEMPTS = 5;
    private final NotificationRepository notifications;
    private final NotificationEmailSender sender;
    private final Notifier notifier;

    public NotificationEmailRetryService(NotificationRepository notifications,
            NotificationEmailSender sender, Notifier notifier) {
        this.notifications = notifications;
        this.sender = sender;
        this.notifier = notifier;
    }

    @Transactional
    @Scheduled(fixedDelayString = "${petcare.mail.retry-interval-ms:300000}",
            initialDelayString = "${petcare.mail.retry-initial-delay-ms:60000}")
    public void retryPending() {
        if (!sender.isEnabled() || !notifier.statusNotificationsEnabled()) return;
        for (var notification : notifications
                .findTop50ByEmailStatusAndEmailAttemptsLessThanOrderByCreatedAtAsc("Pending", MAX_ATTEMPTS)) {
            notifier.applyDelivery(notification,
                    sender.send(notification.getUserId(), notification.getTitle(), notification.getBody()));
            notifications.save(notification);
        }
    }
}
