package app.db;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface userRepository extends JpaRepository<User, Long> {
    List<User> findByUsername(String username);

    List<User> findById(int id);

    boolean existsByUsername(String username);

    List<User> findByUsernameAndPassword(String username, String password);
}
