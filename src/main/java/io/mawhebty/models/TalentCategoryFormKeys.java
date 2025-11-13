package io.mawhebty.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "talent_category_form_keys",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"talent_category_id", "talent_sub_category_id", "field_key_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(onlyExplicitlyIncluded = true)
public class TalentCategoryFormKeys {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    private Integer id;

    @Column(nullable = false)
    private Integer fieldKeyId; // ID from Dashboard

    @Column(nullable = false)
    private String nameEn;

    @Column(nullable = false)
    private String nameAr;

    @Column(nullable = false)
    private String fieldType; // text, select, checkbox...

    @Column(nullable = false)
    @Builder.Default
    private Boolean isRequired = false;

//    @Column
//    private Integer sortOrder = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "talent_category_id")
    @JsonIgnore
    private TalentCategory talentCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "talent_sub_category_id")
    @JsonIgnore
    private TalentSubCategory talentSubCategory;

    @OneToMany(mappedBy = "formKey", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @Builder.Default
    private List<TalentCategoryFormValue> values = new ArrayList<>();

}
