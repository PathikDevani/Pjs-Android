package com.example.pathik.pjs_android;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.text.format.Formatter;

import com.pjs.Pjs;

import java.io.IOException;

/**
 * Created by Pathik on 12/10/2015.
 */
public class MyService extends Service{

    @Override
    public void onCreate() {
        super.onCreate();


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private Pjs pjs;
    public void runServer(Context context){
        if(pjs == null){
            pjs = new Pjs(8080,context);
            try {
                pjs.start();
                runAsForeground();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public void stopServer(){
        if(pjs != null){
            stopAsForeground();
            pjs.stop();
            pjs = null;
        }

    }


    public class MyBinder extends Binder{
        public MyService getService(){
            return  MyService.this;
        }
    }

    private void runAsForeground(){
        WifiManager wifiMgr = (WifiManager) getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
        int ip = wifiInfo.getIpAddress();
        String ipAddress = Formatter.formatIpAddress(ip);


        Intent notificationIntent = new Intent(this,MyService.class);
        PendingIntent pendingIntent= PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);



        Notification notification=new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentText(ipAddress)
                .setContentTitle("Flysync")
                .setTicker("flysync ruing...")
                .setContentIntent(pendingIntent).build();

        startForeground(1, notification);
    }

    public void stopAsForeground(){
        stopForeground(true);
    }
}
