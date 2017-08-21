package org.flowgrid.emojic;

import android.support.text.emoji.EmojiCompat;
import android.support.text.emoji.widget.EmojiTextView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

import org.flowgrid.emojic.logic.CombinationNode;
import org.flowgrid.emojic.logic.Node;
import org.flowgrid.emojic.logic.NotNode;
import org.flowgrid.emojic.logic.PropertyNode;
import org.kobjects.emoji.Emoji;
import org.kobjects.emoji.Emoji.Property;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import static android.support.annotation.Dimension.PX;

public class GameActivity extends Activity {
  final StyleSpan BOLD_SPAN = new StyleSpan(android.graphics.Typeface.BOLD);
  
  final int TRUE_COLOR = 0x88bdcf46;
  final int FALSE_COLOR = 0x88ed6c30;
  
  RelativeLayout gameView;
  TextView trueShape;
  TextView falseShape;
  private TextView questionView;
  private TextView scoreView;
  private TextView highScoreView;
  private int shapeSize;
  private int shapeFontSize;
  Node question;
  int trueMatches;
  int falseMatches;
  long t0;
  int time;
  int highScore;
  int storedHighScore;
  int score;
  int count;
  private int screenWidth;
  private int screenHeight;
  int textSize;
  int level;
  boolean running;
  boolean gameOver;
  SharedPreferences preferences;
  ImageView endImage;
  Paint shapePaint = new Paint();
  
  static final HashMap<Property,Integer> drawableMap = new HashMap<Property,Integer>();
  static void map(Property p, int id) {
    drawableMap.put(p, id);
  }
  static {
    map(Property.BIRD, R.drawable.bird);
    map(Property.BLUE, R.drawable.blue);
    map(Property.BOAT, R.drawable.boat);
    map(Property.CIRCLE, R.drawable.circle);
    map(Property.SWEETS, R.drawable.sweets);
    map(Property.EMERGENCY_VEHICLE, R.drawable.emergency_vehicle);
    map(Property.FISH, R.drawable.fish);
    map(Property.FLYING, R.drawable.flying);
    map(Property.FOOD, R.drawable.food);
    map(Property.GREEN, R.drawable.green);
    map(Property.HEART, R.drawable.heart);
    map(Property.INSECT, R.drawable.insect);
    map(Property.MAMMAL, R.drawable.mammal);
    map(Property.MOTORIZED, R.drawable.motroized);
    map(Property.PLANT, R.drawable.plant);
    map(Property.RAIL_VEHICLE, R.drawable.rail_vehicle);
    map(Property.RED, R.drawable.red);
    map(Property.SQUARE, R.drawable.square);
    map(Property.SWIMMING_OR_FLOATING, R.drawable.swimming);
    map(Property.TRIANGLE, R.drawable.triangle);
    map(Property.YELLOW, R.drawable.yellow);
  }
  
  @SuppressWarnings("deprecation")
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  
    preferences = PreferenceManager.getDefaultSharedPreferences(this);
    Display display = getWindowManager().getDefaultDisplay();
    screenWidth = display.getWidth();
    screenHeight = display.getHeight();
    highScore = storedHighScore = preferences.getInt("highScore", 0);

    shapeSize = Math.min(screenWidth, screenHeight) / 5;
    shapeFontSize = shapeSize * 8 / 10;
    shapePaint.setTextSize(shapeFontSize);

    gameView = new RelativeLayout(this) {
      Paint paint = new Paint();
      RectF rect = new RectF();
      public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint.setColor(running || gameOver ? Color.WHITE : TRUE_COLOR);
        rect.left = -getWidth()/2;
        rect.top = -getHeight()/2;
        rect.right = 1.5f*getWidth();
        rect.bottom = 1.5f*getHeight();
        canvas.drawArc(rect,-90, 360 - ((System.currentTimeMillis() - t0) / 1000) * 360f/time, true, paint);
      }
    };
    setContentView(gameView);
    gameView.setBackgroundColor(0xffeeeeee);
    // True and false
    
    trueShape = new TextView(this);
    trueShape.setText(" \u2714 ");
    trueShape.setTextColor(Color.WHITE);
    trueShape.setTextSize(TypedValue.COMPLEX_UNIT_PX, shapeSize * 0.66f);
    gameView.addView(trueShape);
    RelativeLayout.LayoutParams trueParams = 
        (RelativeLayout.LayoutParams) trueShape.getLayoutParams();
    trueParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
    //trueParams.addRule(RelativeLayout.CENTER_VERTICAL);
    trueParams.height = LayoutParams.MATCH_PARENT;
    trueShape.setBackgroundColor(TRUE_COLOR);
    trueShape.setGravity(Gravity.CENTER_VERTICAL);
    
    falseShape = new TextView(this);
    falseShape.setText(" \u2716 ");
    falseShape.setTextColor(Color.WHITE);
    falseShape.setTextSize(TypedValue.COMPLEX_UNIT_PX, shapeSize * 0.66f);
    gameView.addView(falseShape);
    RelativeLayout.LayoutParams falseParams = 
        (RelativeLayout.LayoutParams) falseShape.getLayoutParams();
    falseParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
    falseParams.height = LayoutParams.MATCH_PARENT;
