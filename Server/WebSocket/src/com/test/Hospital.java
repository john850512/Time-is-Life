package com.test;
import java.io.*;
import java.net.URL;
import java.net.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.BufferedWriter;  
import java.io.FileWriter;   

public class Hospital {
	
	public static String hospital = "";
	public static void webcrawler() throws Exception {
		String url = "http://khd.kcg.gov.tw/controls/modules/hcis/ExportXml.aspx?hdt_id=11"; 
		Document xmlDoc =  Jsoup.connect(url).ignoreContentType(true).get(); 
	
		Elements type = xmlDoc.select("���O�W��"); 
		Elements name = xmlDoc.select("���c�W��"); 
		Elements address = xmlDoc.select("�a�}");  
		Elements lat = xmlDoc.select("�g��Lng");  
		Elements lon = xmlDoc.select("�n��Lat");  
		BufferedWriter wStream=new BufferedWriter( new FileWriter(".\\WriteFileTest.txt"));
		for(int i=0;i<name.size();i++)
		{
				System.out.println(i); 
				if(!type.get(i).text().equals("��|"))  { continue;}
				System.out.println("type:"+type.get(i).text()); 
				System.out.println("Name:"+name.get(i).text()); 
				System.out.println("Address:"+address.get(i).text()); 
				System.out.println("Lat:"+lat.get(i).text()); 
				System.out.println("Lon:"+lon.get(i).text()); 
				System.out.println(""); 
				wStream.write("Name:"+name.get(i).text()); 
				wStream.newLine();
				wStream.write("Address:"+address.get(i).text()); 
				wStream.newLine();
				wStream.write("Lat:"+lat.get(i).text()); 
				wStream.newLine();
				wStream.write("Lon:"+lon.get(i).text()); 
				wStream.newLine();
				wStream.newLine();			
		}
	}

}


