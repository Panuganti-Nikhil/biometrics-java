package com.example.cseghrender.repository;

import com.example.cseghrender.model.ExamSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ExamSubmissionRepository extends JpaRepository<ExamSubmission, Long> {
    List<ExamSubmission> findByRollNo(String rollNo);
    List<ExamSubmission> findByExamId(Long examId);
    Optional<ExamSubmission> findByExamIdAndRollNo(Long examId, String rollNo);
    List<ExamSubmission> findByRollNoAndStatus(String rollNo, String status);
}
