package tv;

import static tv.Util.*;
import static js.base.Tools.*;

import java.io.IOException;
import java.util.List;
import java.util.Stack;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.screen.AbstractScreen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

import js.base.BaseObject;
import js.file.Files;
import js.geometry.IPoint;
import js.geometry.IRect;

public class WinMgr extends BaseObject {

  public static final WinMgr SHARED_INSTANCE;

  public WinMgr pushContainer(JContainer container) {
    checkNotNull(container, "expected container");

    // If this is not going to be the top-level window, add it as a child to the current parent.

    if (!mStack.isEmpty()) {
      var parent = container();
      parent.addChild(container);
    }

    applyParam(container);
    push(container);
    return this;
  }

  public JContainer pushContainer() {
    var container = new JContainer();
    container.mHorzFlag = mHorzFlag;
    mHorzFlag = false;
    pushContainer(container);
    return container;
  }

  public WinMgr popContainer() {
    pop();
    return this;
  }

  private void push(JContainer container) {
    checkState(mStack.size() < 100, "stack is too large");
    if (mStack.isEmpty()) {
      mCurrentTree.topLevelContainer = container;
    }
    mStack.push(container);
  }

  public WinMgr horz() {
    mHorzFlag = true;
    return this;
  }

  public WinMgr chars(int charCount) {
    checkArgument(charCount > 0, "expected positive character count");
    mSizeExpr = charCount;
    return this;
  }

  public WinMgr pct(int pct) {
    checkArgument(pct > 0, "expected positive percentage");
    mSizeExpr = -pct;
    return this;
  }

  public WinMgr roundedBorder() {
    mBorderType = BORDER_ROUNDED;
    return this;
  }

  public WinMgr thickBorder() {
    mBorderType = BORDER_THICK;
    return this;
  }

  public WinMgr thinBorder() {
    mBorderType = BORDER_THIN;
    return this;
  }

  public WinMgr name(String pendingName) {
    mPendingName = pendingName;
    return this;
  }

  private void pop() {
    if (mStack.isEmpty())
      badState("attempt to pop the outermost container");
    mStack.pop();
  }

  private JContainer container() {
    return mStack.peek();
  }

  /**
   * Construct a window and add it to the current container
   */
  public WinMgr window() {
    return window(new JWindow());
  }

  /**
   * Add a window to the current container
   */
  public WinMgr window(JWindow window) {
    checkArgument(window != null, "no window supplied");
    var c = container();
    c.addChild(window);
    applyParam(window);
    return this;
  }

  private void applyParam(JWindow w) {
    w.setSize(mSizeExpr);
    w.setBorder(mBorderType);
    if (!nullOrEmpty(mPendingName))
      w.setName(mPendingName);
    resetPendingWindowVars();
  }

  private void resetPendingWindowVars() {
    mHorzFlag = false;
    mSizeExpr = -100;
    mBorderType = BORDER_NONE;
    mPendingName = null;
  }

  public JContainer topLevelContainer() {
    return mCurrentTree.topLevelContainer;
  }

  private boolean mHorzFlag;
  private int mBorderType;
  private int mSizeExpr; // 0: unknown > 1: number of chars < 1: -percentage
  private String mPendingName;

  public void doneConstruction() {
    // Ensure that only the root container remains on the stack
    if (mStack.size() != 0)
      badState("window stack size is unexpected:", mStack.size(),
          "or doesn't have top-level container at bottom");
  }

  public void setCursorPosition(int x, int y) {
    mScreen.setCursorPosition(new TerminalPosition(x, y));
  }

  public void hideCursor() {
    mScreen.setCursorPosition(null);
  }

  public AbstractScreen abstractScreen() {
    return mScreen;
  }

  public void mainLoop() {
    boolean omitMsg = true;
    int k = 0;
    setFooterMessage("Hello there!");
    while (isOpen()) {
      update();
      sleepMs(30);
      if (omitMsg)
        sleepMs(170);
      if (!omitMsg) updateFooterMessage();
      if (quitRequested())
        close();
      if (!omitMsg) {
        if (++k % 60 == 0) {
          setFooterMessage("k =", k);
        }
      }
    }
  }

  private char showChar = 'A';
  private char prevShowChar = ' ';

