package io.mawhebty.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "user_role")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserRole extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

//    @Enumerated(EnumType.STRING)
//    @Column(unique = true, nullable = false)
//    private UserRoleEnum name;

    @Column(unique = true, nullable = false)
    private String name;// TALENT, RESEARCHER

    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PermissionRole> permissionRoles = new ArrayList<>();
}