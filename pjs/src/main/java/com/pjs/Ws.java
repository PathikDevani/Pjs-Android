package com.pjs;

import android.util.Log;

import java.io.IOException;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoWSD;

/**
 * Created by Pathik on 12/10/2015.
 */

public class Ws extends NanoWSD.WebSocket {
    public Ws(NanoHTTPD.IHTTPSession handshakeRequest) {
        super(handshakeRequest);
    }

    @Override
    protected void onOpen() {
        Log.i("My","open");
    }

    @Override
    protected void onClose(NanoWSD.WebSocketFrame.CloseCode code, String reason, boolean initiatedByRemote) {
        Log.i("My","close");
    }

    @Override
    protected void onMessage(NanoWSD.WebSocketFrame message) {
        Log.i("My","close");
    }

    @Override
    protected void onPong(NanoWSD.WebSocketFrame pong) {
        Log.i("My","pong");
    }

    @Override
    protected void onException(IOException exception) {
        Log.i("My","onException");
    }
}