  public void update() {
    var m = winMgr();

    try {

     // focusManager().update();

      KeyStroke keyStroke = mScreen.pollInput();
      if (keyStroke != null) {
        var key = new KeyEvent(keyStroke);

        boolean processed = false;

        switch (key.toString()) {
          case KeyEvent.QUIT:
            quit();
            return;
          case KeyEvent.ESCAPE:
            quit();
//            if (focusManager().popIfPossible()) {
//              processed = true;
//            } else {
//              alert("quitting on escape");
//              quit();
//            }
            break;
          default:
            showChar++;
            if (showChar > 'Z')
              showChar = 'A';
//          if (focusManager().processUndoKeys(key))
//            processed = true;
            break;
        }

//        if (!processed) {
//          var f = focusManager().focus();
//          if (f == null) {
//            pr("There is no focus!");
//          } else {
//            f.processKeyEvent(key);
//          }
//        }
      }

      var c = m.topLevelContainer();
      discardTextGraphics();

      // Update size of terminal
      mScreen.doResizeIfNecessary();
      var currSize = toIpoint(mScreen.getTerminalSize());
      pr(VERT_SP,DASHES,"terminal curr size:",currSize);

      // If the screen size has changed, or the desired layout bounds for the current top-level container
      // has changed, invalidate the layout

      {
        if (!currSize.equals(mPrevLayoutScreenSize)) {
          pr("...new screen size:", currSize);
          mPrevLayoutScreenSize = currSize;
          invalidateRect(new IRect(currSize));
          mRedrawFlag  = true;
        }

//        var desiredBounds = c.preferredBounds(currSize);
//        if (!desiredBounds.equals(c.totalBounds())) {
//          c.setTotalBounds(desiredBounds);
//          c.setLayoutInvalid();
//        }
      }
      if (mRedrawFlag) {
mTextGraphics = null;

      }

      if (mTextGraphics == null) {
        mTextGraphics = mScreen.newTextGraphics();
        mTextGraphics.fill('a');
        prevShowChar = ' '; }

     if (showChar != prevShowChar) {
       prevShowChar = showChar;

       // We need a terminal
       var terminal = WinMgr.SHARED_INSTANCE.terminal();
       terminal.setCursorPosition(5,5);
         terminal.putString("Hello: "+Character.toString((char)showChar));
     }
      mScreen.refresh();

//      redrawAllTreesIntersectingInvalidRect();
//
//      // Make changes visible
//      if (mTextGraphics != null) {
//        pr("refreshing screen");
//        mScreen.refresh();
//        discardTextGraphics();
//      }
    } catch (Throwable t) {
      m.closeIfError(t);
      throw asRuntimeException(t);
    }
  }

  private boolean mRedrawFlag;

  private void prepareTextRecord() {
    if (mTextGraphics == null) {
      var t = abstractScreen().newTextGraphics();
      ColorMgr.SHARED_INSTANCE.prepareRender(t);
      mTextGraphics = t;
      pr("...prepared textRecord:", t, "size:", t.getSize());
    }
  }

  private void discardTextGraphics() {
    if (mTextGraphics != null) {
      pr("...discarding text graphics");
      mTextGraphics = null;
    }
  }

  public TextGraphics textGraphics() {
    checkState(mTextGraphics != null);
    return mTextGraphics;
  }

  private IPoint mPrevLayoutScreenSize;

  public boolean quitRequested() {
    return mQuitFlag;
  }

  public void quit() {
    mQuitFlag = true;
  }

  private boolean mQuitFlag;

  private static IPoint toIpoint(TerminalSize s) {
    return IPoint.with(s.getColumns(), s.getRows());
  }

  public static final boolean ISSUE_VIEW = true && alert("ISSUE_VIEW is in effect");

  public static void pVIEW(Object... messages) {
    if (ISSUE_VIEW)
      pr(insertStringToFront("ISSUE_VIEW --->", messages));
  }


  /**
   * If a view's layout is invalid, calls its layout() method, and invalidates
   * its paint.
   *
   * If the view's paint is invalid, renders it.
   *
   * Recursively processes all child views in this manner as well.
   */
  private void updateView(JWindow w) {

    if (ISSUE_VIEW) {
      if (!w.layoutValid() || !w.paintValid())
        pVIEW(VERT_SP, "updateView", w.name());
    }

    if (!w.layoutValid()) {
      pVIEW("...window", w.name(), "layout is invalid");
      w.repaint();
      w.layout();
      w.setLayoutValid();

      // Invalidate layout of any child views as well
      for (var c : w.children())
        c.setLayoutInvalid();
    }

    if (!w.paintValid()) {
      pr("updateView, window paint not valid:", w.name());
      prepareTextRecord();
      pVIEW("...window", w.name(), "paint is invalid; rendering; bounds:", w.totalBounds());
      // Mark all children invalid
      for (var c : w.children())
        c.setPaintValid(false);
      w.render();
      w.setPaintValid(true);
    }

    for (var c : w.children()) {
      updateView(c);
    }
  }

  /**
   * Restore normal terminal window if an exception is not null (so that
   * subsequent dumping of the exception will actually appear to the user in the
   * normal terminal window)
   */
  public Throwable closeIfError(Throwable t) {
    if (t != null) {
      try {
        close();
      } catch (Throwable t2) {
        pr("(...ignoring exception:", t2.getMessage(), ")");
      }
    }
    return t;
  }

