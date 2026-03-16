package lunis.work.mindflow.user;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByIdAndEmail(Long id, String email);

    boolean existsByEmail(String email);
}
