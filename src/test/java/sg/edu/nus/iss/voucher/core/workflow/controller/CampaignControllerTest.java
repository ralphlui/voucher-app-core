package sg.edu.nus.iss.voucher.core.workflow.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import sg.edu.nus.iss.voucher.core.workflow.dto.*;
import sg.edu.nus.iss.voucher.core.workflow.entity.*;
import sg.edu.nus.iss.voucher.core.workflow.enums.CampaignStatus;
import sg.edu.nus.iss.voucher.core.workflow.service.impl.*;
import sg.edu.nus.iss.voucher.core.workflow.utility.DTOMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class CampaignControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private CampaignService campaignService;
	
	@MockBean
	private StoreService storeService;

	private static List<CampaignDTO> mockCampaigns = new ArrayList<>();
	
	static String userId ="user123";
	
	private static Store store = new Store("1", "MUJI",
			"MUJI offers a wide variety of good quality items from stationery to household items and apparel.", "Test",
			"#04-36/40 Paragon Shopping Centre", "290 Orchard Rd", "", "238859", "Singapore", "Singapore", "Singapore",
			null, null, null, null, false, null, "", "");

	private static Campaign campaign1 = new Campaign("1", "new campaign 1", store, CampaignStatus.CREATED, null, 10, 0,
			null, null, 10, LocalDateTime.now(), LocalDateTime.now(), userId, "", LocalDateTime.now(),
			LocalDateTime.now(), null,"Clothes", false);
	private static Campaign campaign2 = new Campaign("2", "new campaign 2", store, CampaignStatus.CREATED, null, 10, 0,
			null, null, 10, LocalDateTime.now(), LocalDateTime.now(), userId, "", LocalDateTime.now(),
			LocalDateTime.now(), null,"Clothes", false);

	@BeforeAll
	static void setUp() {
		mockCampaigns.add(DTOMapper.toCampaignDTO(campaign1));
		mockCampaigns.add(DTOMapper.toCampaignDTO(campaign2));
	}

	@Test
	void testGetAllActiveCampaigns() throws Exception {
		Pageable pageable = PageRequest.of(0, 10, Sort.by("startDate").ascending());
		Map<Long, List<CampaignDTO>> mockCampaignMap = new HashMap<>();
		mockCampaignMap.put(0L, mockCampaigns);

		Mockito.when(campaignService.findAllActiveCampaigns(pageable)).thenReturn(mockCampaignMap);

		mockMvc.perform(MockMvcRequestBuilders.get("/api/campaigns").param("page", "0").param("size", "10")
				.contentType(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.success").value(true)).andExpect(jsonPath("$.data[0].campaignId").value(1))
				.andDo(print());
	}
	
	
	@Test
    void testGetAllActiveCampaigns_whenNoCampaignsFound() throws Exception {
        
        Map<Long, List<CampaignDTO>> mockCampaignMap = new HashMap<>();

        Pageable pageable = PageRequest.of(0, 10, Sort.by("startDate").ascending());

        Mockito.when(campaignService.findAllActiveCampaigns(pageable)).thenReturn(mockCampaignMap);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/campaigns")
                .param("page", "0").param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Campaign not found."))
                .andDo(print());
    }

	@Test
	void getAllCampaignsByStoreId() throws Exception {
		Pageable pageable = PageRequest.of(0, 10, Sort.by("startDate").ascending());
		Map<Long, List<CampaignDTO>> mockCampaignMap = new HashMap<>();
		mockCampaignMap.put(0L, mockCampaigns);

		Mockito.when(campaignService.findAllCampaignsByStoreId(store.getStoreId(), pageable))
				.thenReturn(mockCampaignMap);
		mockMvc.perform(
				MockMvcRequestBuilders.get("/api/campaigns/stores/{storeId}", store.getStoreId()).param("page", "0").param("size", "10")
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.success").value(true)).andExpect(jsonPath("$.data[0].campaignId").value(1))
				.andDo(print());
	}
	
	@Test
    void testGetCampaignsByUserId() throws Exception {
       
        int page = 0;
        int size = 10;

        Pageable pageable = PageRequest.of(page, size, Sort.by("startDate").ascending());
        Map<Long, List<CampaignDTO>> mockCampaignMap = new HashMap<>();
		mockCampaignMap.put(0L, mockCampaigns);
		
        Mockito.when(campaignService.findAllCampaignsByEmail(userId, pageable)).thenReturn(mockCampaignMap);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/campaigns/users/{userId}", userId)
                .param("page", String.valueOf(page))
                .param("size", String.valueOf(size))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].campaignId").value(1L))
                .andDo(print());
    }

	@Test
	void testCreateCampaign() throws Exception {
		
		Mockito.when(campaignService.create(Mockito.any(Campaign.class))).thenReturn(DTOMapper.toCampaignDTO(campaign1));
		Mockito.when(storeService.findByStoreId(store.getStoreId())).thenReturn(DTOMapper.toStoreDTO(store));
		
		mockMvc.perform(MockMvcRequestBuilders.post("/api/campaigns").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(campaign1))).andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.success").value(true)).andDo(print());
	}

	@Test
	void testUpdateCampaign() throws Exception {
		Mockito.when(campaignService.findById(campaign1.getCampaignId())).thenReturn(Optional.of(campaign1));
		
		campaign1.setDescription("new desc");
		campaign1.setUpdatedBy(userId);
		Mockito.when(campaignService.update(Mockito.any(Campaign.class))).thenReturn(DTOMapper.toCampaignDTO(campaign1));
		
		mockMvc.perform(MockMvcRequestBuilders.put("/api/campaigns/{id}",campaign1.getCampaignId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(campaign1)))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.success").value(true)).andDo(print());
	}
	
	@Test
	void testPromoteCampaign() throws Exception {
	    
	    campaign1.setStartDate(LocalDateTime.now().plusDays(10));
	    campaign1.setEndDate(LocalDateTime.now().plusDays(20));
	    campaign1.setCampaignStatus(CampaignStatus.CREATED);
		
	    String userId="user123";
	
	    Mockito.when(campaignService.findById(campaign1.getCampaignId())).thenReturn(Optional.of(campaign1));
	    Mockito.when(campaignService.promote(campaign1.getCampaignId(),userId)).thenReturn(DTOMapper.toCampaignDTO(campaign1));

	    mockMvc.perform(MockMvcRequestBuilders.patch("/api/campaigns/{campaignId}/users/{userId}/promote", campaign1.getCampaignId(), "user123")
	            .contentType(MediaType.APPLICATION_JSON))
	            .andExpect(MockMvcResultMatchers.status().isOk())
	            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
	            .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
	            .andDo(print());
	}

	
	@Test
	void testGetByCampaignId() throws Exception {

		
		Mockito.when(campaignService.findByCampaignId(campaign1.getCampaignId()))
				.thenReturn(DTOMapper.toCampaignDTO(campaign1));
		mockMvc.perform(
				MockMvcRequestBuilders.get("/api/campaigns/{id}",campaign1.getCampaignId())
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.success").value(true)).andDo(print());
	}
	

}
