package ph.gov.santarosa.petcare.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ph.gov.santarosa.petcare.domain.SystemSetting;

public interface SystemSettingRepository extends JpaRepository<SystemSetting, String> { }
