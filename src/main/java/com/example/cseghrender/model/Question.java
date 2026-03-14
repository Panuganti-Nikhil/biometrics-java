package com.example.cseghrender.model;

import jakarta.persistence.*;

@Entity
@Table(name = "questions")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long examId;

    // "coding", "theory", "mcq", "multiple_choice", "snippet"
    @Column(nullable = false)
    private String questionType;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String questionText;

    // For coding: starter code / snippet
    @Column(columnDefinition = "TEXT")
    private String codeSnippet;

    // For coding: e.g. "python,java,c,cpp,javascript"
    private String allowedLanguages;

    // For MCQ/Multiple Choice: JSON array of options, e.g. ["A", "B", "C", "D"]
    @Column(columnDefinition = "TEXT")
    private String options;

    // For MCQ: single correct answer; Multiple Choice: JSON array
    @Column(columnDefinition = "TEXT")
    private String correctAnswer;

    // For coding: JSON array of test cases, e.g. [{"input":"5","output":"25"},...]
    @Column(columnDefinition = "TEXT")
    private String testCases;

    @Column(nullable = false)
    private Integer marks = 1;

    @Column(nullable = false)
    private Integer orderIndex = 0;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getExamId() { return examId; }
    public void setExamId(Long examId) { this.examId = examId; }

    public String getQuestionType() { return questionType; }
    public void setQuestionType(String questionType) { this.questionType = questionType; }

    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }

    public String getCodeSnippet() { return codeSnippet; }
    public void setCodeSnippet(String codeSnippet) { this.codeSnippet = codeSnippet; }

    public String getAllowedLanguages() { return allowedLanguages; }
    public void setAllowedLanguages(String allowedLanguages) { this.allowedLanguages = allowedLanguages; }

    public String getOptions() { return options; }
    public void setOptions(String options) { this.options = options; }

    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }

    public String getTestCases() { return testCases; }
    public void setTestCases(String testCases) { this.testCases = testCases; }

    public Integer getMarks() { return marks; }
    public void setMarks(Integer marks) { this.marks = marks; }

    public Integer getOrderIndex() { return orderIndex; }
    public void setOrderIndex(Integer orderIndex) { this.orderIndex = orderIndex; }
}
