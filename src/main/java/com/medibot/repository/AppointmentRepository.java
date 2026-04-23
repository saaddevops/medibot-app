package com.medibot.repository;

import com.medibot.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByTriageLevelOrderByBookedAtDesc(String triageLevel);
}
