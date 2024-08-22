package ma.dev7hd.studentspringngapp.repositories;

import ma.dev7hd.studentspringngapp.entities.PaymentStatusChange;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PaymentStatusChangeRepository extends JpaRepository<PaymentStatusChange, UUID> {

}
