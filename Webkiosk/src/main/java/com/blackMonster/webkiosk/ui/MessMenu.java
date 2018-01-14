package com.blackMonster.webkiosk.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.blackMonster.webkioskApp.R;
import com.squareup.picasso.Picasso;

public class MessMenu extends AppCompatActivity {

    ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mess_menu);
        imageView= (ImageView) findViewById(R.id.imageview);
        Picasso.with(this).load(R.drawable.menu).into(imageView);
    }
}
