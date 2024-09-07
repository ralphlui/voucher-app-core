package sg.edu.nus.iss.voucher.core.workflow.utility;

import java.util.HashMap;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import sg.edu.nus.iss.voucher.core.workflow.api.connector.AuthAPICall;

public class JSONReader {

	private static final Logger logger = LoggerFactory.getLogger(JSONReader.class);

	@Autowired
	AuthAPICall apiCall;

	public HashMap<Boolean, String> getSpecificActiveUser(String userId) {

		String responseStr = apiCall.getSpecificActiveUser(userId);
		try {

			JSONParser parser = new JSONParser();
			JSONObject jsonResponse = (JSONObject) parser.parse(responseStr);
			HashMap<Boolean, String> resultMap = new HashMap<Boolean, String>();
			if (jsonResponse == null || jsonResponse.isEmpty()) {
				resultMap.put(false, "Invalid response.");
				return resultMap;
			}
			String message = (String) jsonResponse.get("message");
			Boolean success = (Boolean) jsonResponse.get("success");
			resultMap.put(success, message);
			return resultMap;

		} catch (ParseException e) {
			e.printStackTrace();
			logger.error("Error parsing JSON response... {}", e.toString());
			return null;
		}
	}
	
	public HashMap<String, String> getUserByUserId(String userId) {
		
		HashMap<String, String> resultMap = new HashMap<String, String>();
		String responseStr = apiCall.getSpecificActiveUser(userId);
		try {

			JSONParser parser = new JSONParser();
			JSONObject jsonResponse = (JSONObject) parser.parse(responseStr);
			if (jsonResponse == null || jsonResponse.isEmpty()) {
				return resultMap;
			}
			JSONObject data = (JSONObject) jsonResponse.get("data");
			Boolean success = (Boolean) jsonResponse.get("success");
	
			if (Boolean.TRUE.equals(success) && !GeneralUtility.makeNotNull(data).isEmpty()) {
				String id = (String) data.get("userID");
				String userRole = (String) data.get("role");
				resultMap.put(id, userRole);
			}	

		} catch (ParseException e) {
			e.printStackTrace();
			logger.error("Error parsing JSON response... {}", e.toString());
		}
		logger.info("user Id key in json reader "+ resultMap.keySet());
		return resultMap;
	}
}
