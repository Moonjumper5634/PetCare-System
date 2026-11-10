package ph.gov.santarosa.petcare.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Row of table eligibility_criterion. Generated from the schema spec; relationships are plain FK id fields. */
@Entity
@Table(name = "eligibility_criterion")
public class EligibilityCriterion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "service_name", nullable = false, length = 40)
    private String serviceName;

    @Column(name = "criterion_key", nullable = false, length = 30)
    private String criterionKey;

    @Column(name = "label", nullable = false, length = 120)
    private String label;

    @Column(name = "description", nullable = false, length = 600)
    private String description;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    public EligibilityCriterion() { }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    public String getCriterionKey() { return criterionKey; }
    public void setCriterionKey(String criterionKey) { this.criterionKey = criterionKey; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
