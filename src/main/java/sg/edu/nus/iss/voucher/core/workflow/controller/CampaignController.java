package sg.edu.nus.iss.voucher.core.workflow.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import sg.edu.nus.iss.voucher.core.workflow.dto.*;
import sg.edu.nus.iss.voucher.core.workflow.entity.*;
import sg.edu.nus.iss.voucher.core.workflow.enums.CampaignStatus;
import sg.edu.nus.iss.voucher.core.workflow.service.impl.CampaignService;
import sg.edu.nus.iss.voucher.core.workflow.strategy.impl.CampaignValidationStrategy;
import sg.edu.nus.iss.voucher.core.workflow.utility.*;

@RestController
@Validated
@RequestMapping("/api/core/campaigns")
public class CampaignController {

	private static final Logger logger = LoggerFactory.getLogger(CampaignController.class);

	@Autowired
	private CampaignService campaignService;

	@Autowired
	private CampaignValidationStrategy campaignValidationStrategy;

	@GetMapping(value = "", produces = "application/json")
	public ResponseEntity<APIResponse<List<CampaignDTO>>> getAllActiveCampaigns(
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
		logger.info("Calling Campaign getAllActiveCampaigns API with page={}, size={}", page, size);

		try {
			Pageable pageable = PageRequest.of(page, size, Sort.by("startDate").ascending());
			Map<Long, List<CampaignDTO>> resultMap = campaignService.findAllActiveCampaigns(pageable);

			if (resultMap.isEmpty()) {
				String message = "Campaign not found.";
				logger.error(message);
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(APIResponse.error(message));
			}

			long totalRecord = resultMap.keySet().stream().findFirst().orElse(0L);

			List<CampaignDTO> campaignDTOList = resultMap.getOrDefault(totalRecord, new ArrayList<>());

			logger.info("Total record: {}", totalRecord);
			logger.info("CampaignDTO List: {}", campaignDTOList);

			if (campaignDTOList.size() > 0) {
				return ResponseEntity.status(HttpStatus.OK).body(
						APIResponse.success(campaignDTOList, "Successfully get all active campaigns.", totalRecord));

			} else {
				String message = "Campaign not found.";
				logger.error(message);
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(APIResponse.error(message));
			}

		} catch (Exception ex) {
			logger.error("An error occurred while processing getAllActiveCampaigns API.", ex);
			throw ex;
		}
	}

	@GetMapping(value = "/stores/{storeId}", produces = "application/json")
	public ResponseEntity<APIResponse<List<CampaignDTO>>> getAllCampaignsByStoreId(
			@PathVariable("storeId") String storeId, @RequestParam(defaultValue = "") String status,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
		logger.info("Calling Campaign getAllCampaignsByStoreId API with page={}, size={}", page, size);

		try {
			storeId = GeneralUtility.makeNotNull(storeId).trim();
			if (storeId.isEmpty()) {
				logger.error("Bad Request: Store ID could not be blank.");
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body(APIResponse.error("Bad Request: Store ID could not be blank."));
			}

			Pageable pageable = PageRequest.of(page, size, Sort.by("startDate").ascending());
			Map<Long, List<CampaignDTO>> resultMap;

			if (status.isEmpty()) {
				resultMap = campaignService.findAllCampaignsByStoreId(storeId, pageable);
			} else {
				try {
					CampaignStatus campaignStatus = CampaignStatus.valueOf(status);
					resultMap = campaignService.findByStoreIdAndStatus(storeId, campaignStatus, pageable);
				} catch (IllegalArgumentException ex) {
					logger.error("Failed to get all campaigns by store Id. Campaign Status is invalid.", ex);
					throw ex;
				}
			}

			if (resultMap.isEmpty()) {
				String message = "Campaign not found by storeId: " + storeId;
				logger.error(message);
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(APIResponse.error(message));
			}

			long totalRecord = resultMap.keySet().stream().findFirst().orElse(0L);

			List<CampaignDTO> campaignDTOList = resultMap.getOrDefault(totalRecord, new ArrayList<>());

			logger.info("Total record: {}", totalRecord);
			logger.info("CampaignDTO List: {}", campaignDTOList);
			if (campaignDTOList.size() > 0) {
				return ResponseEntity.status(HttpStatus.OK).body(
						APIResponse.success(campaignDTOList, "Successfully get all active campaigns", totalRecord));

			} else {
				String message = "Campaign not found by storeId: " + storeId;
				logger.error(message);
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(APIResponse.error(message));
			}

		} catch (Exception ex) {
			logger.error(
					"Calling Campaign getAllCampaignsByStoreId API failed. Failed to get all campaigns by store Id.",
					ex);
			throw ex;
		}
	}

	@GetMapping(value = "/users/{userId}", produces = "application/json")
	public ResponseEntity<APIResponse<List<CampaignDTO>>> getCampaignsByUserId(@PathVariable("userId") String userId,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
		long totalRecord = 0;
		try {
			logger.info("Calling Campaign getAllCampaignsByEmail API with page={}, size={}", page, size);

			userId = GeneralUtility.makeNotNull(userId).trim();

			if (!userId.equals("")) {
				Pageable pageable = PageRequest.of(page, size, Sort.by("startDate").ascending());

				Map<Long, List<CampaignDTO>> resultMap = campaignService.findAllCampaignsByEmail(userId, pageable);

				if (resultMap.size() == 0) {
					String message = "Campign not found.";
					logger.error(message);
					return ResponseEntity.status(HttpStatus.NOT_FOUND).body(APIResponse.error(message));
				}

				List<CampaignDTO> campaignDTOList = new ArrayList<CampaignDTO>();

				for (Map.Entry<Long, List<CampaignDTO>> entry : resultMap.entrySet()) {
					totalRecord = entry.getKey();
					campaignDTOList = entry.getValue();

					logger.info("totalRecord: " + totalRecord);
					logger.info("CampaignDTO List: " + campaignDTOList);

				}

				if (campaignDTOList.size() > 0) {

					return ResponseEntity.status(HttpStatus.OK).body(APIResponse.success(campaignDTOList,
							"Successfully get all campaigns by user", totalRecord));

				} else {
					return ResponseEntity.status(HttpStatus.NOT_FOUND)
							.body(APIResponse.error("Campaign not found by user: " + userId));
				}
			} else {
				logger.error("Bad Request:Email could not be blank.");
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body(APIResponse.error("Bad Request:UserId could not be blank."));
			}

		} catch (Exception ex) {
			logger.info("Calling Campaign getAllCampaignsByEmail API failed...Failed to get all campaigns by user");
			throw ex;
		}
	}

