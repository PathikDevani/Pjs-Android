package com.v8;

import java.io.IOException;

import com.eclipsesource.v8.V8;
import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Object;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoWSD.WebSocket;
import fi.iki.elonen.NanoWSD.WebSocketFrame;
import fi.iki.elonen.NanoWSD.WebSocketFrame.CloseCode;

public class V8WebScoket extends WebSocket{

	private V8Object ws;
	
	public V8WebScoket(IHTTPSession handshakeRequest) {
		super(handshakeRequest);
		
		V8Script script = new V8Script("wsList.add()",new V8ScriptCallback() {
			@Override
			public void callback(Object obj) {
				ws = (V8Object) obj;
				ws.registerJavaMethod(V8WebScoket.this, "sendString", "send", new Class<?>[]{String.class});
			}
		});
		V8Worker.addWork(script);
	}

	@Override
	protected void onOpen() {
		V8JavaExecution execution = new V8JavaExecution(new V8JavaExecutionBlock() {
			@Override
			public Object block(V8 runtime) {
				
				ws.executeVoidFunction("onopen", null);
				return null;
			}
		});
		V8Worker.addWork(execution);
	}

	@Override
	protected void onMessage(final WebSocketFrame message) {
		V8JavaExecution execution = new V8JavaExecution(new V8JavaExecutionBlock() {
			@Override
			public Object block(V8 runtime) {
				V8Object jsongMsg = runtime.executeObjectScript("JSON.parse('"+message.getTextPayload()+"')");
				V8Array array = new V8Array(runtime);
				array.add("0", jsongMsg);
				ws.executeVoidFunction("onmessage", array);
				
				
				V8Utils.releasObject(array);
				V8Utils.releasObject(jsongMsg);
				return null;
			}
		});
		V8Worker.addWork(execution);
	}

	@Override
	protected void onPong(WebSocketFrame pong) {
		V8JavaExecution execution = new V8JavaExecution(new V8JavaExecutionBlock() {
			@Override
			public Object block(V8 runtime) {
				ws.executeVoidFunction("onpong", null);
				return null;
			}
		});
		V8Worker.addWork(execution);
	}

	@Override
	protected void onException(IOException exception) {
		V8JavaExecution execution = new V8JavaExecution(new V8JavaExecutionBlock() {
			@Override
			public Object block(V8 runtime) {
				ws.executeVoidFunction("onclose",null);
				return null;
			}
		});
		V8Worker.addWork(execution);
	}
	
	@Override
	protected void onClose(CloseCode code, String reason, boolean initiatedByRemote) {
		V8JavaExecution execution = new V8JavaExecution(new V8JavaExecutionBlock() {
			@Override
			public Object block(V8 runtime) {
				ws.executeVoidFunction("onclose",null);
				return null;
			}
		});
		V8Worker.addWork(execution);
	}
	
	
	
	public void sendString(String msg){
		try {
			send(msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
