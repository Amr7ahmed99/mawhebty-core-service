package io.mawhebty.models;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "permission_role")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PermissionRole extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_id", nullable = false)
    private UserPermission permission;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private UserRole role;
}