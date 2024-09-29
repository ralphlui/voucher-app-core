package sg.edu.nus.iss.voucher.core.workflow.rabbitmq.service;

import static org.mockito.Mockito.*;

import java.time.LocalDateTime;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import sg.edu.nus.iss.voucher.core.workflow.entity.*;
import sg.edu.nus.iss.voucher.core.workflow.enums.CampaignStatus;

import org.json.JSONException;
import org.json.JSONObject;

@SpringBootTest
@ActiveProfiles("test")
public class NotificationPublishingServiceTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private NotificationPublishingService notificationPublishingService;

    private String exchange = "test-exchange";
    private String routingKey = "test.key";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
       
        ReflectionTestUtils.setField(notificationPublishingService, "exchange", exchange);
        ReflectionTestUtils.setField(notificationPublishingService, "routingKey", routingKey);
    }

    static String userId = "user123";

    private static Store store = new Store("1", "MUJI",
            "MUJI offers a wide variety of good quality items from stationery to household items and apparel.", "Test",
            "#04-36/40 Paragon Shopping Centre", "290 Orchard Rd", "", "238859", "Singapore", "Singapore", "Singapore",
            null, null, null, null, false, null, "US1", "");

    private static Campaign campaign = new Campaign("100", "new campaign 1", store, CampaignStatus.CREATED, null, 10, 0,
            null, null, 10, LocalDateTime.now(), LocalDateTime.now(), userId, "", LocalDateTime.now(),
            LocalDateTime.now(), null,"Clothes", false);

    @Test
    public void testPublishMessage() throws JSONException {

        // Invoke the method
        notificationPublishingService.publishMessage(campaign);

        // Create expected message structure
        JSONObject expectedCampaignObject = new JSONObject();
        expectedCampaignObject.put("campaignId", campaign.getCampaignId());
        expectedCampaignObject.put("description", campaign.getDescription());

        JSONObject expectedStoreObject = new JSONObject();
        expectedStoreObject.put("storeId", campaign.getStore().getStoreId());
        expectedStoreObject.put("name", campaign.getStore().getStoreName());

        JSONObject expectedMessage = new JSONObject();
        expectedMessage.put("category", campaign.getCategory());
        expectedMessage.put("campaign", expectedCampaignObject);
        expectedMessage.put("store", expectedStoreObject);

        verify(rabbitTemplate).convertAndSend(eq(exchange), eq(routingKey), eq(expectedMessage.toString()));
    }
}

