package sg.edu.nus.iss.voucher.core.workflow.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import sg.edu.nus.iss.voucher.core.workflow.dto.APIResponse;
import sg.edu.nus.iss.voucher.core.workflow.dto.StoreDTO;
import sg.edu.nus.iss.voucher.core.workflow.dto.ValidationResult;
import sg.edu.nus.iss.voucher.core.workflow.entity.Store;
import sg.edu.nus.iss.voucher.core.workflow.enums.UserRoleType;
import sg.edu.nus.iss.voucher.core.workflow.exception.StoreNotFoundException;
import sg.edu.nus.iss.voucher.core.workflow.service.impl.StoreService;
import sg.edu.nus.iss.voucher.core.workflow.service.impl.UserValidatorService;
import sg.edu.nus.iss.voucher.core.workflow.strategy.impl.StoreValidationStrategy;
import sg.edu.nus.iss.voucher.core.workflow.utility.GeneralUtility;

import org.springframework.data.domain.*;

@RestController
@Validated
@RequestMapping("/api/core/stores")
public class StoreController {
	
	private static final Logger logger = LoggerFactory.getLogger(StoreController.class);

	@Autowired
	private StoreService storeService;
	
	@Autowired
	private StoreValidationStrategy storeValidationStrategy;
	
	@Autowired
	private UserValidatorService userValidatorService;



	@GetMapping(value = "", produces = "application/json")
	public ResponseEntity<APIResponse<List<StoreDTO>>> getAllActiveStoreList(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "500") int size) {
		logger.info("Call store getAll API with page={}, size={}", page, size);

		try {

			Pageable pageable = PageRequest.of(page, size, Sort.by("storeName").ascending());
			Map<Long, List<StoreDTO>> resultMap = storeService.getAllActiveStoreList(pageable);
            logger.info("size" + resultMap.size());
            
            Map.Entry<Long, List<StoreDTO>> firstEntry = resultMap.entrySet().iterator().next();
			long totalRecord = firstEntry.getKey();
			List<StoreDTO> storeDTOList = firstEntry.getValue();
			
			if (storeDTOList.size() > 0) {
				return ResponseEntity.status(HttpStatus.OK).body(
						APIResponse.success(storeDTOList, "Successfully get all active store.", totalRecord));

			} else {
				String message = "No Active Store List.";
				logger.error(message);
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(APIResponse.noList(storeDTOList, message));
			}
			

		} catch (Exception e) {
			logger.error("Error: ", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(APIResponse.error("Error: " + e.getMessage()));
		}

	}
	

	@PostMapping(value = "", produces = "application/json")
	public ResponseEntity<APIResponse<StoreDTO>> createStore(@RequestPart("store") Store store,
			@RequestPart(value = "image", required = false) MultipartFile uploadFile) {
		logger.info("Call store create API...");
		String message = "";
		StoreDTO storeDTO = new StoreDTO();
		try {
			ValidationResult validationResult = storeValidationStrategy.validateCreation(store, uploadFile);

			if (validationResult.isValid()) {

				storeDTO = storeService.createStore(store, uploadFile);
				message = "Store created successfully.";
				return ResponseEntity.status(HttpStatus.OK).body(APIResponse.success(storeDTO, message));

			} else {
				message = validationResult.getMessage();
				logger.error(message);
				return ResponseEntity.status(validationResult.getStatus()).body(APIResponse.error(message));
			}

		} catch (Exception ex) {
			message = "Error: " + ex.toString();
			logger.error(message);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(APIResponse.error(ex.getMessage()));
		}

	}
	
	
	@GetMapping(value = "/{id}", produces = "application/json")
	public ResponseEntity<APIResponse<StoreDTO>> getStoreById(@PathVariable("id") String id) {
		logger.info("Call store getStoreById API...");

		String message = "";

		try {
			String storeId = GeneralUtility.makeNotNull(id).trim();
			logger.info("storeId: " + storeId);

			if (storeId.isEmpty()) {
				message = "Bad Request: Store Id could not be blank.";
				logger.error(message);
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(APIResponse.error(message));
			}

			StoreDTO storeDTO = storeService.findByStoreId(storeId);
			return ResponseEntity.status(HttpStatus.OK)
					.body(APIResponse.success(storeDTO, "Successfully get store Id: " + storeId));

		}

		catch (Exception e) {
			message = e.getMessage();
			logger.error(message);
			HttpStatusCode htpStatuscode = e instanceof StoreNotFoundException ? HttpStatus.NOT_FOUND
					: HttpStatus.INTERNAL_SERVER_ERROR;
			return ResponseEntity.status(htpStatuscode).body(APIResponse.error(message));

		}

	}
	
	@GetMapping(value = "/users/{userId}", produces = "application/json")
	public ResponseEntity<APIResponse<List<StoreDTO>>> getAllStoreByUser(@PathVariable("userId") String userId,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "500") int size) {

		logger.info("Call store getAllByUser API with page={}, size={}", page, size);
		String message = "";

		try {

			String userid = GeneralUtility.makeNotNull(userId).trim();
			if (userid.isEmpty()) {
				message = "User id cannot be blank.";
				logger.error(message);
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(APIResponse.error(message));
			}

			logger.info("UserId: " + userid);
			HashMap<Boolean, String> userMap = userValidatorService.validateActiveUser(userid, UserRoleType.MERCHANT.toString());
			logger.info("user Id key map "+ userMap.keySet());
			
			for (Map.Entry<Boolean, String> entry : userMap.entrySet()) {
				logger.info("user role: " + entry.getValue());
				logger.info("user id: " + entry.getKey());
				
				if (!entry.getKey()) {
					message = entry.getValue();
					logger.error(message);
					return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(APIResponse.error(message));
				}
			}

			Pageable pageable = PageRequest.of(page, size, Sort.by("storeName").ascending());
			Map<Long, List<StoreDTO>> resultMap = storeService.findActiveStoreListByUserId(userid, false, pageable);
			logger.info("size " + resultMap.size());

			Map.Entry<Long, List<StoreDTO>> firstEntry = resultMap.entrySet().iterator().next();
			long totalRecord = firstEntry.getKey();
			List<StoreDTO> storeDTOList = firstEntry.getValue();

			if (storeDTOList.size() > 0) {
				return ResponseEntity.status(HttpStatus.OK)
						.body(APIResponse.success(storeDTOList, "Successfully get all active store by user.", totalRecord));

			} else {
				message = "No Active Store List.";
				logger.error(message);
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(APIResponse.noList(storeDTOList, message));
			}

		} catch (Exception e) {
			message = e.getMessage();
			logger.error(message);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(APIResponse.error(message));

		}
	}
	
	@PutMapping(value = "", produces = "application/json")
	public ResponseEntity<APIResponse<StoreDTO>> updateStore(@RequestPart("store") Store store,
			@RequestPart(value = "image", required = false) MultipartFile uploadFile) {
		logger.info("Call store updat API...");
		String message = "";
		StoreDTO storeDTO = new StoreDTO();
		try {

			ValidationResult validationResult = storeValidationStrategy.validateUpdating(store, uploadFile);
			if (!validationResult.isValid()) {
				message = validationResult.getMessage();
				logger.error(message);
				return ResponseEntity.status(validationResult.getStatus()).body(APIResponse.error(message));
			}

			storeDTO = storeService.updateStore(store, uploadFile);
			message = "Store updated successfully.";

			return ResponseEntity.status(HttpStatus.OK).body(APIResponse.success(storeDTO, message));

		} catch (Exception e) {
			message = "Error: " + e.getMessage();
			logger.error(message);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(APIResponse.error(message));
		}

	}
}
