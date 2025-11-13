package io.mawhebty.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "post_types")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostType{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false)
    private String name; // PROFILE_VIDEO, REEL, IMAGE, REGISTRATION_FILE, etc.
}
