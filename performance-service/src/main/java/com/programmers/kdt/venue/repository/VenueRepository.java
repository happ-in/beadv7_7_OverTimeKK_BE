package com.programmers.kdt.venue.repository;

import com.programmers.kdt.venue.dto.VenueResponse;
import com.programmers.kdt.venue.entity.Venue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface VenueRepository extends JpaRepository<Venue, Long> {

    @Query("select new com.programmers.kdt.venue.dto.VenueResponse(v.venueId, v.venueName) from Venue v")
    List<VenueResponse> findAllVenues();
}
