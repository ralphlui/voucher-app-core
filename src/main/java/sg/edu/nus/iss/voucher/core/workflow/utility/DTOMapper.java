package sg.edu.nus.iss.voucher.core.workflow.utility;

import sg.edu.nus.iss.voucher.core.workflow.dto.StoreDTO;
import sg.edu.nus.iss.voucher.core.workflow.entity.Store;

public class DTOMapper {

	public static StoreDTO mapStoreToResult(Store store) {
		StoreDTO storeDTO = new StoreDTO();
		storeDTO.setStoreId(store.getStoreId());
		storeDTO.setStoreName(store.getStoreName());
		storeDTO.setDescription(GeneralUtility.makeNotNull(store.getDescription()));

		String address = GeneralUtility.makeNotNull(store.getAddress1()).trim();
		address += address.isEmpty() ? "" : ", " + GeneralUtility.makeNotNull(store.getAddress2()).trim();
		address += address.isEmpty() ? "" : ", " + GeneralUtility.makeNotNull(store.getAddress3()).trim();
		address += address.isEmpty() ? "" : ", " + GeneralUtility.makeNotNull(store.getPostalCode());

		storeDTO.setAddress(GeneralUtility.makeNotNull(address));
		storeDTO.setAddress1(store.getAddress1());
		storeDTO.setAddress2(store.getAddress2());
		storeDTO.setAddress3(store.getAddress3());
		storeDTO.setCity(GeneralUtility.makeNotNull(store.getCity()));
		storeDTO.setState(GeneralUtility.makeNotNull(store.getState()));
		storeDTO.setCountry(GeneralUtility.makeNotNull(store.getCountry()));
		storeDTO.setContactNumber(GeneralUtility.makeNotNull(store.getContactNumber()));
		storeDTO.setPostalCode(GeneralUtility.makeNotNull(store.getPostalCode()));
		storeDTO.setImage(GeneralUtility.makeNotNull(store.getImage()));

		storeDTO.setCreatedDate(store.getCreatedDate());
		storeDTO.setUpdatedDate(store.getUpdatedDate());
		storeDTO.setCreatedBy(store.getCreatedBy());
		storeDTO.setUpdatedBy(store.getUpdatedBy());
		return storeDTO;
	}
}
