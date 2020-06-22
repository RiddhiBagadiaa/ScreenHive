package com.example.screenhive;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.res.ResourcesCompat;

import java.time.Instant;
import java.util.Date;

import static com.example.screenhive.MyChannel.CHANNEL_ID;

public class ChatHeadService extends Service {

    private WindowManager windowManager;
    private static View chatheadView  = null;
    WindowManager.LayoutParams params;
    int screenHeight;
    int screenWidth;
    BroadcastReceiver broadcastOnOff;
    static ImageView chatheadImage;
    static SharedPreferences prefs;
    static Chronometer mChronometer;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("ScreenHive")
                .setContentText("Screen timer is running")
                .setSmallIcon(R.drawable.ic_av_timer_black_24dp)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);



        chatheadView = LayoutInflater.from(this).inflate(R.layout.chathead_bubble, null);
        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 0;
        params.y = 100;

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        windowManager.addView(chatheadView, params);


        /* Get the height and width of the screen*/
        DisplayMetrics displayMetrics = new DisplayMetrics();
        //Gets WindowManager for a different display
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        screenHeight = displayMetrics.heightPixels;
        screenWidth = displayMetrics.widthPixels;

        prefs = this.getSharedPreferences("com.example.screenhive.PREFERENCE_FILE_KEY", Context.MODE_PRIVATE);

        chatheadImage = chatheadView.findViewById(R.id.chathead_image);

        mChronometer = chatheadView.findViewById(R.id.chronometer);
        mChronometer.setTypeface(ResourcesCompat.getFont(this, R.font.marvel_bold));

        broadcastOnOff = new BroadcastOnOff();
        IntentFilter onOff = new IntentFilter();
        onOff.addAction(Intent.ACTION_SCREEN_ON);
        onOff.addAction(Intent.ACTION_SCREEN_OFF);
        onOff.addAction(Intent.ACTION_SHUTDOWN);
        onOff.setPriority(999);
        registerReceiver(broadcastOnOff, onOff);

        Date now = Date.from(Instant.now());
        long dayEndLong = ChatHeadService.prefs.getLong("DayEnd", 0);

        if(now.getTime() > dayEndLong) {
            TimeService.resetChronometer();
            SharedPreferences.Editor editor = ChatHeadService.prefs.edit();
            editor.putLong("DayEnd", TimeService.endOfDay(now));
            editor.apply();
        }

        TimeService.startChronometer();

        manageImageAndText(chatheadImage, mChronometer);
        movement();
    }

    @Override
    public int onStartCommand(Intent intent, int flag, int startId) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("ScreenHive")
                .setContentText("Screen timer is running")
                .setSmallIcon(R.drawable.ic_av_timer_black_24dp)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);

        manageImageAndText(chatheadImage, mChronometer);

        return START_REDELIVER_INTENT;
    }

    public void movement() {
        chatheadImage.setOnTouchListener(
                new View.OnTouchListener() {
                    private int initialX;
                    private int initialY;
                    private float touchX;
                    private float touchY;
                    private int lastAction;


                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            initialX = params.x;
                            initialY = params.y;

                            touchX = event.getRawX();
                            touchY = event.getRawY();
                            lastAction= event.getAction();

                            return true;
                        }
                        if (event.getAction() == MotionEvent.ACTION_UP) {
                            if (lastAction == MotionEvent.ACTION_DOWN) {
                                if (lastAction == MotionEvent.ACTION_UP) {
                                    Button button = new Button(ChatHeadService.this);
                                    button.setText("close");

                                    RelativeLayout layout = chatheadView.findViewById(R.id.chathead_bubble);
                                    layout.addView(button);

                                    button.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            stopSelf();
                                            TimeService.pauseChronometer();
                                        }
                                    });
                                }
                            }

                            lastAction = event.getAction();
                            return true;
                        }

                        if (event.getAction() == MotionEvent.ACTION_MOVE) {
                            params.x = initialX + (int) (event.getRawX() - touchX);
                            params.y = initialY + (int) (event.getRawY() - touchY);

                            windowManager.updateViewLayout(chatheadView, params);
                            lastAction = event.getAction();
                            return true;
                        }
                        return false;
                    }
                }
        );
    }

    private void manageImageAndText(ImageView img, Chronometer txt) {
        RelativeLayout.LayoutParams imageParams = new RelativeLayout.LayoutParams(300, 300);
        img.setLayoutParams(imageParams);

        txt.setTextColor(Color.BLACK);
        txt.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 32);
        RelativeLayout.LayoutParams textParams = (RelativeLayout.LayoutParams) txt.getLayoutParams();
        textParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);

        txt.setLayoutParams(textParams);
    }

    @Override
    public void onDestroy() {
        TimeService.pauseChronometer();
        unregisterReceiver(broadcastOnOff);
        windowManager.removeView(chatheadView);
        stopSelf();
    }
}
