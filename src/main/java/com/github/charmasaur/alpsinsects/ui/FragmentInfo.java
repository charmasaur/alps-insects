package com.github.charmasaur.alpsinsects.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;

public class FragmentInfo {
  private static final String TITLE_KEY = "TITLE";
  private static final String SUBTITLE_KEY = "SUBTITLE";

  private final CharSequence title;
  @Nullable private final CharSequence subtitle;

  public FragmentInfo(CharSequence title, @Nullable CharSequence subtitle) {
    this.title = title;
    this.subtitle = subtitle;
  }

  public CharSequence getTitle() {
    return title;
  }

  @Nullable
  public CharSequence getSubtitle() {
    return subtitle;
  }

  public String getId() {
    return title.toString();
  }

  public void save(Bundle bundle) {
    bundle.putCharSequence(TITLE_KEY, title);
    bundle.putCharSequence(SUBTITLE_KEY, subtitle);
  }

  public static FragmentInfo load(Bundle bundle) {
    CharSequence title = bundle.getCharSequence(TITLE_KEY, "Title");
    CharSequence subtitle = bundle.getCharSequence(SUBTITLE_KEY, null);
    return new FragmentInfo(title, subtitle);
  }
}
