package com.grade.system.service;

import com.grade.system.entity.Course;
import com.grade.system.entity.Grade;
import com.grade.system.entity.User;
import com.grade.system.repository.CourseRepository;
import com.grade.system.repository.GradeRepository;
import com.grade.system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GradeService {

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Grade> getAllGrades() {
        return gradeRepository.findAll();
    }

    public List<Grade> getGradesByStudent(Long studentId) {
        return gradeRepository.findByStudentId(studentId);
    }

    public List<Grade> getGradesByTeacher(Long teacherId) {
        List<Course> courses = courseRepository.findByTeacherId(teacherId);
        List<Long> courseIds = courses.stream().map(Course::getId).collect(Collectors.toList());
        if (courseIds.isEmpty()) {
            return new ArrayList<>();
        }
        return gradeRepository.findByCourseIdIn(courseIds);
    }

    public List<Grade> getGradesByClass(String className) {
        List<User> students = userRepository.findByClassName(className);
        List<Long> studentIds = students.stream()
                .filter(u -> "STUDENT".equals(u.getRole()))
                .map(User::getId)
                .collect(Collectors.toList());
        if (studentIds.isEmpty()) {
            return new ArrayList<>();
        }
        return gradeRepository.findByStudentIdIn(studentIds);
    }

    public Grade saveGrade(Grade grade) {
        return gradeRepository.save(grade);
    }

    public Grade updateGrade(Long id, Grade gradeDetails) {
        Grade grade = gradeRepository.findById(id).orElseThrow(() -> new RuntimeException("Grade not found"));
        grade.setScore(gradeDetails.getScore());
        grade.setMakeupScore(gradeDetails.getMakeupScore());
        grade.setTerm(gradeDetails.getTerm());
        return gradeRepository.save(grade);
    }

    public void deleteGrade(Long id) {
        gradeRepository.deleteById(id);
    }
}
