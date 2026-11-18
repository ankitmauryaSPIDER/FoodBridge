package com.foodbridge.repository;
import com.foodbridge.entity.DonationRequestEntity;
import com.foodbridge.entity.Domain;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface DonationRequestRepository extends JpaRepository<DonationRequestEntity, Long> {
    List<DonationRequestEntity> findAllByNgo_IdOrderByCreatedAtDesc(Long ngoId);
    List<DonationRequestEntity> findAllByDonation_Donor_IdOrderByCreatedAtDesc(Long donorId);
    Optional<DonationRequestEntity> findByDonation_IdAndNgo_Id(Long donationId, Long ngoId);
    List<DonationRequestEntity> findAllByDonation_IdAndRequestStatus(Long donationId, Domain.RequestStatus status);
    long countByNgo_Id(Long ngoId);
    long countByRequestStatus(Domain.RequestStatus status);
}
