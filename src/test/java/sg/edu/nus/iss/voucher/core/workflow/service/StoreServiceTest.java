package sg.edu.nus.iss.voucher.core.workflow.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import sg.edu.nus.iss.voucher.core.workflow.api.connector.AuthAPICall;
import sg.edu.nus.iss.voucher.core.workflow.controller.StoreController;
import sg.edu.nus.iss.voucher.core.workflow.dto.StoreDTO;
import sg.edu.nus.iss.voucher.core.workflow.entity.Store;
import sg.edu.nus.iss.voucher.core.workflow.repository.StoreRepository;
import sg.edu.nus.iss.voucher.core.workflow.service.impl.StoreService;
import sg.edu.nus.iss.voucher.core.workflow.utility.JSONReader;

@SpringBootTest
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class StoreServiceTest {
	
	@MockBean
	private StoreRepository storeRepository;


	@Autowired
	private StoreService storeService;
	
	@Mock
	private AuthAPICall apiCall;
	
	@Autowired
	private JSONReader jsonReader;
	
	private static final Logger logger = LoggerFactory.getLogger(StoreController.class);


	private static Store store = new Store("1", "MUJI",
			"MUJI offers a wide variety of good quality items from stationery to household items and apparel.", "Test",
			"#04-36/40 Paragon Shopping Centre", "290 Orchard Rd", "", "238859", "Singapore", "Singapore", "Singapore",
			null, null, null, null, false, null, "US1", "");

	private static List<Store> mockStores = new ArrayList<>();

	@BeforeEach
	void setUp() {

		mockStores.add(store);
	}
	

	@Test
	void getAllActiveStore() {
		long totalRecord = 0;
		List<StoreDTO> storeDTOList = new ArrayList<StoreDTO>();
		Pageable pageable = PageRequest.of(0, 10);
		Page<Store> mockStoresPage = new PageImpl<>(mockStores, pageable, mockStores.size());

		Mockito.when(storeRepository.findByIsDeletedFalse(pageable)).thenReturn(mockStoresPage);

		Map<Long, List<StoreDTO>> storePages = storeService.getAllActiveStoreList(pageable);
		for (Map.Entry<Long, List<StoreDTO>> entry : storePages.entrySet()) {
			totalRecord = entry.getKey();
			storeDTOList = entry.getValue();

		}

		assertThat(totalRecord).isGreaterThan(0);
		assertThat(storeDTOList.get(0).getStoreName()).isEqualTo("MUJI");
	}
	
	@Test
	void testCreateStore() throws Exception {
		Mockito.when(storeRepository.save(Mockito.any(Store.class))).thenReturn(store);
		MockMultipartFile imageFile = new MockMultipartFile("image", "store.jpg", "image/jpg", "store".getBytes());

		StoreDTO storeDTO = storeService.createStore(store, imageFile);

		assertThat(storeDTO).isNotNull();
		assertEquals(storeDTO.getDescription(), store.getDescription());

	}
	
	@Test
	void getByStoreId() {
		store.setStoreId("11");
		Mockito.when(storeRepository.findById(store.getStoreId())).thenReturn(Optional.of(store));
		Optional<Store> dbStore = storeRepository.findById(store.getStoreId());
		assertThat(store).isNotNull();
		assertThat(dbStore.get().getStoreName()).isEqualTo("MUJI");
	}
	
	@Test
	void findByStoreId() {

		Mockito.when(storeRepository.findByStoreIdAndStatus(store.getStoreId(), false)).thenReturn(Optional.of(store));
		StoreDTO storeDTO = storeService.findByStoreId(store.getStoreId());
		assertThat(storeDTO).isNotNull();
		assertEquals(storeDTO.getDescription(), store.getDescription());

	}
	
	@Test
	void findByStoreName() {

		Mockito.when(storeRepository.findByStoreName(store.getStoreName())).thenReturn(store);
		StoreDTO storeDTO = storeService.findByStoreName(store.getStoreName());
		assertThat(storeDTO).isNotNull();
		assertEquals(storeDTO.getDescription(), store.getDescription());

	}
	
	@Test
	void getAllActiveStoreByUser() {
		long totalRecord = 0;
		List<StoreDTO> storeDTOList = new ArrayList<StoreDTO>();
		Pageable pageable = PageRequest.of(0, 10);
		Page<Store> mockStoresPage = new PageImpl<>(mockStores, pageable, mockStores.size());

		Mockito.when(storeRepository.findActiveStoreListByUserId(store.getCreatedBy(), false, pageable)).thenReturn(mockStoresPage);

		Map<Long, List<StoreDTO>> storePage = storeService.findActiveStoreListByUserId(store.getCreatedBy(), false, pageable);
		for (Map.Entry<Long, List<StoreDTO>> entry : storePage.entrySet()) {
			totalRecord = entry.getKey();
			storeDTOList = entry.getValue();

		}
		assertThat(totalRecord).isGreaterThan(0);
		assertThat(storeDTOList.get(0).getStoreName()).isEqualTo("MUJI");
	}
	

	@Test
	void testUpdateStore() throws Exception {
		store.setAddress1("Paragon Shopping Centre");
		store.setContactNumber("+65 238859");

		Mockito.when(storeRepository.save(Mockito.any(Store.class))).thenReturn(store);
		Mockito.when(storeRepository.findById(store.getStoreId())).thenReturn(Optional.of(store));
		MockMultipartFile imageFile = new MockMultipartFile("image", "store.jpg", "image/jpg", "store".getBytes());

		StoreDTO storeDTO = storeService.updateStore(store, imageFile);
		assertThat(storeDTO).isNotNull();
		assertEquals(storeDTO.getDescription(), store.getDescription());
	}	

}
