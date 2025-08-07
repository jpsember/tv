package tv;

import static js.base.Tools.*;

import com.googlecode.lanterna.TextColor;
import js.geometry.MyMath;

import java.awt.*;
import java.util.List;

import static tv.Util.*;

public final class ColorMgr {

  public static final ColorMgr SHARED_INSTANCE = new ColorMgr();

  private ColorMgr() {
  }

  public void prepare() {
    var t = mTextGraphics;
    mNormBgnd = t.getBackgroundColor();
    mNormFgnd = t.getForegroundColor();

    if (lightVal(mNormBgnd) > lightVal(mNormFgnd)) {
      mFirst = 0;
      mSecond = 1;
    } else {
      mFirst = 1;
      mSecond = 0;
    }

    List<Col> colors = arrayList();

    for (var s : split(defColorStr, ' ')) {
      s = chompPrefix(s, "#");
      if (s.isEmpty()) continue;
      var col = ColorMgr.SHARED_INSTANCE.parseColor(s);
      colors.add(col);
    }

    // Construct pairs
    List<TextColor> j = arrayList();

    checkState(mFirst != mSecond);

    for (var c : colors) {
      var cLight = lighter(c);
      pr("orig:", c, CR, "lite:", cLight);
      var first = cLight.toTextColor();
      var sec = c.toTextColor();
      if (mFirst > mSecond) {
        var tmp = first;
        first = sec;
        sec = tmp;
      }
      j.add(first);
      j.add(sec);
    }


    if (true) {
      j.clear();
      j.add(tc(4, 3, 1));
      j.add(tc(1, 1, 2));
    }
    mColorPairs = j.toArray(new OurTextColor[0]);
    pr("colorPairs:",INDENT,  j);

  }

  private static Col lighter(Col c) {
    if (false) {
      var hsv = c.toHSV();
      var value = hsv[2];
      var newValue = (float) Math.pow(value, 0.12f);
      hsv[2] = newValue;
      return Col.fromHSV(hsv);
    } else {
      return new Col(lt(c.r), lt(c.g), lt(c.b));
    }
  }

  private static int lt(int x) {
    float diff = 255 - x;
    return (int) Math.min(255, (diff * .80) + x);
  }

  private static int lightVal(TextColor c) {
    var b = c.getBlue();
    var g = c.getGreen();
    var r = c.getRed();
    return b * b + g * g + r * r;
  }

  public int pick(int value) {
    return 1 + MyMath.myMod(value, mColorPairs.length / 2);
  }

  private static TextColor clz(float r, float g, float b) {
    return new OurTextColor(round(r), round(g), round(b));
  }

  private static int round(float v) {
    var x = v * 256;
    return MyMath.clamp((int) x, 0, 5);
  }


  public void setDefaultColors() {
    var t = mTextGraphics;
    t.setBackgroundColor(mNormBgnd);
    t.setForegroundColor(mNormFgnd);
  }

  public void setColors(int index) {
    var i = index * 2;
    var t = mTextGraphics;

    var c0 = mColorPairs[i + 0];
    var c1 = mColorPairs[i + 1];
    pr("set fgnd color:",c1);
    pr("set bgnd color:",c0);

    t.setBackgroundColor(c0);
    t.setForegroundColor(c1);
  }

  private static String defColorStr =
      "#4DAAB2 #42BD88 ";
  //"#149CEB #EA15AC #B60EF1 #4F0DF2 #4DAAB2 #42BD88 #EF8B10 #EF1021 #A9CD32 #C48F3B #AC537C";


  private Col parseColor(String s) {
    try {
      checkArgument(s.length() == 6, s);
      var value = Integer.parseInt(s, 16);
      var cl = new Col((value >> 16) & 0xff,
          (value >> 8) & 0xff,
          value & 0xff);
      pr("convert:", s, "=>", cl);
      return cl;
    } catch (Throwable t) {
      throw badArg("trouble parsing color from:", quote(s), t.getMessage());
    }
  }


  private TextColor[] mColorPairs;
  private TextColor mNormBgnd, mNormFgnd;
  private int mFirst, mSecond;


  private static class Col {
    Col(int red, int green, int blue) {
      r = scale(red);
      g = scale(green);
      b = scale(blue);
    }

    private static int comp(int hexVal) {
      return hexVal & 0xff;
    }

    public static Col fromHSV(float[] hsv) {
      var c = Color.HSBtoRGB(hsv[0], hsv[1], hsv[2]);
      return new Col(comp(c >> 16), comp(c >> 8), comp(c));
//
//      var hsv = c.toHSV();
//      var newVal = 1f - ((1f - hsv[2]) * .5f);
//      hsv[2] = newVal;
//      return Col.fromHSV(hsv);
    }

    OurTextColor toTextColor() {
      return new OurTextColor(this);
    }

    private static int scale(int c) {
      checkArgument(c >= 0 && c <= 255, "bad component:", c);
      return c;
    }

    int r, g, b;

    public String toString() {
      return String.format("{r:%02X g:%02X b:%02X}", r, g, b);
    }


    public float[] toHSV() {
      float[] hsv = new float[3];
      Color.RGBtoHSB(r, g, b, hsv);
      return hsv;
    }
  }

  private static OurTextColor tc(int r, int g, int b) {
    return new OurTextColor(r, g, b);
  }

  /**
   * Our implementation of the TextColor interface
   */
  private static class OurTextColor implements TextColor {

    public OurTextColor(Col x) {
      this(x.r, x.g, x.b);
    }


    @Override
    public String toString() {
      return String.format("{r:%d g:%d b:%d}", red, green, blue);
    }

    private static int sNextIndex;

    public OurTextColor(int r, int g, int b) {
      checkArgument(r >= 0 && r <= 255, "r:", r);
      checkArgument(g >= 0 && g <= 255, "g:", g);
      checkArgument(b >= 0 && b <= 255, "b:", b);

      red = r;
      green = g;
      blue = b;

      var index = sNextIndex++;
      todo("Is this going to be correct?");
      mForegroundSGR = String.format("%d%d", 3, index).getBytes();
      mBackgroundSGR = String.format("%d%d", 4, index).getBytes();
    }

    @Override
    public byte[] getForegroundSGRSequence() {
      return mForegroundSGR;
    }

    @Override
    public byte[] getBackgroundSGRSequence() {
      return mBackgroundSGR;
    }

    @Override
    public int getRed() {
      return red;
    }

    @Override
    public int getGreen() {
      return green;
    }

    @Override
    public int getBlue() {
      return blue;
    }

    @Override
    @Deprecated
    public Color toColor() {
      throw notSupported();
    }

    private final int red, green, blue;
    private final byte[] mForegroundSGR, mBackgroundSGR;
  }

}
