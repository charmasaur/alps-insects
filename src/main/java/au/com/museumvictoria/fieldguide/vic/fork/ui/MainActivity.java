package au.com.museumvictoria.fieldguide.vic.fork.ui;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.support.v7.widget.SearchView;

import au.com.museumvictoria.fieldguide.vic.fork.R;
import au.com.museumvictoria.fieldguide.vic.fork.ui.fragments.GroupFragment;
import au.com.museumvictoria.fieldguide.vic.fork.ui.fragments.HomeFragment;
import au.com.museumvictoria.fieldguide.vic.fork.ui.fragments.ImageGridFragment;
import au.com.museumvictoria.fieldguide.vic.fork.ui.fragments.SearchFragment;
import au.com.museumvictoria.fieldguide.vic.fork.ui.fragments.SpeciesGroupListFragment;
import au.com.museumvictoria.fieldguide.vic.fork.ui.fragments.SpeciesItemDetailFragment;
import au.com.museumvictoria.fieldguide.vic.fork.ui.fragments.SpeciesItemListFragment;
import au.com.museumvictoria.fieldguide.vic.fork.ui.fragments.SpeciesListFragment;
import au.com.museumvictoria.fieldguide.vic.fork.ui.fragments.WebFragment;
import au.com.museumvictoria.fieldguide.vic.fork.util.Utilities;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements SpeciesItemListFragment.Callbacks,
    SpeciesGroupListFragment.Callback, GroupFragment.Callback {

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
    public final String title;
    @Nullable
    public final String subtitle;

    public Screen(String title, @Nullable String subtitle) {
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
    setFragment(new SpeciesGroupListFragment(), null);

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
      case R.id.menu_settings:
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
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
  public void onGroupSelected(String groupName) {
    Log.i(TAG, "Group selected: " + groupName);
    Bundle arguments = new Bundle();
    arguments.putString("speciesgroup", groupName);

    backStackScreens.put("GROUP", new Screen(groupName, null));
    Fragment fragment = new GroupFragment();
    fragment.setArguments(arguments);
    setFragment(fragment, "GROUP");
  }

  @Override
  public void onSpeciesSelected(String speciesId, String name, @Nullable String subname) {
    Log.i(TAG, "Species selected: " + speciesId);

    //Intent detailIntent = new Intent(this, SpeciesItemDetailActivity.class);
    //detailIntent.putExtra(Utilities.SPECIES_IDENTIFIER, speciesId);
    //startActivity(detailIntent);
    Bundle arguments = new Bundle();
    arguments.putString(Utilities.SPECIES_IDENTIFIER, speciesId);
    SpeciesItemDetailFragment fragment = new SpeciesItemDetailFragment();
    fragment.setArguments(arguments);
    backStackScreens.put("SPECIES", new Screen(name, subname));
    setFragment(fragment, "SPECIES");
  }

  private void handleIntent(Intent intent) {
    Log.i(TAG, "Handling intent: " + intent);
    if (intent.getAction() == null) {
      return;
    }
    switch (intent.getAction()) {
      case Intent.ACTION_VIEW:
        // TODO: Handle this (could occur when a search suggestion is clicked).
        break;
      case Intent.ACTION_SEARCH:
        backStackScreens.put("SEARCH", new Screen("Search", null));
        Fragment searchFragment = new SearchFragment();
        searchFragment.setArguments(intent.getExtras());
        setFragment(searchFragment, "SEARCH");
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

  public void displayInfo(View view) {
    switch (view.getId()) {
    case R.id.heading_distribution:

      Bundle extras = new Bundle();
      extras.putString("pagetitle", "About Distribution");
      extras.putString("pageurl", "aboutdistribution");
      Intent infoIntent = new Intent(this, DisplayInfoActivity.class);
      infoIntent.putExtras(extras);
      startActivity(infoIntent);

      break;

    case R.id.heading_threatened_status:

      Bundle extras1 = new Bundle();
      extras1.putString("pagetitle", "About Threatened Status");
      extras1.putString("pageurl", "aboutthreatenedstatus");
      Intent infoIntent1 = new Intent(this, DisplayInfoActivity.class);
      infoIntent1.putExtras(extras1);
      startActivity(infoIntent1);

      break;

    default:
      break;
    }
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    // Serialize the current tab position.
    Log.d(TAG, "onSaveInstanceState");
    outState.putInt(STATE_SELECTED_NAVIGATION_ITEM,
        getSupportActionBar().getSelectedNavigationIndex());
  }

  @Override
  protected void onRestoreInstanceState(Bundle savedInstanceState) {
    // Restore the previously serialized current tab position.
    Log.d(TAG, "onRestoreInstanceState");
    if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
      getSupportActionBar().setSelectedNavigationItem(
          savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
    }
  }

  /**
   * Callback method from {@link SpeciesItemListFragment.Callbacks} indicating
   * that the item with the given ID was selected.
   */
  @Override
  public void onItemSelected(String inputid) {
    //Log.d(TAG, "Selected ID: " + inputid);

    //if (inputid.startsWith(SpeciesItemListFragment.LIST_TYPE_GROUP + "__")) {
    //  Bundle arguments = new Bundle();
    //  arguments.putString("speciesgroup",
    //      inputid.substring(SpeciesItemListFragment.LIST_TYPE_GROUP
    //          .length() + 2));
    //  Fragment frag = SpeciesListFragment.newInstance(true, arguments);
    //  FragmentTransaction ft = getSupportFragmentManager()
    //      .beginTransaction();
    //  ft.replace(R.id.basecontainer, frag, "speciesgroups");
    //  ft.addToBackStack("speciesgroups");
    //  ft.commit();

    //} else {
    //  String id = inputid
    //      .substring(SpeciesItemListFragment.LIST_TYPE_ALPHABETICAL
    //          .length() + 2);

    //  if (findViewById(R.id.item_detail_container) != null) {
    //    // In two-pane mode, show the detail view in this activity by
    //    // adding or replacing the detail fragment using a
    //    // fragment transaction.
    //    Bundle arguments = new Bundle();
    //    arguments.putString(Utilities.SPECIES_IDENTIFIER, id);
    //    SpeciesItemDetailFragment fragment = new SpeciesItemDetailFragment();
    //    fragment.setArguments(arguments);
    //    getSupportFragmentManager().beginTransaction()
    //        .replace(R.id.item_detail_container, fragment).commit();

    //  } else {
    //    // In single-pane mode, simply start the detail activity
    //    // for the selected item ID.
    //    Intent detailIntent = new Intent(this,
    //        SpeciesItemDetailActivity.class);
    //    detailIntent.putExtra(Utilities.SPECIES_IDENTIFIER, id);
    //    startActivity(detailIntent);
    //  }
    //}
  }
}
