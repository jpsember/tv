package tv;

import static js.base.Tools.*;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.terminal.Terminal;
import js.geometry.MyMath;

import java.awt.*;
import static tv.Util.*;

public final class ColorMgr {

  public static final ColorMgr SHARED_INSTANCE = new ColorMgr();

  private ColorMgr() {
  }


  public void setRandom() {
    setBgndColor(mNormBgnd);
    setFgndColor(sColors[random().nextInt(sColors.length)]);
  }

  public void setDefault() {
    setBgndColor(mNormBgnd);
    setFgndColor(mNormFgnd);
  }

  private void setBgndColor(TextColor t) {
    try {
      mTerminal.setBackgroundColor(t);
    } catch (Throwable tt) {
      throw asRuntimeException(tt);
    }
  }

  private void setFgndColor(TextColor t) {
    try {
      mTerminal.setForegroundColor(t);
    } catch (Throwable tt) {
      throw asRuntimeException(tt);
    }
  }

  public void prepareRender(TextGraphics t) {
    mTerminal = WinMgr.SHARED_INSTANCE.terminal();
    if (mNormBgnd != null) return;
    mNormBgnd = t.getBackgroundColor();
    mNormFgnd = t.getForegroundColor();
  }

  private static TextColor cl(double r, double g, double b) {
    return new OurTextColor(round(r), round(g), round(b));
  }

  private static int round(double v) {
    var x = v * 255;
    return MyMath.clamp((int) x, 0, 255);
  }

  public static final TextColor BLUE = cl(0, 0, 1), //
      RED = cl(1, 0, 0), //
      GREEN = cl(0, 1, 0), //
      DARK_RED = cl(.5, 0, 0), //
      DARK_BLUE = cl(0, 0, .5), //
      DARK_GREEN = cl(0, .5, 0), //
  //
  BLACK = cl(0, 0, 0);
  private static final TextColor sColors[] = {
      TextColor.ANSI.BLUE,
      TextColor.ANSI.CYAN_BRIGHT,
      BLUE, RED, GREEN,
      DARK_BLUE, DARK_RED, DARK_GREEN,
  };

  private TextColor mNormBgnd, mNormFgnd;
  private Terminal mTerminal;


  /**
   * Our implementation of the TextColor interface
   */
  private static class OurTextColor implements TextColor {

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
