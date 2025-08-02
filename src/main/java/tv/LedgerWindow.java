package tv;

import static tv.Util.*;
import static js.base.Tools.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.googlecode.lanterna.Symbols;
import com.googlecode.lanterna.input.KeyType;

import js.base.DateTimeTools;
import js.geometry.MyMath;

public abstract class LedgerWindow extends JWindow implements FocusHandler {

  public LedgerWindow() {
    setBorder(BORDER_THICK);
  }

  public boolean isItemMarked(Object auxData) {
    return false;
  }

  public void setHeaderHeight(int height) {
    mHeaderHeight = height;
  }

  public void setFooterHeight(int height) {
    mFooterHeight = height;
  }

  public int chooseCurrentRow() {
    return 0;
  }

  @Override
  protected String supplyName() {
    return "LedgerWindow";
  }


  @Override
  public void paint() {
    prepareToRender();
    var r = Render.SHARED_INSTANCE;
    var clip = r.clipBounds();

    int headerRowTotal = mHeaderHeight;
    var footerRowTotal = mFooterHeight;
    if (!hasFocus()) {
      footerRowTotal = 0;
    }
    int headerScreenY = clip.y;
    int bodyRowTotal = clip.height - headerRowTotal - footerRowTotal;
    int bodyScreenY = headerScreenY + headerRowTotal;
    var footerScreenY = bodyScreenY + bodyRowTotal;
    mLastBodyRowTotal = bodyRowTotal;


  }



  @Override
  public void processKeyEvent(KeyEvent k) {
    Integer targetEntry = null;
    int pageSize = mLastBodyRowTotal;

    boolean resetHint = true;

    switch (k.toString()) {
    case KeyEvent.ARROW_UP:
      targetEntry = mCursorRow - 1;
      break;
    case KeyEvent.ARROW_DOWN:
      targetEntry = mCursorRow + 1;
      break;
    case ":PageUp":
      targetEntry = mCursorRow - pageSize;
      break;
    case ":PageDown":
      targetEntry = mCursorRow + pageSize;
      break;
    case ":Home":
      targetEntry = 0;
      break;
    case ":End":
     // targetEntry = mEntries.size();
      break;
    case ":Q":
      winMgr().quit();
      return;
    default:

      break;
    }


  }

  private void prepareToRender() {
    if (mPrepared)
      return;


    mPrepared = true;
  }

  private boolean mPrepared;


  public void clearEntries() {

  }

  private List<LedgerField> mLedgerFieldList;

  public LedgerWindow verticalSeparators() {
    checkState(mSep == null);
    mPendingSep = 1;
    return this;
  }

  public LedgerWindow spaceSeparators() {
    checkState(mSep == null);
    mPendingSep = 0;
    return this;
  }

  public LedgerWindow openEntry() {
    checkState(mLedgerFieldList == null);
    mLedgerFieldList = arrayList();
    return this;
  }

  public LedgerWindow add(LedgerField f) {
    checkState(mLedgerFieldList != null);
    mLedgerFieldList.add(f);
    return this;
  }


  private Map<String, Integer> mHintToRowNumberMap;






  private StringBuilder msb = new StringBuilder();
  private int mCursorRow;
  private int mLastBodyRowTotal = 10;
  private int mPendingSep = 1;
  private Integer mSep;
  private int mHeaderHeight = 2;
  private int mFooterHeight = 0;


}
