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
			if (jsonResponse == null) {
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
}
