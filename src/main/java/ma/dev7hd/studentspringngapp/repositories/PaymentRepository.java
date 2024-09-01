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

    /*@Query("SELECT p FROM Payment p WHERE " +
            "(:type IS NULL OR p.type = :type) AND " +
            "(:status IS NULL OR p.status = :status)" )
    Page<Payment> findByFilterTypeAndStatus(
            @Param("type") PaymentType type,
            @Param("status") PaymentStatus status,
            Pageable pageable);

    @Query("SELECT p FROM Payment p WHERE " +
            "p.amount BETWEEN :min AND :max AND " +
            "(:paymentType IS NULL OR p.type = :paymentType) AND " +
            "(:paymentStatus IS NULL OR p.status = :paymentStatus)")
    Page<Payment> findByAmountBetweenAndTypeAndStatus(
            @Param("min") double min,
            @Param("max") double max,
            @Param("paymentType") PaymentType paymentType,
            @Param("paymentStatus") PaymentStatus paymentStatus,
            Pageable pageable);

    @Query("SELECT p FROM Payment p WHERE " +
            "p.student.code = :code AND " +
            "(:paymentStatus IS NULL OR p.status = :paymentStatus) AND " +
            "(:paymentType IS NULL OR p.type = :paymentType)")
    Page<Payment> findByStudentCodeAndStatusAndType(
            @Param("code") String code,
            @Param("paymentStatus") PaymentStatus paymentStatus,
            @Param("paymentType") PaymentType paymentType,
            Pageable pageable);*/

    @Query("SELECT p FROM Payment p WHERE " +
            "(:code = '' OR p.student.code = :code) AND " +
            "((:min IS NULL AND :max IS NULL) OR " +
            "(:min IS NOT NULL AND :max IS NOT NULL AND p.amount BETWEEN :min AND :max) OR " +
            "(:min IS NOT NULL AND :max IS NULL AND p.amount >= :min) OR " +
            "(:min IS NULL AND :max IS NOT NULL AND p.amount <= :max)) AND " +
            "(:type IS NULL OR p.type = :type) AND " +
            "(:status IS NULL OR p.status = :status)")
    Page<Payment> findByFilters(
            @Param("code") String code,
            @Param("min") Double min,
            @Param("max") Double max,
            @Param("type") PaymentType type,
            @Param("status") PaymentStatus status,
            Pageable pageable);


}
