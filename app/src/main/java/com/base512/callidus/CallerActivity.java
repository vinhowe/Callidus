package com.base512.callidus;

import android.animation.ValueAnimator;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

public class CallerActivity extends AppCompatActivity {

    FloatingActionButton callButton;
    TextView callerNumberLabel;
    TextView callFromIndicatorLabel;

    ConstraintLayout callOptionsLayout;

    float callButtonHighMargin;
    float callButtonLowMargin;

    ValueAnimator callButtonAnimator;
    Runnable callButtonAnimationRunnable;
    Handler callButtonAnimationHandler = new Handler();

    Handler lockHandler = new Handler();
    Runnable lockRunnable;
    //ValueAnimator callButtonAnimatorAnimator;

    private static final long[] VIBRATE_PATTERN = {0, 1000, 1000};

    private Vibrator vibrator;

    boolean isCalling = false;

    Ringtone defaultRingtone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_caller);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON|
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD|
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        callButtonHighMargin = getResources().getDimension(R.dimen.button_high_margin);
        callButtonLowMargin = getResources().getDimension(R.dimen.button_low_margin);

        if(!CallPromptService.isRunning) {
            Intent intent = new Intent(this, CallPromptService.class);
            startService(intent);
        }

        setupViews();
        Uri defaultRingtoneUri = RingtoneManager.getActualDefaultRingtoneUri(getApplicationContext(), RingtoneManager.TYPE_RINGTONE);
        defaultRingtone = RingtoneManager.getRingtone(getApplicationContext(), defaultRingtoneUri);
        defaultRingtone.play();

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        vibrator.vibrate(VIBRATE_PATTERN, 1);
    }

    private void setupViews() {
        callButton = findViewById(R.id.callButton);
        callerNumberLabel = findViewById(R.id.callerNumberLabel);
        callFromIndicatorLabel = findViewById(R.id.callFromIndicatorLabel);

        callOptionsLayout = findViewById(R.id.callOptionsLayout);

        callerNumberLabel.setText(generateRandomNumber());

        callButtonAnimator = ValueAnimator.ofFloat(callButtonHighMargin, 0);
        callButtonAnimator.setDuration(200);
        callButtonAnimator.setRepeatMode(ValueAnimator.REVERSE);
        callButtonAnimator.setRepeatCount(20);
        callButtonAnimator.setInterpolator(AnimationUtils.loadInterpolator(this, android.R.interpolator.accelerate_cubic));
        callButtonAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float margin = (float) valueAnimator.getAnimatedValue();
                callButton.setTranslationY(margin);
            }
        });

        callButtonAnimationRunnable = new Runnable(){
            public void run(){
                //do something
                callButtonAnimator.start();
                callButtonAnimationHandler.postDelayed(this, 3000);
            }
        };

        lockRunnable = new Runnable() {
            @Override
            public void run() {
                vibrator.cancel();
                lock();
                finish();
            }
        };

        callButtonAnimationHandler.post(callButtonAnimationRunnable);

        callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isCalling) {
                    isCalling = true;

                    callButtonAnimationHandler.removeCallbacks(callButtonAnimationRunnable);
                    callButtonAnimator.pause();
                    defaultRingtone.stop();
                    vibrator.cancel();
                    callFromIndicatorLabel.setVisibility(View.GONE);
                    getWindow().setBackgroundDrawableResource(R.color.colorAccent);
                    callButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_call_end_red_24dp, getTheme()));

                    callOptionsLayout.setVisibility(View.VISIBLE);

                    lockHandler.postDelayed(lockRunnable, 1000);
                } else {
                    lockHandler.removeCallbacks(lockRunnable);
                    finish();
                }

            }
        });
/*        callButtonAnimatorAnimator = ValueAnimator.ofFloat(90, 1000);
        callButtonAnimatorAnimator.setDuration(10000);
        callButtonAnimatorAnimator.setRepeatMode(ValueAnimator.REVERSE);
        callButtonAnimatorAnimator.setRepeatCount(ValueAnimator.INFINITE);
        callButtonAnimatorAnimator.setInterpolator(AnimationUtils.loadInterpolator(this, android.R.interpolator.decelerate_quad));
        callButtonAnimatorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                callButtonAnimator.setDuration((int)(float) valueAnimator.getAnimatedValue());
            }
        });
        callButtonAnimatorAnimator.start();*/

    }

    private String generateRandomNumber() {
        String areaCode = "864";
        String prefix = String.format("%03d", (int)(Math.random()*999));
        String lineNumber = String.format("%04d", (int)(Math.random()*9999));

        return "("+areaCode+") "+prefix+"-"+lineNumber;
    }

    private void lock() {
        PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
        if (pm.isInteractive()) {
            DevicePolicyManager policy = (DevicePolicyManager)
                    getSystemService(Context.DEVICE_POLICY_SERVICE);
            try {
                policy.lockNow();
            } catch (SecurityException ex) {
                Toast.makeText(
                        this,
                        "must enable device administrator",
                        Toast.LENGTH_LONG).show();
                ComponentName admin = new ComponentName(this, AdminReceiver.class);
                Intent intent = new Intent(
                        DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).putExtra(
                        DevicePolicyManager.EXTRA_DEVICE_ADMIN, admin);
                startActivity(intent);
            }
        }
    }
}
