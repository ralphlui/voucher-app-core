package sg.edu.nus.iss.voucher.core.workflow.aws.service;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;

import sg.edu.nus.iss.voucher.core.workflow.entity.*;

import org.json.JSONException;
import org.json.JSONObject;

@SpringBootTest
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class MessagePublishServiceTest {

    @Mock
    private AmazonSNS amazonSNSClient;

    @InjectMocks
    private MessagePublishService messagePublishService;

    @Value("${aws.sns.feed.topic.arn}") String topicArn;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        
        messagePublishService.topicArn = topicArn;
    }

    @Test
    void testSendNotification() {
        Campaign campaign = new Campaign();
        campaign.setDescription("Test Campaign");

        Store store = new Store();
        store.setStoreName("Test Store");
        campaign.setStore(store);
        campaign.setPreferences("Test Preferences");

        PublishResult publishResult = new PublishResult();
        publishResult.setMessageId("testMessageId");

        when(amazonSNSClient.publish(any(PublishRequest.class))).thenReturn(publishResult);

        messagePublishService.sendNotification(campaign);

        verify(amazonSNSClient).publish(argThat(request -> {
          
            boolean topicArnMatches = topicArn.equals(request.getTopicArn());

            String expectedMessage = null;
			try {
				expectedMessage = new JSONObject()
				        .put("campaign", "Test Campaign")
				        .put("store", "Test Store")
				        .put("preference", "Test Preferences")
				        .toString();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println("JSONException: {} " + e.toString());
			}
            boolean messageMatches = expectedMessage.equals(request.getMessage());

            System.out.println("Actual request: " + request);

            return topicArnMatches && messageMatches;
        }));
    }
}

