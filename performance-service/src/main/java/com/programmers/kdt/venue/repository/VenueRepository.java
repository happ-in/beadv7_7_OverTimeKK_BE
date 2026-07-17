package com.programmers.kdt.venue.repository;

import com.programmers.kdt.venue.entity.Venue;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VenueRepository extends JpaRepository<Venue, Long> {
}
