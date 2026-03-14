package com.example.cseghrender.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "violations")
public class Violation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String rollNo;

    @Column(nullable = false)
    private String studentName;

    @Column(nullable = false)
    private String violationType; // PHONE_DETECTED, MULTIPLE_FACES, NO_FACE, LOOKING_AWAY

    @Column(nullable = false)
    private String severity; // HIGH, MEDIUM

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(columnDefinition = "TEXT")
    private String details;

    public Violation() {}

    public Violation(String rollNo, String studentName, String violationType, String severity, String details) {
        this.rollNo = rollNo;
        this.studentName = studentName;
        this.violationType = violationType;
        this.severity = severity;
        this.details = details;
        this.timestamp = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getRollNo() { return rollNo; }
    public void setRollNo(String rollNo) { this.rollNo = rollNo; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getViolationType() { return violationType; }
    public void setViolationType(String violationType) { this.violationType = violationType; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
}