//    falseParams.addRule(RelativeLayout.CENTER_VERTICAL);
    falseShape.setBackgroundColor(FALSE_COLOR);
    falseShape.setGravity(Gravity.CENTER_VERTICAL);
    
    // Question and score
    
    questionView = new TextView(this);
    questionView.setTextSize(TypedValue.COMPLEX_UNIT_PX, shapeFontSize / 1.5f);
    int padding = shapeFontSize / 5;
    questionView.setGravity(Gravity.CENTER);
    questionView.setBackgroundColor(Color.LTGRAY);
    questionView.setPadding(padding, padding, padding, padding);
    gameView.addView(questionView);
    RelativeLayout.LayoutParams questionParams = 
        (RelativeLayout.LayoutParams) questionView.getLayoutParams();
    questionParams.width = LayoutParams.MATCH_PARENT;
    questionParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
//    questionView.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
    
    scoreView = new TextView(this);
    scoreView.setTextSize(TypedValue.COMPLEX_UNIT_PX, shapeFontSize / 2);
    scoreView.setPadding(padding, padding, padding, padding);
    gameView.addView(scoreView);
    RelativeLayout.LayoutParams scoreParams = 
        (RelativeLayout.LayoutParams) scoreView.getLayoutParams();
    scoreParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
    scoreParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

    highScoreView = new TextView(this);
    highScoreView.setTextSize(TypedValue.COMPLEX_UNIT_PX, shapeFontSize / 2);
    highScoreView.setPadding(padding, padding, padding, padding);
    gameView.addView(highScoreView);
    RelativeLayout.LayoutParams highScoreParams = 
        (RelativeLayout.LayoutParams) highScoreView.getLayoutParams();
    highScoreParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
    highScoreParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);


    final Handler handler = new Handler();
    
    handler.postDelayed(new Runnable() {
        @Override
        public void run() {
          if (running || !gameOver) {
            gameView.invalidate();
            if (!running) {
              t0 -= 4900;
              addScore(1);
            }
            if (System.currentTimeMillis() > t0 + time * 1000) {
              if (running) {
                gameOver(true);
              } else {
                win();
              }
            }
          } 
          handler.postDelayed(this, 100);
        }
    }, 100); // 1 second delay (takes millis)
    
    resetGame();
    startLevel();
  }

  void resetGame() {
    time = 60;
    score = 0;
    highScoreView.setText("Move matches to the title right, others left.");
    scoreView.setText("");
    level = 0;
    gameOver = false;
  }

  void addScore(int points) {
    score += points;
    scoreView.setText("" + score);
    if (score >= highScore) {
      highScore = score;
    }
    highScoreView.setText("" + highScore);
  }
  
  Node generateQuestion(int[] level) {
    if (level[0] >= 2 && Math.random() > 0.16) {
      int reducedLevel = level[0] - 2;
      while (true) {
        level[0] = reducedLevel;
        Node left = generateQuestion(level);
        Node right = generateQuestion(level);
        // Don't use matching left and right parts.
        if (!left.toString().equals(right.toString())) {
          return new CombinationNode(
              Math.random() > 0.33 ? CombinationNode.Type.AND : CombinationNode.Type.OR, 
                  left, right);
        }
      }
    }
    if (level[0] >= 1 && Math.random() > 0.33) {
      level[0]--;
      return new NotNode(generateQuestion(level));
    }
    
    Property property;
    do {
      property = Emoji.Property.values()
          [(int) (Emoji.Property.values().length * Math.random())];
    } while(!drawableMap.containsKey(property));
    return new PropertyNode(property);
  }
  
  void startLevel() {
    trueShape.setBackgroundColor(TRUE_COLOR);
    falseShape.setBackgroundColor(FALSE_COLOR);
    t0 = System.currentTimeMillis();
    running = true;
    trueMatches = falseMatches = 0;
    
    for (int i = gameView.getChildCount() - 1; i >= 0; i--) {
      View v = gameView.getChildAt(i);
      if ((v instanceof Shape) && ((Shape) v).draggable) {
        gameView.removeViewAt(i);
      }
    }
    if (endImage != null) {
      gameView.removeView(endImage);
      endImage = null;
    }

    ArrayList<Emoji> trueList = new ArrayList<Emoji>();
    ArrayList<Emoji> falseList = new ArrayList<Emoji>();
    do {
      trueList.clear();
      falseList.clear();
      
      question = generateQuestion(new int[]{level / 5});

      for (Emoji e: Emoji.map.values()) {
        if (e.properties != 0 && !e.is(Property.ISSUES) && question.valid(e)) {
          if (question.matches(e)) {
            trueList.add(e);
          } else {
            falseList.add(e);
          }
        }
      }
  //  trueShape.setPos(gameView.getWidth() - 3*size, gameView.getHeight() / 2 - 2 * size);
  //  falseShape.setPos(0, gameView.getHeight() / 2 - 2 * size);
    } while(trueList.size() == 0 || falseList.size() == 0);

    
    SpannableStringBuilder builder = new SpannableStringBuilder();
    question.toSpannable(questionView, builder, drawableMap, null);
    builder.setSpan(BOLD_SPAN, 0, builder.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
    
    questionView.setText(builder);

    TreeSet<Emoji> choosen = new TreeSet<Emoji>();
    ArrayList<Emoji> list = null;
    while (choosen.size() < Math.min(5, trueList.size() + falseList.size())) {
      list = list == trueList ? falseList : trueList;
      Emoji e = list.get((int) (Math.random() * list.size()));
      if (!choosen.contains(e)) {
        choosen.add(e);
      }
    }
    // Order by unicode (opposed to truth)
    count = 0;
    for (Emoji e : choosen) {
      Shape shape = new Shape(e.codepoint);
      gameView.addView(shape);
      shape.setPos(
          Math.random() * (screenWidth - shapeSize * 3) + shapeSize, 
          count / 5f * (screenHeight - shapeSize * 4) + shapeSize * 1.5f);
      shape.setDraggable(true);
      count++;
    }
  }

  class Shape extends EmojiTextView {
    int codepoint;
  //  CharSequence text;
    boolean draggable;
    float lastX;
    float lastY;
    float x;
    float y;
    int pointerId;
    
    Shape(int codepoint) {
      super(GameActivity.this);
      this.codepoint = codepoint;
      char[] chars = new char[2];
      Character.toChars(codepoint, chars, 0);
      setText(new String(chars));
      setTextSize(PX, shapeSize / 3);
      setBackgroundColor(0x0ff888888);
    }
    
    public void setDraggable(boolean b) {
      this.draggable = b;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
      if (!draggable) {
        return false;
      }
      
      if (event.getAction() == MotionEvent.ACTION_MOVE ||
          event.getAction() == MotionEvent.ACTION_DOWN) {
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
          float dx = event.getRawX() - lastX;
          float dy = event.getRawY() - lastY;
          setPos(getX() + dx, getY() + dy);
          
          requestLayout();
        }
        lastX = event.getRawX();
        lastY = event.getRawY();
        return true;
      }
      
      if (event.getAction() == MotionEvent.ACTION_UP) {
        if (x > gameView.getWidth() - shapeSize * 1.5) {
          drop(true);
        }
        if (x < shapeSize / 2) {
          drop(false);
        }
        return true;
      }

      return false;
    }

    public float getX() {
      return x;
    }

    public float getY() {
      return y;
    }

    void drop(boolean side) {
      Emoji emoji = Emoji.map.get(codepoint);
      if (question.matches(emoji) == side) {
        gameView.removeView(this);
        addScore(1);
        if (side) {
          trueMatches++;
          trueShape.setBackgroundColor(((trueMatches * 0x22 + 0x87) << 24) | (TRUE_COLOR & 0xffffff));
        } else {
          falseMatches++;
          falseShape.setBackgroundColor(((falseMatches * 0x22 + 0x87) << 24) | (FALSE_COLOR & 0xffffff));
        }
        if (trueMatches + falseMatches == count) {
          addScore(level);
          running = false;
        }
      } else {
        gameOver(false);
        //        gameView.setBackgroundColor(Color.RED);
      }
    }

    void setPos(double x, double y) {
      setX((float) x);
      setY((float) y);
      this.x = (float) x;
      this.y = (float) y;
      /*      ((MarginLayoutParams) getLayoutParams()).leftMargin = (int) x;
      ((MarginLayoutParams) getLayoutParams()).topMargin = (int) y; */
    }
  }
  
  void win() {
    level++;
    if (level % 5 == 0) {
      time = 60;
    } else { 
      time = time * 2 / 3;
    }
    startLevel();
  }
  
  void gameOver(boolean timeout) {
    if (highScore > storedHighScore) {
      preferences.edit().putInt("highScore", highScore).commit();
      storedHighScore = highScore;
    }
    running = false;
    gameOver = true;
    t0 = System.currentTimeMillis();
    endImage = new ImageView(this);
    endImage.setImageResource(timeout ? R.drawable.timeout : 
      level < 5 ? R.drawable.error : R.drawable.explanation);
    endImage.setBackgroundColor((FALSE_COLOR & 0xffffff) | 0x44000000);
    endImage.setScaleType(ScaleType.CENTER);
    //endImage.setGravity(Gravity.CENTER);
    endImage.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        if (System.currentTimeMillis() - t0 > 1000) {
          resetGame();
          startLevel();
        }
      }
    });
    gameView.addView(endImage);
    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) endImage.getLayoutParams();
    params.width = LayoutParams.MATCH_PARENT;
    params.height = LayoutParams.MATCH_PARENT;
//    params.addRule(RelativeLayout.CENTER_IN_PARENT);
  }
}
