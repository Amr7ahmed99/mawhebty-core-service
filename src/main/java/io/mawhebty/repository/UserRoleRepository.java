package io.mawhebty.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import io.mawhebty.models.UserRole;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Integer> {
    Optional<UserRole> findById(Integer roleId);
}
