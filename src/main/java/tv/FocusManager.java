package tv;

import static tv.Util.*;
import static js.base.Tools.*;

import java.util.List;
import java.util.Stack;

import js.base.BaseObject;
import js.geometry.MyMath;
import js.json.JSMap;

/**
 * Manages keyboard focus, and handles a stack of windows that have the focus.
 */
public class FocusManager extends BaseObject {

  public static final FocusManager SHARED_INSTANCE;

  private FocusManager() {
    //alertVerbose();
  }

  public FocusHandler focus() {
    return mFocus;
  }

  void update() {
    if (focus() != null)
      return;
    var lst = handlers(null);
    if (lst.isEmpty())
      return;
    set(lst.get(0));
  }

  public void set(FocusHandler h) {
    if (h == mFocus)
      return;
    if (mFocus != null) {
      winMgr().hideCursor();
      mFocus.loseFocus();
      mFocus.repaint();
    }
    if (h != null && h instanceof JWindow)
      checkArgument(((JWindow) h).parent() != null, "attempt to focus a window that isn't visible:", h);
    mFocus = h;
    if (h != null) {
      h.gainFocus();
      h.repaint();
    }
  }

  public void move(JWindow rootWindowOrNull, int amount) {
    var focusList = handlers(rootWindowOrNull);
    int slot = focusList.indexOf(mFocus);
    switch (amount) {
    case -1:
    case 1:
      slot = MyMath.myMod(slot + amount, focusList.size());
      break;
    default:
      badArg("unhandled moveFocus arg", amount);
    }
    set(focusList.get(slot));
  }

  public void moveToNextButton(JWindow rootWindowOrNull) {
    var focusList = handlers(rootWindowOrNull);
    int slot = focusList.indexOf(mFocus);
    int j = slot + 1;
    for (int i = j; i < j + focusList.size(); i++) {
      var f = focusList.get(i % focusList.size());
      if (f instanceof WidgetWindow) {
        var w = (WidgetWindow) f;
        if (w.isButton()) {
          set(f);
          return;
        }
      }
    }
  }

  public final List<FocusHandler> handlers(JWindow topLevelWindowOrNull) {
    var w = nullTo(topLevelWindowOrNull, winMgr().topLevelContainer());
    List<FocusHandler> out = arrayList();
    auxFocusList(out, w);
    return out;
  }

  private void auxFocusList(List<FocusHandler> list, JWindow window) {
    if (window instanceof FocusHandler) {
      list.add((FocusHandler) window);
    }

    for (var c : window.children())
      auxFocusList(list, c);
  }

  private FocusHandler mFocus;

  static {
    SHARED_INSTANCE = new FocusManager();
  }

  private static final int METHOD_APPEND = 0, METHOD_REPLACE = 1;

  public void setTopLevelContainer(JContainer c) {
    mOurTopLevelContainer = c;
  }

  /**
   * Have next pushed window be the new top level container
   */
  public FocusManager asNewTopLevel() {
    mNewTopLevelFlag = true;
    return this;
  }

  /**
   * Push focus on stack, append window to top level container, and make it the
   * new focus
   */
  public void pushAppend(JWindow window) {
    push(window, METHOD_APPEND);
  }

  /**
   * Push focus on stack, replace top level container's contents with this new
   * window, and make it the focus
   */
  public void pushReplace(JWindow window) {
    push(window, METHOD_REPLACE);
  }

  private void push(JWindow window, int method) {
    log("push", window.name(), "method:", method);

    // If window is already in the stack somewhere, pop to it
    log("...looking for window in stack");
    int popTo = -1;
    for (int i = mStack.size() - 1; i >= 0; i--) {
      var ent = mStack.get(i);
      log("......stack element:", INDENT, ent);
      if (ent.oldFocusHandler == window) {
        log("...found window in stack at:", i);
        popTo = i;
      }
    }
    if (popTo >= 0) {
      log("...popping stack to size", popTo, "to restore stacked focus");
      while (mStack.size() > popTo) {
        pop();
      }
      return;
    }

    var mgr = winMgr();
    checkNotNull(window);
    var top = ourTopLevelContainer();
    checkState(top != window);

    var ent = new StackEntry(method, mFocus);
    ent.oldTopLevelContainer = top;
    ent.windows.addAll(top.children());

    // If window is not already visible, add it, and modify stack accordingly
    if (!mgr.inView(window)) {
      switch (method) {
      default:
        notSupported("push method");
        break;
      case METHOD_REPLACE:
        top.removeChildren();
        top.addChild(window);
        break;
      case METHOD_APPEND:
        top.addChild(window);
        break;
      }
    }
    if (mNewTopLevelFlag) {
      mOurTopLevelContainer = window;
    }
    mNewTopLevelFlag = false;
    mStack.push(ent);
    log("...pushed entry to stack, size now:", mStack.size());
    trySettingFocus(window);
  }

  private void trySettingFocus(JWindow window) {
    FocusHandler newHandler = null;

    if (window instanceof FocusHandler) {
      newHandler = (FocusHandler) window;
    } else {
      // See which children are focus handlers
      var childHandlers = handlers(window);
      if (!childHandlers.isEmpty())
        newHandler = childHandlers.get(0);
    }
    if (newHandler == null) {
      alert("window has no FocusHandlers:", window);
    } else
      set(newHandler);
  }

  public void pop() {
    log("pop; stack size:", mStack.size());
    if (mStack.isEmpty()) {
      badState("FocusHandler stack is empty");
    }

    var ent = mStack.pop();
    var top = ourTopLevelContainer();
    top.removeChildren();
    for (var child : ent.windows) {
      top.addChild(child);
    }
    mOurTopLevelContainer = ent.oldTopLevelContainer;

    // If the restored top-level container is not in the current window tree, 
    // pop window trees until it is
    var mgr = winMgr();
    while (!mgr.currentTreeContains(mOurTopLevelContainer)) {
      mgr.popContainerTree();
    }

    set(ent.oldFocusHandler);
  }

  private static class StackEntry extends BaseObject {
    StackEntry(int method, FocusHandler handlerToSave) {
      this.windows = arrayList();
      this.oldFocusHandler = handlerToSave;
    }

    @Override
    public JSMap toJson() {
      var m = map();
      if (oldFocusHandler != null)
        m.put("focus_handler", oldFocusHandler.toString());
      if (oldTopLevelContainer != null)
        m.put("top_level_container", oldTopLevelContainer.name());
      var lst = list();
      for (var w : windows)
        lst.add(w.name());
      m.put("windows", lst);
      return m;
    }

    FocusHandler oldFocusHandler;
    JWindow oldTopLevelContainer;
    List<JWindow> windows;
  }

  public boolean popIfPossible() {
    // Don't pop the last container
    if (mStack.size() == 0)
      return false;
    pop();
    return true;
  }

  private JWindow ourTopLevelContainer() {
    var c = mOurTopLevelContainer;
    if (c == null)
      c = winMgr().topLevelContainer();
    return c;
  }

  private Stack<StackEntry> mStack = new Stack<>();
  private boolean mNewTopLevelFlag;
  private JWindow mOurTopLevelContainer;



  /**
   * If the current focus is 'invalid', e.g. due to an undo or redo, pop its
   * stack
   */
  public void validate() {
    var f = focus();
    if (f == null)
      return;
    if (!f.focusPossible()) {
      pop();
      validate();
    }
  }

}
