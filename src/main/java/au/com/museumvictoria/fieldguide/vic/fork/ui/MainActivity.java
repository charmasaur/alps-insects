package au.com.museumvictoria.fieldguide.vic.fork.ui;

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
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.support.v7.widget.SearchView;

import au.com.museumvictoria.fieldguide.vic.fork.R;
import au.com.museumvictoria.fieldguide.vic.fork.db.FieldGuideDatabase;
import au.com.museumvictoria.fieldguide.vic.fork.ui.fragments.AboutFragment;
import au.com.museumvictoria.fieldguide.vic.fork.ui.fragments.GroupFragment;
import au.com.museumvictoria.fieldguide.vic.fork.ui.fragments.ImageGridFragment;
import au.com.museumvictoria.fieldguide.vic.fork.ui.fragments.SearchFragment;
import au.com.museumvictoria.fieldguide.vic.fork.ui.fragments.SpeciesGroupListFragment;
import au.com.museumvictoria.fieldguide.vic.fork.ui.fragments.SpeciesItemDetailFragment;
import au.com.museumvictoria.fieldguide.vic.fork.util.Utilities;

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

  /**
   * Represents a screen that can be showing to the user.
   */
  private static final class Screen {
    public final CharSequence title;
    @Nullable
    public final CharSequence subtitle;

    public Screen(CharSequence title, @Nullable CharSequence subtitle) {
      this.title = title;
      this.subtitle = subtitle;
    }
  };

  private final Map<String, Screen> backStackScreens = new HashMap<>();

  private Toolbar toolbar;

  private Screen homeScreen;

  private boolean showOptions = true;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    Log.i(TAG, "onCreate");

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    homeScreen = new Screen("Field Guide", "Australian Alpine Insects");

    toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    getSupportFragmentManager().addOnBackStackChangedListener(backStackChangedListener);
    setFragment(SpeciesGroupListFragment.newInstance(), null);

    handleIntent(getIntent());
  }

  @Override
  protected void onNewIntent(Intent intent) {
    handleIntent(intent);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    if (!showOptions) {
      return false;
    }
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
        backStackScreens.put("ABOUT", new Screen(getString(R.string.menu_about_name), null));
        setFragment(AboutFragment.newInstance(), "ABOUT");
        break;
      case android.R.id.home:
        onBackPressed();
        break;
      default:
        break;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onGroupSelected(String groupName, String groupOrder) {
    Log.i(TAG, "Group selected: " + groupName);

    backStackScreens.put("GROUP", new Screen(groupName, null));
    setFragment(GroupFragment.newInstance(groupOrder), "GROUP");
  }

  // TODO: At the moment this is the method of two callbacks simultaneously.. That might make sense
  // though.
  @Override
  public void onSpeciesSelected(String speciesId, String name, @Nullable String subname) {
    Log.i(TAG, "Species selected: " + speciesId);

    // TODO: Consider moving the formatting of the labels into the fragment.
    SpannableString subnameFormatted;
    // TODO: For now just hide the secondary text (since the italicisation looks silly -- too
    // slanted), but if Rachel thinks we should show it always then we can figure something out.
    subname = null;
    if (subname == null) {
      subnameFormatted = null;
    } else {
      subnameFormatted = new SpannableString(subname);
      subnameFormatted.setSpan(new StyleSpan(Typeface.ITALIC), 0, subname.length(),
          Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }
    backStackScreens.put("SPECIES", new Screen(name, subnameFormatted));
    setFragment(SpeciesItemDetailFragment.newInstance(speciesId), "SPECIES");
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
  }

  @Override
  protected void onRestoreInstanceState(Bundle savedInstanceState) {
  }

  private void handleIntent(Intent intent) {
    Log.i(TAG, "Handling intent: " + intent);
    if (intent.getAction() == null) {
      return;
    }
    switch (intent.getAction()) {
      case Intent.ACTION_VIEW:
        // TODO: Handle this (could occur when a search suggestion is clicked).
        Cursor cursor = getContentResolver().query(intent.getData(), null, null, null, null);
        onSpeciesSelected(
            cursor.getString(cursor.getColumnIndex(android.provider.BaseColumns._ID)),
            cursor.getString(cursor.getColumnIndex(FieldGuideDatabase.SPECIES_LABEL)),
            cursor.getString(cursor.getColumnIndex(FieldGuideDatabase.SPECIES_SUBLABEL)));
        break;
      case Intent.ACTION_SEARCH:
        backStackScreens.put("SEARCH", new Screen("Search", null));
        setFragment(SearchFragment.newInstance(intent.getExtras()), "SEARCH");
        break;
      default:
        break;
    };
  }

  /**
   * Sets the current fragment, updating the UI as necessary.
   */
  private void setFragment(Fragment fragment, @Nullable String backStackEntryName) {
    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction()
        .replace(R.id.basecontainer, fragment);
    if (backStackEntryName != null) {
      transaction.addToBackStack(backStackEntryName);
    }
    transaction.commit();
    if (backStackEntryName == null) {
      updateScreen();
    }
  }

  private void updateScreen() {
    Screen currentScreen;
    int backStackSize = getSupportFragmentManager().getBackStackEntryCount();
    if (backStackSize == 0) {
      // Home screen is showing.
      currentScreen = homeScreen;
      if (!showOptions) {
        showOptions = true;
        supportInvalidateOptionsMenu();
      }
    } else {
      currentScreen = backStackScreens.get(
          getSupportFragmentManager().getBackStackEntryAt(backStackSize - 1).getName());
      if (showOptions) {
        showOptions = false;
        supportInvalidateOptionsMenu();
      }
    }
    getSupportActionBar().setTitle(currentScreen.title);
    getSupportActionBar().setSubtitle(currentScreen.subtitle);
    getSupportActionBar().setDisplayHomeAsUpEnabled(backStackSize != 0);
  }

  private final FragmentManager.OnBackStackChangedListener backStackChangedListener =
      new FragmentManager.OnBackStackChangedListener() {
    @Override
    public void onBackStackChanged() {
      Log.i(TAG, "Back stack changed: " + getSupportFragmentManager().getBackStackEntryCount());
      updateScreen();
    }
  };

  // TODO: Deal with everything below this line.

  public void backToGroups(View view) {
    onBackPressed();
  }
}
