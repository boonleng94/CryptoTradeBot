import java.net.URI;
import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


@ClientEndpoint
public class WebSocketAPI {

    Session userSession = null;
    private MessageHandler messageHandler;

    public WebSocketAPI(URI endpointURI) {
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, endpointURI);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Callback hook for Connection open events.
     *
     * @param userSession the userSession which is opened.
     */
    @OnOpen
    public void onOpen(Session userSession) {
        System.out.println("opening websocket");
        this.userSession = userSession;
    }

    /**
     * Callback hook for Connection close events.
     *
     * @param userSession the userSession which is getting closed.
     * @param reason the reason for connection close
     */
    @OnClose
    public void onClose(Session userSession, CloseReason reason) {
        System.out.println("closing websocket");
        this.userSession = null;
    }

    /**
     * Callback hook for Message Events. This method will be invoked when a client send a message.
     *
     * @param message The text message
     */
    @OnMessage
    public void onMessage(String message) {
        if (this.messageHandler != null) {
            this.messageHandler.handleMessage(message);

        }
    }

    /**
     * register message handler
     *
     * @param msgHandler
     */
    public void addMessageHandler(MessageHandler msgHandler) {
        this.messageHandler = msgHandler;
    }

    /**
     * Send a message.
     *
     * @param message
     */
    public void sendMessage(String message) {
        System.out.println("sending msg");
        this.userSession.getAsyncRemote().sendText(message);
    }

    /**
     * Message handler.
     *
     */
    public static interface MessageHandler {

        public void handleMessage(String message);
    }
    
    
    //Ping server 
    public String pingServerJson() {

  		// json msg to send
  		JSONObject jsonObj = new JSONObject();
  		JSONArray jsonArray = new JSONArray();
  		
  		try {
  			jsonObj.put("method", "server.ping");
  			jsonObj.put("params", jsonArray);
  			jsonObj.put("id", 11);

  		} catch (JSONException e) {
  			// TODO Auto-generated catch block
  			e.printStackTrace();
  		}

  		return jsonObj.toString();

  	}

  	//Get System time
  	public String getSystemTimeJson() {

  		// json msg to send
  		JSONObject jsonObj = new JSONObject();
  		JSONArray jsonArray = new JSONArray();
  		
  		try {
  			jsonObj.put("method", "server.time");
  			jsonObj.put("params", jsonArray);
  			jsonObj.put("id", 12);

  		} catch (JSONException e) {
  			// TODO Auto-generated catch block
  			e.printStackTrace();
  		}

  		return jsonObj.toString();

  	}

  	//Get market status for certain market pairing
  	//market "e.g. BTCBCH", period in sec (86400 = 24 hours), type = acquire/subscribe
  	public String getMarketStatusJson(String type,String market, int period) {

  		// json msg to send
  		JSONObject jsonObj = new JSONObject();
  		JSONArray jsonArray = new JSONArray();
  		jsonArray.put(market);
  		jsonArray.put(period);

  		String action = null;
  		
  		if(type.equals("acquire")) {
  			action="state.query";
  		}
  		else {
  			action="state.subscribe";
  		}
  		
  		try {
  			jsonObj.put("method", action);
  			jsonObj.put("params", jsonArray);
  			jsonObj.put("id", 15);

  		} catch (JSONException e) {
  			// TODO Auto-generated catch block
  			e.printStackTrace();
  		}

  		return jsonObj.toString();

  	}
}