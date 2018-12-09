package com.GangOfFour.FIDeTech;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {

    LinearLayout water,petrol,phtest,team;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        water= findViewById(R.id.water);
        petrol=findViewById(R.id.petrol);
        phtest=findViewById(R.id.phTest);
        team=findViewById(R.id.team);
        team.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(MainActivity.this,Profile.class);
                startActivity(intent);
            }
        });
        water.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent(MainActivity.this,WaterTest.class);
                startActivity(intent);
            }
        });
    }
}
