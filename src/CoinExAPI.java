import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

public class CoinExAPI {
	private String access_id;
	private String secret_key;
	private static String tonce = Long.toString(System.currentTimeMillis());

	public CoinExAPI(String access_id, String secret_key) {
		super();
		this.access_id = access_id;
		this.secret_key = secret_key;
	}

	public static HttpURLConnection setHttpHeaders(HttpURLConnection conn, String signature) {
		conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
		conn.setRequestProperty("Accept", "application/json");
		conn.setRequestProperty("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.90 Safari/537.36");
		conn.setRequestProperty("authorization", signature);
		return conn;
	}

	private String getSignature(Map<String, String> params)
			throws UnsupportedEncodingException, NoSuchAlgorithmException {
		String paramsToSign = "";
		int index = 0;

		for (Entry<String, String> entry : params.entrySet()) {
			if (index == 0) {
				paramsToSign += entry.getKey() + "=" + entry.getValue();
			} else {
				paramsToSign += "&" + entry.getKey() + "=" + entry.getValue();
			}
			index++;
		}

		paramsToSign += "&secret_key=" + secret_key;

		System.out.println("Params to sign: " + paramsToSign);

		byte[] bytesOfMessage = paramsToSign.getBytes("UTF-8");

		MessageDigest md = MessageDigest.getInstance("MD5");
		byte[] thedigest = md.digest(bytesOfMessage);

		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < thedigest.length; i++) {
			sb.append(Integer.toString((thedigest[i] & 0xff) + 0x100, 16).substring(1));
		}

