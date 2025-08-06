package tv;

import static tv.Util.*;
import static js.base.Tools.*;

import java.util.List;

import com.googlecode.lanterna.Symbols;

import js.base.BaseObject;
import js.geometry.IRect;

public class JWindow extends BaseObject {

  /**
   * Subclasses should override this to supply custom painting. Default does
   * nothing
   */
  public void paint() {
  }

  public JWindow() {
  }

  public final boolean hasFocus() {
    return focusManager().focus() == this;
  }

  public IRect totalBounds() {
    return mWindowBounds;
  }

  public JWindow parent() {
    return mParent;
  }

  /**
   * Get list of children. Treat list as read only! To modify the list, use
   * add/removeChild
   */
  List<JWindow> children() {
    return mChildren;
  }

  public void removeChild(JWindow child) {
    boolean removed = removeChildIfExists(child);
    checkState(removed, "window", child, "is not a child of", this);
  }

  public boolean removeChildIfExists(JWindow child) {
    var i = mChildren.indexOf(child);
    if (i < 0)
      return false;
    mChildren.remove(i);
    child.mParent = null;
    setLayoutInvalid();
    return true;
  }

  public void removeChildren() {
    while (!mChildren.isEmpty())
      removeChild(mChildren.get(0));
  }

  public void addChild(JWindow child) {
    if (mChildren.contains(child))
      badState("attempt to add child twice!");
    checkState(child != this, "attempt to add window as child to itself");
    mChildren.add(child);
    child.mParent = this;
    setLayoutInvalid();
  }

  void setTotalBounds(IRect bounds) {
    mWindowBounds = bounds;
  }

  boolean paintValid() {
    return hasFlag(FLG_PAINTVALID);
  }

  boolean partialPaintValid() {
    return hasFlag(FLG_PARTIALPAINTVALID);
  }

  void setPaintValid(boolean valid) {
    setFlag(FLG_PAINTVALID, valid);
  }

  void setPartialPaintValid(boolean valid) {
    setFlag(FLG_PARTIALPAINTVALID, valid);
  }

  boolean layoutValid() {
    return hasFlag(FLG_LAYOUTVALID);
  }

  void setLayoutInvalid() {
    clearFlag(FLG_LAYOUTVALID);
  }

  void setLayoutValid() {
    setFlag(FLG_LAYOUTVALID);
  }

  private void setFlag(int f) {
    mFlags |= f;
  }

  private void clearFlag(int f) {
    mFlags &= ~f;
  }

  private boolean hasFlag(int f) {
    return (mFlags & f) != 0;
  }

  private void setFlag(int flag, boolean state) {
    if (!state)
      clearFlag(flag);
    else
      mFlags |= flag;
  }

  /**
   * Remove the window from the view hierarchy
   */
  public void remove() {
    checkState(mParent != null, "window is not in view hierarchy");
    mParent.removeChild(this);
  }

  public void repaint() {
    setPaintValid(false);
  }

  public void repaintPartial() {
    setPartialPaintValid(false);
  }

  public void setSizeChars(int chars) {
    checkArgument(chars >= 1, "illegal number of chars");
    mSizeExpr = chars;
  }

  public void setSizePct(int pct) {
    checkArgument(pct >= 1 && pct <= 100, "illegal percentage");
    mSizeExpr = -pct;
  }

  void layout() {
  }

  /**
   * Render the window onto the screen
   */
  void render(boolean partial) {
    var r = Render.prepare(this, partial);

    var totalBounds = totalBounds();
    checkNotNull(totalBounds, "JWindow has no totalBounds!", INDENT, this);
    if (!partial)
      r.clearRect(totalBounds);
    int btype = mFlags & FLG_BORDER;
    if (btype != BORDER_NONE) {
      if (!partial)
        r.drawRect(totalBounds, btype);
      r.setClipBounds(calcContentBounds());
    }
    paint();
    Render.unprepare();
  }

  IRect calcContentBounds() {
    var g = totalBounds();
    int btype = mFlags & FLG_BORDER;
    if (btype != BORDER_NONE) {
      g = g.withInset(2, 1);
    }
    return g;
  }

  final void setSize(int sizeExpr) {
    mSizeExpr = sizeExpr;
  }

  int getSizeExpr() {
    checkArgument(mSizeExpr != 0, "size expression must not be zero; window:", this);
    return mSizeExpr;
  }

  void setBorder(int type) {
    checkArgument(type >= 0 && type < BORDER_TOTAL);
    mFlags = (mFlags & ~FLG_BORDER) | type;
  }

  public final void plotHorzLine(int y) {
    var r = Render.SHARED_INSTANCE;
    r.clearRow(y, Symbols.SINGLE_LINE_HORIZONTAL);
  }

  private int mSizeExpr = -50;

  private int mFlags;
  private static final int FLG_BORDER = 0x3;
  private static final int FLG_PAINTVALID = 1 << 2;
  private static final int FLG_LAYOUTVALID = 1 << 3;
  private static final int FLG_PARTIALPAINTVALID = 1 << 4;
  private IRect mWindowBounds;
  private JWindow mParent;
  private List<JWindow> mChildren = arrayList();

}
