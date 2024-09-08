package sg.edu.nus.iss.voucher.core.workflow.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import sg.edu.nus.iss.voucher.core.workflow.dto.APIResponse;
import sg.edu.nus.iss.voucher.core.workflow.dto.VoucherDTO;
import sg.edu.nus.iss.voucher.core.workflow.service.impl.VoucherService;

@RestController
@Validated
@RequestMapping("/api/vouchers")
public class VoucherController {

	private static final Logger logger = LoggerFactory.getLogger(VoucherController.class);

	@Autowired
	private VoucherService voucherService;

	@GetMapping(value = "/{id}", produces = "application/json")
	public ResponseEntity<APIResponse<VoucherDTO>> getByVoucherId(@PathVariable ("id") String id) {
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
				return ResponseEntity.status(HttpStatus.OK)
						.body(APIResponse.success(voucherDTO, message));
			}
			message = "Voucher not found by voucherId: " + voucherId;
			logger.error(message);
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(APIResponse.error(message));

		} catch (Exception ex) {
			logger.error("Calling Voucher get Voucher API failed...");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(APIResponse.error("Failed to get voucherId " + voucherId));
		}

	}
}
