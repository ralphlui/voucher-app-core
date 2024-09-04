package sg.edu.nus.iss.voucher.core.workflow.service.impl;

import java.time.LocalDateTime;
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
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;

import sg.edu.nus.iss.voucher.core.workflow.configuration.VoucherCoreSecurityConfig;
import sg.edu.nus.iss.voucher.core.workflow.dto.StoreDTO;
import sg.edu.nus.iss.voucher.core.workflow.entity.Store;
import sg.edu.nus.iss.voucher.core.workflow.repository.StoreRepository;
import sg.edu.nus.iss.voucher.core.workflow.service.IStoreService;
import sg.edu.nus.iss.voucher.core.workflow.utility.DTOMapper;
import sg.edu.nus.iss.voucher.core.workflow.utility.GeneralUtility;
import sg.edu.nus.iss.voucher.core.workflow.utility.ImageUploadToS3;

@Service
public class StoreService implements IStoreService {

	private static final Logger logger = LoggerFactory.getLogger(StoreService.class);

	@Autowired
	private StoreRepository storeRepository;

	@Autowired
	private AmazonS3 s3Client;

	@Autowired
	private VoucherCoreSecurityConfig securityConfig;

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

	@Override
	public StoreDTO createStore(Store store, MultipartFile uploadFile) throws Exception {

		try {
			store = this.uploadImage(store, uploadFile);
			store.setCreatedDate(LocalDateTime.now());
			logger.info("Saving store...");
			Store createdStore = storeRepository.save(store);
			logger.info("Saved successfully...{}", createdStore.getStoreId());
			StoreDTO storeDTO = DTOMapper.toStoreDTO(createdStore);
			if (storeDTO == null) {
				throw new Exception("Create store failed: Unable to create a new store.");
			}
			return storeDTO;

		} catch (Exception e) {
			logger.error("Error occurred while user creating, " + e.toString());
			e.printStackTrace();
			throw e;

		}

	}
	

	@Override
	public StoreDTO findByStoreName(String storename) {
		StoreDTO storeDTO = new StoreDTO();
		try {
			Store store = storeRepository.findByStoreName(storename);
			storeDTO = DTOMapper.toStoreDTO(store);
		} catch (Exception ex) {
			logger.error("findByStoreId exception... {}", ex.toString());

		}
		return storeDTO;

	}

	@Override
	public Store uploadImage(Store store, MultipartFile uploadFile) {
		try {
			if (!GeneralUtility.makeNotNull(uploadFile).equals("")) {
				logger.info("create store: " + store.getStoreName() + "::" + uploadFile.getOriginalFilename());
				if (securityConfig != null) {

					boolean isImageUploaded = ImageUploadToS3.checkImageExistBeforeUpload(s3Client, uploadFile,
							securityConfig, securityConfig.getS3ImagePublic().trim());
					if (isImageUploaded) {
						String imageUrl = securityConfig.getS3ImageUrlPrefix().trim() + "/"
								+ securityConfig.getS3ImagePublic().trim() + uploadFile.getOriginalFilename().trim();
						store.setImage(imageUrl);
					}
				}

			}
		} catch (Exception e) {
			logger.error("Error occurred while uploading Image, " + e.toString());
			e.printStackTrace();

		}
		return store;
	}

}
