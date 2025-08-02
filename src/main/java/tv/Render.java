package tv;

import static tv.Util.*;
import static js.base.Tools.*;

import java.util.Stack;

import com.googlecode.lanterna.Symbols;
import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;

import js.base.BaseObject;
import js.geometry.IPoint;
import js.geometry.IRect;
import js.geometry.MyMath;

public final class Render extends BaseObject {

  /**
   * This is only valid during prepare() and unprepare() calls
   */
  public static Render SHARED_INSTANCE;

  private Render() {
  }

  public IRect clipBounds() {
    return mClipBounds;
  }

  public Render setClipBounds(IRect r) {
    mClipBounds = r;
    return this;
  }

  @Deprecated
  public JWindow window() {
    return mWindow;
  }

  public Render clearRow(int y, char character) {
    var c = mClipBounds;
    return clearRect(new IRect(c.x, y, c.width, 1), character);
  }

  public Render clearRect(IRect bounds, char character) {
    var p = clampToClip(bounds);
    if (!p.isDegenerate()) {
      mTextGraphics.fillRectangle(new TerminalPosition(p.x, p.y), new TerminalSize(p.width, p.height),
          character);
    }
    return this;
  }

  public Render clearRect(IRect bounds) {
    return clearRect(bounds, ' ');
  }

  private static final char[] sBorderChars = { //
      Symbols.SINGLE_LINE_HORIZONTAL, Symbols.SINGLE_LINE_VERTICAL,

      Symbols.SINGLE_LINE_TOP_LEFT_CORNER, Symbols.SINGLE_LINE_BOTTOM_LEFT_CORNER,
      Symbols.SINGLE_LINE_TOP_RIGHT_CORNER, Symbols.SINGLE_LINE_BOTTOM_RIGHT_CORNER, //

      Symbols.DOUBLE_LINE_HORIZONTAL, Symbols.DOUBLE_LINE_VERTICAL, Symbols.DOUBLE_LINE_TOP_LEFT_CORNER,
      Symbols.DOUBLE_LINE_BOTTOM_LEFT_CORNER, Symbols.DOUBLE_LINE_TOP_RIGHT_CORNER,
      Symbols.DOUBLE_LINE_BOTTOM_RIGHT_CORNER, //

      '╴', Symbols.SINGLE_LINE_VERTICAL, '╭', '╰', '╮', '╯', //
  };

  public Render drawString(int x, int y, int maxLength, String s) {
    if (verbose())
      log("drawString x:", x, " y:", y, " maxLength:", maxLength, " s:", quote(s));
    do {
      var b = clipBounds();
      if (verbose())
        log("clipBounds:", INDENT, b);

      // Determine which substring is within the window bounds
      if (y < b.y || y >= b.endY())
        break;

      var startX = MyMath.clamp(x, b.x, b.endX());
      var availWidth = Math.min(Math.min(s.length(), maxLength), b.endX() - startX);
      if (availWidth <= 0)
        break;
      var tg = textGraphics();
      tg.putString(startX, y, s.substring(0, availWidth));
    } while (false);
    return this;
  }

  public Render drawRect(IRect bounds, int type) {
    do {
      checkArgument(type >= 1 && type < BORDER_TOTAL, "unsupported border type:", type);
      int ci = (type - 1) * 6;
      var p = clampToClip(bounds);
      if (p.width < 2 || p.height < 2)
        break;
      var tg = textGraphics();
      var x1 = p.x;
      var y1 = p.y;
      var x2 = p.endX();
      var y2 = p.endY();
      if (p.width > 2) {
        tg.drawLine(x1 + 1, y1, x2 - 2, y1, sBorderChars[ci + 0]);
        tg.drawLine(x1 + 1, y2 - 1, x2 - 2, y2 - 1, sBorderChars[ci + 0]);
      }
      if (p.height >= 2) {
        tg.drawLine(x1, y1 + 1, x1, y2 - 2, sBorderChars[ci + 1]);
        tg.drawLine(x2 - 1, y1 + 1, x2 - 1, y2 - 2, sBorderChars[ci + 1]);
      }
      tg.setCharacter(x1, y1, sBorderChars[ci + 2]);
      tg.setCharacter(x1, y2 - 1, sBorderChars[ci + 3]);
      tg.setCharacter(x2 - 1, y1, sBorderChars[ci + 4]);
      tg.setCharacter(x2 - 1, y2 - 1, sBorderChars[ci + 5]);
    } while (false);
    return this;
  }

  private TextGraphics textGraphics() {
    return mTextGraphics;
  }

  /**
   * Prepare for subsequent operations to occur with a particular window
   */
  static Render prepare(JWindow window, boolean partial) {
    var r = sShared;
    SHARED_INSTANCE = r;
    r.auxPrepare(window, partial);
    return r;
  }

  private void auxPrepare(JWindow window, boolean partial) {
    mWindow = window;
    mClipBounds = window.totalBounds();
    mStack = new Stack<>();
    var t = winMgr().abstractScreen().newTextGraphics();
    if (mNormBgnd == null) {
      mNormBgnd = t.getBackgroundColor();
      mNormFgnd = t.getForegroundColor();
    }
    mTextGraphics = t;
    mPartial = partial;
  }

  private TextColor mNormFgnd;
  private TextColor mNormBgnd;

  static Render unprepare() {
    SHARED_INSTANCE.auxUnprepare();
    return null;
  }

  private void auxUnprepare() {
    if (!mStack.isEmpty())
      alert("Render.stack isn't empty");
    mStack = null;
    mWindow = null;
    mClipBounds = null;
    mTextGraphics = null;
    SHARED_INSTANCE = null;
  }

  /**
   * Clamp a point to be within the bounds of the window
   */
  private IPoint clampToClip(int wx, int wy) {
    var cx1 = MyMath.clamp(wx, mClipBounds.x, mClipBounds.endX());
    var cy1 = MyMath.clamp(wy, mClipBounds.y, mClipBounds.endY());
    return new IPoint(cx1, cy1);
  }

  private IRect clampToClip(IRect r) {
    var p1 = clampToClip(r.x, r.y);
    var p2 = clampToClip(r.endX(), r.endY());
    return IRect.rectContainingPoints(p1, p2);
  }

  public Render pushStyle(int style) {
    checkArgument(style >= 0 && style < STYLE_TOTAL);
    mStack.push(mTextGraphics);
    var t = winMgr().abstractScreen().newTextGraphics();

    switch (style) {
    case STYLE_INVERSE:
      t.setForegroundColor(TextColor.ANSI.WHITE_BRIGHT);
      t.setBackgroundColor(TextColor.ANSI.BLACK);
      break;
    case STYLE_NORMAL:
      t.setForegroundColor(mNormFgnd);
      t.setBackgroundColor(mNormBgnd);
      break;
    case STYLE_MARKED:
      t.setForegroundColor(TextColor.ANSI.WHITE);
      t.setBackgroundColor(TextColor.ANSI.BLUE);
      break;
    case STYLE_INVERSE_AND_MARK:
      t.setForegroundColor(TextColor.ANSI.WHITE_BRIGHT);
      t.setBackgroundColor(TextColor.ANSI.BLUE_BRIGHT);
      break;
    }
    mTextGraphics = t;
    return this;
  }

  public Render pop() {
    mTextGraphics = mStack.pop();
    return this;
  }

  public boolean partial() {
    return mPartial;
  }

  private Stack<TextGraphics> mStack = new Stack<>();
  private IRect mClipBounds;
  private JWindow mWindow;
  private TextGraphics mTextGraphics;
  private boolean mPartial;
  private static final Render sShared = new Render();
}
