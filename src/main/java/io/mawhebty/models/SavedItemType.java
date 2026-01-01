package io.mawhebty.models;

import io.mawhebty.enums.SavedItemTypeEnum;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "saved_item_types")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SavedItemType{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(unique = true, nullable = false)
    private SavedItemTypeEnum name;
}