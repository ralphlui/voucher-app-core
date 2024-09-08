package sg.edu.nus.iss.voucher.core.workflow.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import sg.edu.nus.iss.voucher.core.workflow.dto.VoucherDTO;
import sg.edu.nus.iss.voucher.core.workflow.entity.Voucher;
import sg.edu.nus.iss.voucher.core.workflow.repository.VoucherRepository;
import sg.edu.nus.iss.voucher.core.workflow.service.IVoucherService;
import sg.edu.nus.iss.voucher.core.workflow.utility.DTOMapper;

@Service
public class VoucherService implements IVoucherService {
	
	private static final Logger logger = LoggerFactory.getLogger(VoucherService.class);

	@Autowired
	private VoucherRepository voucherRepository;
	
	@Override
	public VoucherDTO findByVoucherId(String voucherId) throws Exception {
		try {
			Voucher voucher = voucherRepository.findById(voucherId).orElse(null);
			
			if (voucher == null) {
				logger.error("Voucher not found...");
				throw new Exception("Voucher not found by voucherId: " + voucherId);
			}
			
			logger.info("Voucher found...");
			VoucherDTO voucherDTO = DTOMapper.toVoucherDTO(voucher);
			logger.info("Voucher DTO found...");
			return voucherDTO;
			
		} catch (Exception ex) {
			logger.error("Finding voucher by voucher Id exception... {}", ex.toString());
			throw ex;

		}
	}

}
