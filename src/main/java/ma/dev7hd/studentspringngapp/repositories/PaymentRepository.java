package ma.dev7hd.studentspringngapp.repositories;

import jakarta.transaction.Transactional;
import ma.dev7hd.studentspringngapp.entities.Payment;
import ma.dev7hd.studentspringngapp.enumirat.PaymentStatus;
import ma.dev7hd.studentspringngapp.enumirat.PaymentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

@Transactional
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    List<Payment> findByStudentCode(String studentCode);
    List<Payment> findByStatus(PaymentStatus paymentStatus);
    List<Payment> findByType(PaymentType paymentType);

    @Query("SELECT p FROM Payment p WHERE " +
            "(:code = '' OR p.student.code = :code) AND " +
            "(:email = '' OR p.student.email = :email) AND " +
            "((:min IS NULL AND :max IS NULL) OR " +
            "(:min IS NOT NULL AND :max IS NOT NULL AND p.amount BETWEEN :min AND :max) OR " +
            "(:min IS NOT NULL AND :max IS NULL AND p.amount >= :min) OR " +
            "(:min IS NULL AND :max IS NOT NULL AND p.amount <= :max)) AND " +
            "(:type IS NULL OR p.type = :type) AND " +
            "(:status IS NULL OR p.status = :status)")
    Page<Payment> findByFilters(
            @Param("code") String code,
            @Param("email") String email,
            @Param("min") Double min,
            @Param("max") Double max,
            @Param("type") PaymentType type,
            @Param("status") PaymentStatus status,
            Pageable pageable);

    @Query(value = "SELECT m.month AS month, COALESCE(COUNT(p.id), 0) AS count " +
            "FROM (SELECT 1 AS month UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL " +
            "SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL " +
            "SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10 UNION ALL SELECT 11 UNION ALL " +
            "SELECT 12) m " +
            "LEFT JOIN payment p ON MONTH(p.date) = m.month " +
            "AND (:month IS NULL OR MONTH(p.date) = :month) " +
            "GROUP BY m.month " +
            "ORDER BY m.month ASC",
            nativeQuery = true)
    List<Long[]> countAllPaymentsGroupByDateAndOptionalMonth(@Param("month") Integer month);

}
