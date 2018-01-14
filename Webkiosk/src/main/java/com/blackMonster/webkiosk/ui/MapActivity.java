package com.blackMonster.webkiosk.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.blackMonster.webkioskApp.R;

public class MapActivity extends AppCompatActivity {

    EditText edclass;
    Button btnclass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        edclass= (EditText) findViewById(R.id.edclass);
        btnclass= (Button) findViewById(R.id.btnclass);

        btnclass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String str=edclass.getText().toString();
                if(str.equals("G1")||str.equals("G2")||str.equals("G3")||str.equals("G4")||str.equals("G5")||str.equals("G6")||str.equals("G7")
                        ||str.equals("G8")||str.equals("G9")|| str.equals("LT1")||str.equals("LT2")||str.equals("LT3")){
                Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.jiit1.com");
                if (launchIntent != null) {
                    startActivity(launchIntent);
                }
            }else if(str.equals("FF1")||str.equals("FF2")||str.equals("FF3")||str.equals("FF4")||str.equals("FF5")||str.equals("FF6")||
                            str.equals("FF7")||str.equals("FF8")||str.equals("FF9")|| str.equals("BIOTECH LAB")||str.equals("MATHS FACULTY ")){
                        Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.jiit2.com");
                        if (launchIntent != null) {
                            startActivity(launchIntent);
                        }
                    }
                else  if(str.equals("TS1")||str.equals("TS2")||str.equals("TS3")||str.equals("TS4")||str.equals("TS5")||str.equals("TS6")||
                        str.equals("TS7")||str.equals("TS8")||str.equals("TS9")|| str.equals("TS10")||str.equals("TS11")||str.equals("TS12")||str.equals("CS1")||str.equals("CS2")||str.equals("CS3")||str.equals("CS4")||str.equals("TNP")||
                        str.equals("MML")){
                    Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.jiit3.com");
                    if (launchIntent != null) {
                        startActivity(launchIntent);
                    }

                }
                else  if(str.equals("LT4")||str.equals("LT5")||str.equals("G10")||str.equals("G11")||str.equals("AUDI")||str.equals("G12")||
                        str.equals("G13")){
                    Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.jiit4.com");
                    if (launchIntent != null) {
                        startActivity(launchIntent);
                    }

                }
                else  if(str.equals("F4")||str.equals("F5")||str.equals("F6")||str.equals("F7")||str.equals("F8")||str.equals("F9")||
                        str.equals("F10")){
                    Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.jiit7.com");
                    if (launchIntent != null) {
                        startActivity(launchIntent);
                    }

                }
                else  if(str.equals("TS13")||str.equals("TS14")||str.equals("TS15")||str.equals("TS16")||str.equals("TS17")||str.equals("TS18")||
                        str.equals("TS19")||str.equals("TS20")||str.equals("S6")||str.equals("S7")){
                    Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.jiit6.com");
                    if (launchIntent != null) {
                        startActivity(launchIntent);
                    }

                }
                else  if(str.equals("AR LAB")||str.equals("SP LAB")||str.equals("BIOTECH LAB")){
                    Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.jiit5.com");
                    if (launchIntent != null) {
                        startActivity(launchIntent);
                    }

                }
            }
        });
    }
}
