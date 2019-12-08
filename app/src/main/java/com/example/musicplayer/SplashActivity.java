package com.example.musicplayer;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;


public class SplashActivity extends AppCompatActivity {
    private ImageView iv_splash_screen;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        setFindView();
        setInitial();
    }

    private void setFindView() {
        iv_splash_screen = findViewById(R.id.iv_splash_screen);
    }

    private void setInitial() {
        Glide.with(SplashActivity.this)
                .load(R.drawable.splash_logo)
                .apply(new RequestOptions().transform(new CircleCrop()))
                .into(iv_splash_screen);

        Handler handler = new Handler();
        handler.postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();
        }, 2000);

    }

}
