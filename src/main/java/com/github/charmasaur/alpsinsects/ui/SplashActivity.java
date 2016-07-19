package com.github.charmasaur.alpsinsects.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import com.github.charmasaur.alpsinsects.R;
import com.github.charmasaur.alpsinsects.db.FieldGuideDatabase;

public final class SplashActivity extends AppCompatActivity {
  private static final String TAG = SplashActivity.class.getSimpleName();

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    final Handler handler = new Handler();
    // Load the database on a background thread.
    new Thread(new Runnable() {
      public void run() {
        Log.i(TAG, "Opening database.");
        FieldGuideDatabase.getInstance(getApplicationContext()).open();

        handler.postDelayed(new Runnable() {public void run() {
        Log.i(TAG, "Database opened, starting main activity.");
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

        finish();
        }}, 0);
      }
    }).start();
  }
}
