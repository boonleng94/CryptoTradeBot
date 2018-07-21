import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;

import org.json.JSONException;
import org.json.JSONObject;

public class BotLogicTest {
	private static String access_id = "enter your access id here";
	private static String secret_key = "enter your secret key here";
	
	//Change this to whatever market u wanna play in
	private static String market = "CETETH";
	//Change this to whatever market min. qty (api min = 0.01)
	private static double minQty = 1;
	//Tokens to retain in balance
	private static double minTokenBal = 500;

	public static CoinExAPI cep = new CoinExAPI(access_id, secret_key);

	// Bot logic:
	// 0.1. Get Balance BTC and USDT
	// 0.2. Buy.sell BTC/USD to get same amount worth in BTC and USDT
	// 0.3  Open websocket (To allow more calls)
	// 0.4. Get Mining Difficulty
	// 0.5 Set ratio to mine to prevent overmining (95% default)
	// 0.6 Check tokens mined
	// 0.7 Check tokens not yet mined
	// 0.8 Cancel all pending orders
	// 0.9 Buy/sell BTC/USD to get same amount worth in BTC and USDT
	// 1. Get bid
	// 2. Put .xx buy order and same sell order
	// 3.Check Pending (Buy or sell)
	// 4 Update tokens mined and not mined if order executed
	// 6.5 If order still pending for a certain period of time, cancel order and go
	// back to step 2 and place buy or sell order (whichever that was cancelled at +
	// #% for buy order or - #% for sell order)
	// 7. Repeat from 0.6

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
	
	private static void setOrders() throws UnsupportedEncodingException, NoSuchAlgorithmException, JSONException {
		// Step 1
		// Get Real time Highest Bid (buy price) and Lowest Ask (lowest sell price)
		double[] bidAsk = cep.getBidAsk(market);
		double bid = bidAsk[0];
		double ask = bidAsk[1];
		System.out.printf(market + "\nBID = %.8f, ASK = %.8f", bid, ask);

		int divisor = 10000; // tentative 10k - to ensure our bid is higher than current bid for filling the
								// order using our ask
		double orderBid = bid + bid / divisor;
		System.out.printf("\nTESTBID = %.8f", orderBid);

		// Step 2
		// Set Limit Buy/Sell Order, qty = 0.01BTC worth
		String qty = String.valueOf(0.01 / orderBid);

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
	
	private static void mine() throws UnsupportedEncodingException, NoSuchAlgorithmException, JSONException {
		setOrders();
		
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

		// go back step 2 and set buy/sell order
		setOrders();
	}
	
	public static void main(String[] args)throws UnsupportedEncodingException, NoSuchAlgorithmException, JSONException, InterruptedException {		
		// Step 0.1 + 0.2 Done
		buySameBTCUSD();
		
		//mine every 30s
		while (true) {
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
				mine();
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
