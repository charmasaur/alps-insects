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
  private static final String SCREEN_SLAVE_PREFIX_KEY = "SLAVE_";
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
    public final boolean slave;
    public final int id;

    public Screen(CharSequence title, @Nullable CharSequence subtitle, String fragmentTag,
        String name, boolean slave, int id) {
      this.title = title;
      this.subtitle = subtitle;
      this.fragmentTag = fragmentTag;
      this.name = name;
      this.slave = slave;
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

  public void pushFragment(CharSequence title, @Nullable CharSequence subtitle, Fragment fragment,
      String name, @Nullable String parent, boolean slave) {
    fragmentManager.executePendingTransactions();

    if (root == null) {
      if (parent != null) {
        throw new RuntimeException("Root must be added first");
      }
      if (slave) {
        throw new RuntimeException("Root must be a master");
      }
      FragmentTransaction transaction = fragmentManager.beginTransaction();
      String fragmentTag = "" + (++urgh);
      transaction.add(containerViewId, fragment, fragmentTag);
      int id = transaction.commit();
      root = new Screen(title, subtitle, fragmentTag, name, slave, id);
      return;
    }

    if (parent == null) {
      // The parent is either the current leaf, or whatever is before that if the current leaf
      // matches the new thing.
      parent = getLeaf().name;
      if (parent == name) {
        // If getFromBack(1) is null then we must have tried to add the root to the root. Crashing
        // is fine in that case 'cause something weird is happening.
        parent = getFromBack(1).name;
      }
    }

    // If necessary, pop from the backstack until we find the parent.
    int index = 0;
    while (getFromBack(index).name != parent) {
      ++index;
      fragmentManager.popBackStack();
    }
    FragmentTransaction transaction = fragmentManager.beginTransaction();

    if (slave) {
      if (getFromBack(index).slave) {
        throw new RuntimeException("Slave cannot have a slave parent");
      }
    }

    if (dualPane) {
      if (!slave) {
        // We need to hide the parent, and if that's a slave we need to hide its parent too.
        Screen parentScreen = getFromBack(index);
        transaction.hide(fragmentManager.findFragmentByTag(parentScreen.fragmentTag));
        if (parentScreen.slave) {
          transaction.hide(fragmentManager.findFragmentByTag(getFromBack(index + 1).fragmentTag));
        }
      }
      // TODO: It would be nice if when we have [master, slave] showing, back removes both of them.
      // TODO: Or perhaps we should have up vs back. Then back would use the current behaviour
      // (undo the last thing), and up would go back to the next level of the tree...
    } else {
      transaction.hide(fragmentManager.findFragmentByTag(getFromBack(index).fragmentTag));
    }

    String fragmentTag = "" + (++urgh);
    transaction.add(containerViewId, fragment, fragmentTag);
    transaction.addToBackStack(null);
    int id = transaction.commit();
    Screen newLeaf = new Screen(title, subtitle, fragmentTag, name, slave, id);
    backStackScreens.put(id, newLeaf);
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
    outState.putBoolean(SCREEN_SLAVE_PREFIX_KEY + i, screen.slave);
    outState.putString(SCREEN_NAME_PREFIX_KEY + i, screen.name);
  }

  private Screen getScreen(Bundle bundle, int i) {
    Integer id = bundle.getInt(SCREEN_ID_PREFIX_KEY + i);
    CharSequence title = bundle.getCharSequence(SCREEN_TITLE_PREFIX_KEY + i);
    CharSequence subtitle = bundle.getCharSequence(SCREEN_SUBTITLE_PREFIX_KEY + i);
    String fragmentTag = bundle.getString(SCREEN_FRAGMENT_TAG_PREFIX_KEY + i);
    Boolean slave = bundle.getBoolean(SCREEN_SLAVE_PREFIX_KEY + i);
    String name = bundle.getString(SCREEN_NAME_PREFIX_KEY + i);
    return new Screen(title, subtitle, fragmentTag, name, slave, id);
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
