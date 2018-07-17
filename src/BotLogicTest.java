import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import org.json.JSONException;
import org.json.JSONObject;

public class BotLogicTest {
 	private static String access_id = "enter your access id here";
 	private static String secret_key = "enter your secret key here";

	public static CoinExAPI cep = new CoinExAPI(access_id, secret_key);
	
//	Bot logic: 
//		0.1. Get Balance BTC and USDT
//		0.2. Buy.sell BTC/USD to get same amount worth in BTC and USDT
//		0.3 Open websocket (To allow more calls)
//		0.4. Get Mining Difficulty
//		0.5 Set ratio to mine to prevent overmining (95% default)
//		0.6 Check tokens mined
//		0.7 Check tokens not yet mined
//		0.8 Cancel all pending orders
//		0.9 Buy/sell BTC/USD to get same amount worth in BTC and USDT
//		1. Get bid 
//		2. Put .xx buy order and same sell order
//		2.5 Check market depth for the latest best asks and bids.  (ask/bid)
//		[If asks is higher than order means buy order pending. If bid is lower than order means sell order pending ]
//		3.Check Pending (Buy or sell)
//		4 Update tokens mined and not mined if order executed
//		6.5 If order still pending for a certain period of time, cancel order and go back to step 2 and place buy or sell order (whichever that was cancelled at + #% for buy order or - #% for sell order)
//		7. Repeat from 0.6
	
	private static void buySameBTCUSD(double BTCBal, double USDTBal) throws UnsupportedEncodingException, NoSuchAlgorithmException, JSONException {
		System.out.printf("\nBTC Bal = %.8f", BTCBal);
		System.out.printf("\nUSDT Bal = %.8f\n", USDTBal);
		
		//Get Real time Lowest Ask (lowest sell price)
		String market = "BTCUSDT";
		double[] bidAsk = cep.getBidAsk(market);
		double ask =  bidAsk[1];
		System.out.printf(market + " ASK = %.8f", ask);
		
		//If BTC in USDT is more than USDT, sell some BTC to get equivalent USDT
		double BTCinUSDT = BTCBal * ask;
		if (BTCinUSDT  > USDTBal) {
			double USDTToGet = (BTCinUSDT - USDTBal)/2;
			double BTCToSell = USDTToGet/ask;
			
			System.out.printf("\nBTC TO SELL = " + BTCToSell + ", IN USD VALUE = " + BTCToSell*ask);
			
			//Set Limit sell Order
			String qty = Double.toString(BTCToSell);
//			JSONObject obj1 = new JSONObject(cep.setLimitOrder(market, qty, String.valueOf(ask), "sell"));
//			int sellOrderID = obj1.getJSONObject("data").getInt("id");
//			System.out.println(sellOrderID);
			
			//If USDT is more than BTC in USDT, sell some USDT (buy BTC) to get equivalent USDT
		} else if (BTCinUSDT  < USDTBal) {
			double USDTToSell = (USDTBal - BTCinUSDT)/2;
			double BTCToGet = USDTToSell/ask;
			System.out.printf("\nUSDT To Sell = " + USDTToSell + ", IN BTC VALUE = %.8f\n\n", BTCToGet);
			
			//Set Limit buy Order
			String qty = Double.toString(BTCToGet);
//			JSONObject obj1 = new JSONObject(cep.setLimitOrder(market, qty, String.valueOf(ask), "buy"));
//			int buyOrderID = obj1.getJSONObject("data").getInt("id");
//			System.out.println(buyOrderID);
		}
	}
	
	public static void main(String[] args) throws UnsupportedEncodingException, NoSuchAlgorithmException, JSONException {
		//Step 0.1 Done (Account Balance (Attributes: available/frozen))
		JSONObject bal = new JSONObject(cep.getAccInfo());
		String BTCBal = bal.getJSONObject("data").getJSONObject("BTC").getString("available");		 
		String USDTBal = bal.getJSONObject("data").getJSONObject("USDT").getString("available");

		
		//Step 0.2 Done
		buySameBTCUSD(Double.parseDouble(BTCBal), Double.parseDouble(USDTBal));

		
		//Step 0.3 (TODO - Only need one call every 08:00 GMT+8, since difficulty changes daily)
		//Step 0.4, 0.6, 0.7 DONE
		JSONObject diff = new JSONObject(cep.getMineDiff());
		int updateTime = diff.getJSONObject("data").getInt("update_time");
		System.out.println("Mining difficulty update time: " + updateTime);
		double mineDiff = diff.getJSONObject("data").getDouble("difficulty");
		System.out.println("Mining difficulty: " + mineDiff + " CET/hour");
		double mined = diff.getJSONObject("data").getDouble("prediction");
		System.out.println("Mined amount: " + mined + " CET");
		double unmined = mineDiff - mined;
		System.out.println("Unmined amount: " + unmined + " CET");
		
		//Step 0.8 (TODO)
		//If unmined = 0 or negative, cancel all pending orders, stop mining for that hour
		if (unmined <= 0) {
			//Cancel pending orders
		}

		
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
