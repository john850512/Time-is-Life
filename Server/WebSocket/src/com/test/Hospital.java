package com.test;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class Hospital {
	public static StringBuilder hospital = new StringBuilder();
	public static void main(String[] args)  throws Exception {
		// TODO Auto-generated method stub
		final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
	    executorService.scheduleAtFixedRate(new Runnable() {
	        @Override
	        public void run() {
	        	try {
					webcrawler();
				} catch (Exception e) {
					e.printStackTrace();
				}
	        }
	    }, 0, 1, TimeUnit.DAYS);
	}

	public static void webcrawler() throws Exception {
		String url = "http://khd.kcg.gov.tw/controls/modules/hcis/ExportXml.aspx?hdt_id=11"; 
		Document xmlDoc =  Jsoup.connect(url).ignoreContentType(true).get(); 
		System.out.println("�}�l����");
		Elements type = xmlDoc.select("���O�W��"); 
		Elements name = xmlDoc.select("���c�W��"); 
		Elements address = xmlDoc.select("�a�}");  
		Elements lat = xmlDoc.select("�g��Lng");  
		Elements lon = xmlDoc.select("�n��Lat");  
		BufferedWriter wStream=new BufferedWriter( new FileWriter(".\\WriteFileTest.txt"));
		for(int i=0;i<name.size();i++)
		{
				System.out.println(i); 
				System.out.println("type:"+type.get(i).text()); 
				System.out.println("Name:"+name.get(i).text()); 
				System.out.println("Address:"+address.get(i).text()); 
				System.out.println("Lat:"+lat.get(i).text()); 
				System.out.println("Lon:"+lon.get(i).text()); 
				System.out.println(""); 
				

				wStream.write(name.get(i).text()+",");	
				wStream.write(address.get(i).text()+",");	
				wStream.write(lat.get(i).text()+",");	
				wStream.write(lon.get(i).text());
				wStream.write("[");
				
				//Name,Address,Lat,Lon[Name,Address,Lat,Lon[
				/*
				hospital.append(type.get(i).text()+",");	
				hospital.append(type.get(i).text()+",");	
				hospital.append(type.get(i).text()+",");	
				hospital.append(type.get(i).text());
				hospital.append("[");
				*/
		}
		wStream.flush();
		wStream.close();
		System.out.println("���ε���");
	}



}


