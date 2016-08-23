package com.github.charmasaur.alpsinsects.ui;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashMap;
import java.util.Map;

public class FragmentController {
  private static final String TAG = FragmentController.class.getSimpleName();
  /**
   * Represents a screen that can be showing to the user.
   */
  private static final class Screen {
    public final CharSequence title;
    @Nullable
    public final CharSequence subtitle;
    public final Fragment fragment;
    public final String name;

    private int id;

    public Screen(CharSequence title, @Nullable CharSequence subtitle, Fragment fragment,
        String name) {
      this.title = title;
      this.subtitle = subtitle;
      this.fragment = fragment;
      this.name = name;
    }

    public void setId(int id) {
      this.id = id;
    }

    public int getId() {
      return id;
    }
  };

  private final Map<Integer, Screen> backStackScreens = new HashMap<>();

  private final FragmentManager fragmentManager;
  private final ActionBar actionBar;
  private final int containerViewId;
  private final int numViews;

  @Nullable
  private Screen root;

  private int urgh;

  public FragmentController(FragmentManager fragmentManager, int containerViewId, int numViews,
      ActionBar actionBar) {
    this.fragmentManager = fragmentManager;
    this.containerViewId = containerViewId;
    this.numViews = numViews;
    this.actionBar = actionBar;

    fragmentManager.addOnBackStackChangedListener(backStackChangedListener);
  }

  public void pushFragment(CharSequence title, @Nullable CharSequence subtitle, Fragment fragment,
      String name, @Nullable String parent) {
    fragmentManager.executePendingTransactions();

    if (parent == null) {
      if (root != null) {
        throw new RuntimeException("Only one parentless fragment allowed");
      }
      root = new Screen(title, subtitle, fragment, name);
      FragmentTransaction transaction = fragmentManager.beginTransaction();
      transaction.add(containerViewId, fragment, "" + (++urgh));
      transaction.commit();
      return;
    }

    // Otherwise, backtrack through the stack until we find the parent.
    Screen leaf = null;
    while (true) {
      leaf = getLeaf();
      if (leaf.name == parent) {
        break;
      }
      // TODO: This looks pretty gross... Maybe we should just look back through the back stack entries and queue up pops.
      fragmentManager.popBackStackImmediate();
    }
    Screen newLeaf = new Screen(title, subtitle, fragment, name);
    FragmentTransaction transaction = fragmentManager.beginTransaction();
    // TODO: Tidy.
    int idx = fragmentManager.getBackStackEntryCount() - numViews;
    Screen toHide = null;
    if (idx == -1) {
      toHide = root;
    } else if (idx >= 0) {
      toHide = backStackScreens.get(fragmentManager.getBackStackEntryAt(idx).getId());
    }
    if (toHide != null) {
      transaction.hide(toHide.fragment);
    }
    transaction.add(containerViewId, fragment, "" + (++urgh));
    transaction.addToBackStack(null);
    int id = transaction.commit();
    newLeaf.setId(id);
    backStackScreens.put(id, newLeaf);
  }

  private void updateScreen() {
    Screen leaf = getLeaf();
    actionBar.setTitle(leaf.title);
    actionBar.setSubtitle(leaf.subtitle);
    actionBar.setDisplayHomeAsUpEnabled(leaf != root);
    // TODO: Options menu.
  }

  //  Screen currentScreen;
  //  int backStackSize = fragmentManager.getBackStackEntryCount();
  //  if (backStackSize == 0) {
  //    // Root screen is now showing.
  //    leaf = root;
  //  } else {
  //    currentScreen = backStackScreens.get(
  //        getSupportFragmentManager().getBackStackEntryAt(backStackSize - 1).getName());
  //    if (showOptions) {
  //      showOptions = false;
  //      supportInvalidateOptionsMenu();
  //    }
  //  }
  //  getSupportActionBar().setTitle(currentScreen.title);
  //  getSupportActionBar().setSubtitle(currentScreen.subtitle);
  //  getSupportActionBar().setDisplayHomeAsUpEnabled(backStackSize != 0);
  //}

  private Screen getLeaf() {
    int backStackSize = fragmentManager.getBackStackEntryCount();
    if (backStackSize == 0) {
      return root;
    }
    return backStackScreens.get(fragmentManager.getBackStackEntryAt(backStackSize - 1).getId());
  }

  private final FragmentManager.OnBackStackChangedListener backStackChangedListener =
      new FragmentManager.OnBackStackChangedListener() {
    @Override
    public void onBackStackChanged() {
      Log.i(TAG, "Back stack changed: " + fragmentManager.getBackStackEntryCount());
      updateScreen();
    }
  };


  // TODO: Now need to listen to changes (so we can update leaf, and then update the corresponding
  // title and subtitle). Maybe we don't need to store leaf at all, and can just look at the top
  // back stack entry or something?
  //
  // TODO: Note that the title and subtitle to show are just leaf's. Nice!
}
