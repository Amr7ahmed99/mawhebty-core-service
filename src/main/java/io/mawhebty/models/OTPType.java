package io.mawhebty.models;

import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "otp_types")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OTPType{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String name; // REGISTRATION, PASSWORD_RESET

//    @Column(nullable = true)
//    private String description;
}