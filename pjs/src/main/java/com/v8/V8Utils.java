package com.v8;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;


import com.eclipsesource.v8.Releasable;
import com.eclipsesource.v8.V8;

public class V8Utils {
	public static String readFile(File file)   {
		StringBuilder everything = new StringBuilder();
		
		BufferedReader buffIn = null;
		try {
			buffIn = new BufferedReader(new FileReader(file));
			String line;
			while( (line = buffIn.readLine()) != null) {
			   everything.append(line+'\n');
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}finally{
			if (buffIn != null) {
				try {
					buffIn.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	    return everything.toString();
	}
	
	
	public static void executeJsFile(V8 runtime ,File file){
		runtime.executeScript(readFile(file));
	}
	
	public static void loadPJsFile(V8 runtime ,File file,String key){
		String pointer = file.lastModified()+":"+file.length();
		boolean isJsExits = runtime.executeBooleanScript("pjsList._isPjsExists('"+key+"','"+pointer+"')");
		
		if(!isJsExits){
			
			System.out.println("[JAVA]:" + "load pjs:" + key);
			String js = readFile(file);
			
			String str = "pjsList._addPjs(new Pjs('"+key+"','"+pointer+"',function(){"+ js+ "}));";
			runtime.executeVoidScript(str);
		}
		
	}	
	
	public static void releasObject(Object obj){
		if(obj instanceof Releasable){
			((Releasable) obj).release();
		}
	}
	
}
