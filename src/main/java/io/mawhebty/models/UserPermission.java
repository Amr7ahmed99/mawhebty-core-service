package io.mawhebty.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user_permissions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"name"}))
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false)
    private String name;

//    @Column(name = "is_limited", nullable = false, columnDefinition = "boolean default false")
//    private boolean isLimited;

    @OneToMany(mappedBy = "permission", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PermissionRole> permissionRoles = new ArrayList<>();
}
