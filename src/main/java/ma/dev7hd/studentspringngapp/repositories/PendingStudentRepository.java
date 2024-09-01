package ma.dev7hd.studentspringngapp.repositories;

import ma.dev7hd.studentspringngapp.entities.PendingStudent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PendingStudentRepository extends JpaRepository<PendingStudent, String> {
}
