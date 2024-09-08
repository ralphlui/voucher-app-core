package sg.edu.nus.iss.voucher.core.workflow.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import sg.edu.nus.iss.voucher.core.workflow.dto.VoucherDTO;
import sg.edu.nus.iss.voucher.core.workflow.entity.Campaign;
import sg.edu.nus.iss.voucher.core.workflow.entity.Store;
import sg.edu.nus.iss.voucher.core.workflow.entity.Voucher;
import sg.edu.nus.iss.voucher.core.workflow.enums.CampaignStatus;
import sg.edu.nus.iss.voucher.core.workflow.enums.VoucherStatus;
import sg.edu.nus.iss.voucher.core.workflow.repository.VoucherRepository;
import sg.edu.nus.iss.voucher.core.workflow.service.impl.VoucherService;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class VoucherServiceTest {

	@MockBean
	private VoucherRepository voucherRepository;

	@Autowired
	private VoucherService voucherService;


	private static List<Voucher> mockVouchers = new ArrayList<>();
	private static Store store = new Store("1", "MUJI",
			"MUJI offers a wide variety of good quality items from stationery to household items and apparel.", "Test",
			"#04-36/40 Paragon Shopping Centre", "290 Orchard Rd", "", "238859", "Singapore", "Singapore", "Singapore",
			null, null, null, null, false, null, "M1", "");
	private static Campaign campaign = new Campaign("1", "new voucher 1", store, CampaignStatus.CREATED, null, 0, 0,
			null, null, 0, null, null, "US1", "US1", null, null, mockVouchers, "", false);
	private static Voucher voucher1 = new Voucher("1", campaign, VoucherStatus.CLAIMED, LocalDateTime.now(), null,
			"U1");
	private static Voucher voucher2 = new Voucher("2", campaign, VoucherStatus.CLAIMED, LocalDateTime.now(), null,
			"U1");

	@BeforeAll
	static void setUp() {
		mockVouchers.add(voucher1);
		mockVouchers.add(voucher2);
	}
	
	@Test
	void findSingleVoucher() throws Exception {
		Mockito.when(voucherRepository.findById(voucher1.getVoucherId())).thenReturn(Optional.of(voucher1));
		VoucherDTO voucherDTO = voucherService.findByVoucherId(voucher1.getVoucherId());
		assertEquals(voucherDTO.getVoucherId(), voucher1.getVoucherId());
	}
}
