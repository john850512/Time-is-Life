package com.test;

import java.io.IOException;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
@ServerEndpoint("/websocket")
public class HelloWorldServlet {
	public static StringBuilder allPath = new StringBuilder();
    @OnMessage
    public void onMessage(String message, Session session) throws IOException, InterruptedException {
    	if(message.startsWith("gma:")){
    		allPath.append("$1$"+message.split("gma:")[1]);
    	}
    	else{
            // Send the first message to the client
            //session.getBasicRemote().sendText("This is the first server message"); 
    		session.getBasicRemote().sendText(allPath.toString()); 
    	}
        // Print the client message for testing purposes
    	System.out.println("allpath: " + allPath.toString());
        System.out.println("Received: " + message);



    }

    @OnOpen
    public void onOpen() {
        System.out.println("Client connected");
    }

    @OnClose
    public void onClose() {
        System.out.println("Connection closed");
    }
}
