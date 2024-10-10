package ma.dev7hd.studentspringngapp.repositories.registrations;

import ma.dev7hd.studentspringngapp.entities.registrations.PendingStudent;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface PendingStudentRepository extends JpaRepository<PendingStudent, String> {

    @Query("SELECT a FROM PendingStudent a WHERE " +
            "(:email IS NULL OR a.email like :email%) ")
    Page<PendingStudent> findByPendingStudentsByFilter(
            @Param("email") String email,
            Pageable pageable);

    boolean existsById(@NotNull String email);

    @Query("SELECT p.photo FROM PendingStudent p WHERE p.email IN :emails")
    List<String> findAllPhotosById(List<String> emails);
}
