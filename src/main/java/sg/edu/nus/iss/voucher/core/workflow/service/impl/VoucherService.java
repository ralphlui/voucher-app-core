package sg.edu.nus.iss.voucher.core.workflow.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import sg.edu.nus.iss.voucher.core.workflow.api.connector.AuthAPICall;
import sg.edu.nus.iss.voucher.core.workflow.dto.VoucherDTO;
import sg.edu.nus.iss.voucher.core.workflow.entity.Campaign;
import sg.edu.nus.iss.voucher.core.workflow.entity.Voucher;
import sg.edu.nus.iss.voucher.core.workflow.enums.VoucherStatus;
import sg.edu.nus.iss.voucher.core.workflow.repository.CampaignRepository;
import sg.edu.nus.iss.voucher.core.workflow.repository.VoucherRepository;
import sg.edu.nus.iss.voucher.core.workflow.service.IVoucherService;
import sg.edu.nus.iss.voucher.core.workflow.utility.DTOMapper;

@Service
public class VoucherService implements IVoucherService {
	
	private static final Logger logger = LoggerFactory.getLogger(VoucherService.class);

	@Autowired
	private VoucherRepository voucherRepository;
	

	@Autowired
	private CampaignRepository campaignRepository;
	
	@Autowired
	AuthAPICall apiCall;
	
	
	@Override
	public VoucherDTO findByVoucherId(String voucherId) throws Exception {
		try {
			Voucher voucher = voucherRepository.findById(voucherId).orElse(null);
			
			if (voucher == null) {
				logger.error("Voucher not found...");
				throw new Exception("Voucher not found by voucherId: " + voucherId);
			}
			
			logger.info("Voucher found...");
			VoucherDTO voucherDTO = DTOMapper.toVoucherDTO(voucher);
			logger.info("Voucher DTO found...");
			List<Voucher> voucherList = voucherRepository
					.findByCampaignCampaignId(voucherDTO.getCampaign().getCampaignId());
			voucherDTO.getCampaign().setNumberOfClaimedVouchers(voucherList.size());
			return voucherDTO;
			
		} catch (Exception ex) {
			logger.error("Finding voucher by voucher Id exception... {}", ex.toString());
			throw ex;

		}
	}

	@Override
	public VoucherDTO findVoucherByCampaignIdAndUserId(Campaign campaign, String userId) {

		logger.info("Getting voucher for CampaignId" + campaign.getCampaignId() + " " + userId);
		try {
			VoucherDTO voucherDTO = new VoucherDTO();
			Voucher voucher = voucherRepository.findByCampaignAndClaimedBy(campaign, userId);
			if (voucher != null) {
				voucherDTO = DTOMapper.toVoucherDTO(voucher);
			}
			return voucherDTO;

		} catch (Exception ex) {
			logger.error("Finding voucher by campaing Id and userId exception... {}", ex.toString());
			throw ex;

		}
	}

	@Override
	public List<Voucher> findVoucherListByCampaignId(String campaignId) {
		// TODO Auto-generated method stub
		return voucherRepository.findByCampaignCampaignId(campaignId);
	}

	@Override
	public VoucherDTO claimVoucher(Voucher voucher) throws Exception {
		try {

			Campaign campaign = campaignRepository.findById(voucher.getCampaign().getCampaignId()).orElseThrow();
			voucher.setVoucherStatus(VoucherStatus.CLAIMED);
			voucher.setClaimedBy(voucher.getClaimedBy());
			voucher.setClaimTime(LocalDateTime.now());
			voucher.setCampaign(campaign);
			logger.info("Saving voucher...");
			Voucher savedVoucher = voucherRepository.save(voucher);
			logger.info("Saved successfully...");

			if (savedVoucher == null) {
				logger.error("Voucher claim failed....");
				throw new Exception("Voucher claim failed.");
			}

			VoucherDTO voucherDTO = DTOMapper.toVoucherDTO(savedVoucher);
			List<Voucher> voucherList = voucherRepository
					.findByCampaignCampaignId(voucherDTO.getCampaign().getCampaignId());
			voucherDTO.getCampaign().setNumberOfClaimedVouchers(voucherList.size());

			return voucherDTO;
		} catch (Exception ex) {
			logger.error("Voucher saving exception... {}", ex.toString());
			throw ex;
		}
	}

}
