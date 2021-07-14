package com.gyde;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.gyde.mylibrary.screens.GydeHomeActivity;

import java.util.List;

public class MainActivity2 extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        Button gydeHelp = findViewById(R.id.btn_start);
        gydeHelp.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity2.this, GydeHomeActivity.class);
            startActivity(intent);
        });
    }

    private void getDeepLinkingData() {
        try {
            Uri uri = getIntent().getData();
            if (uri != null) {
                List<String> parameters = uri.getPathSegments();
                String param = parameters.get(parameters.size() - 1);
                Intent intent = new Intent(MainActivity2.this, GydeHomeActivity.class);
                intent.putExtra("GYDE_DEEP_LINK_DATA", param);
                startActivity(intent);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

//    private fun getDeepLinkingData() {
//        try {
//            val uri:Uri ? = intent.data
//            if (uri != null) {
//                val parameters = uri.pathSegments
//                val param = parameters[parameters.size - 1]
//                startActivity(
//                        Intent(this @MainActivity,GydeHomeActivity:: class.java).putExtra(
//                        "GYDE_DEEP_LINK_DATA", param
//                )
//                )
//            }
//        } catch (ex:Exception){
//            ex.printStackTrace()
//        }
//    }
}