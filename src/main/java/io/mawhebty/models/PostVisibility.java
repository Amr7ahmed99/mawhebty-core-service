package io.mawhebty.models;

import io.mawhebty.enums.PostVisibilityEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "post_visibility")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostVisibility{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;


    @Column(unique = true, nullable = false)
    @Builder.Default
    private String name= PostVisibilityEnum.PUBLIC.getName(); // DRAFT, PENDING_MODERATION, PUBLISHED, REJECTED
}
