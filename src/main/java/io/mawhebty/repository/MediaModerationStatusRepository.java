package io.mawhebty.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import io.mawhebty.models.MediaModerationStatus;

@Repository
public interface MediaModerationStatusRepository extends JpaRepository<MediaModerationStatus, Integer> {
    Optional<MediaModerationStatus> findByName(String status);
}
