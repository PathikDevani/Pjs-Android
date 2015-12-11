package com.v8;

public class V8JavaExecution implements Runnable{
	
	private V8JavaExecutionBlock block;
	private V8JavaExecutionCallBack callback;
	
	public V8JavaExecution( V8JavaExecutionBlock block) {
		this.block = block;
	}
	public V8JavaExecution( V8JavaExecutionBlock block, V8JavaExecutionCallBack callback) {
		this.block = block;
		this.callback = callback;
	}
	
	@Override
	public void run() {
		
		try {
			Object obj = block.block(V8Worker.runtime);
			if (callback != null) {
				callback.callback(obj);
			}			
		} catch (Exception e) {
			System.out.println("V8JavaExecution");
			e.printStackTrace();
		}

	}



}
