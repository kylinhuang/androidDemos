package com.kylin.notification;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.view.View;

public class MainActivity extends Activity implements View.OnClickListener {

    private NotificationManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.send).setOnClickListener(this);

         manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.send:
                Notification notification = new NotificationCompat.Builder(this)
                        .setContentText("通知内容")
                        .setContentTitle("通知标题")
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                        .setWhen(System.currentTimeMillis())
                        .build();
                manager.notify(1,notification);
                break;

        }
    }
}
