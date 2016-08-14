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

    public Screen(CharSequence title, @Nullable CharSequence subtitle, Fragment fragment,
        String name) {
      this.title = title;
      this.subtitle = subtitle;
      this.fragment = fragment;
      this.name = name;
    }
  };

  private Screen homeScreen;

  private boolean dualPane;

  @Nullable
  private Screen leaf;

  public void pushFragment(CharSequence title, @Nullable CharSequence subtitle, Fragment fragment,
      @Nullable String parent) {
    while (leaf != null && leaf.name != parent) {
      // pop
    }
    if (leaf == null && parent != null) {
      throw new RuntimeException("Parent screen not found: " + parent);
    }
    leaf = new Screen(title, subtitle, fragment, parent);
    // push
  }

  public void onBackPressed() {
    // pop
  }
}
