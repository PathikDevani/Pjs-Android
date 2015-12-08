package com.pjs;

import android.app.Activity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;



import com.v8.V8WebScoket;
import com.v8.V8Worker;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoWSD;
import fi.iki.elonen.NanoHTTPD.Response.Status;

public class Pjs extends NanoWSD{
	
	private List<File> rootDirs = new ArrayList<File>();
	private String js = "pjs";
	private String css = "scss";
	private String dot_css = "." + css;
	

	private Activity activity;

	public Pjs(int port,Activity activity) {
		super(port);

		this.activity = activity;
		startQUEUE(activity);
		rootDirs.add(new File("./www").getAbsoluteFile());
	}
	
	@Override
	protected WebSocket openWebSocket(IHTTPSession handshake) {
		return new V8WebScoket(handshake);
	}
	
	
	@Override
	protected Response serveHttp(IHTTPSession session) {
		
		Map<String, String> header = session.getHeaders();
		Map<String, String> files = new HashMap<String, String>();
		
		
		
		try {
			session.parseBody(files);
		} catch (IOException | ResponseException e) {
			e.printStackTrace();
		}

		String uri = session.getUri();
		for (File homeDir : this.rootDirs) {
			if (!homeDir.isDirectory()) {
				return getInternalErrorResponse("given path is not a directory ("+ homeDir + ").");
			}
		}
		
		return respond(Collections.unmodifiableMap(header), session, uri);
	}
	
