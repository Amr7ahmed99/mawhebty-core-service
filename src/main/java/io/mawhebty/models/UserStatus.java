package io.mawhebty.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "user_statuses")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserStatus{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String name; // DRAFT, PENDING_MODERATION, ACTIVE, REJECTED, SUSPENDED
    
}
