package com.example.cseghrender.repository;

import com.example.cseghrender.model.Exam;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ExamRepository extends JpaRepository<Exam, Long> {
    List<Exam> findBySection(String section);
    List<Exam> findByStatus(String status);
    List<Exam> findBySectionOrSection(String section1, String section2);
    java.util.Optional<Exam> findByExamNameIgnoreCase(String examName);
}
