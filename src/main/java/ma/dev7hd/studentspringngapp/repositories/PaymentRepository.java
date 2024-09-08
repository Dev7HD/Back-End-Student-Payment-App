package ma.dev7hd.studentspringngapp.repositories;

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

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    List<Payment> findByStudentCode(String studentCode);
    List<Payment> findByStatus(PaymentStatus paymentStatus);
    List<Payment> findByType(PaymentType paymentType);

    void deleteByStudentCode(String studentCode);

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

    /*@Query("SELECT FUNCTION('MONTH', p.date) AS month, COUNT(p) AS count " +
            "FROM Payment p " +
            "WHERE (:month IS NULL OR FUNCTION('MONTH', p.date) = :month) " +
            "GROUP BY FUNCTION('MONTH', p.date)")
    Map<String, Integer> countPaymentsByMonth(@Param("month") Integer month);

    @Query("SELECT MONTH(p.date) AS mounth, COUNT(p) AS count " +
            "FROM Payment p " +
            "WHERE (:month IS NULL OR MONTH(p.date) = :month) " +
            "GROUP BY MONTH(p.date)")
    List<Object[]> countAllPaymentsGroupByDateAndOptionalMonth(@Param("month") Integer month);*/

    @Query("SELECT MONTH(p.date) AS month, COUNT(p) AS count " +
            "FROM Payment p " +
            "WHERE (:month IS NULL OR MONTH(p.date) = :month) " +
            "GROUP BY MONTH(p.date)" +
            "ORDER BY MONTH(p.date) ASC")
    List<Long[]> countAllPaymentsGroupByDateAndOptionalMonth(@Param("month") Integer month);
}
