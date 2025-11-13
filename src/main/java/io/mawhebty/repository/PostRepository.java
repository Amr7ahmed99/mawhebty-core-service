package io.mawhebty.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import io.mawhebty.models.Post;
import io.mawhebty.models.PostType;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    Optional<Post> findByOwnerUserId(Long ownerUserId);

    Optional<Post> findByOwnerUserIdAndType(Long ownerUserId, PostType type);

}
