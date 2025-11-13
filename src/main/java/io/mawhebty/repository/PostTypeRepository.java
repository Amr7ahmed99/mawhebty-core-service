package io.mawhebty.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import io.mawhebty.models.PostType;

@Repository
public interface PostTypeRepository extends JpaRepository<PostType, Integer> {
    Optional<PostType> findByName(String type);
}
