package com.example.videomeeting.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.videomeeting.R;

public class Splash extends AppCompatActivity {

    private Button buttonGo;

    private Animation animationButton , animationText;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        buttonGo = findViewById(R.id.buttonAnim);
        textView = findViewById(R.id.TextAnimation);

        animationButton = AnimationUtils.loadAnimation(this , R.anim.animmation);
        buttonGo.setAnimation(animationButton);

        animationText = AnimationUtils.loadAnimation(this , R.anim.anim_text);
        textView.setAnimation(animationText);

    }

    public void button(View view) {
        Intent i = new Intent(getApplicationContext() , SinginActivity.class);
        startActivity(i);
        finish();
    }
}