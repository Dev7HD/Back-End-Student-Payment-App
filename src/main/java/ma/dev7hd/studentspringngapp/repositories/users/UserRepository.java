package ma.dev7hd.studentspringngapp.repositories.users;

import ma.dev7hd.studentspringngapp.entities.users.User;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);

    boolean existsById(@NotNull String email);
}
