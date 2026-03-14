package com.example.cseghrender.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "exams")
public class Exam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    // Short unique code name like "DSA-MID", "OS-FINAL" — used by chatbot /marks <name>
    @Column(unique = true)
    private String examName;

    @Column(columnDefinition = "TEXT")
    private String description;

    // "coding", "theory", "mcq", "mixed"
    @Column(nullable = false)
    private String examType;

    // Target section or "ALL"
    @Column(nullable = false)
    private String section;

    // Duration in minutes
    @Column(nullable = false)
    private Integer duration;

    // "scheduled", "active", "completed"
    @Column(nullable = false)
    private String status = "scheduled";

    private LocalDateTime scheduledAt;
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private Integer totalMarks = 0;

    @Column(nullable = false)
    private Boolean proctored = true;

    // Whether admin has released marks to students
    @Column(nullable = false)
    private Boolean marksReleased = false;

    // Hours students have to attempt the exam (e.g., 12 = must start within 12 hrs or auto-zero)
    private Integer attemptWindowHours;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getExamType() { return examType; }
    public void setExamType(String examType) { this.examType = examType; }

    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }

    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(LocalDateTime scheduledAt) { this.scheduledAt = scheduledAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Integer getTotalMarks() { return totalMarks; }
    public void setTotalMarks(Integer totalMarks) { this.totalMarks = totalMarks; }

    public Boolean getProctored() { return proctored; }
    public void setProctored(Boolean proctored) { this.proctored = proctored; }

    public String getExamName() { return examName; }
    public void setExamName(String examName) { this.examName = examName; }

    public Boolean getMarksReleased() { return marksReleased; }
    public void setMarksReleased(Boolean marksReleased) { this.marksReleased = marksReleased; }

    public Integer getAttemptWindowHours() { return attemptWindowHours; }
    public void setAttemptWindowHours(Integer attemptWindowHours) { this.attemptWindowHours = attemptWindowHours; }
}
