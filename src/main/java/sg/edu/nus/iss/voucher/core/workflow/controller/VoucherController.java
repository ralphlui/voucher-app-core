package sg.edu.nus.iss.voucher.core.workflow.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import sg.edu.nus.iss.voucher.core.workflow.dto.APIResponse;
import sg.edu.nus.iss.voucher.core.workflow.dto.VoucherDTO;
import sg.edu.nus.iss.voucher.core.workflow.entity.Campaign;
import sg.edu.nus.iss.voucher.core.workflow.entity.Voucher;
import sg.edu.nus.iss.voucher.core.workflow.exception.CampaignNotFoundException;
import sg.edu.nus.iss.voucher.core.workflow.service.impl.CampaignService;
import sg.edu.nus.iss.voucher.core.workflow.service.impl.UserValidatorService;
import sg.edu.nus.iss.voucher.core.workflow.service.impl.VoucherService;
import sg.edu.nus.iss.voucher.core.workflow.utility.GeneralUtility;

@RestController
@Validated
@RequestMapping("/api/vouchers")
public class VoucherController {

	private static final Logger logger = LoggerFactory.getLogger(VoucherController.class);

	@Autowired
	private VoucherService voucherService;

	@Autowired
	private CampaignService campaignService;
	
	@Autowired
	private UserValidatorService userValidatorService;

	@GetMapping(value = "/{id}", produces = "application/json")
	public ResponseEntity<APIResponse<VoucherDTO>> getByVoucherId(@PathVariable("id") String id) {
		String voucherId = id.trim();
		try {
			logger.info("Calling get Voucher API...");
			if (voucherId.isEmpty()) {
				logger.error("Bad Request:Voucher ID could not be blank.");
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body(APIResponse.error("Bad Request:Voucher could not be blank."));
			}

			VoucherDTO voucherDTO = voucherService.findByVoucherId(voucherId);
			String message = "";
			if (voucherDTO.getVoucherId().equals(voucherId)) {

				message = "Successfully get voucherId " + voucherId;
				logger.info(message);
				return ResponseEntity.status(HttpStatus.OK).body(APIResponse.success(voucherDTO, message));
			}
			message = "Voucher not found by voucherId: " + voucherId;
			logger.error(message);
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(APIResponse.error(message));

		} catch (Exception ex) {
			logger.error("Calling Voucher get Voucher API failed...");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(APIResponse.error("Failed to get voucherId " + voucherId));
		}

	}

	@PostMapping(value = "/claim", produces = "application/json")
	public ResponseEntity<APIResponse<VoucherDTO>> claimVoucher(@RequestBody Voucher voucher) {
		try {
			logger.info("Calling Voucher claim API...");

			String campaignId = GeneralUtility.makeNotNull(voucher.getCampaign().getCampaignId()).trim();

			String message = validateUser(voucher.getClaimedBy());
			if (!message.isEmpty()) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(APIResponse.error(message));
			}
			
			//Validate Campaign
			Campaign campaign = validateCampaign(campaignId);

			//Check if Voucher Already Claimed
			if (isVoucherAlreadyClaimed(voucher, campaign)) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body(APIResponse.error("Voucher already claimed."));
			}

			//Check if Campaign is Fully Claimed
			if (isCampaignFullyClaimed(campaignId, campaign)) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(APIResponse.error("Campaign is fully claimed."));
			}

			//Claim the Voucher
			VoucherDTO voucherDTO = voucherService.claimVoucher(voucher);
			return ResponseEntity.status(HttpStatus.OK)
					.body(APIResponse.success(voucherDTO, "Voucher claimed successfully."));

		} catch (CampaignNotFoundException ex) {
			logger.error(ex.getMessage());
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(APIResponse.error(ex.getMessage()));
		} catch (Exception ex) {
			logger.error("Calling Voucher claim API failed: " + ex.getMessage(), ex);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(APIResponse.error("Voucher claim failed."));
		}
	}
	
	private String validateUser(String userId) {
		HashMap<Boolean, String> userMap = userValidatorService.validateActiveUser(userId, "CUSTOMER");
		logger.info("user Id key map "+ userMap.keySet());
		
		for (Map.Entry<Boolean, String> entry : userMap.entrySet()) {
			logger.info("user role: " + entry.getValue());
			logger.info("user id: " + entry.getKey());
			
			if (!entry.getKey()) {
				String message = entry.getValue();
				logger.error(message);
				return message;
			}
		}
		return "";
	}

	// Validate Campaign
	private Campaign validateCampaign(String campaignId) {
		return campaignService.findById(campaignId)
				.orElseThrow(() -> new CampaignNotFoundException("Campaign not found by campaignId: " + campaignId));
	}

	// Check if the voucher has already been claimed by this user
	private boolean isVoucherAlreadyClaimed(Voucher voucher, Campaign campaign) {
		VoucherDTO voucherDTO = voucherService.findVoucherByCampaignIdAndUserId(campaign, voucher.getClaimedBy());
		if (voucherDTO != null && voucherDTO.getVoucherId() != null && !voucherDTO.getVoucherId().isEmpty()) {
			logger.error("Voucher already claimed.");
			return true;
		}
		return false;
	}

	// Check if the campaign has already given out all its vouchers
	private boolean isCampaignFullyClaimed(String campaignId, Campaign campaign) {
		List<Voucher> claimedVoucherList = voucherService.findVoucherListByCampaignId(campaignId);
		logger.info("claimedVoucherList: " + claimedVoucherList);
		if (campaign.getNumberOfVouchers() <= claimedVoucherList.size()) {
			logger.error("Campaign is fully claimed.");
			return true;
		}
		return false;
	}
}
