package com.example.clientchessapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;

public class MatchSetup extends AppCompatActivity {

    int rating;
    int cpu;
    ProtocolObject deadman_switch;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_setup);

        if(this.deadman_switch == null) {
            this.deadman_switch = new ProtocolObject();
            this.deadman_switch.deadman_start();
        }

        ProtocolObject request_object = new ProtocolObject();

        if(request_object.is_logged()){
            request_object.send_request("101,3+1,"+rating+","+cpu);
        }
        else{
            //log in in log in activity then queue up
        }

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

        deadman_handler.removeCallbacks(deadman_runnable); //Stops the deadman switch on this activity
    }
}