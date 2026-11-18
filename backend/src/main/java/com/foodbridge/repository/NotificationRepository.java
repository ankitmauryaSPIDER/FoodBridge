package com.foodbridge.repository;
import com.foodbridge.entity.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {
    List<NotificationEntity> findTop50ByUser_IdOrderByCreatedAtDesc(Long userId);
}
