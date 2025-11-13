package io.mawhebty.models;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;
import jakarta.persistence.Index;

@Entity
@Table(name= "user_otp", indexes = {
    @Index(name = "idx_otp_user", columnList = "user_id"),
    @Index(name = "idx_otp_expiry", columnList = "expiryDate"),
    @Index(name = "idx_otp_used", columnList = "is_used")
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserOTP extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name= "user_id", nullable = false)
    private User user;
    private String hashedCode;
        
    // @ManyToOne
    // @JoinColumn(name = "otp_type_id", nullable = false)
    // private OTPType otpType;
    
    private LocalDateTime expiryDate;

    @Column(name = "is_used", nullable = false, columnDefinition = "boolean default false")
    private boolean isUsed;
}
