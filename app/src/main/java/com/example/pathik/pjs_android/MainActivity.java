package com.example.pathik.pjs_android;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;


import com.contact.Contact;
import com.contact.ContactLoader;
import com.contact.RawContact;
import com.pjs.Pjs;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends ActionBarActivity {


    private ServiceConnection conn;
    public MyService myService;
    public Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        intent = new Intent(this,MyService.class);
        startService(intent);


        Button btnStart = (Button) findViewById(R.id.btn_start);
        Button btnStop = (Button) findViewById(R.id.btn_stop);


        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(myService != null){
                    myService.runServer(getBaseContext());
                }
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(myService != null){
                    myService.stopServer();
                }
            }
        });


        ContactLoader loader = new ContactLoader(getBaseContext(),32);
        Contact contact = loader.loadInBackground();
        ArrayList<RawContact> s = contact.getRawContacts();

        for(int i = 0; i <s.size();i++){
            RawContact rawContact = s.get(i);

            Log.i("My",i+":"+s.toString());
        }


    }

    @Override
    protected void onStart() {
        super.onStart();

        conn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                Log.i("My","bind-service-connected");
                MyService.MyBinder binder = (MyService.MyBinder) iBinder;
                myService = binder.getService();
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                Log.i("My","bind-service-disconnected");
            }
        };

        startService(intent);
        bindService(intent, conn, BIND_AUTO_CREATE);

    }


    @Override
    protected void onStop() {
        super.onStop();
        unbindService(conn);
    }


}
