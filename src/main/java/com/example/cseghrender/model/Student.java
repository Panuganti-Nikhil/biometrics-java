package com.example.cseghrender.model;

import jakarta.persistence.*;

@Entity
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String rollNo;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String section;

    private Integer attendance = 0;

    @Column(columnDefinition = "TEXT")
    private String faceData;

    public Student() {}

    public Student(String rollNo, String name, String section, Integer attendance) {
        this.rollNo = rollNo;
        this.name = name;
        this.section = section;
        this.attendance = attendance;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRollNo() { return rollNo; }
    public void setRollNo(String rollNo) { this.rollNo = rollNo; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }

    public Integer getAttendance() { return attendance; }
    public void setAttendance(Integer attendance) { this.attendance = attendance; }

    public String getFaceData() { return faceData; }
    public void setFaceData(String faceData) { this.faceData = faceData; }
}
