package io.mawhebty.models;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "subscription_plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(onlyExplicitlyIncluded = true)
public class SubscriptionPlan extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    private Long id;

    @Column(name = "name_en", nullable = false)
    @ToString.Include
    private String nameEn;

    @Column(name = "name_ar", nullable = false)
    @ToString.Include
    private String nameAr;

    @Column(name = "description_en", columnDefinition = "TEXT")
    private String descriptionEn;

    @Column(name = "description_ar", columnDefinition = "TEXT")
    private String descriptionAr;

    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "duration_days", nullable = false)
    private Integer durationDays;

    @Column(name = "is_default", nullable = false, columnDefinition = "boolean default false")
    @Builder.Default
    private Boolean isDefault = false;

    @Column(name = "is_active", nullable = false, columnDefinition = "boolean default true")
    @Builder.Default
    private Boolean isActive = true;
}