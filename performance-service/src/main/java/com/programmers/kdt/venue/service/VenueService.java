package com.programmers.kdt.venue.service;

import com.programmers.kdt.venue.dto.HallResponse;
import com.programmers.kdt.venue.dto.VenueResponse;
import com.programmers.kdt.venue.repository.HallRepository;
import com.programmers.kdt.venue.repository.SeatRepository;
import com.programmers.kdt.venue.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VenueService {

    private final VenueRepository venueRepository;
    private final HallRepository hallRepository;
    private final SeatRepository seatRepository;

    public List<VenueResponse> findVenues() {
        return venueRepository.findAllVenues();
    }

    public List<HallResponse> findHallsByVenueId(Long venueId) {
        return hallRepository.findHallsByVenueId(venueId);
    }

    public List<String> findZonesByHallId(Long hallId) {
        return seatRepository.findDistinctZoneByHallId(hallId);
    }
}