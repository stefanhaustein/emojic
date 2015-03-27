package org.flowgrid.emojic.logic;

import java.util.HashMap;

import org.kobjects.emoji.Emoji;
import org.kobjects.emoji.Emoji.Property;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;

public class CombinationNode implements Node {
  
  static final ForegroundColorSpan GREEN_SPAN = new ForegroundColorSpan(0xff757f3f); 
  static final ForegroundColorSpan VIOLET_SPAN = new ForegroundColorSpan(0xffd7598b);
  
  public enum Type {AND, OR};
  
  private Type type;
  private Node left;
  private Node right;
  
  public CombinationNode(Type type, Node left, Node right) {
    this.type = type;
    this.left = left;
    this.right = right;
  }
  
  public String toString() {
    return "(" + left.toString() + (type == Type.AND ? " \u2227 " : " \u2228 ")  + right.toString() + ")";
  }

  @Override
  public boolean matches(Emoji emoji) {
    boolean l = left.matches(emoji);
    boolean r = right.matches(emoji);
    return type == Type.AND ? (l && r) : (l || r);
  }
  
  @Override
  public boolean valid(Emoji emoji) {
    return left.valid(emoji) && right.valid(emoji);
  }

  @Override
  public void toSpannable(Context context, SpannableStringBuilder builder,
      HashMap<Property, Integer> drawablemap, Object parent) {
    boolean braces = parent != null && parent != type;
    if (braces) {
      builder.append("(");
    }
    left.toSpannable(context, builder, drawablemap, type);
    builder.append((type == Type.AND ? " \u2227 " : " \u2228 "));
    ForegroundColorSpan span = type == Type.AND ? VIOLET_SPAN : GREEN_SPAN;
    
    builder.setSpan(span, builder.length() - 2, builder.length() - 1, 
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    
    right.toSpannable(context, builder, drawablemap, type);
    if (braces) {
      builder.append(")");
    }
  }
}
