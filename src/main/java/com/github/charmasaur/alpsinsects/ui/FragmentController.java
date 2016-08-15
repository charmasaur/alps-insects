package com.github.charmasaur.alpsinsects.ui;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.ViewGroup;

public class FragmentController {
  /**
   * Represents a screen that can be showing to the user.
   */
  private static final class Screen {
    public final CharSequence title;
    @Nullable
    public final CharSequence subtitle;
    public final Fragment fragment;
    public final String name;
    @Nullable
    public final Screen parent;

    public Screen(CharSequence title, @Nullable CharSequence subtitle, Fragment fragment,
        String name, @Nullable Screen parent) {
      this.title = title;
      this.subtitle = subtitle;
      this.fragment = fragment;
      this.name = name;
      this.parent = parent;
    }
  };

  private final FragmentManager fragmentManager;
  private final int containerViewId;
  private final int numViews;

  @Nullable
  private Screen leaf;

  public FragmentController(FragmentManager fragmentManager, int containerViewId, int numViews) {
    this.fragmentManager = fragmentManager;
    this.containerViewId = containerViewId;
    this.numViews = numViews;
  }

  public void pushFragment(CharSequence title, @Nullable CharSequence subtitle, Fragment fragment,
      String name, @Nullable String parent) {
    while (leaf != null && leaf.name != parent) {
      fragmentManager.popBackStack();
      leaf = leaf.parent;
    }
    if (leaf == null && parent != null) {
      throw new RuntimeException("Parent screen not found: " + parent);
    }
    leaf = new Screen(title, subtitle, fragment, name, leaf);
    FragmentTransaction transaction = fragmentManager.beginTransaction();
    if (leaf.parent != null) {
      transaction.hide(leaf.parent.fragment);
    }
    transaction.add(containerViewId, fragment);
    transaction.addToBackStack("it");
    transaction.commit();
  }

  // TODO: Now need to listen to changes (so we can update leaf, and then update the corresponding
  // title and subtitle). Maybe we don't need to store leaf at all, and can just look at the top
  // back stack entry or something?
  //
  // TODO: Note that the title and subtitle to show are just leaf's. Nice!
}
