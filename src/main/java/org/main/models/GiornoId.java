package org.main.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

// Classe Embeddable per rappresentare la chiave composta
@Embeddable
public class GiornoId implements Serializable {

    @Column(name = "data_inizio")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm[:ss[.SSS]]", timezone = "UTC")
    private LocalDateTime data_inizioId;

    @Column(name = "data_fine")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm[:ss[.SSS]]", timezone = "UTC")
    private LocalDateTime data_fineId;
    
    private Long sportelloId;

    // Costruttore, getter, setter, equals e hashCode
    public GiornoId() {
    }

    public GiornoId(LocalDateTime data_inizio, LocalDateTime data_fine, Long sportelloId) {
        this.data_inizioId = data_inizio;
        this.data_fineId = data_fine;
        this.sportelloId = sportelloId;
    }

    public LocalDateTime getData_inizioId() {
        return data_inizioId;
    }

    public void setData_inizioId(LocalDateTime data_inizioId) {
        this.data_inizioId = data_inizioId;
    }

    public LocalDateTime getData_fineId() {
        return data_fineId;
    }

    public void setData_fineId(LocalDateTime data_fineId) {
        this.data_fineId = data_fineId;
    }

    public Long getSportelloId() {
        return sportelloId;
    }

    public void setSportelloId(Long sportelloId) {
        this.sportelloId = sportelloId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GiornoId giornoId = (GiornoId) o;
        return Objects.equals(data_inizioId, giornoId.data_inizioId) &&
                Objects.equals(data_fineId, giornoId.data_fineId) &&
                Objects.equals(sportelloId, giornoId.sportelloId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data_inizioId, data_fineId, sportelloId);
    }
}
