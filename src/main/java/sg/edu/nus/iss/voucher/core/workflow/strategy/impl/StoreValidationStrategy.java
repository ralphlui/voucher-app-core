package sg.edu.nus.iss.voucher.core.workflow.strategy.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import sg.edu.nus.iss.voucher.core.workflow.dto.StoreDTO;
import sg.edu.nus.iss.voucher.core.workflow.dto.ValidationResult;
import sg.edu.nus.iss.voucher.core.workflow.entity.Store;
import sg.edu.nus.iss.voucher.core.workflow.service.impl.StoreService;
import sg.edu.nus.iss.voucher.core.workflow.strategy.IAPIHelperValidationStrategy;
import sg.edu.nus.iss.voucher.core.workflow.utility.GeneralUtility;
import sg.edu.nus.iss.voucher.core.workflow.utility.JSONReader;

@Service
public class StoreValidationStrategy implements IAPIHelperValidationStrategy<Store> {
	
	@Autowired
	private StoreService storeService;
	
	@Autowired
	private JSONReader jsonReader;

	@Override
	public ValidationResult validateCreation(Store store, MultipartFile val) {

		ValidationResult validationResult = new ValidationResult();
		String userId = GeneralUtility.makeNotNull(store.getCreatedBy());

		if (userId.isEmpty()) {
			validationResult.setMessage("Bad Request: Store Create user id field could not be blank.");
			validationResult.setValid(false);
			validationResult.setStatus(HttpStatus.BAD_REQUEST);
			return validationResult;
		}

		ValidationResult validationObjResult = validateObject(userId);
		if (!validationObjResult.isValid()) {
			return validationObjResult;
		}

		if (store.getStoreName() == null || store.getStoreName().isEmpty()) {
			validationResult.setMessage("Bad Request: Store name could not be blank.");
			validationResult.setStatus(HttpStatus.BAD_REQUEST);
			validationResult.setValid(false);
			return validationResult;

		}

		StoreDTO storeDTO = storeService.findByStoreName(store.getStoreName());
		try {
			if (GeneralUtility.makeNotNull(storeDTO.getStoreName()).equals(store.getStoreName())) {
				validationResult.setMessage("Store already exists.");
				validationResult.setStatus(HttpStatus.BAD_REQUEST);
				validationResult.setValid(false);
				return validationResult;

			}
		} catch (Exception ex) {
			if (storeDTO != null) {

				validationResult.setMessage("Store already exists.");
				validationResult.setValid(false);
				return validationResult;

			}
		}
		validationResult.setValid(true);
		return validationResult;
	}
	
	@Override
	public ValidationResult validateObject(String userId) {

		ValidationResult validationResult = new ValidationResult();
		HashMap<Boolean, String> response = jsonReader.getSpecificActiveUser(userId);
		Boolean success = false;
		String message = "";
		for (Map.Entry<Boolean, String> entry : response.entrySet()) {
			success = entry.getKey();
			message = entry.getValue();
		}
		validationResult.setValid(success);
		validationResult.setMessage(message);
		if (!success) {
			validationResult.setStatus(HttpStatus.UNAUTHORIZED);
		}
		return validationResult;

	}

}
