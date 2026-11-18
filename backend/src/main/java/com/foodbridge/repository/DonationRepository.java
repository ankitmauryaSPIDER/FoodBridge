package com.foodbridge.repository;
import com.foodbridge.entity.DonationEntity;
import com.foodbridge.entity.Domain;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
public interface DonationRepository extends JpaRepository<DonationEntity, Long> {
    List<DonationEntity> findAllByStatusAndPickupDeadlineAfterOrderByCreatedAtDesc(Domain.DonationStatus status, LocalDateTime now);
    List<DonationEntity> findAllByDonor_IdOrderByCreatedAtDesc(Long donorId);
    List<DonationEntity> findAllByStatusOrderByCreatedAtDesc(Domain.DonationStatus status);
    List<DonationEntity> findAllByStatusAndPickupDeadlineBefore(Domain.DonationStatus status, LocalDateTime now);
    long countByDonor_Id(Long donorId);
    long countByDonor_IdAndStatus(Long donorId, Domain.DonationStatus status);
    long countByStatus(Domain.DonationStatus status);
}
