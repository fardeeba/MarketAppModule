@file:Suppress("DEPRECATION")

package com.skylarksit.module.gcm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.skylarksit.module.BaseActivity
import com.skylarksit.module.MainActivity
import com.skylarksit.module.R
import com.skylarksit.module.ui.utils.LocalStorage
import com.skylarksit.module.utils.Utilities

class MyFcmListenerService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        sendRegistrationToServer(token)
    }

    private fun sendRegistrationToServer(refreshedToken: String) {
        val token = LocalStorage.instance().getString("token")
        if (token.isEmpty() || token != refreshedToken) {
            Utilities.isTokenModified = true
            LocalStorage.instance().save(applicationContext, "token", refreshedToken)
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {


        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.from)

        // Check if message contains a data payload.
//        if (remoteMessage.getData().size() > 0) {
        Log.d(TAG, "Message data payload: " + remoteMessage.data)

//            if (/* Check if data needs to be processed by long running job */ true) {
        // For long-running tasks (10 seconds or more) use Firebase Job Dispatcher.
//                scheduleJob();
//            } else {
        // Handle message within 10 seconds
        handleNow(remoteMessage.data)
        //            }

//        }

        // Check if message contains a notification payload.
        if (remoteMessage.notification != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.notification)
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }

    private fun handleNow(bundle: Map<String, String>) {
        var id = 1
        val type = bundle["type"]
        val data = bundle["data"]
        val message = bundle["message"]
        val branchLink = bundle["branch"]
        var pendingIntent: PendingIntent? = null
        var title = ""
        var orderUid: String? = null
        if (branchLink != null) {
            val intent = Intent(this, BaseActivity::class.java)
            intent.putExtra("branch", branchLink)
            intent.putExtra("branch_force_new_session", true)
            pendingIntent = PendingIntent.getActivity(this, 15, intent, PendingIntent.FLAG_ONE_SHOT)
            title = "Notification"
            id = 1
        } else {
            if (ORDERS.equals(type, ignoreCase = true)) {
                if (data != null) {
                    orderUid = data
                    val intent = Intent(this, BaseActivity::class.java)
                    intent.putExtra("orderUid", orderUid)
                    pendingIntent =
                        PendingIntent.getActivity(this, data.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT)
                    val broadcast = Intent(MainActivity.ACTION_UPDATE_ORDER)
                    broadcast.putExtra("orderUid", orderUid)
                    sendBroadcast(broadcast)
                }
                id = 2
                title = "Order Status"
            }
        }
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val channelOneId = "com.skylarks"
        val channelOne = "Channel One"
        val notificationChannel: NotificationChannel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = NotificationChannel(
                channelOneId,
                channelOne, NotificationManager.IMPORTANCE_HIGH
            )
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = R.color.buttonColor
            notificationChannel.setShowBadge(true)
            notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            manager.createNotificationChannel(notificationChannel)
        }
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val builder = Notification.Builder(applicationContext)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_stat_name)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
        if (orderUid != null) {
            builder.setGroup(data)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(channelOneId)
        }
        val notification = builder.build()
        if (message != null) {
            manager.notify(id /* ID of notification */, notification)
        }
    }

    companion object {
        private const val TAG = "MyFirebaseMsgService"
        const val ORDERS = "20"
        const val SKYLARKS = "5"
    }
}
