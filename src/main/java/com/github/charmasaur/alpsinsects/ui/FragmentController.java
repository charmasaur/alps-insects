package com.github.charmasaur.alpsinsects.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashMap;
import java.util.Map;

public class FragmentController {
  private static final String TAG = FragmentController.class.getSimpleName();

  private static final String FRAGMENT_TOTAL_FRAGMENTS_CREATED_KEY =
    "FRAGMENT_TOTAL_FRAGMENTS_CREATED";
  private static final String FRAGMENT_COUNT_KEY = "FRAGMENT_COUNT";
  private static final String SCREEN_ID_PREFIX_KEY = "ID_";
  private static final String SCREEN_INFO_PREFIX_KEY = "INFO_";
  private static final String SCREEN_FRAGMENT_TAG_PREFIX_KEY = "TAG_";
  private static final String SCREEN_LEVEL_PREFIX_KEY = "LEVEL_";
  private static final String SCREEN_PARENT_TAG_PREFIX_KEY = "PARENT_TAG_";

  /**
   * Represents a screen that can be showing to the user.
   */
  private static final class Screen {
    public final FragmentInfo fragmentInfo;
    public final String fragmentTag;
    public final int level;
    public final int id;
    public final String parentTag;

    public Screen(FragmentInfo fragmentInfo, String fragmentTag, int level, String parentTag,
        int id) {
      this.fragmentInfo = fragmentInfo;
      this.fragmentTag = fragmentTag;
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
  private final int containerViewId;
  private final boolean dualPane;

  @Nullable
  private Screen root;

  private int totalFragmentsCreated;

  public FragmentController(FragmentManager fragmentManager, int containerViewId,
      boolean dualPane) {
    this.fragmentManager = fragmentManager;
    this.containerViewId = containerViewId;
    this.dualPane = dualPane;
  }

  /**
   * @param level the level of this fragment. Even for a master, that plus one if it's the master's
   *     slave.
   */
  public void pushFragment(FragmentInfo fragmentInfo, Fragment fragment, int level) {
    fragmentManager.executePendingTransactions();

    if (root == null) {
      if (isSlave(level)) {
        throw new RuntimeException("Root must be a master");
      }
      FragmentTransaction transaction = fragmentManager.beginTransaction();
      String fragmentTag = nextTag();
      transaction.add(containerViewId, fragment, fragmentTag);
      int id = transaction.commit();
      root = new Screen(fragmentInfo, fragmentTag, level, null, id);
      return;
    }

    Screen leaf = getLeaf();

    // This doesn't really belong here (should be in MainActivity, since FragmentInfo should be
    // opaque to this class). But it's a bit simpler, and doesn't do any harm.
    if (fragmentInfo.getId().equals(leaf.fragmentInfo.getId())) {
      return;
    }

    String parentTag;
    if (isSlave(level)) {
      if (isSlave(leaf.level)) {
        // This and the leaf are siblings (they might not have the same level, but that's OK --
        // actual level is only used for determining how far up "up" means).
        parentTag = leaf.parentTag;
      } else {
        // This is the leaf's child.
        parentTag = leaf.fragmentTag;
      }
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

    String fragmentTag = nextTag();
    transaction.add(containerViewId, fragment, fragmentTag);
    transaction.addToBackStack(null);
    int id = transaction.commit();
    Screen newLeaf = new Screen(fragmentInfo, fragmentTag, level, parentTag, id);
    backStackScreens.put(id, newLeaf);
  }

  public void navigateUp() {
    fragmentManager.executePendingTransactions();

    int curLevel = getLeaf().level;

    // Pop from the backstack until the level changes (in dual pane slaves and their masters are
    // considered to have the same level), or until we hit the root.
    int index = 0;
    while (true) {
      Screen newLeaf = getFromBack(index);
      if (newLeaf == root || !compareLevels(newLeaf.level, curLevel)) {
        break;
      }
      fragmentManager.popBackStack();
      ++index;
    }
  }

  public void save(Bundle outState) {
    outState.putInt(FRAGMENT_TOTAL_FRAGMENTS_CREATED_KEY, totalFragmentsCreated);
    outState.putInt(FRAGMENT_COUNT_KEY, backStackScreens.size());
    int i = 0;
    for (Map.Entry<Integer, Screen> screen : backStackScreens.entrySet()) {
      putScreen(screen.getValue(), outState, i);
      ++i;
    }
    putScreen(root, outState, i);
  }

  public void load(Bundle bundle) {
    totalFragmentsCreated = bundle.getInt(FRAGMENT_TOTAL_FRAGMENTS_CREATED_KEY);
    int count = bundle.getInt(FRAGMENT_COUNT_KEY);
    for (int i = 0; i < count; ++i) {
      Screen screen = getScreen(bundle, i);
      backStackScreens.put(screen.id, screen);
    }
    root = getScreen(bundle, count);
  }

  public FragmentInfo getLeafInfo() {
    return getLeaf().fragmentInfo;
  }

  public FragmentInfo getRootInfo() {
    return root.fragmentInfo;
  }

  private void putScreen(Screen screen, Bundle outState, int i) {
    outState.putInt(SCREEN_ID_PREFIX_KEY + i, screen.id);
    Bundle infoBundle = new Bundle();
    screen.fragmentInfo.save(infoBundle);
    outState.putBundle(SCREEN_INFO_PREFIX_KEY + i, infoBundle);
    outState.putString(SCREEN_FRAGMENT_TAG_PREFIX_KEY + i, screen.fragmentTag);
    outState.putInt(SCREEN_LEVEL_PREFIX_KEY + i, screen.level);
    outState.putString(SCREEN_PARENT_TAG_PREFIX_KEY + i, screen.parentTag);
  }

  private Screen getScreen(Bundle bundle, int i) {
    Integer id = bundle.getInt(SCREEN_ID_PREFIX_KEY + i);
    FragmentInfo fragmentInfo = FragmentInfo.load(bundle.getBundle(SCREEN_INFO_PREFIX_KEY + i));
    String fragmentTag = bundle.getString(SCREEN_FRAGMENT_TAG_PREFIX_KEY + i);
    Integer level = bundle.getInt(SCREEN_LEVEL_PREFIX_KEY + i);
    String parentTag = bundle.getString(SCREEN_PARENT_TAG_PREFIX_KEY + i);
    return new Screen(fragmentInfo, fragmentTag, level, parentTag, id);
  }

  private Screen getLeaf() {
    return getFromBack(0);
  }

  private Screen getFromBack(int count) {
    int backStackSize = fragmentManager.getBackStackEntryCount();
    if (count > backStackSize) {
      return null;
    }
    if (count == backStackSize) {
      return root;
    }
    return backStackScreens
        .get(fragmentManager.getBackStackEntryAt(backStackSize - count - 1).getId());
  }

  private String nextTag() {
    return "" + (++totalFragmentsCreated);
  }

  private boolean compareLevels(int a, int b) {
    return dualPane ? a / 2 == b / 2 : a == b;
  }

  private static boolean isSlave(int level) {
    return level % 2 == 1;
  }
}
