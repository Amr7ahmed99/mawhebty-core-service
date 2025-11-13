package io.mawhebty.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import io.mawhebty.models.PostStatus;

@Repository
public interface PostStatusRepository extends JpaRepository<PostStatus, Integer> {
    Optional<PostStatus> findByName(String status);
}
