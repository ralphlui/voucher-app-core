package sg.edu.nus.iss.voucher.core.workflow.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import sg.edu.nus.iss.voucher.core.workflow.api.connector.AuthAPICall;
import sg.edu.nus.iss.voucher.core.workflow.dto.StoreDTO;
import sg.edu.nus.iss.voucher.core.workflow.entity.Store;
import sg.edu.nus.iss.voucher.core.workflow.service.impl.StoreService;
import sg.edu.nus.iss.voucher.core.workflow.utility.DTOMapper;
import sg.edu.nus.iss.voucher.core.workflow.utility.JSONReader;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class StoreControllerTest {
	
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;
	
	@Autowired
	private JSONReader jsonReader;
	
	@Autowired
	AuthAPICall apiCall;

	@MockBean
	private StoreService storeService;

	private static List<StoreDTO> mockStores = new ArrayList<>();

	private static Store store1 = new Store("1", "MUJI",
			"MUJI offers a wide variety of good quality items from stationery to household items and apparel.", "Test",
			"#04-36/40 Paragon Shopping Centre", "290 Orchard Rd", "", "238859", "Singapore", "Singapore", "Singapore",
			null, null, null, null, false, null, "423edfbf-ec17-471f-b45a-892a75fa9008", "");
	private static Store store2 = new Store("1", "MUJI",
			"MUJI offers a wide variety of good quality items from stationery to household items and apparel.", "Test",
			"#04-36/40 Paragon Shopping Centre", "290 Orchard Rd", "", "238859", "Singapore", "Singapore", "Singapore",
			null, null, null, null, false, null, "", "");

	@BeforeAll
	static void setUp() {

		mockStores.add(DTOMapper.toStoreDTO(store1));
		mockStores.add(DTOMapper.toStoreDTO(store2));
	}
	
	@Test
	void testGetAllActiveStore() throws Exception {

		Pageable pageable = PageRequest.of(0, 10, Sort.by("storeName").ascending());
		Map<Long, List<StoreDTO>> mockStoreMap = new HashMap<>();
		mockStoreMap.put(0L, mockStores);

		Mockito.when(storeService.getAllActiveStoreList(pageable)).thenReturn(mockStoreMap);

		mockMvc.perform(MockMvcRequestBuilders.get("/api/stores").param("page", "0").param("size", "10")
				.contentType(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("Successfully get all active store.")).andDo(print());
	}
	

	@Test
	void testCreateStore() throws Exception {

		MockMultipartFile uploadFile = new MockMultipartFile("image", "store.jpg", "image/jpg", "store".getBytes());

		MockMultipartFile store = new MockMultipartFile("store", "store", MediaType.APPLICATION_JSON_VALUE,
				objectMapper.writeValueAsBytes(store1));

		mockMvc.perform(MockMvcRequestBuilders.multipart("/api/stores").file(store).file(uploadFile)
				.contentType(MediaType.MULTIPART_FORM_DATA)).andExpect(MockMvcResultMatchers.status().isUnauthorized())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.message").value("User account not found.")).andDo(print());
		
		MockMultipartFile storeFile = new MockMultipartFile("store", "store", MediaType.APPLICATION_JSON_VALUE,
				objectMapper.writeValueAsBytes(store2));

		mockMvc.perform(MockMvcRequestBuilders.multipart("/api/stores").file(storeFile).file(uploadFile)
				.contentType(MediaType.MULTIPART_FORM_DATA)).andExpect(MockMvcResultMatchers.status().isBadRequest())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.message").value("Bad Request: Store Create user id field could not be blank.")).andDo(print());

	}
	
	@Test
	void testGetStoreById() throws Exception {
		Mockito.when(storeService.findByStoreId(store1.getStoreId())).thenReturn(DTOMapper.toStoreDTO(store1));
		mockMvc.perform(MockMvcRequestBuilders.get("/api/stores/{id}", store1.getStoreId()).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(store1))).andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.success").value(true)).andDo(print());
	}
	

	@Test
	void testGetAllStoreByUser() throws Exception {


		Pageable pageable = PageRequest.of(0, 10, Sort.by("storeName").ascending());
		Map<Long, List<StoreDTO>> mockStoreMap = new HashMap<>();
		mockStoreMap.put(0L, mockStores);

		Mockito.when(storeService.findActiveStoreListByUserId(store1.getCreatedBy(), false, pageable)).thenReturn(mockStoreMap);

		mockMvc.perform(MockMvcRequestBuilders.get("/api/stores/users/{userId}", store1.getCreatedBy()).param("page", "0").param("size", "10")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data[0].storeName").value(store1.getStoreName()))
				.andDo(print());

	}

}
