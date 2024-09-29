package sg.edu.nus.iss.voucher.core.workflow.rabbitmq.service;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import sg.edu.nus.iss.voucher.core.workflow.entity.Campaign;

@Service
public class NotificationPublishingService {

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Value("${rabbitmq.exchange}")
	private String exchange;

	@Value("${rabbitmq.routingkey}")
	private String routingKey;
	
	private static final Logger logger = LoggerFactory.getLogger(NotificationPublishingService.class);

	public void publishMessage(Campaign campaign) {
		
		// Creating the campaign object
        JSONObject campaignObject = new JSONObject();
        campaignObject.put("campaignId", campaign.getCampaignId());
        campaignObject.put("description", campaign.getDescription());

        // Creating the store object
        JSONObject storeObject = new JSONObject();
        storeObject.put("storeId", campaign.getStore().getStoreId());
        storeObject.put("name", campaign.getStore().getStoreName());

        // Creating the main message object
        JSONObject jsonObjectMsg = new JSONObject();
        jsonObjectMsg.put("category", campaign.getCategory());
        jsonObjectMsg.put("campaign", campaignObject);
        jsonObjectMsg.put("store", storeObject);
 
		rabbitTemplate.convertAndSend(exchange, routingKey, jsonObjectMsg.toString());
		logger.info("Published message: " + jsonObjectMsg.toString());
	}

}
