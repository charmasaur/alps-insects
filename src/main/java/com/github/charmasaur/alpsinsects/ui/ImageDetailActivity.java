package com.github.charmasaur.alpsinsects.ui;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import com.github.charmasaur.alpsinsects.R;
import com.github.charmasaur.alpsinsects.db.FieldGuideDatabase;
import com.github.charmasaur.alpsinsects.ui.fragments.ImageDetailFragment;

import java.util.ArrayList;
import java.util.List;

public class ImageDetailActivity extends AppCompatActivity
    implements ImageDetailFragment.Callback {
  public static final String EXTRA_SPECIES_ID = "species_id";
  public static final String EXTRA_IMAGE = "extra_image";
  public static final String EXTRA_SPECIES_LABEL = "species_label";

  private final LabelVisibilityManager labelVisibilityManager = new LabelVisibilityManager();

  private ImagePagerAdapter mAdapter;
  private ViewPager mPager;
  private ActionBar actionBar;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_image_detail);

    FieldGuideDatabase db = FieldGuideDatabase.getInstance(getApplicationContext());
    if (!getIntent().hasExtra(EXTRA_SPECIES_ID)) {
      throw new RuntimeException("Missing species ID");
    }
    Cursor cursor = db.getSpeciesImages(getIntent().getStringExtra(EXTRA_SPECIES_ID),
        new String[] { FieldGuideDatabase.MEDIA_ID });

    // Set up ViewPager and backing adapter
    mAdapter = new ImagePagerAdapter(getSupportFragmentManager(), cursor);
    mPager = (ViewPager) findViewById(R.id.pager);
    mPager.setAdapter(mAdapter);
    mPager.setPageMargin((int) getResources().getDimension(R.dimen.image_detail_pager_margin));

    // Set up activity to go full screen
    getWindow().addFlags(LayoutParams.FLAG_FULLSCREEN);

    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    actionBar = getSupportActionBar();
    labelVisibilityManager.setActionBar(actionBar);

    // Enable "up" navigation on ActionBar icon and hide title text
    actionBar.setTitle(getIntent().getStringExtra(EXTRA_SPECIES_LABEL));
    actionBar.setDisplayHomeAsUpEnabled(true);

    // Start low profile mode and hide ActionBar
//    mPager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
//    actionBar.hide();

    // Hide and show the ActionBar as the visibility changes
//    mPager.setOnSystemUiVisibilityChangeListener(
//        new View.OnSystemUiVisibilityChangeListener() {
//      @Override
//      public void onSystemUiVisibilityChange(int vis) {
//        if ((vis & View.SYSTEM_UI_FLAG_LOW_PROFILE) != 0) {
//          actionBar.hide();
//        } else {
//          actionBar.show();
//        }
//      }
//    });

    if (getIntent().hasExtra(EXTRA_IMAGE)) {
      mPager.setCurrentItem(getIntent().getIntExtra(EXTRA_IMAGE, 0));
    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        // Home or "up" navigation
//        final Intent intent = new Intent(this, HomeActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        startActivity(intent);

        finish();
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onImageTapped() {
    labelVisibilityManager.onImageTapped();
  }

  @Override
  public void registerLabelVisibilityListener(ImageDetailFragment.Callback.Listener listener) {
    labelVisibilityManager.register(listener);
  }

  @Override
  public void unregisterLabelVisibilityListener(ImageDetailFragment.Callback.Listener listener) {
    labelVisibilityManager.unregister(listener);
  }

  /**
   * The main adapter that backs the ViewPager. A subclass of FragmentStatePagerAdapter as there
   * could be a large number of items in the ViewPager and we don't want to retain them all in
   * memory at once but create/destroy them on the fly.
   */
  private class ImagePagerAdapter extends FragmentStatePagerAdapter {
    private final Cursor cursor;

    public ImagePagerAdapter(FragmentManager fm, Cursor cursor) {
      super(fm);
      this.cursor = cursor;
    }

    @Override
    public int getCount() {
      return cursor.getCount();
    }

    @Override
    public Fragment getItem(int position) {
      cursor.moveToPosition(position);
      return ImageDetailFragment.newInstance(
          cursor.getString(cursor.getColumnIndex(FieldGuideDatabase.MEDIA_ID)));
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
      final ImageDetailFragment fragment = (ImageDetailFragment) object;
      // As the item gets destroyed we try and cancel any existing work.
      super.destroyItem(container, position, object);
    }
  }

  /**
   * Manages the visibility of the labels and action bar.
   */
  private static final class LabelVisibilityManager {
    private final List<ImageDetailFragment.Callback.Listener> listeners = new ArrayList<>();
    @Nullable
    private ActionBar actionBar;

    private boolean visibility = true;

    public void onImageTapped() {
      visibility = !visibility;
      for (ImageDetailFragment.Callback.Listener listener : listeners) {
        listener.onVisibilityChanged(visibility);
      }
      if (actionBar != null) {
        setActionBarVisibility();
      }
    }

    public void register(ImageDetailFragment.Callback.Listener listener) {
      listeners.add(listener);
      listener.onVisibilityChanged(visibility);
    }

    public void unregister(ImageDetailFragment.Callback.Listener listener) {
      boolean removed = listeners.remove(listener);
      if (!removed) {
        throw new RuntimeException("Tried to remove a non-added listener, something's wrong");
      }
    }

    public void setActionBar(ActionBar actionBar) {
      this.actionBar = actionBar;
      setActionBarVisibility();
    }

    private void setActionBarVisibility() {
      if (visibility) {
        actionBar.show();
      } else {
        actionBar.hide();
      }
    }
  }
}
