package io.mawhebty.models;

import io.mawhebty.enums.UserTypeEnum;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_types")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserType{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(unique = true, nullable = false)
    @Builder.Default
    private UserTypeEnum type= UserTypeEnum.INDIVIDUAL;
}