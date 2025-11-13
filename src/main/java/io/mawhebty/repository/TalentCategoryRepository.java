package io.mawhebty.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import io.mawhebty.models.TalentCategory;

import java.util.Optional;

@Repository
public interface TalentCategoryRepository extends JpaRepository<TalentCategory, Integer> {
    Optional<TalentCategory> findByNameEn(String nameEn);
    Optional<TalentCategory> findByNameAr(String nameAr);
    boolean existsByNameArAndNameEn(String nameEn, String nameAr);
    Optional<TalentCategory> findByPartnerId(Integer partnerId);

}