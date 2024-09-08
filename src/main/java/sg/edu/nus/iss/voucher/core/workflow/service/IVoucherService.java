package sg.edu.nus.iss.voucher.core.workflow.service;

import sg.edu.nus.iss.voucher.core.workflow.dto.VoucherDTO;

public interface IVoucherService {

	VoucherDTO findByVoucherId(String voucherId) throws Exception;
}
