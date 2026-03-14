package com.example.cseghrender.controller;

import com.example.cseghrender.model.*;
import com.example.cseghrender.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/exams")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {
    RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT,
    RequestMethod.DELETE, RequestMethod.OPTIONS
})
public class ExamController {

    private final ExamRepository examRepo;
    private final QuestionRepository questionRepo;
    private final ExamSubmissionRepository submissionRepo;

    public ExamController(ExamRepository examRepo, QuestionRepository questionRepo,
                          ExamSubmissionRepository submissionRepo) {
        this.examRepo = examRepo;
        this.questionRepo = questionRepo;
        this.submissionRepo = submissionRepo;
    }

    // ─── ADMIN: Create exam ──────────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<Exam> createExam(@RequestBody Exam exam) {
        exam.setStatus("scheduled");
        return ResponseEntity.ok(examRepo.save(exam));
    }

    // ─── ADMIN: Get all exams ────────────────────────────────────────────────
    @GetMapping
    public List<Exam> getAllExams() {
        return examRepo.findAll();
    }

    // ─── Get single exam ─────────────────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<Exam> getExam(@PathVariable Long id) {
        return examRepo.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    // ─── ADMIN: Update exam ──────────────────────────────────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<Exam> updateExam(@PathVariable Long id, @RequestBody Exam update) {
        return examRepo.findById(id).map(exam -> {
            if (update.getTitle() != null) exam.setTitle(update.getTitle());
            if (update.getDescription() != null) exam.setDescription(update.getDescription());
            if (update.getExamType() != null) exam.setExamType(update.getExamType());
            if (update.getSection() != null) exam.setSection(update.getSection());
            if (update.getDuration() != null) exam.setDuration(update.getDuration());
            if (update.getStatus() != null) exam.setStatus(update.getStatus());
            if (update.getScheduledAt() != null) exam.setScheduledAt(update.getScheduledAt());
            if (update.getTotalMarks() != null) exam.setTotalMarks(update.getTotalMarks());
            if (update.getProctored() != null) exam.setProctored(update.getProctored());
            if (update.getExamName() != null) exam.setExamName(update.getExamName());
            if (update.getMarksReleased() != null) exam.setMarksReleased(update.getMarksReleased());
            if (update.getAttemptWindowHours() != null) exam.setAttemptWindowHours(update.getAttemptWindowHours());
            return ResponseEntity.ok(examRepo.save(exam));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ─── ADMIN: Delete exam ──────────────────────────────────────────────────
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> deleteExam(@PathVariable Long id) {
        if (examRepo.existsById(id)) {
            questionRepo.deleteByExamId(id);
            examRepo.deleteById(id);
            return ResponseEntity.ok("Deleted");
        }
        return ResponseEntity.notFound().build();
    }

    // ─── ADMIN: Add questions to exam ────────────────────────────────────────
    @PostMapping("/{examId}/questions")
    public ResponseEntity<List<Question>> addQuestions(@PathVariable Long examId,
                                                       @RequestBody List<Question> questions) {
        if (!examRepo.existsById(examId)) {
            return ResponseEntity.notFound().build();
        }
        int totalMarks = 0;
        int idx = 0;
        for (Question q : questions) {
            q.setExamId(examId);
            q.setOrderIndex(idx++);
            totalMarks += (q.getMarks() != null ? q.getMarks() : 1);
        }
        List<Question> saved = questionRepo.saveAll(questions);
        // Update total marks
        examRepo.findById(examId).ifPresent(exam -> {
            int existingMarks = questionRepo.findByExamIdOrderByOrderIndexAsc(examId)
                .stream().mapToInt(Question::getMarks).sum();
            exam.setTotalMarks(existingMarks);
            examRepo.save(exam);
        });
        return ResponseEntity.ok(saved);
    }

    // ─── Get questions for an exam ───────────────────────────────────────────
    @GetMapping("/{examId}/questions")
    public List<Question> getQuestions(@PathVariable Long examId) {
        return questionRepo.findByExamIdOrderByOrderIndexAsc(examId);
    }

    // ─── STUDENT: Get exams for their section ────────────────────────────────
    @GetMapping("/student/{section}")
    public List<Exam> getExamsForSection(@PathVariable String section) {
        return examRepo.findBySectionOrSection(section, "ALL");
    }

    // ─── STUDENT: Start exam → creates a submission ──────────────────────────
    @PostMapping("/{examId}/start")
    public ResponseEntity<?> startExam(@PathVariable Long examId,
                                       @RequestBody Map<String, String> body) {
        String rollNo = body.get("rollNo");
        String name = body.get("studentName");

        // Check if already in-progress
        Optional<ExamSubmission> existing = submissionRepo.findByExamIdAndRollNo(examId, rollNo);
        if (existing.isPresent()) {
            ExamSubmission sub = existing.get();
            if ("in_progress".equals(sub.getStatus())) {
                // Return existing so student can resume
                return ResponseEntity.ok(sub);
            }
            if ("submitted".equals(sub.getStatus()) || "graded".equals(sub.getStatus())) {
                return ResponseEntity.status(409)
                    .body("You have already submitted this exam.");
            }
        }

        // Activate exam if it was scheduled
        examRepo.findById(examId).ifPresent(exam -> {
            if ("scheduled".equals(exam.getStatus())) {
                exam.setStatus("active");
                examRepo.save(exam);
            }
        });

        ExamSubmission sub = new ExamSubmission();
        sub.setExamId(examId);
        sub.setRollNo(rollNo);
        sub.setStudentName(name);
        sub.setStatus("in_progress");
        sub.setAnswers("{}");

        Exam exam = examRepo.findById(examId).orElse(null);
        if (exam != null) {
            sub.setTotalMarks(exam.getTotalMarks());
            sub.setRemainingSeconds(exam.getDuration() * 60);
        }

        return ResponseEntity.ok(submissionRepo.save(sub));
    }

    // ─── STUDENT: Save answers (auto-save / manual) ─────────────────────────
    @PutMapping("/submissions/{subId}")
    public ResponseEntity<?> saveAnswers(@PathVariable Long subId,
                                         @RequestBody Map<String, Object> body) {
        return submissionRepo.findById(subId).map(sub -> {
            if (body.containsKey("answers")) {
                sub.setAnswers(body.get("answers").toString());
            }
            if (body.containsKey("remainingSeconds")) {
                sub.setRemainingSeconds(((Number) body.get("remainingSeconds")).intValue());
            }
            return ResponseEntity.ok(submissionRepo.save(sub));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ─── STUDENT: Submit exam ────────────────────────────────────────────────
    @PostMapping("/submissions/{subId}/submit")
    public ResponseEntity<?> submitExam(@PathVariable Long subId) {
        return submissionRepo.findById(subId).map(sub -> {
            sub.setStatus("submitted");
            sub.setSubmittedAt(LocalDateTime.now());
            sub.setRemainingSeconds(0);

            // Auto-grade MCQ and Multiple Choice questions
            try {
                List<Question> questions = questionRepo.findByExamIdOrderByOrderIndexAsc(sub.getExamId());
                String answersJson = sub.getAnswers();
                // Simple JSON parsing — answers are stored as {"qId": "answer", ...}
                int totalScore = 0;
                for (Question q : questions) {
                    String qKey = String.valueOf(q.getId());
                    String studentAnswer = extractJsonValue(answersJson, qKey);

                    if (studentAnswer != null && q.getCorrectAnswer() != null) {
                        if ("mcq".equals(q.getQuestionType())) {
                            if (studentAnswer.trim().equalsIgnoreCase(q.getCorrectAnswer().trim())) {
                                totalScore += q.getMarks();
                            }
                        } else if ("multiple_choice".equals(q.getQuestionType())) {
                            // Compare sorted sets
                            if (normalizeMultiAnswer(studentAnswer).equals(normalizeMultiAnswer(q.getCorrectAnswer()))) {
                                totalScore += q.getMarks();
                            }
                        } else if ("snippet".equals(q.getQuestionType())) {
                            if (studentAnswer.trim().equalsIgnoreCase(q.getCorrectAnswer().trim())) {
                                totalScore += q.getMarks();
                            }
                        }
                        // Coding and theory need manual grading — not auto-scored here
                    }
                }
                sub.setScore(totalScore);
                sub.setStatus("graded");
            } catch (Exception e) {
                System.out.println("[Exam] Auto-grade error: " + e.getMessage());
            }

            return ResponseEntity.ok(submissionRepo.save(sub));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ─── STUDENT: Get their submissions (for exam hub) ───────────────────────
    @GetMapping("/submissions/student/{rollNo}")
    public List<ExamSubmission> getStudentSubmissions(@PathVariable String rollNo) {
        return submissionRepo.findByRollNo(rollNo);
    }

    // ─── ADMIN: Get all submissions for an exam ──────────────────────────────
    @GetMapping("/{examId}/submissions")
    public List<ExamSubmission> getExamSubmissions(@PathVariable Long examId) {
        return submissionRepo.findByExamId(examId);
    }

    // ─── ADMIN: Release / hold marks ─────────────────────────────────────────
    @PutMapping("/{examId}/release-marks")
    public ResponseEntity<?> releaseMarks(@PathVariable Long examId,
                                           @RequestBody Map<String, Boolean> body) {
        return examRepo.findById(examId).map(exam -> {
            boolean release = body.getOrDefault("release", true);
            exam.setMarksReleased(release);
            return ResponseEntity.ok(examRepo.save(exam));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ─── CHATBOT: Get marks by exam name + rollNo ────────────────────────────
    @GetMapping("/marks")
    public ResponseEntity<?> getMarksByExamName(@RequestParam String examName,
                                                @RequestParam String rollNo) {
        Optional<Exam> examOpt = examRepo.findByExamNameIgnoreCase(examName);
        if (examOpt.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                "found", false,
                "message", "No exam found with name '" + examName + "'. Check the exam name and try again."
            ));
        }
        Exam exam = examOpt.get();
        if (Boolean.FALSE.equals(exam.getMarksReleased())) {
            return ResponseEntity.ok(Map.of(
                "found", true,
                "released", false,
                "message", "Marks for '" + exam.getTitle() + "' have not been released yet."
            ));
        }
        Optional<ExamSubmission> subOpt = submissionRepo.findByExamIdAndRollNo(exam.getId(), rollNo);
        if (subOpt.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                "found", true,
                "released", true,
                "attempted", false,
                "score", 0,
                "totalMarks", exam.getTotalMarks(),
                "message", "You did not attempt '" + exam.getTitle() + "'. Score: 0/" + exam.getTotalMarks()
            ));
        }
        ExamSubmission sub = subOpt.get();
        return ResponseEntity.ok(Map.of(
            "found", true,
            "released", true,
            "attempted", true,
            "score", sub.getScore() != null ? sub.getScore() : 0,
            "totalMarks", exam.getTotalMarks(),
            "examTitle", exam.getTitle(),
            "message", "Your score for '" + exam.getTitle() + "': " + (sub.getScore() != null ? sub.getScore() : 0) + "/" + exam.getTotalMarks()
        ));
    }

    // ─── ADMIN: Get marks of ALL students for a specific exam (for student records) ──
    @GetMapping("/{examId}/all-student-marks")
    public ResponseEntity<?> getAllStudentMarks(@PathVariable Long examId) {
        Optional<Exam> examOpt = examRepo.findById(examId);
        if (examOpt.isEmpty()) return ResponseEntity.notFound().build();
        Exam exam = examOpt.get();

        List<ExamSubmission> subs = submissionRepo.findByExamId(examId);
        // Build rollNo → score map
        Map<String, Object> marksMap = new HashMap<>();
        for (ExamSubmission s : subs) {
            marksMap.put(s.getRollNo(), Map.of(
                "score", s.getScore() != null ? s.getScore() : 0,
                "totalMarks", exam.getTotalMarks(),
                "status", s.getStatus()
            ));
        }
        return ResponseEntity.ok(Map.of(
            "examId", examId,
            "examTitle", exam.getTitle(),
            "examName", exam.getExamName() != null ? exam.getExamName() : "",
            "totalMarks", exam.getTotalMarks(),
            "marksReleased", exam.getMarksReleased(),
            "marks", marksMap
        ));
    }

    // ─── AUTO-EXPIRE: Check and auto-zero expired attempt windows ────────────
    @PostMapping("/auto-expire")
    public ResponseEntity<?> autoExpireExams() {
        List<Exam> exams = examRepo.findAll();
        int expired = 0;
        for (Exam exam : exams) {
            if (exam.getAttemptWindowHours() != null && exam.getScheduledAt() != null
                && ("scheduled".equals(exam.getStatus()) || "active".equals(exam.getStatus()))) {
                LocalDateTime deadline = exam.getScheduledAt().plusHours(exam.getAttemptWindowHours());
                if (LocalDateTime.now().isAfter(deadline)) {
                    exam.setStatus("completed");
                    examRepo.save(exam);
                    expired++;
                }
            }
        }
        return ResponseEntity.ok(Map.of("expiredExams", expired));
    }

    // ─── Helper: Extract value from simple JSON string ───────────────────────
    private String extractJsonValue(String json, String key) {
        if (json == null) return null;
        String searchKey = "\"" + key + "\"";
        int idx = json.indexOf(searchKey);
        if (idx == -1) return null;
        int colonIdx = json.indexOf(":", idx);
        if (colonIdx == -1) return null;
        int start = json.indexOf("\"", colonIdx + 1);
        if (start == -1) return null;
        int end = json.indexOf("\"", start + 1);
        if (end == -1) return null;
        return json.substring(start + 1, end);
    }

    private String normalizeMultiAnswer(String answer) {
        if (answer == null) return "";
        String[] parts = answer.replaceAll("[\\[\\]\"\\s]", "").split(",");
        Arrays.sort(parts);
        return String.join(",", parts).toLowerCase();
    }
}
