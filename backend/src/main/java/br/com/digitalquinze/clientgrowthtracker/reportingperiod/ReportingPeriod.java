package br.com.digitalquinze.clientgrowthtracker.reportingperiod;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import br.com.digitalquinze.clientgrowthtracker.restaurant.Restaurant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "reporting_periods")
public class ReportingPeriod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Column(name = "reference_month", nullable = false)
    private LocalDate referenceMonth;

    @Column(name = "open_days", nullable = false)
    private Short openDays;

    @Column
    private String notes;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public ReportingPeriod() {
    }

    public ReportingPeriod(
            Restaurant restaurant,
            LocalDate referenceMonth,
            Short openDays,
            String notes) {

        this.restaurant = restaurant;
        this.referenceMonth = referenceMonth;
        this.openDays = openDays;
        this.notes = notes;
    }

    @PrePersist
    public void prePersist() {
        OffsetDateTime now = OffsetDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public LocalDate getReferenceMonth() {
        return referenceMonth;
    }

    public Short getOpenDays() {
        return openDays;
    }

    public String getNotes() {
        return notes;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setReferenceMonth(LocalDate referenceMonth) {
        this.referenceMonth = referenceMonth;
    }

    public void setOpenDays(Short openDays) {
        this.openDays = openDays;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}