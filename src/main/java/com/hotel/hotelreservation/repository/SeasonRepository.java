package com.hotel.hotelreservation.repository;

import com.hotel.hotelreservation.model.Season;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for seasonal pricing ranges.
 */
@Repository
public interface SeasonRepository extends JpaRepository<Season, Long> {

    /**
     * Returns seasons that contain the given date.
     * We allow multiple seasons to overlap (first-match logic can be implemented in service).
     */
    @Query("select s from Season s where :date between s.startDate and s.endDate")
    List<Season> findActiveSeasons(@Param("date") LocalDate date);

    /**
     * Find seasons overlapping a date range (useful for multi-night price calculation).
     */
    @Query("select s from Season s where not (s.endDate < :start or s.startDate > :end)")
    List<Season> findSeasonsOverlapping(@Param("start") LocalDate start, @Param("end") LocalDate end);
}
