package com.v8;


public class V8Script implements Runnable{
	private String script;
	private V8ScriptCallback scriptCallback;
	
	
	public V8Script() {
		
	}
	
	public V8Script(String script,V8ScriptCallback scriptCallback){
		this.script = script;
		this.scriptCallback = scriptCallback;
	}
	

	@Override
	public void run() {
		try {
			Object obj = V8Worker.runtime.executeScript(script);
			if(scriptCallback != null){
				scriptCallback.callback(obj);
			}
			//V8Utils.releasObject(obj);
		} catch (Exception e) {
			System.out.println("V8script");
			e.printStackTrace();
		}
	}
	
}
