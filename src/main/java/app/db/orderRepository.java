package app.db;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface orderRepository extends JpaRepository<Order, Long> {
    List<Order> findById(long id);

    List<Order> findByuserName(String userName);

}
