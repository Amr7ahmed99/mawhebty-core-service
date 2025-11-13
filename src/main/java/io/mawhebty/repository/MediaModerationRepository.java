package io.mawhebty.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import io.mawhebty.models.MediaModeration;

@Repository
public interface MediaModerationRepository extends JpaRepository<MediaModeration, Long> {
}