package com.ferrariapps.whatsapp.helper;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.ferrariapps.whatsapp.R;
import com.ferrariapps.whatsapp.activity.MainActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    Bitmap myBitmap;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage notificacao) {

        if (notificacao.getData() != null) {
            enviarNotificacao(notificacao.getData().get("key1"), notificacao.getData().get("key2"), notificacao.getData().get("key3"));
        }

    }

    private void enviarNotificacao(String titulo, String corpo, String foto) {
        String canal = getString(R.string.default_notification_channel_id);
        Uri uriSom = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        if (!foto.equals("")){
            try {
                URL url = new URL(foto);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                myBitmap = BitmapFactory.decodeStream(input);
            } catch (IOException e) {
                Log.e("Erro", "enviarNotificacao: "+e.getMessage());
                myBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.padrao);
            }
        }else{
            myBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.padrao);
        }
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.addLine(corpo);
        NotificationCompat.Builder builder=new NotificationCompat.Builder(this, canal)
                .setSound(uriSom)
                .setColor(getResources().getColor(R.color.teal_200))
                .setAutoCancel(true)
                .setContentTitle(titulo)
                .setContentText(corpo)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.icon_notification)
                .setStyle(inboxStyle)
                .setPriority(Notification.PRIORITY_MAX)
                .setLargeIcon(myBitmap);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(canal, "canal", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }
        notificationManager.notify(0, builder.build());


    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
    }
}