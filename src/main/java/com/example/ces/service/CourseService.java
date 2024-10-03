package com.example.ces.service;

import com.example.ces.dto.CourseDto;
import com.example.ces.exception.CourseFullException;
import com.example.ces.exception.NotFoundException;
import com.example.ces.exception.WindowClosedException;
import com.example.ces.model.Course;
import com.example.ces.model.CourseSelectionWindow;
import com.example.ces.model.Enrollment;
import com.example.ces.model.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.ces.repository.CourseRepository;
import com.example.ces.repository.CourseSelectionWindowRepository;
import com.example.ces.repository.EnrollmentRepository;
import com.example.ces.repository.StudentRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CourseService {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private CourseSelectionWindowRepository courseSelectionWindowRepository;

    //логика записи студента на курс
    @Transactional
    public String enrollStudent(Long studentId, Long courseId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new NotFoundException("Student not found"));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NotFoundException("Course not found"));

        CourseSelectionWindow window = courseSelectionWindowRepository.findByCourse(course);
        LocalDateTime now = LocalDateTime.now();

        if (window == null || now.isBefore(window.getStartTime()) || now.isAfter(window.getEndTime())) {
            throw new WindowClosedException("Enrollment window is closed");
        }

        //Synchronized позволит нам отслеживать наполнение курса, и не даст записать студента когда лимит будет достигнут
        synchronized (this) {
            if (course.getEnrolledStudents() >= course.getMaxCapacity()) {
                throw new CourseFullException("No available seats in the course");
            }

            Enrollment enrollment = new Enrollment();
            enrollment.setStudent(student);
            enrollment.setCourse(course);
            enrollment.setEnrollmentDate(now);

            enrollmentRepository.save(enrollment);

            course.setEnrolledStudents(course.getEnrolledStudents() + 1);
            courseRepository.save(course);
        }

        return "Student enrolled successfully";
    }

    //логика поиска всех курсов с наличием пустых мест на курсах
    public List<CourseDto> getAllCoursesWithAvailableSpots() {
        return courseRepository.findAll().stream().map(course -> {
            int occupied = course.getEnrolledStudents();
            int available = course.getMaxCapacity() - occupied;
            return new CourseDto(course.getId(), course.getName(), occupied, available);
        }).collect(Collectors.toList());
    }

    //логика поиска курса по id с наличием пустых мест на курсе
    public CourseDto getCourseById(Long courseId) {
        Optional<Course> courseOptional = courseRepository.findById(courseId);
        if (courseOptional.isPresent()) {
            Course course = courseOptional.get();
            int occupied = course.getEnrolledStudents();
            int available = course.getMaxCapacity() - occupied;
            return new CourseDto(course.getId(), course.getName(), occupied, available);
        } else {
            throw new NotFoundException("Course with id " + courseId + " not found.");
        }
    }
}
