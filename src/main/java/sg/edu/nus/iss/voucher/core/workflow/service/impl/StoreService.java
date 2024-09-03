package sg.edu.nus.iss.voucher.core.workflow.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import sg.edu.nus.iss.voucher.core.workflow.dto.StoreDTO;
import sg.edu.nus.iss.voucher.core.workflow.entity.Store;
import sg.edu.nus.iss.voucher.core.workflow.repository.StoreRepository;
import sg.edu.nus.iss.voucher.core.workflow.service.IStoreService;
import sg.edu.nus.iss.voucher.core.workflow.utility.DTOMapper;

@Service
public class StoreService implements IStoreService {

	private static final Logger logger = LoggerFactory.getLogger(StoreService.class);

	@Autowired
	private StoreRepository storeRepository;

	@Override
	public Map<Long, List<StoreDTO>> getAllActiveStoreList(Pageable pageable) {
		try {
			Page<Store> storePages = storeRepository.findByIsDeletedFalse(pageable);
			long totalRecord = storePages.getTotalElements();
			List<StoreDTO> storeDTOList = new ArrayList<>();
			if (totalRecord > 0) {

				for (Store store : storePages.getContent()) {
					StoreDTO storeDTO = DTOMapper.mapStoreToResult(store);
					storeDTOList.add(storeDTO);
				}
			}

			Map<Long, List<StoreDTO>> result = new HashMap<>();
			result.put(totalRecord, storeDTOList);
			return result;

		} catch (Exception ex) {
			logger.error("findByIsDeletedFalse exception... {}", ex.toString());
			return null;
		}

	}
}
