package ma.dev7hd.studentspringngapp.repositories;

import ma.dev7hd.studentspringngapp.entities.BanedRegistration;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BanedRegistrationRepository extends JpaRepository<BanedRegistration, String> {
}
