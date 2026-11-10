package ph.gov.santarosa.petcare.config;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Two clocks, on purpose.
 *  - "clock": the application's idea of today. The dev profile pins it to 17 Aug 2026 (petcare.reference-date)
 *    so seeded pets keep their expected eligibility results. Empty in production = real date.
 *  - "securityClock": always the real time. Login lockouts and one-time-code expiry must never be frozen.
 */
@Configuration
public class ClockConfig {

    private static final ZoneId MANILA = ZoneId.of("Asia/Manila");

    @Bean
    @Primary
    public Clock clock(@Value("${petcare.reference-date:}") String referenceDate) {
        if (referenceDate == null || referenceDate.isBlank()) return Clock.system(MANILA);
        return Clock.fixed(LocalDate.parse(referenceDate.trim()).atStartOfDay(MANILA).toInstant(), MANILA);
    }

    @Bean("securityClock")
    public Clock securityClock() { return Clock.system(MANILA); }
}
