package com.example.clientchessapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    OkHttpClient client = new OkHttpClient();

    ProtocolObject deadman_switch = new ProtocolObject();




    Call get(String url, Callback callback) {
        Request request = new Request.Builder()
                .url(url)
                .build();
        Call call = client.newCall(request);
        call.enqueue(callback);
        return call;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(this.deadman_switch == null) {
            this.deadman_switch = new ProtocolObject();
            this.deadman_switch.deadman_start();
        }

        Button play_online = findViewById(R.id.play_online);
        final TextView tv1 = findViewById(R.id.test);
        String used_url = "https://fizzychess-server.herokuapp.com/";

        ProtocolObject request = new ProtocolObject(getApplicationContext());
        request.writeToFile("cock",getApplicationContext());

        final TextView test = findViewById(R.id.file_test);

        test.setText(request.readFile(getFilesDir().toString(),"game.txt"));

        Callback callback = new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                tv1.setText("SERVER:Failed 2 Connect");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

                if (response.isSuccessful()) {
                    final String rs = response.body().string();
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tv1.setText("SERVER:Connected Successfully");
                        }
                    });
                }
            }
        };

        this.get(used_url,callback);
        play_online.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this , MatchSetup.class);
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        if(this.deadman_switch == null) {
            this.deadman_switch = new ProtocolObject();
            this.deadman_switch.deadman_start();
        }
    }
    @Override
    protected void onPause(){
        super.onPause();

        Handler deadman_handler = this.deadman_switch.get_deadman_handler();
        Runnable deadman_runnable = this.deadman_switch.getDeadman_runnable();
        if(deadman_runnable != null) {
            deadman_handler.removeCallbacks(deadman_runnable); //Stops the deadman switch on this activity
        }
    }
}

