package io.mawhebty.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
        import lombok.*;

import java.util.Arrays;
import java.util.List;

@Entity
@Table(
        name = "talent_category_form_values",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"talent_profile_id", "form_key_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(onlyExplicitlyIncluded = true)
public class TalentCategoryFormValue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(columnDefinition = "TEXT")
    private String value;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form_key_id", nullable = false)
    @JsonIgnore
    private TalentCategoryFormKeys formKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "talent_profile_id", nullable = false)
    @JsonIgnore
    private TalentProfile talentProfile;

    public List<String> getValuesAsList() {
        if (value == null || value.isBlank()) return List.of();
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(v -> !v.isEmpty())
                .toList();
    }

    public void setValuesFromList(List<String> values) {
        this.value = String.join(",", values);
    }
}
