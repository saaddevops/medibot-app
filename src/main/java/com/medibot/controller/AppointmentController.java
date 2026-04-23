package com.medibot.controller;

import com.medibot.model.Appointment;
import com.medibot.service.AppointmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AppointmentController {

    private final AppointmentService service;

    public AppointmentController(AppointmentService service) {
        this.service = service;
    }

    @PostMapping("/appointments")
    public ResponseEntity<Appointment> book(@RequestBody Map<String, String> request) {
        String patientName = request.get("patientName");
        String symptoms    = request.get("symptoms");
        Appointment appt   = service.bookAppointment(patientName, symptoms);
        return ResponseEntity.ok(appt);
    }

    @GetMapping("/appointments")
    public ResponseEntity<List<Appointment>> getAll() {
        return ResponseEntity.ok(service.getAllAppointments());
    }

    @GetMapping("/appointments/triage/{level}")
    public ResponseEntity<List<Appointment>> getByLevel(@PathVariable String level) {
        return ResponseEntity.ok(service.getByTriageLevel(level));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "MediBot"));
    }

} 
