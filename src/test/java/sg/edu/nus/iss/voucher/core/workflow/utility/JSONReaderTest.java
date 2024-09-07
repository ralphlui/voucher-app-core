package sg.edu.nus.iss.voucher.core.workflow.utility;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import sg.edu.nus.iss.voucher.core.workflow.api.connector.AuthAPICall;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.json.simple.parser.ParseException;

@SpringBootTest
@ActiveProfiles("test")
public class JSONReaderTest {
	
	 @InjectMocks
	    private JSONReader jsonReader;

	    @Mock
	    private AuthAPICall apiCall;

	  @Test
	    public void testGetActiveSpecificUser() throws ParseException {
	        String mockResponse = "{\"totalRecord\":1,\"success\":true,\"data\":{\"email\":\"test1@example.com\",\"username\":\"user1\"}}";

	        when(apiCall.getSpecificActiveUser(anyString())).thenReturn(mockResponse);

	        HashMap<Boolean, String> result = jsonReader.getSpecificActiveUser("testId");
	        Boolean success = false;
	        
	        for (Map.Entry<Boolean, String> entry : result.entrySet()) {
				success = entry.getKey();
			}

	        assertNotNull(result);
	        assertEquals(1, result.size());
	        assertEquals(success, true);
	        
	        mockResponse = "{\"totalRecord\":0,\"success\":false,\"data\": null}";

	        when(apiCall.getSpecificActiveUser(anyString())).thenReturn(mockResponse);
	        result = jsonReader.getSpecificActiveUser("testId");
	        
	        for (Map.Entry<Boolean, String> entry : result.entrySet()) {
				success = entry.getKey();
			}

	        assertEquals(success, false);

	    }
	  
		@Test
		public void getUserByUserId() throws ParseException {
			String mockResponse = "{\"totalRecord\":1,\"success\":true,\"data\":{\"userID\":\"testId\",\"email\":\"test1@example.com\",\"username\":\"user1\"}}";

			when(apiCall.getSpecificActiveUser(anyString())).thenReturn(mockResponse);

			HashMap<String, String> result = jsonReader.getUserByUserId("testId");
			String userId = "";

			for (Map.Entry<String, String> entry : result.entrySet()) {
				userId = entry.getKey();
			}

			assertNotNull(result);
			assertEquals(1, result.size());
			assertEquals(userId, "testId");

		}
}
