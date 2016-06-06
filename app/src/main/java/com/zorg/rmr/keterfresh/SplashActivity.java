package com.zorg.rmr.keterfresh;

/**
 * Created by ronsegal on 6/2/2016.
 */
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {
    private Handler handler;
    private static final int SPLASH_TIMEOUT = 3000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_activity);

        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, KeterFresh.class);
                startActivity(intent);
                finish();
            }
        }, SPLASH_TIMEOUT);


    }
}
