package ph.gov.santarosa.petcare.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

/** Row of table service_record. Generated from the schema spec; relationships are plain FK id fields. */
@Entity
@Table(name = "service_record")
public class ServiceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "request_id", nullable = false, length = 20)
    private String requestId;

    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate;

    @Column(name = "staff_name", nullable = false, length = 120)
    private String staffName;

    @Column(name = "remarks", length = 1000)
    private String remarks;

    @Column(name = "vaccine_type", length = 80)
    private String vaccineType;

    @Column(name = "batch_number", length = 60)
    private String batchNumber;

    @Column(name = "next_due")
    private LocalDate nextDue;

    @Column(name = "microchip_number", length = 40)
    private String microchipNumber;

    @Column(name = "procedure_date")
    private LocalDate procedureDate;

    @Column(name = "post_remarks", length = 1000)
    private String postRemarks;

    @Column(name = "presenter_name", length = 160)
    private String presenterName;

    @Column(name = "provider_role", length = 80)
    private String providerRole;

    @Column(name = "source_type", nullable = false, length = 40)
    private String sourceType;

    @Column(name = "source_reference", length = 120)
    private String sourceReference;

    @Column(name = "service_location", length = 160)
    private String serviceLocation;

    public ServiceRecord() { }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public LocalDate getRecordDate() { return recordDate; }
    public void setRecordDate(LocalDate recordDate) { this.recordDate = recordDate; }
    public String getStaffName() { return staffName; }
    public void setStaffName(String staffName) { this.staffName = staffName; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    public String getVaccineType() { return vaccineType; }
    public void setVaccineType(String vaccineType) { this.vaccineType = vaccineType; }
    public String getBatchNumber() { return batchNumber; }
    public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }
    public LocalDate getNextDue() { return nextDue; }
    public void setNextDue(LocalDate nextDue) { this.nextDue = nextDue; }
    public String getMicrochipNumber() { return microchipNumber; }
    public void setMicrochipNumber(String microchipNumber) { this.microchipNumber = microchipNumber; }
    public LocalDate getProcedureDate() { return procedureDate; }
    public void setProcedureDate(LocalDate procedureDate) { this.procedureDate = procedureDate; }
    public String getPostRemarks() { return postRemarks; }
    public void setPostRemarks(String postRemarks) { this.postRemarks = postRemarks; }
    public String getPresenterName() { return presenterName; }
    public void setPresenterName(String presenterName) { this.presenterName = presenterName; }
    public String getProviderRole() { return providerRole; }
    public void setProviderRole(String providerRole) { this.providerRole = providerRole; }
    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }
    public String getSourceReference() { return sourceReference; }
    public void setSourceReference(String sourceReference) { this.sourceReference = sourceReference; }
    public String getServiceLocation() { return serviceLocation; }
    public void setServiceLocation(String serviceLocation) { this.serviceLocation = serviceLocation; }
}
