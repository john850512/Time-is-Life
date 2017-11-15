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
    	if(message.startsWith("[web]WebOnOpen")){ //�Ӧ�web���s�u�إ�
    		System.out.println("[server]ReceiveFromWeb(SessionID:"+session.getId()+"):" + message);
    		webSession = session;	
    	}
    	else if(message.split("]")[1].startsWith("clientOnOpen")){ //�Ӧ�client���s�u�إ�
    		System.out.println("[server]ReceiveFromClient(SessionID:"+session.getId()+"):" + message);
    		driverSession = session;	
    	}
    	else if(message.startsWith("[web]gma")){ //�Ӧ�web��^���ɯ���|
    		allPath.append(message.split("gma")[1]);
    		System.out.println("[server]ReceiveFromWeb(SessionID:"+session.getId()+"):" + allPath.toString());
    	}
    	else if(message.startsWith("[web]chgDriverPos:")){ //�Ӧ�web��^���ǰeclient�ۤv��m
    		driverSession.getBasicRemote().sendText(message); 
    		System.out.println("[server]ReceiveFromWeb(SessionID:"+session.getId()+"):" + message);
    		System.out.println("[server]SendToClient(SessionID:"+driverSession.getId()+"):" + message);
    	}
    	else if(message.split("]")[1].startsWith("navigation")){ //�Ӧ�client���ɯ�ШD
    		allSession.get(webSessionID).getBasicRemote().sendText(message);
    		System.out.println("[server]ReceiveFromClient(SessionID:"+session.getId()+"):" + allPath.toString());
    		System.out.println("[server]SendToWeb(SessionID:"+webSession.getId()+"):" + allPath.toString());
    	}
    	else if(message.split("]")[1].startsWith("delete navigation")){ //�Ӧ�client���R���ɯ���|
    		int deleteID = Integer.parseInt(message.split(":")[1].split("]")[0]);
    		//
    		String temp = allPath.toString();
    		allPath.setLength(0);
    		String[] pathTemp = temp.split("\\$");
    		System.out.println(pathTemp.length);
    		for(int i = 1 ; i < pathTemp.length ; i+=2){
    			System.out.println("**"+pathTemp[i]);
    			if(Integer.parseInt(pathTemp[i]) != deleteID)
    				allPath.append("$"+pathTemp[i]+"$"+pathTemp[i+1]);
    		}
    		//System.out.println("*"+allPath);

    		System.out.println("[server]ReceiveFromClient(SessionID:"+session.getId()+"):" + message);
    	}
    	else if(message.split("]")[1].startsWith("request allpath")){ //�Ӧ�client���ШD�ɯ���|
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
    	else if(message.split("]")[1].startsWith("send itselfPosition:")){ //�Ӧ�client���ǰeclient�ۤv��m
    		webSession.getBasicRemote().sendText(message); 
    		System.out.println("[server]ReceiveFromClient(SessionID:"+session.getId()+"):" + message);
    		System.out.println("[server]SendToClient(SessionID:"+webSession.getId()+"):" + message);
    	}
    	else if(message.split("]")[1].startsWith("close Connection")){ //�Ӧ�client���ǰe�����s�u�T��
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
