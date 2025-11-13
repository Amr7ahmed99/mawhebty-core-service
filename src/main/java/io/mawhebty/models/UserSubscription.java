package io.mawhebty.models;

import io.mawhebty.enums.SubscriptionStatusEnum;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_subscriptions", indexes = {
        @Index(name = "idx_user_subscription_user", columnList = "user_id"),
        @Index(name = "idx_user_subscription_plan", columnList = "plan_id"),
        @Index(name = "idx_user_subscription_status", columnList = "status_id"),
        @Index(name = "idx_user_subscription_expires", columnList = "expires_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(onlyExplicitlyIncluded = true)
public class UserSubscription extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private SubscriptionPlan plan;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

//    @Enumerated(EnumType.STRING)
//    @Column(name = "status", nullable = false)
//    @Builder.Default
//    private SubscriptionStatusEnum status = SubscriptionStatusEnum.ACTIVE;

    @Column(name = "status_id", nullable = false)
    private Integer statusId;

    @Transient
    public SubscriptionStatusEnum getStatus() {
        return SubscriptionStatusEnum.fromId(this.statusId);
    }

    @Column(name = "auto_renew", nullable = false, columnDefinition = "boolean default true")
    @Builder.Default
    private Boolean autoRenew = true;

    @Column(name = "stripe_subscription_id")
    private String stripeSubscriptionId;
}