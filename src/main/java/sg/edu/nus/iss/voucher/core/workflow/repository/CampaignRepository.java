package sg.edu.nus.iss.voucher.core.workflow.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import sg.edu.nus.iss.voucher.core.workflow.entity.Campaign;
import sg.edu.nus.iss.voucher.core.workflow.enums.CampaignStatus;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, String> {

    
    @Query("SELECT c FROM Campaign c WHERE c.campaignStatus IN ?1")
    List<Campaign> findByCampaignStatusIn(List<CampaignStatus> statuses);

    Page<Campaign> findByStoreStoreId(String storeId,Pageable pageable);
    
    Page<Campaign> findByStoreStoreIdAndCampaignStatus(String storeId,CampaignStatus status,Pageable pageable);

    Page<Campaign> findByCreatedBy(String email,Pageable pageable);
    
    List<Campaign>  findByDescription(String description);
    
    @Query("SELECT c FROM Campaign c WHERE c.campaignStatus IN ?1")
    Page<Campaign> findByCampaignStatusIn(List<CampaignStatus> statuses,Pageable pageable);
    
    List<Campaign> findByEndDateBefore(LocalDateTime currentDate);

}