package io.mawhebty.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import io.mawhebty.models.PostVisibility;

@Repository
public interface PostVisibilityRepository extends JpaRepository<PostVisibility, Integer> {
    Optional<PostVisibility> findByName(String visibility);
}
