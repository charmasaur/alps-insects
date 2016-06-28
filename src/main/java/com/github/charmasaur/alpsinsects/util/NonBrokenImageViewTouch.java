package com.github.charmasaur.alpsinsects.util;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * An {@link ImageViewTouch} that stops the parent from intercepting touch events when the image
 * can be panned.
 */
public final class NonBrokenImageViewTouch extends ImageViewTouch {
  public NonBrokenImageViewTouch(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public NonBrokenImageViewTouch(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    if (getScale() > 1.f && getParent() != null) {
      getParent().requestDisallowInterceptTouchEvent(true);
    }
    return super.onTouchEvent(event);
  }
};
