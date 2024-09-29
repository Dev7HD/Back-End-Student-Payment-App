package ma.dev7hd.studentspringngapp.repositories.notifications.admin;

import ma.dev7hd.studentspringngapp.entities.users.Admin;
import ma.dev7hd.studentspringngapp.entities.notifications.admins.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("SELECT n FROM Notification n WHERE :admin NOT MEMBER OF n.adminRemover ORDER BY n.seen LIMIT 10")
    List<Notification> findAllByAdminRemoverIsNot(Admin admin);

    @Query("SELECT n FROM Notification n " +
            "WHERE :admin NOT MEMBER OF n.adminRemover AND " +
            "(:seen IS NULL OR n.seen = :seen) " +
            "ORDER BY n.registerDate")
    Page<Notification> findAllWithPagination(Admin admin, Boolean seen, Pageable pageable);

    @Query("SELECT COUNT(n) FROM Notification n WHERE :admin NOT MEMBER OF n.adminRemover AND n.seen = FALSE ")
    Long countAdminNotifications(@Param("admin") Admin admin);

}
