package io.mawhebty.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "genders")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Gender {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;
}