	@GetMapping(value = "/{id}", produces = "application/json")
	public ResponseEntity<APIResponse<CampaignDTO>> getByCampaignId(@PathVariable("id") String id) {
		try {
			logger.info("Calling get Campaign API...");

			String campaignId = GeneralUtility.makeNotNull(id).trim();

			if (!campaignId.equals("")) {

				CampaignDTO campaignDTO = campaignService.findByCampaignId(campaignId);

				if (campaignDTO.getCampaignId().equals(campaignId)) {
					return ResponseEntity.status(HttpStatus.OK)
							.body(APIResponse.success(campaignDTO, "Successfully get campaignId " + campaignId));
				} else {
					return ResponseEntity.status(HttpStatus.NOT_FOUND)
							.body(APIResponse.error("Campaign not found by campaignId: " + campaignId));

				}

			} else {
				logger.error("Bad Request:Campaign ID could not be blank.");
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body(APIResponse.error("Bad Request:CampaignId could not be blank."));
			}

		} catch (Exception ex) {
			logger.error("Calling Campaign get Campaign API failed...");
			throw ex;
		}

	}

	@PostMapping(value = "", produces = "application/json")
	public ResponseEntity<APIResponse<CampaignDTO>> createCampaign(@RequestBody Campaign campaign) {
		try {
			logger.info("Calling Campaign create API...");
			String message = "";
			ValidationResult validationResult = campaignValidationStrategy.validateCreation(campaign, null);
			if (validationResult.isValid()) {

				CampaignDTO campaignDTO = campaignService.create(campaign);
				if (campaignDTO != null && !campaignDTO.getCampaignId().isEmpty()) {
					return ResponseEntity.status(HttpStatus.OK)
							.body(APIResponse.success(campaignDTO, "Created successfully"));
				} else {
					logger.error("Failed to create campaign.");
					return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
							.body(APIResponse.error("Create Campaign failed."));
				}

			} else {
				message = validationResult.getMessage();
				logger.error(message);
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(APIResponse.error(message));
			}

		} catch (Exception ex) {
			logger.error("An error occurred while processing createCampaign API.", ex);
			throw ex;
		}
	}

	@PutMapping(value = "/{id}", produces = "application/json")
	public ResponseEntity<APIResponse<CampaignDTO>> updateCampaign(@PathVariable("id") String id,
			@RequestBody Campaign campaign) {
		try {
			logger.info("Calling Campaign update API...");
			String message = "";
			String campaignId = GeneralUtility.makeNotNull(id).trim();

			if (!campaignId.equals("")) {
				campaign.setCampaignId(campaignId);
				ValidationResult validationResult = campaignValidationStrategy.validateUpdating(campaign, null);
				if (validationResult.isValid()) {
					CampaignDTO campaignDTO = campaignService.update(campaign);
					if (campaignDTO != null && !campaignDTO.getCampaignId().isEmpty()) {
						return ResponseEntity.status(HttpStatus.OK)
								.body(APIResponse.success(campaignDTO, "Updated sucessfully"));
					} else {
						logger.error("Calling Campaign create API failed...");
						return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
								APIResponse.error("Update Campaign failed:  campaignId: " + campaign.getCampaignId()));
					}

				} else {
					message = validationResult.getMessage();
					logger.error(message);
					return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(APIResponse.error(message));
				}
			} else {
				logger.error("Bad Request:Campaign ID could not be blank.");
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body(APIResponse.error("Bad Request:CampaignId could not be blank."));
			}

		} catch (Exception ex) {
			logger.info("Calling Campaign update API failed..." + ex.toString());
			throw ex;
		}
	}

	@PatchMapping(value = "/{campaignId}/users/{userId}/promote", produces = "application/json")
	public ResponseEntity<APIResponse<CampaignDTO>> promoteCampaign(@PathVariable("campaignId") String campaignId,
			@PathVariable("userId") String userId) {
		try {
			logger.info("Calling Campaign Promote API...");

			ValidationResult validationResult = campaignValidationStrategy.validateObject(campaignId);
			if (!validationResult.isValid()) {
				String message = validationResult.getMessage();
				logger.error(message);
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(APIResponse.error(message));
			}
			
			validationResult= campaignValidationStrategy.validateUser(userId);
			if (!validationResult.isValid()) {
				String message = validationResult.getMessage();
				logger.error(message);
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(APIResponse.error(message));
			}
			
			CampaignDTO campaignDTO = campaignService.promote(campaignId,userId);
			if (campaignDTO != null && !campaignDTO.getCampaignId().isEmpty()) {
				return ResponseEntity.status(HttpStatus.OK)
						.body(APIResponse.success(campaignDTO, "Campaign promoted successfully"));
			} else {
				String message = "Campaign Promotion has failed.";
				logger.error(message);
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(APIResponse.error(message));
			}

		} catch (Exception ex) {
			logger.error("Calling Promote Campaign API failed", ex);
			throw ex;
		}
	}

}
