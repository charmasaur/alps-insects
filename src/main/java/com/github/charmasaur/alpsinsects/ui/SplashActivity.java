package au.com.museumvictoria.fieldguide.vic.fork.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import au.com.museumvictoria.fieldguide.vic.fork.R;
import au.com.museumvictoria.fieldguide.vic.fork.db.FieldGuideDatabase;

public final class SplashActivity extends AppCompatActivity {
  private static final String TAG = SplashActivity.class.getSimpleName();

  @Override
  public void onCreate(Bundle savedInstanceState) {
    setTheme(R.style.Theme_FieldGuide_FullScreen);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_splash);

    // Load the database on a background thread.
    new Thread(new Runnable() {
      public void run() {
        Log.i(TAG, "Opening database.");
        FieldGuideDatabase.getInstance(getApplicationContext()).open();

        Log.i(TAG, "Database opened, starting main activity.");
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

        finish();
      }
    }).start();
  }
}
