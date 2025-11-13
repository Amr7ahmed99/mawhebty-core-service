package io.mawhebty.repository;

import io.mawhebty.models.UserPermission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserPermissionRepository extends JpaRepository<UserPermission, Integer> {
    Optional<UserPermission> findByName(String name);
}