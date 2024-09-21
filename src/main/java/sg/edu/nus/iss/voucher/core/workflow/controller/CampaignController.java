package sg.edu.nus.iss.voucher.core.workflow.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import sg.edu.nus.iss.voucher.core.workflow.dto.*;
import sg.edu.nus.iss.voucher.core.workflow.entity.*;
import sg.edu.nus.iss.voucher.core.workflow.enums.*;
import sg.edu.nus.iss.voucher.core.workflow.service.impl.*;
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
	
	@Autowired
	private AuditService auditService;
	
	
	@Value("${audit.activity.type.prefix}")
	String activityTypePrefix;

	@GetMapping(value = "", produces = "application/json")
	public ResponseEntity<APIResponse<List<CampaignDTO>>> getAllActiveCampaigns(@RequestHeader("X-User-Id") String userId,@RequestParam(defaultValue = "") String description,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
		logger.info("Calling Campaign getAllActiveCampaigns API with userId={}, description={}, page={}, size={}",userId,description, page, size);
		
		AuditDTO auditDTO = auditService.createAuditDTO(userId, "Active Campaign List", activityTypePrefix,"/api/core/campaigns", HTTPVerb.GET);

		try {
			String message="";
			Pageable pageable = PageRequest.of(page, size, Sort.by("startDate").ascending());
			Map<Long, List<CampaignDTO>> resultMap = campaignService.findAllActiveCampaigns(description,pageable);
			
			if (resultMap.isEmpty()) {
			    message = "Campaign not found.";
				logger.error(message);
				auditService.logAudit(auditDTO,404,message);
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(APIResponse.error(message));
			}

			long totalRecord = resultMap.keySet().stream().findFirst().orElse(0L);

			List<CampaignDTO> campaignDTOList = resultMap.getOrDefault(totalRecord, new ArrayList<>());

			logger.info("Total record: {}", totalRecord);
			logger.info("CampaignDTO List: {}", campaignDTOList);

			if (campaignDTOList.size() > 0) {
				message ="Successfully get all active campaigns.";
				
				auditService.logAudit(auditDTO,200,message);
				return ResponseEntity.status(HttpStatus.OK).body(
						APIResponse.success(campaignDTOList, message, totalRecord));

			} else {
				message = "Campaign not found.";
				logger.error(message);
				auditService.logAudit(auditDTO,404,message);
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(APIResponse.error(message));
			}

		} catch (Exception ex) {
			logger.error("An error occurred while processing getAllActiveCampaigns API.", ex);
			String message="An error occurred while processing getAllActiveCampaigns API.";
			auditDTO.setRemarks(ex.toString());
			auditService.logAudit(auditDTO,500,message);		
			
			throw ex;
		}
	}


	@GetMapping(value = "/stores/{storeId}", produces = "application/json")
	public ResponseEntity<APIResponse<List<CampaignDTO>>> getAllCampaignsByStoreId(@RequestHeader("X-User-Id") String userId,
			@PathVariable("storeId") String storeId, @RequestParam(defaultValue = "") String status,@RequestParam(defaultValue = "") String description,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
		logger.info("Calling Campaign getAllCampaignsByStoreId API with userId={}, status={}, description={}, page={}, size={}",userId,status,description, page, size);
		AuditDTO auditDTO = auditService.createAuditDTO(userId, "Campaign List by Store", activityTypePrefix,"/api/core/campaigns/stores/"+storeId, HTTPVerb.GET);
		String message="";
		try {
			storeId = GeneralUtility.makeNotNull(storeId).trim();
			if (storeId.isEmpty()) {
				message = "Bad Request: Store ID could not be blank.";
				logger.error(message);
				auditService.logAudit(auditDTO,400,message);
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body(APIResponse.error(message));
			}

			Pageable pageable = PageRequest.of(page, size, Sort.by("startDate").ascending());
			Map<Long, List<CampaignDTO>> resultMap;

			if (status.isEmpty()) {
				resultMap = campaignService.findAllCampaignsByStoreId(storeId, description, pageable);
			} else {
				try {
					CampaignStatus campaignStatus = CampaignStatus.valueOf(status);
					resultMap = campaignService.findByStoreIdAndStatus(storeId, campaignStatus, pageable);
				} catch (IllegalArgumentException ex) {
					
					message ="Failed to get all campaigns by store Id. Campaign Status is invalid.";
					logger.error(message, ex);
					auditDTO.setRemarks(ex.toString());
					auditService.logAudit(auditDTO,404,message);
					
					throw ex;
				}
			}

			if (resultMap.isEmpty()) {
				 message = "Campaign not found by storeId: " + storeId;
				logger.error(message);
				
				auditService.logAudit(auditDTO,404,message);
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(APIResponse.error(message));
			}

			long totalRecord = resultMap.keySet().stream().findFirst().orElse(0L);

			List<CampaignDTO> campaignDTOList = resultMap.getOrDefault(totalRecord, new ArrayList<>());

			logger.info("Total record: {}", totalRecord);
			logger.info("CampaignDTO List: {}", campaignDTOList);
			if (campaignDTOList.size() > 0) {
				message ="Successfully get all active campaigns.";
				auditService.logAudit(auditDTO,200,message);
				return ResponseEntity.status(HttpStatus.OK).body(
						APIResponse.success(campaignDTOList, "Successfully get all active campaigns", totalRecord));

			} else {
				 message = "Campaign not found by storeId: " + storeId;
				logger.error(message);
				auditService.logAudit(auditDTO,404,message);
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(APIResponse.error(message));
			}

		} catch (Exception ex) {
			message ="Calling Campaign getAllCampaignsByStoreId API failed. Failed to get all campaigns by store Id." ;
			logger.error(
					message,
					ex);
			auditDTO.setRemarks(ex.toString());
			auditService.logAudit(auditDTO,500,message);
			throw ex;
		}
	}

	@GetMapping(value = "/users/{userId}", produces = "application/json")
	public ResponseEntity<APIResponse<List<CampaignDTO>>> getCampaignsByUserId(@RequestHeader("X-User-Id") String xUserId,@PathVariable("userId") String userId,
			@RequestParam(defaultValue = "") String description,@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
		long totalRecord = 0;
		logger.info("Calling Campaign getAllCampaignsByEmail API with userId={}, description={}, page={}, size={}",xUserId,description, page, size);

		AuditDTO auditDTO = auditService.createAuditDTO(userId, "Campaign List by User", activityTypePrefix,"/api/core/campaigns/users/"+userId, HTTPVerb.GET);
		String message="";
		try {
			
			userId = GeneralUtility.makeNotNull(userId).trim();

			if (!userId.equals("")) {
				Pageable pageable = PageRequest.of(page, size, Sort.by("startDate").ascending());

				Map<Long, List<CampaignDTO>> resultMap = campaignService.findAllCampaignsByUserId(userId,description, pageable);

				if (resultMap.size() == 0) {
					 message = "Campign not found.";
					logger.error(message);
					auditService.logAudit(auditDTO,400,message);
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
					message="Successfully get all campaigns by user.";
					auditService.logAudit(auditDTO,200,message);
					return ResponseEntity.status(HttpStatus.OK).body(APIResponse.success(campaignDTOList,
							message, totalRecord));

				} else {
					message = "Campaign not found by user: " + userId;
					auditService.logAudit(auditDTO,404,message);
					return ResponseEntity.status(HttpStatus.NOT_FOUND)
							.body(APIResponse.error(message));
				}
			} else {
				message = "Bad Request:Email could not be blank.";
				logger.error(message);
				auditService.logAudit(auditDTO,400,message);
				
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body(APIResponse.error("Bad Request:UserId could not be blank."));
			}

		} catch (Exception ex) {
			message = "Calling Campaign getAllCampaignsByEmail API failed...Failed to get all campaigns by user";
			logger.error(message);
			auditDTO.setRemarks(ex.toString());
			auditService.logAudit(auditDTO,500,message);
			
			throw ex;
		}
	}

	@GetMapping(value = "/{id}", produces = "application/json")
	public ResponseEntity<APIResponse<CampaignDTO>> getByCampaignId(@RequestHeader("X-User-Id") String userId,@PathVariable("id") String id) {
	
		AuditDTO auditDTO = auditService.createAuditDTO(userId, "Find Campaign by Id", activityTypePrefix,"/api/core/campaigns/"+id, HTTPVerb.GET);
		String message="";
		try {
			logger.info("Calling get Campaign API...");

			String campaignId = GeneralUtility.makeNotNull(id).trim();

			if (!campaignId.equals("")) {

				CampaignDTO campaignDTO = campaignService.findByCampaignId(campaignId);

				if (campaignDTO.getCampaignId().equals(campaignId)) {
					message =  "Successfully get campaignId " + campaignId;
					
					auditService.logAudit(auditDTO,200,message);
					return ResponseEntity.status(HttpStatus.OK)
							.body(APIResponse.success(campaignDTO,message));
				} else {
					message = "Campaign not found by campaignId: " + campaignId;
					
					auditService.logAudit(auditDTO,404,message);
					return ResponseEntity.status(HttpStatus.NOT_FOUND)
							.body(APIResponse.error(message));

				}

			} else {
				message = "Bad Request:CampaignId could not be blank.";
				logger.error(message);
				auditService.logAudit(auditDTO,400,message);
				
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body(APIResponse.error(message));
			}

		} catch (Exception ex) {
			message = "Calling Campaign get Campaign API failed.";
			logger.error(message);
			auditDTO.setRemarks(ex.toString());
			auditService.logAudit(auditDTO,500,message);
			throw ex;
		}

	}

	@PostMapping(value = "", produces = "application/json")
	public ResponseEntity<APIResponse<CampaignDTO>> createCampaign(@RequestHeader("X-User-Id") String userId,@RequestBody Campaign campaign) {
		
		AuditDTO auditDTO = auditService.createAuditDTO(userId, "Create Campaign", activityTypePrefix,"/api/core/campaigns", HTTPVerb.POST);
		String message="";
		
		try {
			logger.info("Calling Campaign create API...");
			
			ValidationResult validationResult = campaignValidationStrategy.validateCreation(campaign, null);
			if (validationResult.isValid()) {

				CampaignDTO campaignDTO = campaignService.create(campaign);
				if (campaignDTO != null && !campaignDTO.getCampaignId().isEmpty()) {
					message ="Created successfully";
					auditService.logAudit(auditDTO,200,message);
					return ResponseEntity.status(HttpStatus.OK)
							.body(APIResponse.success(campaignDTO,message));
				} else {
					message ="Failed to create campaign.";
					auditService.logAudit(auditDTO,500,message);
					logger.error(message);
					return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
							.body(APIResponse.error(message));
				}

			} else {
				message = validationResult.getMessage();
				logger.error(message);
				auditService.logAudit(auditDTO,400,message);
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(APIResponse.error(message));
			}

		} catch (Exception ex) {
			message ="An error occurred while processing Create Campaign API.";
			auditDTO.setRemarks(ex.toString());
			
			logger.error(message, ex);
			auditService.logAudit(auditDTO,500,message);
			throw ex;
		}
	}

	@PutMapping(value = "/{id}", produces = "application/json")
	public ResponseEntity<APIResponse<CampaignDTO>> updateCampaign(@RequestHeader("X-User-Id") String userId,@PathVariable("id") String id,
			@RequestBody Campaign campaign) {
		AuditDTO auditDTO = auditService.createAuditDTO(userId, "Update Campaign", activityTypePrefix,"/api/core/campaigns/"+id, HTTPVerb.PUT);
		String message="";
		
		try {
			logger.info("Calling Campaign update API...");
			
			String campaignId = GeneralUtility.makeNotNull(id).trim();

			if (!campaignId.equals("")) {
				campaign.setCampaignId(campaignId);
				ValidationResult validationResult = campaignValidationStrategy.validateUpdating(campaign, null);
				if (validationResult.isValid()) {
					CampaignDTO campaignDTO = campaignService.update(campaign);
					if (campaignDTO != null && !campaignDTO.getCampaignId().isEmpty()) {
						message ="Updated sucessfully.";
						auditService.logAudit(auditDTO,200,message);
						return ResponseEntity.status(HttpStatus.OK)
								.body(APIResponse.success(campaignDTO, message));
					} else {
						logger.error("Calling Campaign create API failed...");
						return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
								APIResponse.error("Update Campaign failed:  campaignId: " + campaign.getCampaignId()));
					}

				} else {
					message = validationResult.getMessage();
					logger.error(message);
					auditService.logAudit(auditDTO,404,message);
					return ResponseEntity.status(HttpStatus.NOT_FOUND).body(APIResponse.error(message));
				}
			} else {
				message ="Bad Request:Campaign ID could not be blank.";
				logger.error(message);
				auditService.logAudit(auditDTO,400,message);
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body(APIResponse.error(message));
			}

		} catch (Exception ex) {
			message ="An error occurred while processing Update Campaign API.";
			auditDTO.setRemarks(ex.toString());
			
			logger.info(message);
			auditService.logAudit(auditDTO,500,message);
			throw ex;
		}
	}

	@PatchMapping(value = "/{campaignId}/users/{userId}/promote", produces = "application/json")
	public ResponseEntity<APIResponse<CampaignDTO>> promoteCampaign(@RequestHeader("X-User-Id") String xUserId,@PathVariable("campaignId") String campaignId,
			@PathVariable("userId") String userId) {
		AuditDTO auditDTO = auditService.createAuditDTO(userId, "Promote Campaign", activityTypePrefix,"/api/core/campaigns/"+campaignId+"/users/"+userId+"/promote", HTTPVerb.PATCH);
		String message="";
		
		try {
			logger.info("Calling Campaign Promote API...");

			ValidationResult validationResult = campaignValidationStrategy.validateObject(campaignId);
			if (!validationResult.isValid()) {
				 message = validationResult.getMessage();
				logger.error(message);
				auditService.logAudit(auditDTO,400,message);
				return ResponseEntity.status(validationResult.getStatus()).body(APIResponse.error(message));
			}
			
			validationResult= campaignValidationStrategy.validateUser(userId);
			if (!validationResult.isValid()) {
				 message = validationResult.getMessage();
				logger.error(message);
				auditService.logAudit(auditDTO,400,message);
				return ResponseEntity.status(validationResult.getStatus()).body(APIResponse.error(message));
			}
			
			CampaignDTO campaignDTO = campaignService.promote(campaignId,userId);
			if (campaignDTO != null && campaignDTO.getCampaignId()!=null) {
				message ="Campaign promoted successfully.";
				auditService.logAudit(auditDTO,200,message);
				return ResponseEntity.status(HttpStatus.OK)
						.body(APIResponse.success(campaignDTO, message));
			} else {
				message = "Campaign Promotion has failed.";
				logger.error(message);
				auditService.logAudit(auditDTO,500,message);
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(APIResponse.error(message));
			}

		} catch (Exception ex) {
			message ="An error occurred while processing Promote Campaign API.";
			logger.error(message);
			auditDTO.setRemarks(ex.toString());
			auditService.logAudit(auditDTO,500,message);
			logger.error("", ex);
			throw ex;
		}
	}

}
