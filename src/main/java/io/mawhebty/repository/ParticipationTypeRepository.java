package io.mawhebty.repository;

import io.mawhebty.models.ParticipationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ParticipationTypeRepository extends JpaRepository<ParticipationType, Integer> {
    Optional<ParticipationType> findById(Integer id);
}