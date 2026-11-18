package com.foodbridge.repository;
import com.foodbridge.entity.Domain;
import com.foodbridge.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmail(String email);
    boolean existsByEmail(String email);
    List<UserEntity> findAllByRoleOrderByCreatedAtDesc(Domain.Role role);
    List<UserEntity> findAllByRoleAndVerifiedFalseOrderByCreatedAtAsc(Domain.Role role);
    long countByRole(Domain.Role role);
}
