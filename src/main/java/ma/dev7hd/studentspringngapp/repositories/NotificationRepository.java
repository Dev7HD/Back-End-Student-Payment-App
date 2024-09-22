package ma.dev7hd.studentspringngapp.repositories;

import ma.dev7hd.studentspringngapp.entities.Admin;
import ma.dev7hd.studentspringngapp.entities.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("SELECT n FROM Notification n WHERE :admin NOT MEMBER OF n.adminRemover ORDER BY n.registerDate")
    List<Notification> findAllByAdminRemoverIsNot(Admin admin);

    @Query("SELECT n FROM Notification n " +
            "WHERE :admin NOT MEMBER OF n.adminRemover AND " +
            "(:seen IS NULL OR n.seen = :seen)" +
            "ORDER BY n.registerDate")
    Page<Notification> findAllWithPagination(Admin admin, Boolean seen, Pageable pageable);

}
