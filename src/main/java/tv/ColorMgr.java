package tv;

import static js.base.Tools.*;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import js.geometry.MyMath;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static tv.Util.*;

public final class ColorMgr {

  public static final ColorMgr SHARED_INSTANCE = new ColorMgr();

  private ColorMgr() {
  }

//  public void setRandom() {
//    setBgndColor(mNormBgnd);
//    setFgndColor(MyMath.randomElement(random(), mColors)); //sColors[random().nextInt(sColors.length)]);
//  }
//
//  public void setDefault() {
//    setBgndColor(mNormBgnd);
//    setFgndColor(mNormFgnd);
//  }

//  private void setBgndColor(TextColor t) {
//    var tg = mTextGraphics;
//    tg.setBackgroundColor(t);
//  }
//
//  private void setFgndColor(TextColor t) {
//    mTextGraphics.setForegroundColor(t);
//  }

  public void prepare() {
//    if (mNormBgnd != null) return;
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
//    for (var c : colors) {
//      pr("color:",c);
//      var h = c.toHSV();
//      pr("hsv:",h);
//      var c2 =  Col.fromHSV(h);
//      pr("back to col:",c2);
//    }
//die();

    // Construct pairs
    var j = new TextColor[2 * colors.size()];


    int i = 0;
    for (var c : colors) {
      var cLight = lighter(c);
      pr("orig:", c, CR, "lite:", cLight);
      j[i + mFirst] = cLight.toTextColor();
      j[i + mSecond] = c.toTextColor();
      pr("i:", i, INDENT,
          cLight, CR, c);
      checkState(mFirst != mSecond);
      pr("converted:", INDENT, j[i + mFirst], CR, j[i + mSecond]);
      i += 2;
    }
    mColorPairs = j;
  }

  private static Col lighter(Col c) {
    var hsv = c.toHSV();
    var value = hsv[2];
    var newValue =  (float) Math.pow(  value, 0.12f) ;
    hsv[2] = newValue;
    return Col.fromHSV(hsv);
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
    t.setBackgroundColor(c0);
    t.setForegroundColor(c1);

    if (++wtf < 105) {
      pr("index:", index, "i:", i, "mColorPairs:", c0, c1);
    }
  }

  int wtf = 0;
  private static String defColorStr = "#EA15AC #B60EF1 #4F0DF2 #149CEB #4DAAB2 #42BD88 #EF8B10 #EF1021 #A9CD32 #C48F3B #AC537C";


  private Col parseColor(String s) {
    try {
      checkArgument(s.length() == 6, s);
      var value = Integer.parseInt(s, 16);
      return new Col((value >> 16) & 0xff,
          (value >> 8) & 0xff,
          value & 0xff);
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

  /**
   * Our implementation of the TextColor interface
   */
  private static class OurTextColor implements TextColor {

    public OurTextColor(Col x) {
      this(scale(x.r), scale(x.g), scale(x.b));
    }

    private static int scale(int colVal) {
      return (int) MyMath.clamp(colVal * (6 / 256f), 0, 5);
    }

    @Override
    public String toString() {
      return String.format("{r:%d g:%d b:%d}", red, green, blue);
    }

    private static int sNextIndex;

    public OurTextColor(int r, int g, int b) {
      red = r;
      green = g;
      blue = b;

      var index = sNextIndex++;
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
