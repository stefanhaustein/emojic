package org.flowgrid.emojic.logic;

import java.util.HashMap;

import org.kobjects.emoji.Emoji;
import org.kobjects.emoji.Emoji.Property;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.widget.TextView;

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
  public void toSpannable(TextView view, SpannableStringBuilder builder,
      HashMap<Property, Integer> drawablemap, Object parent) {
    Integer id = drawablemap.get(property);
    if (id == null) {
      builder.append(property.toString());
    } else {
      builder.append(" ");
      Drawable icon = ContextCompat.getDrawable(view.getContext(), id);

      float targetSize = view.getTextSize();
      float scale = targetSize / icon.getIntrinsicHeight();

      icon.setBounds(0, 0, (int) (icon.getIntrinsicWidth() * scale), (int) targetSize);

      builder.setSpan(new ImageSpan(icon, ImageSpan.ALIGN_BOTTOM),
          builder.length() - 1, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }
  }
}
