package sg.edu.nus.iss.voucher.core.workflow.aws.service;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.*;

import sg.edu.nus.iss.voucher.core.workflow.entity.Campaign;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MessagePublishService {

    @Autowired
    private AmazonSNS amazonSNSClient;

    @Value("${aws.sns.feed.topic.arn}") String topicArn;
    
    private static final Logger logger = LoggerFactory.getLogger(MessagePublishService.class);

    public void sendNotification(Campaign campaign) {
    	
        JSONObject jsonObjectMsg = new JSONObject();
        jsonObjectMsg.put("campaign", campaign.getDescription());
        jsonObjectMsg.put("store",campaign.getStore().getStoreName());
        jsonObjectMsg.put("preference", campaign.getPreferences());
        
    	logger.info("Message published to SNS: " + jsonObjectMsg.toString());
        PublishRequest request = new PublishRequest().withTopicArn(topicArn.trim()).withMessage(jsonObjectMsg.toString());
        PublishResult result= amazonSNSClient.publish(request);
        logger.info("Message published successfully to SNS with Id: " + result.getMessageId());
    }
}
