package sg.edu.nus.iss.voucher.core.workflow.utility;

import java.util.HashMap;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import sg.edu.nus.iss.voucher.core.workflow.api.connector.AuthAPICall;

@Component
public class JSONReader {

	private static final Logger logger = LoggerFactory.getLogger(JSONReader.class);

	@Autowired
	AuthAPICall apiCall;

	public HashMap<Boolean, String> validateActiveUser(String userId) {
		HashMap<Boolean, String> resultMap = new HashMap<Boolean, String>();

		try {
			String responseStr = apiCall.validateActiveUser(userId);
			if (responseStr == null || responseStr.isEmpty()) {
				resultMap.put(false, "Invalid API response.");
				return resultMap;
			}
			JSONParser parser = new JSONParser();
			JSONObject jsonResponse = (JSONObject) parser.parse(responseStr);
			if (jsonResponse == null || jsonResponse.isEmpty()) {
				resultMap.put(false, "Empty user JSON response.");
				return resultMap;
			}
			JSONObject data = (JSONObject) jsonResponse.get("data");
			Boolean success = (Boolean) jsonResponse.get("success");
			String message = (String) jsonResponse.get("message");

			if (!GeneralUtility.makeNotNull(data).isEmpty()) {
				String id = (String) data.get("userID");
				String userRole = (String) data.get("role");
				if (!id.equals(userId) || !userRole.toUpperCase().equals("MERCHANT")) {
					message = "Invalid user Id or role";
					resultMap.put(false, message);
					return resultMap;
				}
			}

			resultMap.put(success, message);

		} catch (ParseException e) {
			e.printStackTrace();
			logger.error("Error parsing JSON response... {}", e.toString());
		}
		return resultMap;
	}
}
