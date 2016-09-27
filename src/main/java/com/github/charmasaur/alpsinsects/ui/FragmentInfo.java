package com.github.charmasaur.alpsinsects.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;

public class FragmentInfo {
  private static final String TITLE_KEY = "TITLE";
  private static final String SUBTITLE_KEY = "SUBTITLE";
  private static final String ID_KEY = "ID";

  private final CharSequence title;
  @Nullable private final CharSequence subtitle;
  private final String id;

  private FragmentInfo(CharSequence title, @Nullable CharSequence subtitle, String id) {
    this.title = title;
    this.subtitle = subtitle;
    this.id = id;
    if (id == null) {
      throw new RuntimeException("Null ID not allowed");
    }
  }

  public CharSequence getTitle() {
    return title;
  }

  @Nullable
  public CharSequence getSubtitle() {
    return subtitle;
  }

  public String getId() {
    return id;
  }

  public void save(Bundle bundle) {
    bundle.putCharSequence(TITLE_KEY, title);
    bundle.putCharSequence(SUBTITLE_KEY, subtitle);
    bundle.putString(ID_KEY, id);
  }

  public static FragmentInfo load(Bundle bundle) {
    CharSequence title = bundle.getCharSequence(TITLE_KEY, "Title");
    CharSequence subtitle = bundle.getCharSequence(SUBTITLE_KEY, null);
    String id = bundle.getString(ID_KEY, "");
    return new FragmentInfo(title, subtitle, id);
  }

  public static FragmentInfo fromHeading(CharSequence title, @Nullable CharSequence subtitle) {
    return new FragmentInfo(title, subtitle, title.toString());
  }

  public static FragmentInfo fromHeadingAndId(CharSequence title, @Nullable CharSequence subtitle,
      String id) {
    return new FragmentInfo(title, subtitle, id);
  }
}
