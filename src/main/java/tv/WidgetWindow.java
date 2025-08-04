package tv;

import static tv.Util.*;
import static js.base.Tools.*;

import js.geometry.MyMath;

public class WidgetWindow extends JWindow implements FocusHandler {

  public static final int DEFAULT_WIDTH = 30;

  public interface HintListener {
    void hintChanged(String text);
  }

  public WidgetWindow width(int width) {
    mWidth = width;
    return this;
  }

  public int width() {
    return mWidth;
  }

  public WidgetWindow label(String label) {
    mLabel = label;
    return this;
  }

  public WidgetWindow value(Object value) {
    return this;
  }

  public WidgetWindow button(ButtonListener listener) {
    mButtonListener = listener;
    return this;
  }

  public WidgetWindow hintListener(HintListener hl) {
    mHintListener = hl;
    return this;
  }

  public WidgetWindow helper(WidgetHelper helper) {
    mHelper = helper;
    return this;
  }

  public WidgetWindow focusRootWindow(JWindow rootWindow) {
    mFocusRootWindow = rootWindow;
    return this;
  }

  @Override
  public void loseFocus() {

  }


  @Override
  public void gainFocus() {
    mHintDisabled = false;
    mCursorPos = -1;
  }

  public boolean isEmpty() {
    return nullOrEmpty(mContent);
  }

  @Override
  public void paint() {
    var r = Render.SHARED_INSTANCE;

    boolean hf = hasFocus();

    var b = r.clipBounds();
    b = b.withInset(1, 0);
    var SEP = 1;

    var labelWidth = Math.min(b.width / 2, 16);
    var valueWidth = mWidth;

    if (isButton()) {
//      r.pushStyle(hf ? STYLE_INVERSE : STYLE_NORMAL);
      r.drawString(b.x + labelWidth + SEP, b.y, labelWidth, mLabel);
//      r.pop();
    } else {

      var ef = mLabel + ":";
      r.drawString(b.x + labelWidth - ef.length(), b.y, labelWidth, ef);

      var lx = b.x + labelWidth + SEP;
      var ly = b.y;

      var s = mContent;
      if (nonEmpty(mHint))
        s = mHint;

      var style = STYLE_NORMAL;
      if (hf) {
        int curPos = mCursorPos;
        if (curPos < 0) {
          // Highlight the entire text
          style = STYLE_INVERSE;
          curPos = s.length();
        } else {
          s = truncate(s, mWidth);
        }

        if (hf) {
          winMgr().setCursorPosition(lx + curPos, ly);
        }
      }
     // r.pushStyle(style);
      r.drawString(lx, ly, valueWidth, s);
//      r.pop();
    }

  }



  public boolean isButton() {
    return mButtonListener != null;
  }

  private String getHintForHelper() {
    var cont = mContent;
    var c = mCursorPos;
    if (c >= 0)
      cont = truncate(cont, c);
    return cont;
  }

  @Override
  public void processKeyEvent(KeyEvent k) {
    var fm = focusManager();
    switch (k.keyType()) {
    case Enter: {
      if (isButton())
        mButtonListener.buttonPressed();
      else {
        applyHint();
        fm.move(mFocusRootWindow, 1);
      }
    }
      break;

    case Tab:
      applyHint();
      fm.move(mFocusRootWindow, 1);
      break;
    case ArrowDown:
      applyHint();
      fm.move(mFocusRootWindow, 1);
      break;
    case ArrowUp:
      applyHint();
      fm.move(mFocusRootWindow, -1);
      break;
    case ArrowLeft:
      suppressHint();
      if (mCursorPos == 0)
        break;
      if (mCursorPos > 0)
        mCursorPos--;
      else
        mCursorPos = mContent.length() - 1;
      break;
    case ArrowRight:
      suppressHint();
      if (mCursorPos < mContent.length())
        mCursorPos++;
      break;
    case Backspace:
      suppressHint();
      if (mCursorPos > 0) {
        mContent = mContent.substring(0, mCursorPos - 1) + mContent.substring(mCursorPos);
        mCursorPos--;
      } else {
        mCursorPos = 0;
        mContent = "";
      }
      break;
    case Delete:
      suppressHint();
      if (mCursorPos < 0) {
        mContent = "";
        mCursorPos = 0;
      } else {
        if (mCursorPos < mContent.length())
          mContent = mContent.substring(0, mCursorPos) + mContent.substring(mCursorPos + 1);
      }
      break;
    case Home:
      suppressHint();
      mCursorPos = 0;
      break;
    case End:
      suppressHint();
      mCursorPos = mContent.length();
      break;
    case Character: {
      var c = k.getCharacter();
      insertChar(c);
      mHumanEdited = true;
    }
      break;
    default:
      //todo("have some sort of fallback");
      break;
    }

    // If this is no longer the focused window, return immediately
    if (fm.focus() != this)
      return;

    mContent = truncate(mContent, mWidth);
    mCursorPos = MyMath.clamp(mCursorPos, -1, mWidth);

    mHint = null;
    if (!mHintDisabled) {
      if (mHelper != null) {
        var prefix = getHintForHelper();
        var newHint = mHelper.getHint(prefix);
        if (!newHint.equals(mHint)) {
          mHint = newHint;
          callHintListener(mHint);
        }
      }
    }

    repaint();
  }

  /**
   * Disable any more hints during this focus session
   */
  private void suppressHint() {
    mHintDisabled = true;
  }

  public void setContent(String text) {
    text = nullToEmpty(text);
    mContent = text;
    mCursorPos = -1;
  }

  private void insertChar(char c) {
    if (mCursorPos < 0) {
      mContent = "";
      mCursorPos = 0;
    }
    mContent = mContent.substring(0, mCursorPos) + Character.toString(c) + mContent.substring(mCursorPos);
    mCursorPos++;
  }

  /**
   * If a hint exists, replace user-typed content with it
   */
  private void applyHint() {
    if (nonEmpty(mHint)) {
      setContent(mHint);
    }
  }

  private void callHintListener(String hint) {
    if (mHintListener != null) {
      mHintListener.hintChanged(hint);
    }
  }

  private static String truncate(String s, int maxWidth) {
    if (s.length() > maxWidth)
      return s.substring(0, maxWidth);
    return s;
  }

  private int mCursorPos = -1; // position of cursor, or -1 if entire string is highlighted
  private String mContent = "";

  private int mWidth = DEFAULT_WIDTH;
  private String mLabel = "<no label!>";
  private JWindow mFocusRootWindow;
  private ButtonListener mButtonListener;
  private WidgetHelper mHelper;
  private String mHint;
  private HintListener mHintListener;
  private boolean mHumanEdited;
  private boolean mHintDisabled;

  public boolean isHumanEdited() {
    return mHumanEdited;
  }

  @Override
  public boolean focusPossible() {
    return true;
  }

  @Override
  public boolean undoEnabled() {
    return false;
  }
}
