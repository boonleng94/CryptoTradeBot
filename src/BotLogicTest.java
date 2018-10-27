import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;

import org.json.JSONException;
import org.json.JSONObject;

public class BotLogicTest {
	private static String access_id = "enter your own";
	private static String secret_key = "enter your own";
	
	//Change this to whatever market u wanna play in
	private static String market = "BTCUSDT";
	//Change this to whatever market min. qty (api min = 0.01)
	private static double minQty = 1;
	//Tokens to retain in balance
	private static double minTokenBal = 500;
	//Excess Ratio for calculateFormula
	private static double excessRatio = 30;
	//Reduce amount to buy for qty if not enough USDT to buy (BTCUSDT)
	private static double amountReduction = 0.0005;
	
	public static CoinExAPI cep = new CoinExAPI(access_id, secret_key);


	private static void buySameBTCUSD() throws UnsupportedEncodingException, NoSuchAlgorithmException, JSONException {
		// Step 0.1 Done (Account Balance (Attributes: available/frozen))
		JSONObject bal = new JSONObject(cep.getAccInfo());
		double BTCBal = Double.parseDouble(bal.getJSONObject("data").getJSONObject("BTC").getString("available"));
		double USDTBal = Double.parseDouble(bal.getJSONObject("data").getJSONObject("USDT").getString("available"));
		
		System.out.printf("\nBTC Bal = %.8f", BTCBal);
		System.out.printf("\nUSDT Bal = %.8f\n", USDTBal);

		// Get Real time Lowest Ask (lowest sell price)
		String BTCUSDTmarket = "BTCUSDT";
		double[] bidAsk = cep.getBidAsk(BTCUSDTmarket);
		double ask = bidAsk[1];
		System.out.printf(BTCUSDTmarket + " ASK = %.8f", ask);

		// If BTC in USDT is more than USDT, sell some BTC to get equivalent USDT
		double BTCinUSDT = BTCBal * ask;
		if (BTCinUSDT > USDTBal) {
			double USDTToGet = (BTCinUSDT - USDTBal) / 2;
			double BTCToSell = USDTToGet / ask;

			System.out.printf("\nBTC TO SELL = " + BTCToSell + ", IN USD VALUE = " + BTCToSell * ask);

			// Set Limit sell Order
			String qty = Double.toString(BTCToSell);
			// JSONObject obj1 = new JSONObject(cep.setLimitOrder(BTCUSDTmarket, qty, String.valueOf(ask), "sell"));
			// int sellOrderID = obj1.getJSONObject("data").getInt("id");
			// System.out.println(sellOrderID);

			// If USDT is more than BTC in USDT, sell some USDT (buy BTC) to get equivalent
			// USDT
		} else if (BTCinUSDT < USDTBal) {
			double USDTToSell = (USDTBal - BTCinUSDT) / 2;
			double BTCToBuy = USDTToSell / ask;
			System.out.printf("\nUSDT To Sell = " + USDTToSell + ", IN BTC VALUE = %.8f\n\n", BTCToBuy);

			// Set Limit buy Order
			String qty = Double.toString(BTCToBuy);
			// JSONObject obj1 = new JSONObject(cep.setLimitOrder(BTCUSDTmarket, qty, String.valueOf(ask), "buy"));
			// int buyOrderID = obj1.getJSONObject("data").getInt("id");
			// System.out.println(buyOrderID);
		}
	}
	
	private static double[] getMiningInfo() throws UnsupportedEncodingException, NoSuchAlgorithmException, JSONException{
		double[] result = new double[3];
		
		JSONObject diff = new JSONObject(cep.getMineDiff());
		
		int updateTime = diff.getJSONObject("data").getInt("update_time");
		System.out.println("Mining difficulty update time: " + updateTime);
		
		double mineDiff = diff.getJSONObject("data").getDouble("difficulty");
		double mined = diff.getJSONObject("data").getDouble("prediction");
		double unmined = mineDiff - mined;
		
		result[0] = mineDiff;
		result[1] = mined;
		result[2] = unmined;
		
		return result;
	}
	
