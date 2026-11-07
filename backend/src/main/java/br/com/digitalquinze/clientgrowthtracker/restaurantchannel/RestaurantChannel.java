package br.com.digitalquinze.clientgrowthtracker.restaurantchannel;

import java.time.OffsetDateTime;

import br.com.digitalquinze.clientgrowthtracker.channel.Channel;
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
@Table(name = "restaurant_channels")
public class RestaurantChannel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id", nullable = false)
    private Channel channel;

    @Column(name = "external_store_id")
    private String externalStoreId;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public RestaurantChannel() {
    }

    public RestaurantChannel(
            Restaurant restaurant,
            Channel channel,
            String externalStoreId) {

        this.restaurant = restaurant;
        this.channel = channel;
        this.externalStoreId = externalStoreId;
        this.active = true;
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

    public Channel getChannel() {
        return channel;
    }

    public String getExternalStoreId() {
        return externalStoreId;
    }

    public boolean isActive() {
        return active;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setExternalStoreId(String externalStoreId) {
        this.externalStoreId = externalStoreId;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}