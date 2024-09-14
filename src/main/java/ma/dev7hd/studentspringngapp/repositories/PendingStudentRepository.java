package ma.dev7hd.studentspringngapp.repositories;

import ma.dev7hd.studentspringngapp.entities.PendingStudent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PendingStudentRepository extends JpaRepository<PendingStudent, String> {

    @Query("SELECT a FROM PendingStudent a WHERE " +
            "(:email IS NULL OR a.email = :email) AND " +
            "(:isSeen IS NULL OR a.seen = :isSeen) ")
    Page<PendingStudent> findByPendingStudentsByFilter(
            @Param("email") String email,
            @Param("isSeen") boolean isSeen,
            Pageable pageable);

    List<PendingStudent> findAllBySeen(boolean isSeen);
}
