package io.mawhebty.repository;

import io.mawhebty.models.SavedItemType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SavedItemTypeRepository extends JpaRepository<SavedItemType, Integer> {
}