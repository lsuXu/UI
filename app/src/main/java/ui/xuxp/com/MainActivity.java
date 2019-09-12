package ui.xuxp.com;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import ui.xuxp.com.view.ProgressBarView;

public class MainActivity extends AppCompatActivity {

    private ProgressBarView progressBarView ;

    private Button enable,disable ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressBarView = findViewById(R.id.bar);
        enable = findViewById(R.id.enable);
        disable = findViewById(R.id.disable);
        enable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBarView.setEnabled(true);
            }
        });
        disable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBarView.setEnabled(false);
            }
        });
    }
}
