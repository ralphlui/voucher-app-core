package sg.edu.nus.iss.voucher.core.workflow.controller;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import sg.edu.nus.iss.voucher.core.workflow.dto.APIResponse;
import sg.edu.nus.iss.voucher.core.workflow.dto.StoreDTO;
import sg.edu.nus.iss.voucher.core.workflow.service.impl.StoreService;

import org.springframework.data.domain.*;

@RestController
@Validated
@RequestMapping("/api/stores")
public class StoreController {
	
	private static final Logger logger = LoggerFactory.getLogger(StoreController.class);

	@Autowired
	private StoreService storeService;


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
						APIResponse.success(storeDTOList, "Successfully get all active store..", totalRecord));

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
}