		String signature = sb.toString().toUpperCase();
		System.out.println("Signature (in hex format): " + signature);
		return signature;
	}

	public String getAccInfo() throws UnsupportedEncodingException, NoSuchAlgorithmException {
		Map<String, String> params = new TreeMap<String, String>();
		params.put("access_id", access_id);
		params.put("tonce", tonce);

		String output = "";
		String urlParams = "";
		int index = 0;

		for (Entry<String, String> entry : params.entrySet()) {
			if (index == 0) {
				urlParams += entry.getKey() + "=" + entry.getValue();
			} else {
				urlParams += "&" + entry.getKey() + "=" + entry.getValue();
			}
			index++;
		}

		String signature = getSignature(params);

		try {
			URL url = new URL("https://api.coinex.com/v1/balance/?" + urlParams);
			System.out.println(url);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn = setHttpHeaders(conn, signature);

			if (conn.getResponseCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
			}

			BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

			System.out.println("Output from Server .... \n");
			String temp = br.readLine();
			while (temp != null) {
				output += temp + System.getProperty("line.separator");
				temp = br.readLine();
			}

			conn.disconnect();

		} catch (MalformedURLException e) {

			e.printStackTrace();

		} catch (IOException e) {

			e.printStackTrace();

		}

		return output;
	}

	public String getMineDiff() throws UnsupportedEncodingException, NoSuchAlgorithmException {
		Map<String, String> params = new TreeMap<String, String>();
		params.put("access_id", access_id);
		params.put("tonce", tonce);

		String output = "";
		String urlParams = "";
		int index = 0;

		for (Entry<String, String> entry : params.entrySet()) {
			if (index == 0) {
				urlParams += entry.getKey() + "=" + entry.getValue();
			} else {
				urlParams += "&" + entry.getKey() + "=" + entry.getValue();
			}
			index++;
		}

		String signature = getSignature(params);

		try {
			URL url = new URL("https://api.coinex.com/v1/order/mining/difficulty?" + urlParams);
			System.out.println(url);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn = setHttpHeaders(conn, signature);

			if (conn.getResponseCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
			}

			BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

			System.out.println("Output from Server .... \n");
			String temp = br.readLine();
			while (temp != null) {
				output += temp + System.getProperty("line.separator");
				temp = br.readLine();
			}

			conn.disconnect();

		} catch (MalformedURLException e) {

			e.printStackTrace();

		} catch (IOException e) {

			e.printStackTrace();

		}

		return output;
	}

	public String getPendingOrders(String market, String page, String limit) throws UnsupportedEncodingException, NoSuchAlgorithmException, JSONException {
		Map<String, String> params = new TreeMap<String, String>();
		params.put("access_id", access_id);
		params.put("market", market);
		params.put("page", page);
		params.put("limit", limit);
		params.put("tonce", tonce);

		String output = "";
		String urlParams = "";
		int index = 0;

		for (Entry<String, String> entry : params.entrySet()) {
			if (index == 0) {
				urlParams += entry.getKey() + "=" + entry.getValue();
			} else {
				urlParams += "&" + entry.getKey() + "=" + entry.getValue();
			}
			index++;
		}

		String signature = getSignature(params);

		try {
			URL url = new URL("https://api.coinex.com/v1/order/pending?" + urlParams);
			System.out.println(url);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn = setHttpHeaders(conn, signature);

			if (conn.getResponseCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
			}

			BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

			System.out.println("Output from Server .... \n");
			String temp = br.readLine();
			while (temp != null) {
				output += temp + System.getProperty("line.separator");
				temp = br.readLine();
			}

			conn.disconnect();

		} catch (MalformedURLException e) {

			e.printStackTrace();

		} catch (IOException e) {

			e.printStackTrace();

		}
				
		return output;
	}

	public double[] getBidAsk(String market) throws UnsupportedEncodingException, NoSuchAlgorithmException, JSONException {

		String output = "";

		try {
			URL url = new URL("https://api.coinex.com/v1/market/ticker?market=" + market);
			System.out.println(url);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");

			if (conn.getResponseCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
			}

			BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

			System.out.println("Output from Server .... \n");
			String temp = br.readLine();
			while (temp != null) {
				output += temp + System.getProperty("line.separator");
				temp = br.readLine();
			}

			conn.disconnect();

		} catch (MalformedURLException e) {

			e.printStackTrace();

		} catch (IOException e) {

			e.printStackTrace();

		}
		JSONObject bidAsk = new JSONObject(output);
		double[] result = { bidAsk.getJSONObject("data").getJSONObject("ticker").getDouble("buy"),bidAsk.getJSONObject("data").getJSONObject("ticker").getDouble("sell") };
		return result;
	}

	public String cancelOrders(String id, String market)
			throws UnsupportedEncodingException, NoSuchAlgorithmException {
		Map<String, String> params = new TreeMap<String, String>();
		params.put("access_id", access_id);
		params.put("id", id);
		params.put("market", market);
		params.put("tonce", tonce);

		String output = "";
		String urlParams = "";
		int index = 0;

		for (Entry<String, String> entry : params.entrySet()) {
			if (index == 0) {
				urlParams += entry.getKey() + "=" + entry.getValue();
			} else {
				urlParams += "&" + entry.getKey() + "=" + entry.getValue();
			}
			index++;
		}

		String signature = getSignature(params);

		try {
			URL url = new URL("https://api.coinex.com/v1/order/pending?" + urlParams);
			System.out.println(url);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("DELETE");
			conn = setHttpHeaders(conn, signature);

			if (conn.getResponseCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
			}

			BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

			System.out.println("Output from Server .... \n");
			String temp = br.readLine();
			while (temp != null) {
				output += temp + System.getProperty("line.separator");
				temp = br.readLine();
			}

			conn.disconnect();

		} catch (MalformedURLException e) {

			e.printStackTrace();

		} catch (IOException e) {

			e.printStackTrace();

		}

		return output;
	}

	public String setLimitOrder(String market, String amount, String price, String type)
			throws UnsupportedEncodingException, NoSuchAlgorithmException {
		Map<String, String> params = new TreeMap<String, String>();
		params.put("access_id", access_id);
		params.put("amount", amount);
		params.put("market", market);
		params.put("price", price);
		params.put("tonce", tonce);
		params.put("type", type);

		String output = "";
		String urlParams = "";
		int index = 0;

		for (Entry<String, String> entry : params.entrySet()) {
			if (index == 0) {
				urlParams += entry.getKey() + "=" + entry.getValue();
			} else {
				urlParams += "&" + entry.getKey() + "=" + entry.getValue();
			}
			index++;
		}

		String signature = getSignature(params);

		try {
			URL url = new URL("https://api.coinex.com/v1/order/limit?" + urlParams);
			System.out.println(url);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn = setHttpHeaders(conn, signature);
			conn.setDoOutput(true);

			OutputStream os = conn.getOutputStream();
			Gson gson = new Gson();
			String json = gson.toJson(params);
			os.write(json.getBytes());
			os.flush();
			// if (conn.getResponseCode() != 200) {
			// throw new RuntimeException("Failed : HTTP error code : "
			// + conn.getResponseCode());
			// }

			BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

			System.out.println("Output from Server .... \n");
			String temp = br.readLine();
			while (temp != null) {
				output += temp + System.getProperty("line.separator");
				temp = br.readLine();
			}

			conn.disconnect();

		} catch (MalformedURLException e) {

			e.printStackTrace();

		} catch (IOException e) {

			e.printStackTrace();

		}

		return output;
	}

}