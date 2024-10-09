package ma.dev7hd.studentspringngapp.repositories.users;

import ma.dev7hd.studentspringngapp.entities.users.Student;
import ma.dev7hd.studentspringngapp.enumirat.ProgramID;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface StudentRepository extends JpaRepository<Student,String> {
    List<Student> findStudentByProgramId(ProgramID programId);
    boolean existsByEmail(@NotNull String email);

    boolean existsByCode(@NotNull String code);

    Optional<Student> findStudentByCode(String code);
    @Query("SELECT s FROM Student s WHERE " +
            "(:email IS NULL OR s.email LIKE %:email%) AND " +
            "(:firstName IS NULL OR s.firstName LIKE %:firstName%) AND " +
            "(:lastName IS NULL OR s.lastName LIKE %:lastName%) AND " +
            "(:code IS NULL OR :code = '' OR s.code = :code) AND " +
            "(:programId IS NULL OR s.programId = :programId)")
    Page<Student> findByFilter(
            @Param("email") String email,
            @Param("firstName") String firstName,
            @Param("lastName") String lastName,
            @Param("programId") ProgramID programID,
            @Param("code") String code,
            Pageable pageable);

    Integer countByProgramId(ProgramID programId);

    @Query("SELECT s.email FROM Student s")
    Set<String> findAllEmails();

    @Query("SELECT MAX(SUBSTRING(s.code, LENGTH(s.code) - 3)) FROM Student s ")
    Optional<Integer> findLastIdentifierByProgram();
}

