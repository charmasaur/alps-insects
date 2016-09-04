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

  private static final String FRAGMENT_URGH_KEY = "FRAGMENT_URGH";
  private static final String FRAGMENT_COUNT_KEY = "FRAGMENT_COUNT";
  private static final String SCREEN_ID_PREFIX_KEY = "ID_";
  private static final String SCREEN_TITLE_PREFIX_KEY = "TITLE_";
  private static final String SCREEN_SUBTITLE_PREFIX_KEY = "SUBTITLE_";
  private static final String SCREEN_FRAGMENT_TAG_PREFIX_KEY = "TAG_";
  private static final String SCREEN_LEVEL_PREFIX_KEY = "LEVEL_";
  private static final String SCREEN_PARENT_TAG_PREFIX_KEY = "PARENT_TAG_";
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
    public final int level;
    public final int id;
    public final String parentTag;

    public Screen(CharSequence title, @Nullable CharSequence subtitle, String fragmentTag,
        String name, int level, String parentTag, int id) {
      this.title = title;
      this.subtitle = subtitle;
      this.fragmentTag = fragmentTag;
      this.name = name;
      this.level = level;
      this.parentTag = parentTag;
      this.id = id;
    }
  };

  /**
   * Stores information about screens in the back stack. To avoid this growing indefinitely we have
   * to rely on the fragment manager reusing IDs, which is a bit of a shame. But it's only a few
   * bytes each time, so it's not exactly the worst leak in the world.
   */
  private final Map<Integer, Screen> backStackScreens = new HashMap<>();

  private final FragmentManager fragmentManager;
  private final ActionBar actionBar;
  private final int containerViewId;
  private final boolean dualPane;

  @Nullable
  private Screen root;

  private int urgh;

  public FragmentController(FragmentManager fragmentManager, int containerViewId, boolean dualPane,
      ActionBar actionBar) {
    this.fragmentManager = fragmentManager;
    this.containerViewId = containerViewId;
    this.dualPane = dualPane;
    this.actionBar = actionBar;

    fragmentManager.addOnBackStackChangedListener(backStackChangedListener);
  }

  /**
   * @param level the level of this fragment. Even for a master, that plus one if it's the master's
   *     slave.
   */
  public void pushFragment(CharSequence title, @Nullable CharSequence subtitle, Fragment fragment,
      String name, int level) {
    fragmentManager.executePendingTransactions();

    if (root == null) {
      if (isSlave(level)) {
        throw new RuntimeException("Root must be a master");
      }
      FragmentTransaction transaction = fragmentManager.beginTransaction();
      String fragmentTag = "" + (++urgh);
      transaction.add(containerViewId, fragment, fragmentTag);
      int id = transaction.commit();
      root = new Screen(title, subtitle, fragmentTag, name, level, null, id);
      return;
    }

    Screen leaf = getLeaf();
    String parentTag;
    if (level == leaf.level) {
      parentTag = leaf.parentTag;
    } else if (isSlave(level)) {
      parentTag = leaf.fragmentTag;
    } else {
      parentTag = null;
    }

    FragmentTransaction transaction = fragmentManager.beginTransaction();

    if (dualPane) {
      if (isSlave(level)) {
        // If there's already a slave we need to hide that.
        if (isSlave(leaf.level)) {
          transaction.hide(fragmentManager.findFragmentByTag(leaf.fragmentTag));
        }
      } else {
        // We need to hide the current leaf, and if that's a slave we need to hide its parent too.
        transaction.hide(fragmentManager.findFragmentByTag(leaf.fragmentTag));
        if (isSlave(leaf.level)) {
          transaction.hide(fragmentManager.findFragmentByTag(leaf.parentTag));
        }
      }
    } else {
      transaction.hide(fragmentManager.findFragmentByTag(leaf.fragmentTag));
    }

    String fragmentTag = "" + (++urgh);
    transaction.add(containerViewId, fragment, fragmentTag);
    transaction.addToBackStack(null);
    int id = transaction.commit();
    Screen newLeaf = new Screen(title, subtitle, fragmentTag, name, level, parentTag, id);
    backStackScreens.put(id, newLeaf);
  }

  public void onUpPressed() {
    fragmentManager.executePendingTransactions();

    int curLevel = getLeaf().level;
    // Pop from the backstack until the level changes (where slaves and their masters are
    // considered to have the same level for this).
    int index = 0;
    while (getFromBack(index).level / 2 == curLevel / 2) {
      ++index;
      fragmentManager.popBackStack();
    }
  }

  public static boolean isSlave(int level) {
    return level % 2 == 1;
  }

  public void save(Bundle outState) {
    outState.putInt(FRAGMENT_URGH_KEY, urgh);
    outState.putInt(FRAGMENT_COUNT_KEY, backStackScreens.size());
    int i = 0;
    for (Map.Entry<Integer, Screen> screen : backStackScreens.entrySet()) {
      putScreen(screen.getValue(), outState, i);
      ++i;
    }
    putScreen(root, outState, i);
  }

  public void load(Bundle bundle) {
    urgh = bundle.getInt(FRAGMENT_URGH_KEY);
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
    outState.putInt(SCREEN_LEVEL_PREFIX_KEY + i, screen.level);
    outState.putString(SCREEN_PARENT_TAG_PREFIX_KEY + i, screen.parentTag);
    outState.putString(SCREEN_NAME_PREFIX_KEY + i, screen.name);
  }

  private Screen getScreen(Bundle bundle, int i) {
    Integer id = bundle.getInt(SCREEN_ID_PREFIX_KEY + i);
    CharSequence title = bundle.getCharSequence(SCREEN_TITLE_PREFIX_KEY + i);
    CharSequence subtitle = bundle.getCharSequence(SCREEN_SUBTITLE_PREFIX_KEY + i);
    String fragmentTag = bundle.getString(SCREEN_FRAGMENT_TAG_PREFIX_KEY + i);
    Integer level = bundle.getInt(SCREEN_LEVEL_PREFIX_KEY + i);
    String parentTag = bundle.getString(SCREEN_PARENT_TAG_PREFIX_KEY + i);
    String name = bundle.getString(SCREEN_NAME_PREFIX_KEY + i);
    return new Screen(title, subtitle, fragmentTag, name, level, parentTag, id);
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
