package sg.edu.nus.iss.voucher.core.workflow.strategy.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import sg.edu.nus.iss.voucher.core.workflow.dto.StoreDTO;
import sg.edu.nus.iss.voucher.core.workflow.dto.ValidationResult;
import sg.edu.nus.iss.voucher.core.workflow.entity.Campaign;
import sg.edu.nus.iss.voucher.core.workflow.enums.CampaignStatus;
import sg.edu.nus.iss.voucher.core.workflow.service.impl.CampaignService;
import sg.edu.nus.iss.voucher.core.workflow.service.impl.StoreService;
import sg.edu.nus.iss.voucher.core.workflow.strategy.IAPIHelperValidationStrategy;
import sg.edu.nus.iss.voucher.core.workflow.utility.GeneralUtility;

@Service
public class CampaignValidationStrategy implements IAPIHelperValidationStrategy<Campaign> {

	@Autowired
	private CampaignService campaignService;

	@Autowired
	private StoreService storeService;


	@Override
	public ValidationResult validateCreation(Campaign campaign, MultipartFile val) {
		ValidationResult validationResult = new ValidationResult();
		if (campaign.getDescription() == null || campaign.getDescription().isEmpty()) {
			validationResult.setMessage("Description cannot be empty.");
			validationResult.setStatus(HttpStatus.BAD_REQUEST);
			validationResult.setValid(false);
			return validationResult;
		}

		if (campaign.getStore() == null || campaign.getStore().getStoreId().isEmpty()) {
			validationResult.setMessage("Store Id cannot be empty.");
			validationResult.setStatus(HttpStatus.BAD_REQUEST);
			validationResult.setValid(false);
			return validationResult;
		}

		if (campaign.getCreatedBy() == null || campaign.getCreatedBy().isEmpty()) {
			validationResult.setMessage("CreatedBy cannot be empty.");
			validationResult.setStatus(HttpStatus.BAD_REQUEST);
			validationResult.setValid(false);
			return validationResult;
		}

		List<Campaign> dbCampaignList = campaignService.findByDescription(campaign.getDescription().trim());
		if (!dbCampaignList.isEmpty()) {
			validationResult.setMessage("Campaign already exists: " + campaign.getDescription());
			validationResult.setStatus(HttpStatus.BAD_REQUEST);
			validationResult.setValid(false);
			return validationResult;
		}

		StoreDTO storeDTO = storeService.findByStoreId(campaign.getStore().getStoreId());

		if (storeDTO == null || storeDTO.getStoreId() == null || storeDTO.getStoreId().isEmpty()) {
			validationResult.setMessage("Invalid store Id: " + campaign.getStore().getStoreId());
			validationResult.setStatus(HttpStatus.BAD_REQUEST);
			validationResult.setValid(false);
			return validationResult;
		}

		String userId = campaign.getCreatedBy();

		if (userId == null || userId.isEmpty()) {
			validationResult.setMessage("Invalid User : " + campaign.getCreatedBy());
			validationResult.setStatus(HttpStatus.BAD_REQUEST);
			validationResult.setValid(false);
			return validationResult;
		}

		validationResult.setValid(true);
		return validationResult;

	}

	@Override
	public ValidationResult validateUpdating(Campaign campaign, MultipartFile val) {
		ValidationResult validationResult = new ValidationResult();
		String campaignId = GeneralUtility.makeNotNull(campaign.getCampaignId()).trim();
		if (campaignId == null || campaignId.isEmpty()) {

			validationResult.setMessage("Campaign ID can not be blank.");
			validationResult.setStatus(HttpStatus.BAD_REQUEST);
			validationResult.setValid(false);
			return validationResult;
		}

		String userId = campaign.getUpdatedBy();

		if (userId == null || userId.isEmpty()) {
			validationResult.setMessage("Invalid User : " + campaign.getUpdatedBy());
			validationResult.setStatus(HttpStatus.BAD_REQUEST);
			validationResult.setValid(false);
			return validationResult;
		}

		Optional<Campaign> dbCampaign = campaignService.findById(campaignId);
		if (dbCampaign.isEmpty()) {
			validationResult.setMessage("Campaign not found by campaignId: " + campaignId);
			validationResult.setStatus(HttpStatus.BAD_REQUEST);
			validationResult.setValid(false);
			return validationResult;
		} else {
			if (!dbCampaign.get().getCampaignStatus().equals(CampaignStatus.CREATED)) {
				validationResult.setMessage(
						"Campaign status has to be CREATED.Requested status :" + dbCampaign.get().getCampaignStatus());
				validationResult.setStatus(HttpStatus.BAD_REQUEST);
				validationResult.setValid(false);
				return validationResult;
			}
		}

		validationResult.setValid(true);
		return validationResult;
	}

	@Override
	public ValidationResult validateObject(String campaignId) {
		ValidationResult validationResult = new ValidationResult();
		
		if(campaignId == null || campaignId.isEmpty()) {
			validationResult.setMessage("Campaign Id could not be blank.");
			validationResult.setStatus(HttpStatus.BAD_REQUEST);
			validationResult.setValid(false);
			return validationResult;
		}
		Optional<Campaign> dbCampaign = campaignService.findById(campaignId);
		LocalDateTime startDate = dbCampaign.get().getStartDate();
		LocalDateTime endDate = dbCampaign.get().getEndDate();

		if (startDate.isBefore(LocalDateTime.now()) && endDate.isBefore(LocalDateTime.now())) {
			validationResult.setMessage("StartDate " + startDate + " should not be less than current date ");
			validationResult.setStatus(HttpStatus.BAD_REQUEST);
			validationResult.setValid(false);
			return validationResult;
		}

		if (endDate.isBefore(LocalDateTime.now())) {
			validationResult.setMessage("EndDate " + endDate + " should not be less than current date ");
			validationResult.setStatus(HttpStatus.BAD_REQUEST);
			validationResult.setValid(false);
			return validationResult;
		}

		if (dbCampaign.get().getCampaignStatus().equals(CampaignStatus.PROMOTED)) {
			validationResult.setMessage("Campaign already promoted.");
			validationResult.setStatus(HttpStatus.BAD_REQUEST);
			validationResult.setValid(false);
			return validationResult;
		}

		validationResult.setValid(true);
		return validationResult;
	}

}