package com.example.ces.controller;

import com.example.ces.model.Course;
import com.example.ces.model.CourseSelectionWindow;
import com.example.ces.model.Student;
import com.example.ces.repository.CourseRepository;
import com.example.ces.repository.CourseSelectionWindowRepository;
import com.example.ces.repository.EnrollmentRepository;
import com.example.ces.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CourseSelectionWindowRepository courseSelectionWindowRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private Long studentId;
    private Long courseId;

    @BeforeEach
    public void setUp() {
        studentRepository.deleteAll();
        courseRepository.deleteAll();
        courseSelectionWindowRepository.deleteAll();
        enrollmentRepository.deleteAll();

        Student student1 = new Student();
        student1.setName("John Doe");
        studentId = studentRepository.save(student1).getId();

        Course course = new Course();
        course.setName("Mathematics");
        course.setMaxCapacity(2);
        course.setEnrolledStudents(0);
        courseId = courseRepository.save(course).getId();

        CourseSelectionWindow window = new CourseSelectionWindow();
        window.setCourse(course);
        window.setStartTime(LocalDateTime.parse("2024-10-02 00:00:00", DATE_TIME_FORMATTER));
        window.setEndTime(LocalDateTime.parse("2024-10-12 00:00:00", DATE_TIME_FORMATTER));
        courseSelectionWindowRepository.save(window);
    }

    @Test
    public void testEnrollStudent_Success() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/courses/enroll")
                        .param("studentId", String.valueOf(studentId))
                        .param("courseId", String.valueOf(courseId)))
                .andExpect(status().isOk())
                .andExpect(content().string("Student enrolled successfully"));

        // Проверка обновленного количества записанных студентов
        Course course = courseRepository.findById(courseId).orElseThrow();
        assert (course.getEnrolledStudents() == 1);
    }

    @Test
    public void testEnrollStudent_Failure_NoSeats() throws Exception {
        // Заполняем курс, чтобы не было мест
        Course course = courseRepository.findById(courseId).orElseThrow();
        course.setEnrolledStudents(course.getMaxCapacity());
        courseRepository.save(course);

        // Act & Assert
        mockMvc.perform(post("/api/courses/enroll")
                        .param("studentId", String.valueOf(studentId))
                        .param("courseId", String.valueOf(courseId)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetAllCourses() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/courses/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Mathematics"));
    }

    @Test
    public void testGetCourseById_Success() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/courses/" + courseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Mathematics"));
    }

    @Test
    public void testGetCourseById_NotFound() throws Exception {
        // Arrange
        Long courseId = 999L; // Неверный ID курса

        // Act & Assert
        mockMvc.perform(get("/api/courses/" + courseId))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testEnrollStudent_Failure_InvalidCourseId() throws Exception {
        // Arrange
        Long invalidCourseId = 999L; // Неверный ID курса

        // Act & Assert
        mockMvc.perform(post("/api/courses/enroll")
                        .param("studentId", String.valueOf(studentId))
                        .param("courseId", String.valueOf(invalidCourseId)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testEnrollStudent_Failure_WindowClosed() throws Exception {
        // Создаем окно записи, которое уже закрыто
        courseSelectionWindowRepository.deleteAll();
        CourseSelectionWindow window = new CourseSelectionWindow();
        window.setCourse(courseRepository.findById(courseId).orElseThrow());
        window.setStartTime(LocalDateTime.parse("2024-09-25 00:00:00", DATE_TIME_FORMATTER)); // Начало окна записи - в прошлом
        window.setEndTime(LocalDateTime.parse("2024-09-30 00:00:00", DATE_TIME_FORMATTER));   // Конец окна записи - в прошлом
        courseSelectionWindowRepository.save(window);

        // Act & Assert
        mockMvc.perform(post("/api/courses/enroll")
                        .param("studentId", String.valueOf(studentId))
                        .param("courseId", String.valueOf(courseId)))
                .andExpect(status().isBadRequest());
    }
}