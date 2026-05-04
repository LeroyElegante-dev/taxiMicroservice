package com.taxi.tripservice.entity;

import com.taxi.common.model.TripStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "trips")
public class TripEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "passenger_id", nullable = false)
    private Long passengerId;

    @Column(name = "driver_id")
    private Long driverId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private TripStatus status;

    @Column(name = "origin_lat", nullable = false)
    private BigDecimal originLat;

    @Column(name = "origin_lng", nullable = false)
    private BigDecimal originLng;

    @Column(name = "dest_lat", nullable = false)
    private BigDecimal destLat;

    @Column(name = "dest_lng", nullable = false)
    private BigDecimal destLng;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    private Integer rating;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Long getPassengerId() {
        return passengerId;
    }

    public void setPassengerId(Long passengerId) {
        this.passengerId = passengerId;
    }

    public Long getDriverId() {
        return driverId;
    }

    public void setDriverId(Long driverId) {
        this.driverId = driverId;
    }

    public TripStatus getStatus() {
        return status;
    }

    public void setStatus(TripStatus status) {
        this.status = status;
    }

    public BigDecimal getOriginLat() {
        return originLat;
    }

    public void setOriginLat(BigDecimal originLat) {
        this.originLat = originLat;
    }

    public BigDecimal getOriginLng() {
        return originLng;
    }

    public void setOriginLng(BigDecimal originLng) {
        this.originLng = originLng;
    }

    public BigDecimal getDestLat() {
        return destLat;
    }

    public void setDestLat(BigDecimal destLat) {
        this.destLat = destLat;
    }

    public BigDecimal getDestLng() {
        return destLng;
    }

    public void setDestLng(BigDecimal destLng) {
        this.destLng = destLng;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