	private static void cancelPendingOrders() throws UnsupportedEncodingException, NoSuchAlgorithmException, JSONException {
		// Step 0.8
		JSONObject pendingOrders = new JSONObject(cep.getPendingOrders(market, "1", "20"));
		int count = pendingOrders.getJSONObject("data").getInt("count");
		System.out.println("Pending orders count: " + count);

		// cancel all pending orders
		for (int i = 0; i < count; i++) {
			// get pending order id
			int cancelOrderID = pendingOrders.getJSONObject("data").getJSONArray("data").getJSONObject(i).getInt("id");
			// get pending order market pair
			String cancelOrderMkt = pendingOrders.getJSONObject("data").getJSONArray("data").getJSONObject(i).getString("market");
			System.out.println("Order ID to cancel: " + cancelOrderID);
			System.out.println("Order MKT to cancel: " + cancelOrderMkt);

			// cancel pending order
			cep.cancelOrders(Integer.toString(cancelOrderID), cancelOrderMkt);
		}
	}
	
	private static void setOrders(double hourlyDiff) throws UnsupportedEncodingException, NoSuchAlgorithmException, JSONException {
		// Step 1
		// Get Real time Highest Bid (buy price) and Lowest Ask (lowest sell price)
		double[] bidAsk = cep.getBidAsk(market);
		double bid = bidAsk[0];
		double ask = bidAsk[1];
		
		//get btc/usdt balance in account
		JSONObject bal = new JSONObject(cep.getAccInfo());
		double BTCBal = Double.parseDouble(bal.getJSONObject("data").getJSONObject("BTC").getString("available"));
		double USDTBal = Double.parseDouble(bal.getJSONObject("data").getJSONObject("USDT").getString("available"));

		System.out.printf(market + "\nBID = %.8f, ASK = %.8f", bid, ask);

		int divisor = 10000; // tentative 10k - to ensure our bid is higher than current bid for filling the order using our ask //to change after testing
		double orderBid = bid + bid / divisor;
		System.out.printf("\nTESTBID = %.8f", orderBid);

		// Step 2
		// Set Limit Buy/Sell Order, qty = no.of btc to buy
		String qty = String.valueOf(Double.parseDouble(calculateQty(hourlyDiff,"CETUSDT")) / orderBid);

		//check if enough btc to sell
		if(Double.parseDouble(qty) > BTCBal) {
			qty = String.valueOf(BTCBal);
		}
		
		//check if enough USDT to buy btc
		while(Double.parseDouble(qty) * orderBid > USDTBal) {
			
			//reduce qty to buy (FOR BTCUSDT)
			qty = String.valueOf(Double.parseDouble(qty) - amountReduction);
		}
		
		if(Double.parseDouble(qty) * orderBid > USDTBal) {
			System.out.println("Not enough USDT to buy BTC");
		}
		
		// Check >= minQty
		if (Double.parseDouble(qty) >= minQty) {
			// // Place limit buy order
			// JSONObject obj1 = new JSONObject(cep.setLimitOrder(market, qty,
			// String.valueOf(orderBid), "buy"));
			// int buyOrderID = obj1.getJSONObject("data").getInt("id");
			// System.out.println("Limit order buy ID: " + buyOrderID);
			//
			// // Place limit sell order
			// JSONObject obj2 = new JSONObject(cep.setLimitOrder(market, qty,
			// String.valueOf(orderBid), "sell"));
			// int sellOrderID = obj1.getJSONObject("data").getInt("id");
			// System.out.println("Limit order sell ID: " + sellOrderID);
		}
	}

	private static void sellExcessTokens(String qty) throws UnsupportedEncodingException, NoSuchAlgorithmException, JSONException {
		// Get Real time Highest Bid (buy price) and Lowest Ask (lowest sell price)
		double[] bidAsk = cep.getBidAsk(market);
		double bid = bidAsk[0];
		double ask = bidAsk[1];
		System.out.printf(market + "\nBID = %.8f, ASK = %.8f", bid, ask);

		double orderBid = (bid + ask) / 2;
		System.out.printf("\nTESTBID = %.8f", orderBid);

//		// Place limit sell order
//		JSONObject obj2 = new JSONObject(cep.setLimitOrder(market, qty, String.valueOf(orderBid), "sell"));
//		int sellOrderID = obj1.getJSONObject("data").getInt("id");
//		System.out.println("Limit order sell ID: " + sellOrderID);
		
	}
	
