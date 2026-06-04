package com.hotel.hotelreservation.repository;

import com.hotel.hotelreservation.model.CleaningTask;
import com.hotel.hotelreservation.model.CleaningTaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CleaningTaskRepository extends JpaRepository<CleaningTask, Long> {
    List<CleaningTask> findByStatus(CleaningTaskStatus status);
}
