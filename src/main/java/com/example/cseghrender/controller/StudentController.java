package com.example.cseghrender.controller;

import com.example.cseghrender.model.Student;
import com.example.cseghrender.repository.StudentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/students")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {
    RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT,
    RequestMethod.DELETE, RequestMethod.OPTIONS, RequestMethod.PATCH
})
public class StudentController {

    private final StudentRepository studentRepository;

    public StudentController(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    // Get all registered students (returns safe DTO without faceData to avoid 500 on large LOB)
    @GetMapping
    public List<StudentSummaryDTO> getAllStudents() {
        return studentRepository.findAll().stream()
            .map(s -> new StudentSummaryDTO(s.getId(), s.getRollNo(), s.getName(), s.getSection(), s.getAttendance(),
                    s.getFaceData() != null && !s.getFaceData().isEmpty()))
            .toList();
    }

    // Get one student by roll number — includes faceData for biometric comparison
    @GetMapping("/{rollNo}")
    public ResponseEntity<?> getStudent(@PathVariable String rollNo) {
        Optional<Student> studentOpt = studentRepository.findByRollNo(rollNo);
        if (studentOpt.isPresent()) {
            return ResponseEntity.ok(studentOpt.get());
        }
        return ResponseEntity.notFound().build();
    }

    // Register/update face data for an existing student
    @PostMapping("/verify")
    public ResponseEntity<?> verifyStudent(@RequestBody VerifyRequest req) {
        Optional<Student> studentOpt = studentRepository.findByRollNo(req.getRollNo());

        if (studentOpt.isPresent()) {
            Student student = studentOpt.get();
            if (req.getName() != null && !req.getName().trim().isEmpty()) {
                student.setName(req.getName().trim());
            }
            if (req.getFaceData() != null && !req.getFaceData().trim().isEmpty()) {
                student.setFaceData(req.getFaceData().trim());
            }
            studentRepository.save(student);
            return ResponseEntity.ok(student);
        }
        return ResponseEntity.status(404).body("Roll number not found in admin database.");
    }

    // Add new student from admin
    @PostMapping
    public ResponseEntity<Student> addStudent(@RequestBody Student req) {
        return ResponseEntity.ok(studentRepository.save(req));
    }

    // Update student
    @PutMapping("/{rollNo}")
    public ResponseEntity<?> updateStudent(@PathVariable String rollNo, @RequestBody Student req) {
        Optional<Student> studentOpt = studentRepository.findByRollNo(rollNo);
        if (studentOpt.isPresent()) {
            Student s = studentOpt.get();
            if (req.getName() != null) s.setName(req.getName());
            if (req.getSection() != null) s.setSection(req.getSection());
            if (req.getAttendance() != null) s.setAttendance(req.getAttendance());
            studentRepository.save(s);
            return ResponseEntity.ok(s);
        }
        return ResponseEntity.notFound().build();
    }

    // Mark attendance
    @PostMapping("/{rollNo}/attendance")
    public ResponseEntity<?> markAttendance(@PathVariable String rollNo) {
        Optional<Student> studentOpt = studentRepository.findByRollNo(rollNo);
        if (studentOpt.isPresent()) {
            Student s = studentOpt.get();
            s.setAttendance(Math.min(100, s.getAttendance() + 2));
            studentRepository.save(s);
            return ResponseEntity.ok(s);
        }
        return ResponseEntity.notFound().build();
    }

    // Delete student (admin use)
    @DeleteMapping("/{rollNo}")
    public ResponseEntity<?> deleteStudent(@PathVariable String rollNo) {
        Optional<Student> studentOpt = studentRepository.findByRollNo(rollNo);
        if (studentOpt.isPresent()) {
            studentRepository.delete(studentOpt.get());
            return ResponseEntity.ok("Deleted");
        }
        return ResponseEntity.notFound().build();
    }
}

// ─── DTOs ────────────────────────────────────────────────────────────────────

class StudentSummaryDTO {
    public Long id;
    public String rollNo;
    public String name;
    public String section;
    public Integer attendance;
    public boolean hasFaceData;

    public StudentSummaryDTO(Long id, String rollNo, String name, String section, Integer attendance, boolean hasFaceData) {
        this.id = id;
        this.rollNo = rollNo;
        this.name = name;
        this.section = section;
        this.attendance = attendance;
        this.hasFaceData = hasFaceData;
    }
}

class VerifyRequest {
    private String rollNo;
    private String name;
    private String faceData;

    public String getRollNo() { return rollNo; }
    public void setRollNo(String rollNo) { this.rollNo = rollNo; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getFaceData() { return faceData; }
    public void setFaceData(String faceData) { this.faceData = faceData; }
}