	private static void mine(double hourlyDiff) throws UnsupportedEncodingException, NoSuchAlgorithmException, JSONException {
		
		//place buy/sell orders
		setOrders(hourlyDiff);
		
		// Step 3
		// check pending order for 200sec (if there is pending order after setting buy/sell) this is to ensure orders are eaten within 200s
		int count = -1;
		boolean pending_order = true;

		long start_time = System.currentTimeMillis();
		// wait time 200sec
		long wait_time = 200000;
		long end_time = start_time + wait_time;

		while (System.currentTimeMillis() < end_time) {
			JSONObject pendingOrders = new JSONObject(cep.getPendingOrders(market, "1", "20"));
			count = pendingOrders.getJSONObject("data").getInt("count");
			// still got pending orders
			if (count != 0) {
				try {
					// pause for 5sec
					Thread.sleep(5000);
				} catch (InterruptedException ex) {
					Thread.currentThread().interrupt();
				}
			}
			// no pending orders
			else {
				pending_order = false;
				break;
			}
		}

		// Step 6.5
		// if after 200s, there is any pending order, cancel the orders
		if (pending_order) {
			cancelPendingOrders();
		}
	}
	
	//calculate amount in USDT to buy/sell per order
	private static String calculateQty(double hourlyDiff, String tokenPairing) throws UnsupportedEncodingException, NoSuchAlgorithmException, JSONException {
		
		double[] tokenPrice = cep.getBidAsk(tokenPairing.toUpperCase());
		
		double legendaryFormula = (hourlyDiff * tokenPrice[0] * excessRatio) /2;
		
		return String.format("%.3f", legendaryFormula);

	}
	
	public static void main(String[] args)throws UnsupportedEncodingException, NoSuchAlgorithmException, JSONException, InterruptedException {		
		//mine every 30s
		while (true) {
			
			// Step 0.1 + 0.2 Done
			buySameBTCUSD();
			
			// Step 0.4, 0.6, 0.7 DONE
			double[] miningInfo = getMiningInfo();

			double mineDiff = miningInfo[0];
			System.out.println("Mining difficulty: " + mineDiff + " CET/hour");
			double mined = miningInfo[1];
			System.out.println("Mined amount: " + mined + " CET");
			double unmined = miningInfo[2];
			System.out.println("Unmined amount: " + unmined + " CET");

			// Step 0.8
			// If unmined = 0 or negative, cancel all pending orders, stop mining for that
			
			// As this check is in while loop, need to do counter to ensure time is only retrieved once per hour)
			boolean getCurrentTime = false;
			Calendar now = null;
			
			if (unmined > 0) {
				// Mining quota not reached
				mine(mineDiff);
				getCurrentTime = false;
			} else {
				// Mining quota reached
				// Cancel pending orders
				cancelPendingOrders();

				// Step 0.9 (same as step 0.2) Done
				buySameBTCUSD();
				
				if (!getCurrentTime) {
					now = Calendar.getInstance();
					getCurrentTime = true;
				}

				//Mining reset every hour
				//Current time = now + 1
				if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) == (now.get(Calendar.HOUR_OF_DAY) + 1)) {
					//System.out.println("Continue mining");
					JSONObject bal = new JSONObject(cep.getAccInfo());
					double tokenBal = Double.parseDouble(bal.getJSONObject("data").getJSONObject("CET").getString("available"));
					
					if (tokenBal > minTokenBal) {
						sellExcessTokens(String.valueOf(tokenBal - minTokenBal));
					}
				}
			}
			
			//30s loop
			Thread.sleep(30000);
		}
	
	}

	// WebSocket Connection and Handle incoming msg (working but not implemented)
	private static void WebSocketConnect() {

		try {
			// Open websocket
			final WebSocketAPI clientEndPoint = new WebSocketAPI(new URI("wss://socket.coinex.com/"));

			// Add listener
			clientEndPoint.addMessageHandler(new WebSocketAPI.MessageHandler() {
				public void handleMessage(String message) {
					System.out.println("incoming msg: " + message);
				}
			});

			// Send json msg
			clientEndPoint.sendMessage(clientEndPoint.getMarketStatusJson("subscribe", "BTCBCH", 86400));

			// Wait 50 seconds for messages from websocket (testing purposes, allow cont
			// listening and connection)
			Thread.sleep(50000);

		} catch (InterruptedException ex) {
			System.err.println("InterruptedException exception: " + ex.getMessage());
		} catch (URISyntaxException ex) {
			System.err.println("URISyntaxException exception: " + ex.getMessage());
		}
	}

}
