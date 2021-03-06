package org.flowgrid.emojic.logic;

import java.util.HashMap;

import org.kobjects.emoji.Emoji;
import org.kobjects.emoji.Emoji.Property;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.widget.TextView;

public interface Node {
  public abstract boolean matches(Emoji emoji);
  public abstract boolean valid(Emoji emoji);
  public abstract void toSpannable(TextView view, SpannableStringBuilder builder,
      HashMap<Property, Integer> drawablemap, Object parent);
}
