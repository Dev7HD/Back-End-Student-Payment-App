package ma.dev7hd.studentspringngapp.repositories;

import ma.dev7hd.studentspringngapp.entities.Admin;
import ma.dev7hd.studentspringngapp.entities.Payment;
import ma.dev7hd.studentspringngapp.entities.PaymentStatusChange;
import ma.dev7hd.studentspringngapp.enumirat.PaymentStatus;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface PaymentStatusChangeRepository extends JpaRepository<PaymentStatusChange, UUID> {

    @Query("SELECT s FROM PaymentStatusChange s WHERE " +
            "(:admin IS NULL OR s.admin = :admin) AND " +
            "(:payment IS NULL OR s.payment = :payment) AND " +
            "(:newStatus IS NULL OR s.newStatus = :newStatus) AND " +
            "(:oldStatus IS NULL OR s.oldStatus = :oldStatus)")
    Page<PaymentStatusChange> findAll(
            @Param("admin") Admin admin,
            @Param("payment") Payment payment,
            @Param("newStatus") PaymentStatus newStatus,
            @Param("oldStatus") PaymentStatus oldStatus,
            @NotNull Pageable pageable);
}
