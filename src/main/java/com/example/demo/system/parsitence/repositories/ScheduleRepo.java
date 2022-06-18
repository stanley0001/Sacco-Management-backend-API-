package com.example.demo.system.parsitence.repositories;

import com.example.demo.system.parsitence.models.Schedule.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ScheduleRepo extends JpaRepository<Schedule, Long> {

    Optional<List<Schedule>> findByScheduleTimeLessThan(LocalDateTime now);
}
