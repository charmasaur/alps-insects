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

  private static final long SPLASH_TIME_MS = 1 * 1000;

  private boolean databaseLoaded;
  private boolean timerFired;
  private boolean destroyed;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    final Handler handler = new Handler();

    handler.postDelayed(new Runnable() {
      @Override
      public void run() {
        timerFired = true;
        maybeStart();
      }
    }, SPLASH_TIME_MS);

    // Load the database on a background thread.
    new Thread(new Runnable() {
      public void run() {
        Log.i(TAG, "Opening database.");
        FieldGuideDatabase.getInstance(getApplicationContext()).open();

        handler.post(new Runnable() {
          @Override
          public void run() {
            databaseLoaded = true;
            maybeStart();
          }
        });
      }
    }).start();
  }

  @Override
  public void onDestroy() {
    destroyed = true;
    super.onDestroy();
  }

  private void maybeStart() {
    if (!destroyed && databaseLoaded && timerFired) {
      Log.i(TAG, "Database opened, starting main activity.");
      Intent intent = new Intent(getApplicationContext(), MainActivity.class);
      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
      startActivity(intent);

      finish();
    }
  }
}
