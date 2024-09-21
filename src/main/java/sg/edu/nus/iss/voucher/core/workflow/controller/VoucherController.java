package sg.edu.nus.iss.voucher.core.workflow.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import sg.edu.nus.iss.voucher.core.workflow.dto.*;
import sg.edu.nus.iss.voucher.core.workflow.entity.*;
import sg.edu.nus.iss.voucher.core.workflow.enums.UserRoleType;
import sg.edu.nus.iss.voucher.core.workflow.enums.VoucherStatus;
import sg.edu.nus.iss.voucher.core.workflow.exception.CampaignNotFoundException;
import sg.edu.nus.iss.voucher.core.workflow.exception.VoucherNotFoundException;
import sg.edu.nus.iss.voucher.core.workflow.service.impl.*;
import sg.edu.nus.iss.voucher.core.workflow.utility.GeneralUtility;

@RestController
@Validated
@RequestMapping("/api/core/vouchers")
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
	public ResponseEntity<APIResponse<VoucherDTO>> claimVoucher(@RequestBody VoucherRequest voucherRequest) {
		try {
			logger.info("Calling Voucher claim API...");

			String campaignId = GeneralUtility.makeNotNull(voucherRequest.getCampaignId()).trim();
			String claimBy = voucherRequest.getClaimedBy();

			String message = validateUser(claimBy);
			if (!message.isEmpty()) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(APIResponse.error(message));
			}
			
			//Validate Campaign
			Campaign campaign = validateCampaign(campaignId);

			//Check if Voucher Already Claimed
			if (isVoucherAlreadyClaimed(claimBy, campaign)) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body(APIResponse.error("Voucher already claimed."));
			}

			//Check if Campaign is Fully Claimed
			if (isCampaignFullyClaimed(campaignId, campaign)) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(APIResponse.error("Campaign is fully claimed."));
			}

			//Claim the Voucher
			VoucherDTO voucherDTO = voucherService.claimVoucher(voucherRequest);
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
	
	@GetMapping(value = "/users/{userId}", produces = "application/json")
	public ResponseEntity<APIResponse<List<VoucherDTO>>> findAllClaimedVouchersByUserId(@PathVariable("userId") String userId,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "50") int size) {

		long totalRecord = 0;
		try {
			logger.info("Calling get Voucher by email API with page={}, size={}", page, size);

			if (!userId.equals("")) {

				Pageable pageable = PageRequest.of(page, size, Sort.by("claimTime").ascending());


				Map<Long, List<VoucherDTO>> resultMap = voucherService.findByClaimedBy(userId, pageable);

				if (resultMap.size() == 0) {
					String message = "Voucher not found by user: " + userId;
					logger.error(message);
					return ResponseEntity.status(HttpStatus.NOT_FOUND).body(APIResponse.error(message));
				}

				List<VoucherDTO> voucherDTOList = new ArrayList<VoucherDTO>();

				for (Map.Entry<Long, List<VoucherDTO>> entry : resultMap.entrySet()) {
					totalRecord = entry.getKey();
					voucherDTOList = entry.getValue();

					logger.info("totalRecord: " + totalRecord);
					logger.info("voucherDTO List: " + voucherDTOList);

				}

				if (voucherDTOList.size() > 0) {
					return ResponseEntity.status(HttpStatus.OK).body(APIResponse.success(voucherDTOList,
							"Successfully get claimed vouchers by user: " + userId, totalRecord));

				} else {
					String message = "Voucher not found by user: " + userId;
					logger.error(message);
					return ResponseEntity.status(HttpStatus.NOT_FOUND).body(APIResponse.error(message));
				}

			} else {
				logger.error("Bad Request:Email could not be blank.");
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body(APIResponse.error("Bad Request:Email could not be blank."));
			}
		} catch (Exception ex) {
			logger.error("Calling Voucher get Voucher by email API failed...");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(APIResponse.error("Failed to get voucher for user " + userId));
		}

	}
	
	@GetMapping(value = "/campaigns/{campaignId}", produces = "application/json")
	public ResponseEntity<APIResponse<List<VoucherDTO>>> findAllClaimedVouchersBycampaignId(
			@PathVariable("campaignId") String campaignId, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "50") int size) {

		long totalRecord = 0;
		try {
			logger.info("Calling get Voucher by campaignId API...");

			if (!campaignId.equals("")) {

				Pageable pageable = PageRequest.of(page, size, Sort.by("claimTime").ascending());

				Map<Long, List<VoucherDTO>> resultMap = voucherService.findAllClaimedVouchersByCampaignId(campaignId,
						pageable);

				if (resultMap.size() == 0) {
					String message = "Voucher not found by campaignId: " + campaignId;
					logger.error(message);
					return ResponseEntity.status(HttpStatus.NOT_FOUND).body(APIResponse.error(message));
				}

				List<VoucherDTO> voucherDTOList = new ArrayList<VoucherDTO>();

				for (Map.Entry<Long, List<VoucherDTO>> entry : resultMap.entrySet()) {
					totalRecord = entry.getKey();
					voucherDTOList = entry.getValue();

					logger.info("totalRecord: " + totalRecord);
					logger.info("voucherDTO List: " + voucherDTOList);

				}

				if (voucherDTOList.size() > 0) {

					return ResponseEntity.status(HttpStatus.OK).body(APIResponse.success(voucherDTOList,
							"Successfully get claimed vouchers by campaignId: " + campaignId, totalRecord));
				} else {
					String message = "Voucher not found by campaignId: " + campaignId;
					logger.error(message);
					return ResponseEntity.status(HttpStatus.NOT_FOUND).body(APIResponse.error(message));

				}
			} else {
				logger.error("Bad Request:Campaign ID could not be blank.");
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body(APIResponse.error("Bad Request:CampaignId could not be blank."));
			}

		} catch (Exception ex) {
			logger.error("Calling Voucher get Voucher by campaignId API failed...");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(APIResponse.error("Failed to get voucher for campaignId " + campaignId));
		}

	}

	@PatchMapping(value = "{voucherID}/consume", produces = "application/json")
	public ResponseEntity<APIResponse<VoucherDTO>> updateVoucher(@PathVariable("voucherID") String voucherID) {
		String voucherId = GeneralUtility.makeNotNull(voucherID).trim();

		logger.info("Calling Voucher consume API...");

		try {

			VoucherDTO voucherDTO = voucherService.findByVoucherId(voucherId);

			if (voucherDTO != null && !voucherDTO.getVoucherStatus().equals(VoucherStatus.CLAIMED)) {
				logger.error("Voucher already consumed or not in a claimable state. Id: {}", voucherId);
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body(APIResponse.error("Voucher already consumed."));
			}

			VoucherDTO updatedVoucherDTO = voucherService.consumeVoucher(voucherId);

			if (updatedVoucherDTO.getVoucherStatus().equals(VoucherStatus.CONSUMED)) {
				return ResponseEntity.status(HttpStatus.OK)
						.body(APIResponse.success(updatedVoucherDTO, "Voucher consumed successfully."));
			} else {
				logger.error("Voucher consumption failed. Id: {}", voucherId);
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
						.body(APIResponse.error("Voucher consumption failed."));
			}

		} catch (Exception ex) {
			logger.error("Exception during Voucher consume API call. Id: {}, Error: {}", voucherId, ex.getMessage());
			HttpStatusCode htpStatuscode = ex instanceof VoucherNotFoundException ? HttpStatus.NOT_FOUND
					: HttpStatus.INTERNAL_SERVER_ERROR;
			return ResponseEntity.status(htpStatuscode)
					.body(APIResponse.error(ex.getMessage()));
		}
	}

	private String validateUser(String userId) {
		HashMap<Boolean, String> userMap = userValidatorService.validateActiveUser(userId, UserRoleType.CUSTOMER.toString());
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
	private boolean isVoucherAlreadyClaimed(String claimBy, Campaign campaign) {
		VoucherDTO voucherDTO = voucherService.findVoucherByCampaignIdAndUserId(campaign, claimBy);
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