	private Response respond(Map<String, String> headers, IHTTPSession session,String uri) {
		
		
		uri = uri.trim().replace(File.separatorChar, '/');
		if (uri.indexOf('?') >= 0) {
			uri = uri.substring(0, uri.indexOf('?'));
		}

		if (uri.contains("../")) {
			return getForbiddenResponse("Won't serve ../ for security reasons.");
		}
		
		
		
		
		boolean canServeUri = false;
		boolean canCss = false;
		File homeDir = null;
		
		
		for (int i = 0; !canServeUri && i < this.rootDirs.size(); i++) {
			homeDir = this.rootDirs.get(i);
			canServeUri = canServeUri(uri, homeDir);
			canCss = canServeUri(uri.substring(0, (uri.length() < 4 ? 0 : uri.length() - 4)) + dot_css, homeDir);
			
		}

		if (!canServeUri  && !canCss) {
			return getNotFoundResponse();
		}
		
		
		File f;
		String mimeTypeForFile = getMimeTypeForFile(uri);
		

		if (mimeTypeForFile.equalsIgnoreCase(css) || mimeTypeForFile.equalsIgnoreCase(js)) {
			
			
			return getNotFoundResponse();
		} else if (canCss) {
			f = new File(homeDir, uri);
			if (!f.exists()) {
				try {
					f.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else {
			f = new File(homeDir, uri);
		}
		
		if (f.isDirectory() && !uri.endsWith("/")) {
			uri += "/";
			Response res = newFixedLengthResponse(Response.Status.REDIRECT,
					NanoHTTPD.MIME_HTML, "<html><body>Redirected: <a href=\""
							+ uri + "\">" + uri + "</a></body></html>");
			res.addHeader("Location", uri);
			return res;
		}
		
		if (f.isDirectory()) {
			String indexFile = findIndexFileInDirectory(f);
			if (indexFile == null) {
				return getForbiddenResponse("No directory listing.");
			} else {
				mimeTypeForFile = getMimeTypeForFile(indexFile);
				f = new File(homeDir, uri + indexFile);

			}
		}
		
		
		
		if (getExtantion(f.getName()).equalsIgnoreCase("css")) {
			return serveSassFile(f,session);
		}
		
		Response response = serveFile(uri, headers, f, mimeTypeForFile);
		return response != null ? response : newFixedLengthResponse(Status.OK,"text/plain", "ok");
	}

	
	private Response serveSassFile(File f,IHTTPSession session) {

		Response r = null;
		File fSass = new File(f.getAbsolutePath().substring(0,f.getAbsolutePath().length() - 3) +"scss");
		
		
		if(fSass.exists()){
			String etag = fSass.lastModified()+"";
			
			if(!etag.equals(session.getHeaders().get("if-none-match"))){
				try {
					//System.out.println("scss server");
					String cmd = "cmd.exe /c sass \"" + fSass.getCanonicalPath() + "\" \"" + f.getCanonicalPath()+"\"";
					System.out.println(cmd);
					exCommnad(cmd);
					
					
					System.out.println(etag + "    " + f.lastModified());
					
					r = newFixedLengthResponse(Status.OK, "text/css", new FileInputStream(f),f.length());
					r.addHeader("Etag", etag);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}else {
				r = newFixedLengthResponse(Status.NOT_MODIFIED, "text/html", "");
			}

		}else {
			
			if(f.exists()){				
				try {
					String etag = f.lastModified()+"";
					if(!etag.equals(session.getHeaders().get("if-none-match"))){	
						r = newFixedLengthResponse(Status.OK, "text/css", new FileInputStream(f), f.length());
						r.addHeader("Etag", etag);
					}else {
						r = newFixedLengthResponse(Status.NOT_MODIFIED, "text/css", "");
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}
		}
		return r;
		
	}
	
	public void exCommnad(String cmd){
		try {
			Process process = Runtime.getRuntime().exec(cmd);
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			while ((reader.readLine()) != null) {
			}
			process.waitFor();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private boolean canServeUri(String uri, File homeDir) {
		boolean canServeUri;
		File f = new File(homeDir, uri);
		canServeUri = f.exists();
		return canServeUri;
	}
	
	
	Response serveFile(String uri, Map<String, String> header, File file,
			String mime) {
		Response res;

		try {
			String etag = Integer.toHexString((file.getAbsolutePath()
					+ file.lastModified() + "" + file.length()).hashCode());

			long startFrom = 0;
			long endAt = -1;
			String range = header.get("range");
			if (range != null) {
				if (range.startsWith("bytes=")) {
					range = range.substring("bytes=".length());
					int minus = range.indexOf('-');
					try {
						if (minus > 0) {
							startFrom = Long.parseLong(range
									.substring(0, minus));
							endAt = Long.parseLong(range.substring(minus + 1));
						}
					} catch (NumberFormatException ignored) {

					}
				}
			}

			String ifRange = header.get("if-range");
			boolean headerIfRangeMissingOrMatching = (ifRange == null || etag.equals(ifRange));

			String ifNoneMatch = header.get("if-none-match");
			boolean headerIfNoneMatchPresentAndMatching = ifNoneMatch != null
					&& (ifNoneMatch.equals("*") || ifNoneMatch.equals(etag));
			long fileLen = file.length();

			if (headerIfRangeMissingOrMatching && range != null
					&& startFrom >= 0 && startFrom < fileLen) {
				if (headerIfNoneMatchPresentAndMatching) {
					res = newFixedLengthResponse(Response.Status.NOT_MODIFIED,
							mime, "");
					res.addHeader("ETag", etag);
				} else {
					if (endAt < 0) {
						endAt = fileLen - 1;
					}
					long newLen = endAt - startFrom + 1;
					if (newLen < 0) {
						newLen = 0;
					}

					FileInputStream fis = new FileInputStream(file);
					fis.skip(startFrom);

					res = newFixedLengthResponse(
							Response.Status.PARTIAL_CONTENT, mime, fis, newLen);
					res.addHeader("Accept-Ranges", "bytes");
					res.addHeader("Content-Length", "" + newLen);
					res.addHeader("Content-Range", "bytes " + startFrom + "-"
							+ endAt + "/" + fileLen);
					res.addHeader("ETag", etag);
				}
			} else {

				res = newFixedFileResponse(file, mime);
				res.addHeader("Content-Length", "" + fileLen);
				res.addHeader("ETag", etag);
				Date expdate = new Date();
				expdate.setTime(expdate.getTime() + (3600 * 1000));
				DateFormat df = new SimpleDateFormat("EEE, dd-MMM-yyyy HH:mm:ss zzz");
				df.setTimeZone(TimeZone.getTimeZone("GMT"));
				res.addHeader("Expires", df.format(expdate));
			}
		} catch (IOException ioe) {
			res = getForbiddenResponse("Reading file failed.");
		}

		return res;
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
	
	@SuppressWarnings("serial")
	public static final List<String> INDEX_FILE_NAMES = new ArrayList<String>() {
		{
			add("index.html");
		}
	};
	
	private String getExtantion(String uri) {
		int dot = uri.lastIndexOf('.');
		return uri.substring(dot + 1).toLowerCase() + "";
	}
	
	private String findIndexFileInDirectory(File directory) {
		for (String fileName : INDEX_FILE_NAMES) {
			File indexFile = new File(directory, fileName);
			if (indexFile.isFile()) {
				return fileName;
			}
		}
		return null;
	}
	
	
	public static final BlockingQueue<Runnable> QUEUE = new LinkedBlockingDeque<Runnable>();
	private static void startQUEUE(final Activity activity) {
		final Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				final V8Worker worker = new V8Worker(activity);
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
