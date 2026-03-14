package com.example.cseghrender.seeder;

import com.example.cseghrender.model.Student;
import com.example.cseghrender.repository.StudentRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class DataSeeder implements CommandLineRunner {

    private final StudentRepository studentRepository;

    public DataSeeder(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (studentRepository.count() == 0) {
            System.out.println("Seeding 330 students...");
            List<Student> students = new ArrayList<>();
            String[] sections = {"a", "b", "c", "d", "e"};
            Random random = new Random();

            for (String section : sections) {
                for (int i = 1; i <= 66; i++) {
                    String number = String.format("%02d", i);
                    String rollNo = "23eg101" + section + number;
                    String formattedSection = section.toUpperCase();
                    String name = "Student " + formattedSection + " " + number;
                    // generate a random realistic attendance between 60 and 100
                    int attendance = 60 + random.nextInt(41);
                    students.add(new Student(rollNo, name, formattedSection, attendance));
                }
            }

            studentRepository.saveAll(students);
            System.out.println("Seeding complete! 330 students generated.");
        } else {
            System.out.println("Students data already exists, skipping seed.");
        }
    }
}
