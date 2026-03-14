package com.example.cseghrender.controller;

import com.example.cseghrender.model.Violation;
import com.example.cseghrender.repository.ViolationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/violations")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {
    RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE, RequestMethod.OPTIONS
})
public class ViolationController {

    private final ViolationRepository violationRepository;

    public ViolationController(ViolationRepository violationRepository) {
        this.violationRepository = violationRepository;
    }

    // Admin: Get all violations, most recent first
    @GetMapping
    public List<Violation> getAllViolations() {
        return violationRepository.findAllByOrderByTimestampDesc();
    }

    // Admin: Get violations for a specific student
    @GetMapping("/student/{rollNo}")
    public List<Violation> getStudentViolations(@PathVariable String rollNo) {
        return violationRepository.findByRollNoOrderByTimestampDesc(rollNo);
    }

    // Student: Report a violation from their exam session
    @PostMapping
    public ResponseEntity<Violation> reportViolation(@RequestBody ViolationRequest req) {
        Violation v = new Violation(
            req.getRollNo(),
            req.getStudentName(),
            req.getViolationType(),
            req.getSeverity(),
            req.getDetails()
        );
        return ResponseEntity.ok(violationRepository.save(v));
    }

    // Admin: Clear all violations (reset for new exam session)
    @DeleteMapping
    public ResponseEntity<String> clearAll() {
        violationRepository.deleteAll();
        return ResponseEntity.ok("All violations cleared.");
    }

    // Count total violations
    @GetMapping("/count")
    public ResponseEntity<Long> count() {
        return ResponseEntity.ok(violationRepository.count());
    }
}

class ViolationRequest {
    private String rollNo;
    private String studentName;
    private String violationType;
    private String severity;
    private String details;

    public String getRollNo() { return rollNo; }
    public void setRollNo(String rollNo) { this.rollNo = rollNo; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getViolationType() { return violationType; }
    public void setViolationType(String violationType) { this.violationType = violationType; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
}
