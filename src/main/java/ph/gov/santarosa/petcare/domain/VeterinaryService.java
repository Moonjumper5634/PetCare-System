package ph.gov.santarosa.petcare.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Row of table veterinary_service. Generated from the schema spec; relationships are plain FK id fields. */
@Entity
@Table(name = "veterinary_service")
public class VeterinaryService {

    @Id
    @Column(name = "name")
    private String name;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    public VeterinaryService() { }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
