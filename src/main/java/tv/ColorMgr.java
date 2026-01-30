package tv;

import static js.base.Tools.*;

import com.googlecode.lanterna.TextColor;

import static tv.Util.*;

public final class ColorMgr {

  public static final ColorMgr SHARED_INSTANCE = new ColorMgr();

  private ColorMgr() {
  }

  public void prepare() {
    var t = mTextGraphics;
    mNormBgnd = t.getBackgroundColor();
    mNormFgnd = t.getForegroundColor();
    // estimate whether user is preferring dark text on light background
    mInvert = lightVal(mNormBgnd) < lightVal(mNormFgnd);
  }

  private static int lightVal(TextColor c) {
    var b = c.getBlue();
    var g = c.getGreen();
    var r = c.getRed();
    return b * b + g * g + r * r;
  }

  public void setDefaultColors() {
    var t = mTextGraphics;
    t.setBackgroundColor(mNormBgnd);
    t.setForegroundColor(mNormFgnd);
  }

  public void setCustomColors(int code) {
    if (code < 0 || code >= 16 * 16) badArg("illegal color code:", code);
    var t = mTextGraphics;
    var c0 = ANSI_COLORS[code & 0xf];
    var c1 = ANSI_COLORS[code >> 4];
    if (mInvert) {
      var tmp = c0;
      c0 = c1;
      c1 = tmp;
    }
    t.setBackgroundColor(c0);
    t.setForegroundColor(c1);
  }

  private static final TextColor[] ANSI_COLORS = {
      TextColor.ANSI.BLACK,
      TextColor.ANSI.RED,
      TextColor.ANSI.GREEN,
      TextColor.ANSI.YELLOW,
      TextColor.ANSI.BLUE,
      TextColor.ANSI.MAGENTA,
      TextColor.ANSI.CYAN,
      TextColor.ANSI.WHITE,
      TextColor.ANSI.BLACK_BRIGHT,
      TextColor.ANSI.RED_BRIGHT,
      TextColor.ANSI.GREEN_BRIGHT,
      TextColor.ANSI.YELLOW_BRIGHT,
      TextColor.ANSI.BLUE_BRIGHT,
      TextColor.ANSI.MAGENTA_BRIGHT,
      TextColor.ANSI.CYAN_BRIGHT,
      TextColor.ANSI.WHITE_BRIGHT,
  };


  private TextColor mNormBgnd, mNormFgnd;
  private boolean mInvert;
}
