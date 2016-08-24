package com.github.charmasaur.alpsinsects.ui;

import android.os.Bundle;
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

  private static final String FRAGMENT_COUNT_KEY = "FRAGMENT_COUNT";
  private static final String SCREEN_ID_PREFIX_KEY = "ID_";
  private static final String SCREEN_TITLE_PREFIX_KEY = "TITLE_";
  private static final String SCREEN_SUBTITLE_PREFIX_KEY = "SUBTITLE_";
  private static final String SCREEN_FRAGMENT_TAG_PREFIX_KEY = "TAG_";
  private static final String SCREEN_NAME_PREFIX_KEY = "NAME_";

  /**
   * Represents a screen that can be showing to the user.
   */
  private static final class Screen {
    public final CharSequence title;
    @Nullable
    public final CharSequence subtitle;
    public final String fragmentTag;
    public final String name;
    public final int id;

    public Screen(CharSequence title, @Nullable CharSequence subtitle, String fragmentTag,
        String name, int id) {
      this.title = title;
      this.subtitle = subtitle;
      this.fragmentTag = fragmentTag;
      this.name = name;
      this.id = id;
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

    if (root == null) {
      if (parent != null) {
        throw new RuntimeException("Root must be added first");
      }
      FragmentTransaction transaction = fragmentManager.beginTransaction();
      String fragmentTag = "" + (++urgh);
      transaction.add(containerViewId, fragment, fragmentTag);
      int id = transaction.commit();
      root = new Screen(title, subtitle, fragmentTag, name, id);
      return;
    }

    // If necessary, backtrack through the stack until we find the parent.
    int index = 0;
    if (parent != null) {
      while (getFromBack(index).name != parent) {
        ++index;
        fragmentManager.popBackStack();
      }
    }
    FragmentTransaction transaction = fragmentManager.beginTransaction();
    Screen toHide = getFromBack(index + numViews - 1);
    if (toHide != null) {
      transaction.hide(fragmentManager.findFragmentByTag(toHide.fragmentTag));
    }
    String fragmentTag = "" + (++urgh);
    transaction.add(containerViewId, fragment, fragmentTag);
    transaction.addToBackStack(null);
    int id = transaction.commit();
    Screen newLeaf = new Screen(title, subtitle, fragmentTag, name, id);
    backStackScreens.put(id, newLeaf);
  }

  public void save(Bundle outState) {
    outState.putInt(FRAGMENT_COUNT_KEY, backStackScreens.size());
    int i = 0;
    for (Map.Entry<Integer, Screen> screen : backStackScreens.entrySet()) {
      putScreen(screen.getValue(), outState, i);
      ++i;
    }
    putScreen(root, outState, i);
  }

  public void load(Bundle bundle) {
    int count = bundle.getInt(FRAGMENT_COUNT_KEY);
    for (int i = 0; i < count; ++i) {
      Screen screen = getScreen(bundle, i);
      backStackScreens.put(screen.id, screen);
    }
    root = getScreen(bundle, count);
    updateScreen();
  }

  private void putScreen(Screen screen, Bundle outState, int i) {
    outState.putInt(SCREEN_ID_PREFIX_KEY + i, screen.id);
    outState.putCharSequence(SCREEN_TITLE_PREFIX_KEY + i, screen.title);
    outState.putCharSequence(SCREEN_SUBTITLE_PREFIX_KEY + i, screen.subtitle);
    outState.putString(SCREEN_FRAGMENT_TAG_PREFIX_KEY + i, screen.fragmentTag);
    outState.putString(SCREEN_NAME_PREFIX_KEY + i, screen.name);
  }

  private Screen getScreen(Bundle bundle, int i) {
    Integer id = bundle.getInt(SCREEN_ID_PREFIX_KEY + i);
    CharSequence title = bundle.getCharSequence(SCREEN_TITLE_PREFIX_KEY + i);
    CharSequence subtitle = bundle.getCharSequence(SCREEN_SUBTITLE_PREFIX_KEY + i);
    String fragmentTag = bundle.getString(SCREEN_FRAGMENT_TAG_PREFIX_KEY + i);
    String name = bundle.getString(SCREEN_NAME_PREFIX_KEY + i);
    return new Screen(title, subtitle, fragmentTag, name, id);
  }

  private void updateScreen() {
    Screen leaf = getLeaf();
    actionBar.setTitle(leaf.title);
    actionBar.setSubtitle(leaf.subtitle);
    actionBar.setDisplayHomeAsUpEnabled(leaf != root);
  }

  private Screen getLeaf() {
    return getFromBack(0);
  }

  private Screen getFromBack(int count) {
    int backStackSize = fragmentManager.getBackStackEntryCount();
    Log.i(TAG, "Size: " + backStackSize);
    if (count > backStackSize) {
      return null;
    }
    if (count == backStackSize) {
      return root;
    }
    return backStackScreens
        .get(fragmentManager.getBackStackEntryAt(backStackSize - count - 1).getId());
  }

  private final FragmentManager.OnBackStackChangedListener backStackChangedListener =
      new FragmentManager.OnBackStackChangedListener() {
    @Override
    public void onBackStackChanged() {
      Log.i(TAG, "Back stack changed: " + fragmentManager.getBackStackEntryCount());
      updateScreen();
    }
  };
}
