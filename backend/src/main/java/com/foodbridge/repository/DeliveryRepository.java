package com.foodbridge.repository;
import com.foodbridge.entity.DeliveryEntity;
import com.foodbridge.entity.Domain;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface DeliveryRepository extends JpaRepository<DeliveryEntity, Long> {
    List<DeliveryEntity> findAllByVolunteerIsNullAndStatusOrderByCreatedAtDesc(Domain.DeliveryStatus status);
    List<DeliveryEntity> findAllByVolunteer_IdOrderByCreatedAtDesc(Long volunteerId);
    long countByStatus(Domain.DeliveryStatus status);
    long countByVolunteer_IdAndStatus(Long volunteerId, Domain.DeliveryStatus status);
}
