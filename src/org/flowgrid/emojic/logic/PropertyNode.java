package org.flowgrid.emojic.logic;

import java.util.HashMap;

import org.kobjects.emoji.Emoji;
import org.kobjects.emoji.Emoji.Property;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;

public class PropertyNode implements Node {
  private Property property;
  
  public PropertyNode(Property property) {
    this.property = property;
  }
  
  public boolean matches(Emoji emoji) {
    return emoji.is(property);
  }
  
  public String toString() {
    return property.toString();
  }

  @Override
  public boolean valid(Emoji emoji) {
    boolean ok = true;
    if (Emoji.Category.COLOR.contains(property)) {
      ok &= Emoji.Category.COLOR.contains(emoji);
    }
    if (Emoji.Category.SHAPE.contains(property)) {
      ok &= Emoji.Category.SHAPE.contains(emoji);
    }
    if (property == Property.FOOD) {
      ok &= !Emoji.Category.ANIMAL.contains(emoji);
    }
    return ok;
  }

  @Override
  public void toSpannable(Context context, SpannableStringBuilder builder,
      HashMap<Property, Integer> drawablemap, Object parent) {
    Integer id = drawablemap.get(property);
    if (id == null) {
      builder.append(property.toString());
    } else {
      builder.append(" ");
      builder.setSpan(new ImageSpan(context, id.intValue()),
          builder.length() - 1, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }
  }
}
