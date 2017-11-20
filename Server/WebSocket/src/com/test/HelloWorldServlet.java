package com.test;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.OnError;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.util.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
@ServerEndpoint("/websocket")
public class HelloWorldServlet {
	public static StringBuilder allPath = new StringBuilder();
	public static int navID = 1;
	public static String webSessionID = new String();
	public static Session webSession;
	public static Session driverSession;
	public static LinkedList<Session> linkedList= new LinkedList<>();
	
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
    	else if(message.startsWith("[web]gma")){ 
    		allPath.append(message.split("gma")[1]);
    		System.out.println("[server]AllPath(SessionID:"+session.getId()+"):" + message.split("gma")[1]);
    		System.out.println("[server]ReceiveFromWeb(SessionID:"+session.getId()+"):" + allPath.toString());   
    		System.out.println("~~"+linkedList.size());   
    		/*
    		 * for (int i=0;i<linkedList.size();i++) {
    			 if(linkedList.get(i)==webSession)  continue;
    			 else  {
    		    	 System.out.println("[server]SendToClient(SessionID:"+linkedList.get(i).getId()+"):" + allPath.toString());
    		    	 linkedList.get(i).getBasicRemote().sendText("[allpath]"+allPath.toString());
    		     }
    		 }
    		 */
    	}
    	else if(message.split("]")[1].startsWith("navigation")){ 
    		System.out.println("[server]ReceiveFromClient(SessionID:"+session.getId()+"):" + message.split("]")[2]);  
    		webSession.getBasicRemote().sendText(message); 
    	}
    	else if(message.split("]")[1].startsWith("delete navigation")){ //來自client的刪除導航路徑
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
    		session.getBasicRemote().sendText("[delete navigation]"+"OK"); 
    		System.out.println("[server]ReceiveFromClient(SessionID:"+session.getId()+"):" + message);
    		System.out.println("[server]ReceiveFromClient(SessionID:"+session.getId()+"):" + allPath.toString());
    	}
    	else if(message.split("]")[1].startsWith("request allpath")){
            //session.getBasicRemote().sendText("This is the first server message"); 
    		session.getBasicRemote().sendText("[allpath]"+allPath.toString()); 
    		System.out.println("[server]ReceiveFromClient(SessionID:"+session.getId()+"):" + message);
    		System.out.println("[server]SendToClient(SessionID:"+session.getId()+"):" + allPath.toString());
    	}
    	else if(message.split("]")[1].startsWith("request hospital")){ 
    		String hospital = "" ;
    		System.out.println("HOSPITAL");
    		FileReader fr = new FileReader("E:\\Java\\WebSocket\\WriteFileTest.txt");
    		BufferedReader br = new BufferedReader(fr);
    		while (br.ready()) {
    			hospital+=br.readLine();
    			System.out.println(br.readLine());
    		}
    		fr.close();   		
    	    session.getBasicRemote().sendText("[hospital]"+hospital); 
    		System.out.println("[server]ReceiveFromClient(SessionID:"+session.getId()+"):" + message);
    		System.out.println("[server]SendToClient(SessionID:"+session.getId()+"):" + hospital);
    	}
    	else if(message.startsWith("[web]chgDriverPos:")){ //來自web返回的傳送client自己位置
    		driverSession.getBasicRemote().sendText(message); 
    		System.out.println("[server]ReceiveFromWeb(SessionID:"+session.getId()+"):" + message);
    		System.out.println("[server]SendToClient(SessionID:"+driverSession.getId()+"):" + message);
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
    		System.out.println(session);
    	}
    }

    @OnOpen
    public void onOpen(Session session) throws IOException {
    	Session A = session;
    	linkedList.add(A);	
        System.out.println("[server](SessionID:"+session.getId()+") connected"+Integer.toString(linkedList.size()));
    }

    @OnClose
    public void onClose(Session session) {
    	linkedList.remove(session);	
        System.out.println("[server]Connection closed"+Integer.toString(linkedList.size()));
    }
    @OnError
    public void OnError(Session session, Throwable t) {
        System.out.println("[server]error"+t);
    }
    
}
