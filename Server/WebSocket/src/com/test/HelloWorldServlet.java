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
	Map<Set<String>, Session> allSession = new HashMap<Set<String>, Session>();
    @OnMessage
    public void onMessage(String message, Session session) throws IOException, InterruptedException {
    	if(message.startsWith("[web]onOpen")){ //來自web的連線建立
    		webSessionID = session.getId();
    		System.out.println("[server]ReceiveFromWeb(SessionID:"+session.getId()+"):" + message);

    		Set<String> sessionSet = new HashSet<String>();
    		sessionSet.add(session.getId());
    		allSession.put(sessionSet, session);
    	}
    	else if(message.startsWith("[web]gma")){ //來自web返回的導航路徑
    		allPath.append(message.split("gma")[1]);
    		System.out.println("[server]ReceiveFromWeb(SessionID:"+session.getId()+"):" + allPath.toString());
    	}
    	else if(message.split("]")[1].startsWith("navigation")){ //來自client的導航請求
    		allSession.get(webSessionID).getBasicRemote().sendText(message);
    		System.out.println("[server]ReceiveFromClient(SessionID:"+session.getId()+"):" + allPath.toString());
    		System.out.println("[server]SendToWeb(SessionID:"+session.getId()+"):" + allPath.toString());
    	}
    	else if(message.split("]")[1].startsWith("request allpath")){ //來自client的請求導航路徑
            //session.getBasicRemote().sendText("This is the first server message"); 
    		session.getBasicRemote().sendText(allPath.toString()); 
    		System.out.println("[server]ReceiveFromClient(SessionID:"+session.getId()+"):" + message);
    		System.out.println("[server]SendToClient(SessionID:"+session.getId()+"):" + allPath.toString());
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
