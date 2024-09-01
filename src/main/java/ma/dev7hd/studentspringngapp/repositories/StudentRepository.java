package ma.dev7hd.studentspringngapp.repositories;

import ma.dev7hd.studentspringngapp.entities.Student;
import ma.dev7hd.studentspringngapp.enumirat.ProgramID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student,String> {
    List<Student> findStudentByProgramId(ProgramID programId);
    Optional<Student> findStudentByCode(String code);
    @Query("SELECT s FROM Student s WHERE " +
            "(:email IS NULL OR s.email LIKE %:email%) AND " +
            "(:firstName IS NULL OR s.firstName LIKE %:firstName%) AND " +
            "(:lastName IS NULL OR s.lastName LIKE %:lastName%) AND " +
            "(:code IS NULL OR s.code = :code) AND " +
            "(:programId IS NULL OR s.programId = :programId)")
    Page<Student> findByFilter(
            @Param("email") String email,
            @Param("firstName") String firstName,
            @Param("lastName") String lastName,
            @Param("programId") ProgramID programID,
            @Param("code") String code,
            Pageable pageable);

}

