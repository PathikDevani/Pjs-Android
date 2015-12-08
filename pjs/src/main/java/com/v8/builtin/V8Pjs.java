package com.v8.builtin;

import java.io.File;

import com.eclipsesource.v8.V8;
import com.eclipsesource.v8.V8Object;
import com.v8.V8Utils;

public class V8Pjs extends V8Object{
	
	private V8 runtime;
	
	public V8Pjs(V8 runtime) {
		super(runtime);
		this.runtime = runtime;
		
		registerJavaMethod(this, "loadPjsFile", "load", new Class<?>[]{String.class});
	}
	
	public void loadPjsFile(String path){
		V8Utils.loadPJsFile(runtime, new File("./www/"+path),path);
	}
}
