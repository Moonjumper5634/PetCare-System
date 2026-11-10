package ph.gov.santarosa.petcare.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Row of table id_sequence. Generated from the schema spec; relationships are plain FK id fields. */
@Entity
@Table(name = "id_sequence")
public class IdSequence {

    @Id
    @Column(name = "kind")
    private String kind;

    @Column(name = "prefix", nullable = false, length = 10)
    private String prefix;

    @Column(name = "next_value", nullable = false)
    private Long nextValue;

    public IdSequence() { }

    public String getKind() { return kind; }
    public void setKind(String kind) { this.kind = kind; }
    public String getPrefix() { return prefix; }
    public void setPrefix(String prefix) { this.prefix = prefix; }
    public Long getNextValue() { return nextValue; }
    public void setNextValue(Long nextValue) { this.nextValue = nextValue; }
}
