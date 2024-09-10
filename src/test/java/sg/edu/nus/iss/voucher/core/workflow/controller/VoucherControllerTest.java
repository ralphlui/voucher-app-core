package sg.edu.nus.iss.voucher.core.workflow.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import sg.edu.nus.iss.voucher.core.workflow.api.connector.AuthAPICall;
import sg.edu.nus.iss.voucher.core.workflow.dto.VoucherDTO;
import sg.edu.nus.iss.voucher.core.workflow.entity.Campaign;
import sg.edu.nus.iss.voucher.core.workflow.entity.Store;
import sg.edu.nus.iss.voucher.core.workflow.entity.Voucher;
import sg.edu.nus.iss.voucher.core.workflow.enums.CampaignStatus;
import sg.edu.nus.iss.voucher.core.workflow.enums.VoucherStatus;
import sg.edu.nus.iss.voucher.core.workflow.service.impl.CampaignService;
import sg.edu.nus.iss.voucher.core.workflow.service.impl.UserValidatorService;
import sg.edu.nus.iss.voucher.core.workflow.service.impl.VoucherService;
import sg.edu.nus.iss.voucher.core.workflow.utility.DTOMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class VoucherControllerTest {
	
	@Autowired
	private MockMvc mockMvc;


	@MockBean
	private VoucherService voucherService;
	
	@MockBean
	private CampaignService campaignService;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@Autowired
	AuthAPICall apiCall;



	private static List<VoucherDTO> mockVouchers = new ArrayList<>();
	private static Store store = new Store("1", "MUJI",
			"MUJI offers a wide variety of good quality items from stationery to household items and apparel.", "Test",
			"#04-36/40 Paragon Shopping Centre", "290 Orchard Rd", "", "238859", "Singapore", "Singapore", "Singapore",
			null, null, null, null, false, null, "M1", "");
	private static Campaign campaign = new Campaign("1", "new campaign 1", store, CampaignStatus.CREATED, null, 10, 0,
			null, null, 10, LocalDateTime.now(), LocalDateTime.now(), "test1@gmail.com", "", LocalDateTime.now(),
			LocalDateTime.now(), null,"Clothes", false);
	private static Voucher voucher1 = new Voucher("1", campaign, VoucherStatus.CLAIMED, LocalDateTime.now(), null,
			"U1");
	private static Voucher voucher2 = new Voucher("2", campaign, VoucherStatus.CLAIMED, LocalDateTime.now(), null,
			"U1");

	@BeforeAll
	static void setUp() {
		mockVouchers.add(DTOMapper.toVoucherDTO(voucher1));
		mockVouchers.add(DTOMapper.toVoucherDTO(voucher1));
	}


	@Test
	void testGetVoucherByVoucherId() throws Exception {
		Mockito.when(voucherService.findByVoucherId(voucher2.getVoucherId()))
				.thenReturn(DTOMapper.toVoucherDTO(voucher2));
		mockMvc.perform(MockMvcRequestBuilders.get("/api/vouchers/{id}", voucher2.getVoucherId()).contentType(MediaType.APPLICATION_JSON))
		         .andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.success").value(true)).andDo(print());
	}
	
	@Test
	void testClaimVoucher() throws Exception {

		Mockito.when(campaignService.findById(campaign.getCampaignId())).thenReturn(Optional.of(campaign));

		Mockito.when(voucherService.claimVoucher(Mockito.any(Voucher.class)))
				.thenReturn(DTOMapper.toVoucherDTO(voucher1));

		mockMvc.perform(MockMvcRequestBuilders.post("/api/vouchers/claim").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(voucher1))).andExpect(MockMvcResultMatchers.status().isBadRequest())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.success").value(false))	
				.andExpect(jsonPath("$.message").value("User account not found.")).andDo(print());
	}
}