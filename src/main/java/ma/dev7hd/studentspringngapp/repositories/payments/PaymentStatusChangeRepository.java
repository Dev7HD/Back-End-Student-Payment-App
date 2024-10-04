package ma.dev7hd.studentspringngapp.repositories.payments;

import ma.dev7hd.studentspringngapp.entities.payments.Payment;
import ma.dev7hd.studentspringngapp.entities.payments.PaymentStatusChange;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PaymentStatusChangeRepository extends JpaRepository<PaymentStatusChange, UUID> {

    Optional<PaymentStatusChange> findByPayment(Payment payment);
}
