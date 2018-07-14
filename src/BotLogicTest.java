import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import org.json.JSONException;

public class BotLogicTest {
	private static String access_id = "enter your access id here";
	private static String secret_key = "enter your secret key here";

	public static CoinExAPI cep = new CoinExAPI(access_id, secret_key);
	
	public static void main(String[] args) throws UnsupportedEncodingException, NoSuchAlgorithmException, JSONException {
		// System.out.println("Tonce: " + tonce);		
		
		/* Account Balance
		 System.out.println(getAccInfo());
		 JSONObject obj0 = new JSONObject(getAccInfo());
		 String result0 =
		 obj0.getJSONObject("data").getJSONObject("BCH").getString("available");
		 System.out.println(result0);
		*/
		
		/* Mining Difficulty 
		System.out.println(getMineDiff());
		*/
		
		/*Set Limit Buy/sell Order
		 JSONObject obj1 = new
		 JSONObject(setLimitOrder("10","CETETH","0.0000019","buy"));
		 int result1 = obj1.getJSONObject("data").getInt("id");
		 System.out.println(result1);
		 */
		
		System.out.println(cep.getPendingOrders("CETETH", "1", "5"));

		/*Cancel Order with ID
		 cancelOrders(String.valueOf(999861337), "CETETH"); //first value taken from
		 result 1
		 */
		
		/*Get Real time Highest Bid and Lowest Ask
		System.out.println("Bid: " + getBidAsk()[0] + "\nAsk: " + getBidAsk()[1]);
		*/
	}
}
