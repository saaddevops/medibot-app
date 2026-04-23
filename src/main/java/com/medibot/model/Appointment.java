package com.medibot.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "appointments")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String patientName;

    @Column(length = 1000)
    private String symptoms;

    private String triageLevel;

    @Column(length = 1000)
    private String aiSummary;

    private String suggestedDepartment;

    private String estimatedWaitTime;

    private LocalDateTime bookedAt;

    public Appointment() {}

    public Appointment(String patientName, String symptoms,
                       String triageLevel, String aiSummary,
                       String suggestedDepartment, String estimatedWaitTime) {
        this.patientName         = patientName;
        this.symptoms            = symptoms;
        this.triageLevel         = triageLevel;
        this.aiSummary           = aiSummary;
        this.suggestedDepartment = suggestedDepartment;
        this.estimatedWaitTime   = estimatedWaitTime;
        this.bookedAt            = LocalDateTime.now();
    }

    public Long getId()                         { return id; }
    public String getPatientName()              { return patientName; }
    public void setPatientName(String v)        { this.patientName = v; }
    public String getSymptoms()                 { return symptoms; }
    public void setSymptoms(String v)           { this.symptoms = v; }
    public String getTriageLevel()              { return triageLevel; }
    public void setTriageLevel(String v)        { this.triageLevel = v; }
    public String getAiSummary()                { return aiSummary; }
    public void setAiSummary(String v)          { this.aiSummary = v; }
    public String getSuggestedDepartment()      { return suggestedDepartment; }
    public void setSuggestedDepartment(String v){ this.suggestedDepartment = v; }
    public String getEstimatedWaitTime()        { return estimatedWaitTime; }
    public void setEstimatedWaitTime(String v)  { this.estimatedWaitTime = v; }
    public LocalDateTime getBookedAt()          { return bookedAt; }
    public void setBookedAt(LocalDateTime v)    { this.bookedAt = v; }
}
