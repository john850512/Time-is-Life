package com.test;

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
	public static int path_num = 1;
    @OnMessage
    public void onMessage(String message, Session session) throws IOException, InterruptedException {
    	if(message.startsWith("gma:")){
    		allPath.append("$"+path_num+"$"+message.split("gma:")[1]);
    		path_num++;
    	}
    	else{
            // Send the first message to the client
            //session.getBasicRemote().sendText("This is the first server message"); 
    		session.getBasicRemote().sendText(allPath.toString()); 
    	}
        // Print the client message for testing purposes
    	System.out.println("Received: " + message);
    	System.out.println("allpath: " + allPath.toString());
        
    }

    @OnOpen
    public void onOpen() {
        System.out.println("Client connected");
    }

    @OnClose
    public void onClose() {
        System.out.println("Connection closed");
    }
    @OnError
    public void OnError(Session session, Throwable t) {
        System.out.println("[error]"+t);
    }
}
