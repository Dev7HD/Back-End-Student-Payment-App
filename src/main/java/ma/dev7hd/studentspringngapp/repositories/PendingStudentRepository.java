package ma.dev7hd.studentspringngapp.repositories;

import ma.dev7hd.studentspringngapp.entities.PendingStudent;
import ma.dev7hd.studentspringngapp.enumirat.ProgramID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PendingStudentRepository extends JpaRepository<PendingStudent, String> {
    List<PendingStudent> findAllBySeen(boolean seen);

    @Query("SELECT p FROM PendingStudent p WHERE " +
            "(:email IS NULL OR p.email = :email )")
    Page<PendingStudent> findPendingStudent(
            @Param("email") String email,
            Pageable pageable);
}