  // ------------------------------------------------------------------
  // Lanterna screen
  // ------------------------------------------------------------------

  public void open() {
    checkState(mTreeStack == null);
    mTreeStack = new Stack<>();

    pushContainerTree();

    try {
      var f = new DefaultTerminalFactory();
      // f.setUnixTerminalCtrlCBehaviour(CtrlCBehaviour.TRAP);
      mTerminal = f.createTerminal();
      pr(VERT_SP,"created terminal");
      mScreen = new TerminalScreen(mTerminal);
      mScreen.startScreen();
      winMgr().hideCursor();
    } catch (Throwable t) {
      throw asRuntimeException(t);
    }
  }

  public void close() {
    if (mScreen == null)
      return;
    Files.close(mScreen);
    popContainerTree();
    // There seems to be a problem with restoring the cursor position; it positions the cursor at the end of the last line.
    // Probably because our logging doesn't print a linefeed until necessary.
    pr();
    System.out.println();
    mScreen = null;
    mTerminal = null;
  }

  public boolean isOpen() {
    return mScreen != null;
  }

  public AbstractScreen screen() {
    return mScreen;
  }


  public boolean inView(JWindow window) {
    checkNotNull(window);
    var tc = topLevelContainer();
    while (true) {
      if (window == null)
        break;
      if (window == tc)
        return true;
      window = window.parent();
    }
    return false;
  }

  private Stack<JContainer> mStack;

  private void invalidateRect(IRect bounds) {
    if (mInvalidRect == null)
      mInvalidRect = bounds;
    else
      mInvalidRect = IRect.rectContainingPoints(mInvalidRect.corner(0), mInvalidRect.corner(2));
  }

  private IRect mInvalidRect;

  public Terminal terminal() {
    return this.mTerminal;
  }

  // ------------------------------------------------------------------
  // Trees of JContainers
  // ------------------------------------------------------------------

  private static class WindowTree {
    Stack<JContainer> containerStack = new Stack();
    JContainer topLevelContainer;
  }

  private Stack<WindowTree> mTreeStack;

  /**
   * Push current container tree onto a stack (if there is one), construct a new
   * (empty) container tree, and make it the current one
   */
  public void pushContainerTree() {

    // If there's an existing tree, save to stack
    if (mCurrentTree != null) {
      mTreeStack.push(mCurrentTree);

      mCurrentTree = null;
      mStack = null;
    }

    // Construct a new tree
    var t = new WindowTree();

    // Copy some fields to the state vars
    mCurrentTree = t;
    mStack = t.containerStack;
  }

  /**
   * Discard current container tree, and if there is a previous one in the
   * stack, pop and make it the current one
   */
  public void popContainerTree() {
    checkState(mCurrentTree != null, "no tree to close");
    // Add tree container's bounds to invalidation rect
    invalidateRect(mCurrentTree.topLevelContainer.totalBounds());
    mCurrentTree = null;
    mStack = null;
    if (!mTreeStack.isEmpty()) {
      var t = mTreeStack.pop();
      mCurrentTree = t;
      mStack = t.containerStack;
    }
  }

  private WindowTree mCurrentTree;

  public boolean currentTreeContains(JWindow window) {
    checkNotNull(window, "currentTreeContains, window is null");
    var t = topLevelContainer();
    var x = window;
    while (true) {
      if (x == null)
        return false;
      if (x == t)
        return true;
      x = x.parent();
    }
  }

  private void redrawAllTreesIntersectingInvalidRect() {
    pr(VERT_SP,"redraw isect invalid, rect:",INDENT,mInvalidRect);
    List<WindowTree> cs = arrayList();
    cs.addAll(mTreeStack);
    cs.add(mCurrentTree);
    for (var t : cs) {
      var c = t.topLevelContainer;
      if (c.layoutValid() && c.paintValid()) {
        var b = c.totalBounds();
        if (mInvalidRect != null && b.intersects(mInvalidRect)) {
          pr("invalid rect intersects:", c.name());
          prepareTextRecord();
          c.repaint();
        }
      }
      updateView(c);
    }
    mInvalidRect = null;

    // If terminal was modified, flush it
    try {
      pr("flushing terminal");
      terminal().flush();
    } catch (IOException e) {
      throw asRuntimeException(e);
    }
  }

  public void openTreeWithFocus(int width, int height, JWindow window) {
    pushContainerTree();

    var c = pushContainer();
    c.setPreferredSize(new IPoint(width, height));
    thickBorder().pct(100).window(window);
    popContainer();

    focusManager().pushAppend(window);
  }

  //------------------------------------------------------------------


  private Terminal mTerminal;
  private AbstractScreen mScreen;
  private TextGraphics mTextGraphics;

  private WinMgr() {
  }

  static {
    SHARED_INSTANCE = new WinMgr();
  }

}
