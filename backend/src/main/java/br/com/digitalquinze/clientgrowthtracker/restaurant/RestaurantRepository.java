package br.com.digitalquinze.clientgrowthtracker.restaurant;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RestaurantRepository
        extends JpaRepository<Restaurant, Long> {
}