package sg.edu.nus.iss.voucher.core.workflow.strategy;

import org.springframework.web.multipart.MultipartFile;

import sg.edu.nus.iss.voucher.core.workflow.dto.ValidationResult;

public interface IAPIHelperValidationStrategy<T> {

	ValidationResult validateCreation(T data,MultipartFile val);
	
	ValidationResult validateObject(String userId);
}
