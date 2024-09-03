package sg.edu.nus.iss.voucher.core.workflow.service;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Pageable;

import sg.edu.nus.iss.voucher.core.workflow.dto.StoreDTO;

public interface IStoreService {

	Map<Long, List<StoreDTO>> getAllActiveStoreList(Pageable pageable) ;
}
