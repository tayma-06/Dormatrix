package repo;

import models.users.Student;
import libraries.collections.MyArrayList;
import libraries.collections.MyOptional;

public interface StudentRepository {
    // finds a student by username
    MyOptional<Student> findByUsername(String username);
    // finds a student by student id
    MyOptional<Student> findById(String studentId);
    // returns all students
    MyArrayList<Student> findAll();
}
