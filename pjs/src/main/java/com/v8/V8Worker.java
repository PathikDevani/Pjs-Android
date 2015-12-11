package com.v8;

import android.app.Activity;
import android.content.Context;

import java.io.File;
import java.util.concurrent.BlockingQueue;

import com.eclipsesource.v8.V8;
import com.pjs.Pjs;
import com.v8.builtin.V8Console;
import com.v8.builtin.V8Mysql;
import com.v8.builtin.V8Pjs;

public class V8Worker {

	public static V8 runtime;
	public static final BlockingQueue<Runnable> QUEUE = Pjs.QUEUE;

	public V8Worker(Context context) {


		runtime = V8.createV8Runtime(context.getApplicationInfo().dataDir);
		runtime.add("console", new V8Console(runtime));
		runtime.add("pjs", new V8Pjs(runtime));
		
		
		runtime.registerJavaMethod(new V8Mysql(), "Mysql");
		runtime.executeScript("console.log('hello')");

		
		V8Utils.executeJsFile(runtime, "config.js");
	}
	
	public static void addWork(Runnable work){
		QUEUE.add(work);
	}
	
	
	public void onWork(Runnable runnable) {
		if(runnable.getClass() == V8Script.class){
			V8Script script = (V8Script) runnable;
			new Thread(script).run();
		}else if(runnable.getClass() == V8JavaExecution.class){
			V8JavaExecution execution = (V8JavaExecution) runnable;
			new Thread(execution).run();
		}
	}
}
