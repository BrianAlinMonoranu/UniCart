package app.db;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface productRepository extends JpaRepository<Product, Long> {

    List<Product> findById(int id);
}
