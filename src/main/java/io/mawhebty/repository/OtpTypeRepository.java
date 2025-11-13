package io.mawhebty.repository;

import io.mawhebty.models.OTPType;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OtpTypeRepository extends JpaRepository<OTPType, Integer> {
    Optional<OTPType> findByName(String type);
}