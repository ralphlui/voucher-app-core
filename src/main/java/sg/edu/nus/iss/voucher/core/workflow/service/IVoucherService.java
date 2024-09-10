package sg.edu.nus.iss.voucher.core.workflow.service;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Pageable;

import sg.edu.nus.iss.voucher.core.workflow.dto.VoucherDTO;
import sg.edu.nus.iss.voucher.core.workflow.entity.Campaign;
import sg.edu.nus.iss.voucher.core.workflow.entity.Voucher;

public interface IVoucherService {

	VoucherDTO findByVoucherId(String voucherId) throws Exception;
	
	VoucherDTO
	findVoucherByCampaignIdAndUserId(Campaign campaign, String userId); 
	
	 List<Voucher> findVoucherListByCampaignId(String campaignId);
	 
	 VoucherDTO claimVoucher(Voucher voucher) throws Exception;
	 
	 Map<Long, List<VoucherDTO>> findByClaimedBy(String claimedBy,Pageable pageable);

	 Map<Long, List<VoucherDTO>> findAllClaimedVouchersByCampaignId(String campaignId,Pageable pageable);
}
