import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import org.json.JSONException;
import org.json.JSONObject;

public class BotLogicTest {
	private static String access_id = "enter your access id here";
	private static String secret_key = "enter your secret key here";

	public static CoinExAPI cep = new CoinExAPI(access_id, secret_key);
	
	public static void main(String[] args) throws UnsupportedEncodingException, NoSuchAlgorithmException, JSONException {
//		//Account Balance
//		//Attributes: available/frozen 
//		JSONObject bal = new JSONObject(cep.getAccInfo());
//		String BTCbal = bal.getJSONObject("data").getJSONObject("BTC").getString("available");
//		System.out.println("BTC Bal: " + BTCbal);
//		 
//		String USDTBal = bal.getJSONObject("data").getJSONObject("USDT").getString("available");
//		System.out.println("USDT Bal: " + USDTBal);


		
//		//Mining Difficulty 
//		JSONObject diff = new JSONObject(cep.getMineDiff());
//		String mineDiff = diff.getJSONObject("data").getString("difficulty");
//		System.out.println("Mining difficulty: " + mineDiff + " CET/hour");
		
		
		
//		//Pending orders + Cancel pending (Params: Market/Page num/Orders per page)
//		JSONObject pendingOrders = new JSONObject(cep.getPendingOrders("CETETH", "1", "2"));
//				
//		int count = pendingOrders.getJSONObject("data").getInt("count");
//			
//		System.out.println("Pending orders count: " + count);
//						
//		while (count > 0) {
//			//there are pending orders, get order id to cancel
//			int cancelOrderID = pendingOrders.getJSONObject("data").getJSONArray("data").getJSONObject(0).getInt("count");
//			String cancelOrderMkt = pendingOrders.getJSONObject("data").getJSONArray("data").getJSONObject(count).getString("market");
//			System.out.println("Order ID to cancel: " + cancelOrderID);
//			System.out.println("Order MKT to cancel: " + cancelOrderMkt);
//
//			//Cancel Order (Params: Order ID, Market)
//			cep.cancelOrders(String.valueOf(cancelOrderID), cancelOrderMkt);
//			
//			count--;
//		}
		
		
		
//		//Get Real time Highest Bid (buy price) and Lowest Ask (lowest sell price)
//		String market = "CETETH";
//		double[] bidAsk = cep.getBidAsk(market);
//		double bid =  bidAsk[0];
//		double ask =  bidAsk[1];
//		System.out.printf(market + "\nBID = %.8f, ASK = %.8f", bid, ask);
//		
//		int divisor = 10000; //tentative 10k
//		double orderBid = bid+bid/divisor;
//
//		System.out.printf("\nTESTBID = %.8f", orderBid);
//		
//		//Set Limit Buy/sell Order
//		String qty = "10";
//		JSONObject obj1 = new JSONObject(cep.setLimitOrder(market, qty, String.valueOf(orderBid), "buy"));
//		int buyOrderID = obj1.getJSONObject("data").getInt("id");
//		System.out.println(buyOrderID);
		
	}
}
