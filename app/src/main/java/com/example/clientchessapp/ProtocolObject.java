package com.example.clientchessapp;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import android.content.Context;
import android.os.UserHandle;
import android.util.Log;
import android.view.Display;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static android.app.PendingIntent.getActivity;
import static java.lang.System.currentTimeMillis;
import static java.security.AccessController.getContext;


public class ProtocolObject {

    final String server_url = "https://fizzychess-server.herokuapp.com/";
    Handler deadman_handler;
    Runnable deadman_runnable;
    Context app_context;

    ProtocolObject(){

    }
    ProtocolObject(Context context){
        this.app_context = context;
    }

    public Runnable getDeadman_runnable() {
        return deadman_runnable;
    }

    public Handler get_deadman_handler(){
        return this.deadman_handler;
    }

    public Boolean test_connection() throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(this.server_url)
                .build();
        Response response = client.newCall(request).execute();
        return response.isSuccessful();
    }

    public void writeToFile(String data, Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("game.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    public String readFile(String folder, String file_name){
        String myData = "";
        File myExternalFile = new File(folder,file_name);
        try {
            FileInputStream fis = new FileInputStream(myExternalFile);
            DataInputStream in = new DataInputStream(fis);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            String strLine;
            while ((strLine = br.readLine()) != null) {
                myData = myData + strLine + "\n";
            }
            br.close();
            in.close();
            fis.close();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        return myData;
    }
    public Boolean is_logged(){
        String login_file = readFile("res/raw/","login.txt");
        if (login_file.equals("") || login_file == null) {
            return false;
        }
        String[] login_file_array = login_file.split("//,");
        long current_time =  (currentTimeMillis()/1000);
        long expiration_time = Long.parseLong(login_file_array[2]);
        boolean b = expiration_time > current_time;
        return b;
    }

    public void send_request(String message){
        OkHttpClient client = new OkHttpClient();
        String request_url = this.server_url+message;
        Request request = new Request.Builder()
                .url(request_url)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                } else {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            ProtocolObject handle_response = new ProtocolObject();
                            try {
                                handle_response.process_response(response.body().string());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        });
    }

    public Handler deadman_start(){
        Handler handler = new Handler();

        Runnable deadman = new Runnable() {
            @Override
            public void run() {
                send_request("303");
                handler.postDelayed(this,1500);
            }
        };
        deadman.run();
        this.deadman_handler = handler;
        this.deadman_runnable = deadman;
        return handler;
    }

    private void process_response(@NotNull String response) throws Exception {

        String[] response_array = response.split("//,");

        int response_code = Integer.parseInt( Character.toString(response.charAt(0))+Character.toString(response.charAt(1))+Character.toString(response.charAt(2))+Character.toString(response.charAt(3)));
        switch (response_code){
            case 1000:
                throw new Exception("Invalid Queue Request");
            case 1011:
                String[] body_array = response_array[1].split("//|");
                String game_id = body_array[1];
                String color = body_array[1];
                writeToFile(game_id+","+color, this.app_context);
                break;
            case 1012:
                // Still looking for a match
                break;
            case 1013:
                //Already Queued
                break;
            case 1014:
                //General Failure
                break;
            case 1021:
                // success, queued down
                break;
            case 1022:
                //not in queue
                break;
            case 1023:
                //general failure
                break;

            default:
                throw new IllegalStateException("Unexpected value: " + response_code);
        }

    }
}
