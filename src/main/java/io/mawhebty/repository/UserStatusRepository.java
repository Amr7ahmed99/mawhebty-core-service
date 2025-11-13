package io.mawhebty.repository;

import io.mawhebty.models.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserStatusRepository extends JpaRepository<UserStatus, Long> {
    Optional<UserStatus> findByName(String status);
}
