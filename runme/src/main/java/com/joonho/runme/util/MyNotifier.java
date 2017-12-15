package com.joonho.runme.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;

import com.joonho.runme.Main2Activity;
import com.joonho.runme.R;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by jhpark on 17. 11. 30.
 */

public class MyNotifier {

    public static void go(Context ctx, String title, String cont) {

        Bitmap mLargeIconForNoti = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.arrow_left128);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(ctx)
                .setSmallIcon(R.drawable.common_google_signin_btn_icon_light)
                .setContentTitle(title)
                .setContentText(cont)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setLargeIcon(mLargeIconForNoti)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                ;

        NotificationManager mNotificationManager = (NotificationManager)
                ctx.getSystemService(NOTIFICATION_SERVICE);
        mNotificationManager.notify(0, mBuilder.build());
    }
}
