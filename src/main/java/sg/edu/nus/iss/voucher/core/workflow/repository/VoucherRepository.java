package sg.edu.nus.iss.voucher.core.workflow.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import sg.edu.nus.iss.voucher.core.workflow.entity.Campaign;
import sg.edu.nus.iss.voucher.core.workflow.entity.Voucher;

public interface VoucherRepository extends JpaRepository<Voucher, String> {

	@Query("SELECT v FROM Voucher v WHERE v.campaign= ?1 AND v.claimedBy = ?2")
	Voucher findByCampaignAndClaimedBy(Campaign campaign,String claimedBy);
	
	 List<Voucher> findByCampaignCampaignId(String campaignId);

}
