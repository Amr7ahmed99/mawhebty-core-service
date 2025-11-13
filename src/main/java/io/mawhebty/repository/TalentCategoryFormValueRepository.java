package io.mawhebty.repository;

import io.mawhebty.models.TalentCategoryFormValue;
import io.mawhebty.models.TalentProfile;
import io.mawhebty.models.TalentCategoryFormKeys;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TalentCategoryFormValueRepository extends JpaRepository<TalentCategoryFormValue, Integer> {

    List<TalentCategoryFormValue> findAllByTalentProfile(TalentProfile talentProfile);

    List<TalentCategoryFormValue> findAllByFormKey(TalentCategoryFormKeys formKey);

    Optional<TalentCategoryFormValue> findByTalentProfileAndFormKey(TalentProfile talentProfile, TalentCategoryFormKeys formKey);

    void deleteAllByTalentProfile(TalentProfile talentProfile);
}
