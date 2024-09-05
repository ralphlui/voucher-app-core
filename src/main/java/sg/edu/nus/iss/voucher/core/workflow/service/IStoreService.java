package sg.edu.nus.iss.voucher.core.workflow.service;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import sg.edu.nus.iss.voucher.core.workflow.dto.StoreDTO;
import sg.edu.nus.iss.voucher.core.workflow.entity.Store;

public interface IStoreService {

	Map<Long, List<StoreDTO>> getAllActiveStoreList(Pageable pageable) ;
	
	StoreDTO createStore(Store store, MultipartFile uploadFile)  throws Exception;
	
	Store uploadImage(Store store, MultipartFile uploadFile);
	
	StoreDTO findByStoreName(String storename);
	
	StoreDTO findByStoreId(String storeId);
}
