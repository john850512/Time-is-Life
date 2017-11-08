package com.test;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.io.IOException;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.OnError;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
@ServerEndpoint("/websocket")
public class HelloWorldServlet {
	public static StringBuilder allPath = new StringBuilder();
	public static int navID = 1;
	public static String webSessionID = new String();
	public static Session webSession;
	public static Session driverSession;
	Map<Set<String>, Session> allSession = new HashMap<Set<String>, Session>();
    @OnMessage
    public void onMessage(String message, Session session) throws Exception {
    	//System.out.println(message);
    	if(message.startsWith("[web]WebOnOpen")){ //來自web的連線建立
    		System.out.println("[server]ReceiveFromWeb(SessionID:"+session.getId()+"):" + message);
    		webSession = session;	
    	}
    	else if(message.split("]")[1].startsWith("clientOnOpen")){ //來自client的連線建立
    		System.out.println("[server]ReceiveFromClient(SessionID:"+session.getId()+"):" + message);
    		driverSession = session;	
    	}
    	else if(message.startsWith("[web]gma")){ //來自web返回的導航路徑
    		allPath.append(message.split("gma")[1]);
    		System.out.println("[server]ReceiveFromWeb(SessionID:"+session.getId()+"):" + allPath.toString());
    	}
    	else if(message.startsWith("[web]chgDriverPos:")){ //來自web返回的傳送client自己位置
    		driverSession.getBasicRemote().sendText(message); 
    		System.out.println("[server]ReceiveFromWeb(SessionID:"+session.getId()+"):" + message);
    		System.out.println("[server]SendToClient(SessionID:"+driverSession.getId()+"):" + message);
    	}
    	else if(message.split("]")[1].startsWith("navigation")){ //來自client的導航請求
    		allSession.get(webSessionID).getBasicRemote().sendText(message);
    		System.out.println("[server]ReceiveFromClient(SessionID:"+session.getId()+"):" + allPath.toString());
    		System.out.println("[server]SendToWeb(SessionID:"+webSession.getId()+"):" + allPath.toString());
    	}
    	else if(message.split("]")[1].startsWith("request allpath")){ //來自client的請求導航路徑
    		//session.getBasicRemote().sendText("This is the first server message"); 
    		session.getBasicRemote().sendText("[allpath]"+allPath.toString()); 
    		System.out.println("[server]ReceiveFromClient(SessionID:"+session.getId()+"):" + message);
    		System.out.println("[server]SendToClient(SessionID:"+session.getId()+"):[allpath]" + allPath.toString());
    	}
    	else if(message.split("]")[1].startsWith("request hospital")){ //
    		Hospital.webcrawler();
    		session.getBasicRemote().sendText("[hospital]"+allPath.toString()); 
    		System.out.println("[server]ReceiveFromClient(SessionID:"+session.getId()+"):" + message);
    		System.out.println("[server]SendToClient(SessionID:"+session.getId()+"):" + allPath.toString());
    	}
    	else if(message.split("]")[1].startsWith("send itselfPosition:")){ //來自client的傳送client自己位置
    		webSession.getBasicRemote().sendText(message); 
    		System.out.println("[server]ReceiveFromClient(SessionID:"+session.getId()+"):" + message);
    		System.out.println("[server]SendToClient(SessionID:"+webSession.getId()+"):" + message);
    	}
    	else if(message.split("]")[1].startsWith("close Connection")){ //來自client的傳送關閉連線訊息
    		webSession.getBasicRemote().sendText(message); 
    		System.out.println("[server]ReceiveFromClient(SessionID:"+session.getId()+"):" + message);
    		System.out.println("[server]SendToClient(SessionID:"+webSession.getId()+"):" + message);
    	}
    	else{
    		
    	}
    	
    }

    @OnOpen
    public void onOpen(Session session) {
        System.out.println("[server](SessionID:"+session.getId()+") connected");
    }

    @OnClose
    public void onClose() {
        System.out.println("[server]Connection closed");
    }
    @OnError
    public void OnError(Session session, Throwable t) {
        System.out.println("[server]error"+t);
    }
    
}
