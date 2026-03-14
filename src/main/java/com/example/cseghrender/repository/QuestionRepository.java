package com.example.cseghrender.repository;

import com.example.cseghrender.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByExamIdOrderByOrderIndexAsc(Long examId);
    void deleteByExamId(Long examId);
}
