package com.pjs;

import android.app.Activity;
import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;



import com.v8.V8WebScoket;
import com.v8.V8Worker;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoWSD;
import fi.iki.elonen.NanoHTTPD.Response.Status;

public class Pjs extends NanoWSD{

	public static AndroidUtils androidUtils;
	public Pjs(int port,Context context) {
		super(port);
		androidUtils = new AndroidUtils(context);
		startQUEUE(context);
	}
	
	@Override
	protected WebSocket openWebSocket(IHTTPSession handshake) {
		return new V8WebScoket(handshake);
	}

	@Override
	protected Response serveHttp(IHTTPSession session) {
		return  newFixedLengthResponse(Status.OK,"text/plain", "Nothing to show: security purpose");
	}


	private Response newFixedFileResponse(File file, String mime)
			throws FileNotFoundException {
		Response res;
		res = newFixedLengthResponse(Response.Status.OK, mime,
				new FileInputStream(file), (int) file.length());
		res.addHeader("Accept-Ranges", "bytes");
		return res;
	}

	protected Response getInternalErrorResponse(String s) {
		return newFixedLengthResponse(Response.Status.INTERNAL_ERROR,
				NanoHTTPD.MIME_PLAINTEXT, "INTERNAL ERROR: " + s);
	}

	protected Response getForbiddenResponse(String s) {
		return newFixedLengthResponse(Response.Status.FORBIDDEN,
				NanoHTTPD.MIME_PLAINTEXT, "FORBIDDEN: " + s);
	}

	protected Response getNotFoundResponse() {
		return newFixedLengthResponse(Response.Status.NOT_FOUND,
				NanoHTTPD.MIME_PLAINTEXT, "Error 404, file not found.");
	}


	
	public static final BlockingQueue<Runnable> QUEUE = new LinkedBlockingDeque<Runnable>();
	private static void startQUEUE(final Context context) {
		final Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				final V8Worker worker = new V8Worker(context);
				while (true) {
					try {
						Runnable jsRunnable = QUEUE.take();
						worker.onWork(jsRunnable);

					} catch (InterruptedException ex) {
						System.out.println(ex);
						return;
					}
				}	
			}
		});
		t.setName("worker queue");
		t.start();
	}


	
}
