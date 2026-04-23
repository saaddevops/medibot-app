package com.medibot.service;

import com.medibot.model.Appointment;
import com.medibot.repository.AppointmentRepository;
import com.medibot.service.BedrockAgentTriageService.TriageResult;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AppointmentService {

    private final AppointmentRepository     repo;
    private final BedrockAgentTriageService bedrockService;

    public AppointmentService(AppointmentRepository repo,
                              BedrockAgentTriageService bedrockService) {
        this.repo           = repo;
        this.bedrockService = bedrockService;
    }

    public Appointment bookAppointment(String patientName, String symptoms) {
        TriageResult triage = bedrockService.analyze(patientName, symptoms);
        Appointment appt = new Appointment(
            patientName,
            symptoms,
            triage.triageLevel,
            triage.summary,
            triage.department,
            triage.waitTime
        );
        return repo.save(appt);
    }

    public List<Appointment> getAllAppointments() {
        return repo.findAll();
    }

    public List<Appointment> getByTriageLevel(String level) {
        return repo.findByTriageLevelOrderByBookedAtDesc(level.toUpperCase());
    }
}
