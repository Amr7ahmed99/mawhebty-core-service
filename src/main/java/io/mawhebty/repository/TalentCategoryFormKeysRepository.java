package io.mawhebty.repository;

import io.mawhebty.models.TalentCategoryFormKeys;
import io.mawhebty.models.TalentCategory;
import io.mawhebty.models.TalentSubCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TalentCategoryFormKeysRepository extends JpaRepository<TalentCategoryFormKeys, Integer> {

    Optional<TalentCategoryFormKeys> findByFieldKeyId(Integer fieldKeyId);
    List<TalentCategoryFormKeys> findByFieldKeyIdIn(List<Integer> fieldKeyIds);
    List<TalentCategoryFormKeys> findAllByTalentCategory(TalentCategory talentCategory);
    boolean existsByFieldKeyIdAndTalentCategory(Integer fieldKeyId, TalentCategory category);
    boolean existsByFieldKeyIdAndTalentCategoryAndTalentSubCategory(Integer fieldKeyId, TalentCategory category, TalentSubCategory subCategory);

}
