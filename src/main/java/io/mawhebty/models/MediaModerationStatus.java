package io.mawhebty.models;

import io.mawhebty.enums.MediaModerationStatusEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "media_moderation_statuses")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaModerationStatus{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false)
    @Builder.Default
    private String name= MediaModerationStatusEnum.PENDING.getName();
}
