package io.mawhebty.repository;

import io.mawhebty.models.TalentSubCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TalentSubCategoryRepository extends JpaRepository<TalentSubCategory, Integer> {
    Optional<TalentSubCategory> findByNameEn(String nameEn);
    Optional<TalentSubCategory> findByNameAr(String nameAr);
    boolean existsByNameArAndNameEn(String nameEn, String nameAr);
    Optional<TalentSubCategory> findByPartnerId(Integer partnerId);

}