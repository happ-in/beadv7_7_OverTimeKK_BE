package com.programmers.kdt.performance.venue.repository;

import com.programmers.kdt.performance.venue.entity.Venue;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VenueRepository extends JpaRepository<Venue, Long> {
}
