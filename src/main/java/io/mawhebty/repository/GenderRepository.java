package io.mawhebty.repository;

import io.mawhebty.models.Gender;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GenderRepository extends JpaRepository<Gender, Long> {
        Optional<Gender> findByName(String name);
}