package org.flowgrid.emojic.logic;

import java.util.HashMap;

import org.kobjects.emoji.Emoji;
import org.kobjects.emoji.Emoji.Property;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;

public class NotNode implements Node {
  
  private Node child;
  static final ForegroundColorSpan RED_SPAN = new ForegroundColorSpan(0xffed6c30); 
  
  public NotNode(Node node) {
    this.child = node;
  }
  
  public String toString() {
    return "\u00ac" + child.toString();
  }

  @Override
  public boolean matches(Emoji emoji) {
    return !child.matches(emoji);
  }

  @Override
  public boolean valid(Emoji emoji) {
    return child.valid(emoji);
  }

  @Override
  public void toSpannable(Context context, SpannableStringBuilder builder,
      HashMap<Property, Integer> drawablemap, Object parent) {
    builder.append("\u00ac");
    builder.setSpan(RED_SPAN, builder.length() - 1, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    child.toSpannable(context, builder, drawablemap, this);
  }
}
