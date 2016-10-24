package com.github.charmasaur.alpsinsects.ui;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.OnNavigationListener;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.support.v7.widget.SearchView;

import com.github.charmasaur.alpsinsects.R;
import com.github.charmasaur.alpsinsects.db.FieldGuideDatabase;
import com.github.charmasaur.alpsinsects.ui.fragments.GetInvolvedFragment;
import com.github.charmasaur.alpsinsects.ui.fragments.GroupFragment;
import com.github.charmasaur.alpsinsects.ui.fragments.HtmlTextFragment;
import com.github.charmasaur.alpsinsects.ui.fragments.SearchFragment;
import com.github.charmasaur.alpsinsects.ui.fragments.SpeciesGroupListFragment;
import com.github.charmasaur.alpsinsects.ui.fragments.SpeciesItemDetailFragment;
import com.github.charmasaur.alpsinsects.ui.fragments.WebViewFragment;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements SpeciesGroupListFragment.Callback,
    GroupFragment.Callback, SearchFragment.Callback {
  private static final String TAG = MainActivity.class.getSimpleName();

  /**
   * The serialization (saved instance state) Bundle key representing the
   * current tab position.
   */
  private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";

  private FragmentController fragmentController;

  private Toolbar toolbar;

  private boolean fragmentsResumed;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    Log.i(TAG, "onCreate");

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    boolean dualPane = findViewById(R.id.double_container) != null;

    toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    fragmentController = new FragmentController(getSupportFragmentManager(),
        dualPane ? R.id.double_container : R.id.single_container, dualPane);
    if (savedInstanceState == null) {
      fragmentController.pushFragment(
          FragmentInfo.fromHeading(getString(R.string.title_group_list),
              getString(R.string.subtitle_group_list), "GROUPLIST"),
          SpeciesGroupListFragment.newInstance(), 0);
      updateHeading();
    }

    getSupportFragmentManager().addOnBackStackChangedListener(backStackChangedListener);

    handleIntent(getIntent());
    setIntent(null);
  }

  @Override
  protected void onNewIntent(Intent intent) {
    if (fragmentsResumed) {
      handleIntent(intent);
      setIntent(null);
    } else {
      setIntent(intent);
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.activity_main, menu);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
      SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
      SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
      searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
    }

    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.menu_about:
        fragmentController.pushFragment(
            FragmentInfo.fromHeading(getString(R.string.menu_about_name), null, "ABOUT"),
            HtmlTextFragment.newInstance(R.string.about_string), 2);
        break;
      case R.id.menu_get_involved:
        fragmentController.pushFragment(
            FragmentInfo.fromHeading(getString(R.string.menu_get_involved_name), null, "INV"),
            GetInvolvedFragment.newInstance(), 2);
        break;
      case R.id.menu_resources:
        fragmentController.pushFragment(
            FragmentInfo.fromHeading(getString(R.string.menu_resources_name), null, "RES"),
            HtmlTextFragment.newInstance(R.string.resources_string), 2);
        break;
      case R.id.menu_licenses:
        fragmentController.pushFragment(
            FragmentInfo.fromHeading(getString(R.string.menu_licenses_name), null, "LIC"),
            WebViewFragment.newInstance("open_source_licenses.html"), 2);
        break;
      case android.R.id.home:
        fragmentController.navigateUp();
        break;
      default:
        break;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onGroupSelected(String groupName, String groupOrder) {
    Log.i(TAG, "Group selected: " + groupName);

    fragmentController.pushFragment(FragmentInfo.fromHeading(groupName, null, "GROUP"),
        GroupFragment.newInstance(groupOrder), 4);
  }

  @Override
  public void onSpeciesSelected(String speciesId, String name, @Nullable String subname) {
    Log.i(TAG, "Species selected: " + speciesId);
    pushSpecies(speciesId, name, subname, 5);
  }

  @Override
  public void onSpeciesSelectedSearch(String speciesId, String name, @Nullable String subname) {
    Log.i(TAG, "Species selected search: " + speciesId);
    pushSpecies(speciesId, name, subname, 7);
  }

  private void pushSpecies(String speciesId, String name, @Nullable String subname, int level) {
    subname = null;
    fragmentController.pushFragment(FragmentInfo.fromHeading(name, null, "SPECIES"),
        SpeciesItemDetailFragment.newInstance(speciesId), level);
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    fragmentController.save(outState);
    super.onSaveInstanceState(outState);
  }

  @Override
  protected void onRestoreInstanceState(Bundle savedInstanceState) {
    super.onRestoreInstanceState(savedInstanceState);
    fragmentController.load(savedInstanceState);
    updateHeading();
  }

  @Override
  protected void onResumeFragments() {
    super.onResumeFragments();
    fragmentsResumed = true;
    // Process any intent that we might have received while paused.
    if (getIntent() != null) {
      handleIntent(getIntent());
      setIntent(null);
    }
  }

  @Override
  protected void onPause() {
    super.onPause();
    fragmentsResumed = false;
  }

  private void updateHeading() {
    FragmentInfo fragmentInfo = fragmentController.getLeafInfo();
    getSupportActionBar().setTitle(fragmentInfo.getTitle());
    getSupportActionBar().setSubtitle(fragmentInfo.getSubtitle());
    getSupportActionBar().setDisplayHomeAsUpEnabled(!fragmentInfo.getId().equals(
        fragmentController.getRootInfo().getId()));
  }

  private void handleIntent(Intent intent) {
    Log.i(TAG, "Handling intent: " + intent);
    if (intent.getAction() == null) {
      return;
    }
    switch (intent.getAction()) {
      case Intent.ACTION_VIEW:
        Cursor cursor = getContentResolver().query(intent.getData(), null, null, null, null);
        pushSpecies(
            cursor.getString(cursor.getColumnIndex(FieldGuideDatabase.SPECIES_ID)),
            cursor.getString(cursor.getColumnIndex(FieldGuideDatabase.SPECIES_LABEL)),
            cursor.getString(cursor.getColumnIndex(FieldGuideDatabase.SPECIES_SUBLABEL)),
            8);
        break;
      case Intent.ACTION_SEARCH:
        String query = intent.getExtras().getString(SearchManager.QUERY);
        if (query == null) {
          Log.i(TAG, "Null search query, weird");
          break;
        }
        fragmentController.pushFragment(
            FragmentInfo.fromHeadingAndId("Search", null, query, "SEARCH"),
            SearchFragment.newInstance(intent.getExtras()),
            6);
        break;
      default:
        break;
    };
  }

  private final FragmentManager.OnBackStackChangedListener backStackChangedListener =
      new FragmentManager.OnBackStackChangedListener() {
    @Override
    public void onBackStackChanged() {
      updateHeading();
    }
  };
}
