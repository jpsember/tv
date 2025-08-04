package tv;

import js.file.Files;
import js.geometry.IRect;
import js.parsing.DFA;
import js.parsing.DFACache;
import js.parsing.Lexer;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static tv.Util.BORDER_THICK;
import static tv.Util.winMgr;

import static js.base.Tools.*;

public class TextWindow extends JWindow implements FocusHandler {

  public TextWindow() {
    setBorder(BORDER_THICK);

    // Load the sample text file
    var f = new File("example/sample.csv");
    String content;
    if (!f.exists()) {
      alert("Can't find:", f);
      content = "cannot find: " + f;
    } else {
      content = Files.readString(f);
    }
    prepareLexemes(content);
  }


  public int chooseCurrentRow() {
    return 0;
  }

  @Override
  protected String supplyName() {
    return "TextWindow";
  }


  @Override
  public void paint() {
    prepareToRender();

    var transformStringToClip = mClip.location().negate();

    // Do our calculations in string space, then translate when rendering
    int syMin = 0;
    int syMax = mClip.height;
    int sxMin = 0;
    int sxMax = mClip.width;

    todo("!more clever way of clipping, scrolling to particular start row etc");
    todo("better to render to a grid of bytes, then render the whole screen with a few calls to lanterna");

    for (var f : mFrags) {
      if (f.mY >= syMax) break;
      for (int j = 0; j < f.strCount; j++) {
        int sa = j + f.strStart;
        var ps = mPlacedStrs.get(sa);
        plotString(ps.str, ps.x, ps.y);

        if (true) continue;


        var sy = ps.y;
        if (sy < syMin || sy >= syMax) continue;
        var x0 = ps.x;
        var x1 = ps.x + ps.str.length();

        var cx0 = Math.max(x0, sxMin);
        var cx1 = Math.min(x1, sxMax);
        if (cx0 >= cx1) continue;


        var text = ps.str;
        for (int k = cx0; k < cx1; k++) {
          var strIndex = k - x0;
          var r = Render.SHARED_INSTANCE;
          r.drawString(cx0 + transformStringToClip.x, sy + transformStringToClip.y, text.length(), text.substring(cx0 - x0, cx1 - x0));
        }
      }
    }

    // Render the grid


    try {
      var t = winMgr().terminal();
      char prevColor = (char) -1;
      var j = 0;
      var sb = new StringBuilder();
      for (int y = 0; y < mClip.height; y++) {
        t.setCursorPosition( mClip.x,y);
        for (int x = 0; x < mClip.width; x++) {
          var col = mCharGrid[j+0];
          var chr = mCharGrid[j+1];
          j += 2;

          if (col != prevColor) {
            if (sb.length() != 0) {
              pr("plotting:",sb);
              t.putString(sb.toString());
              sb.setLength(0);
            }
            prevColor = col;
            int fgndIndex = ((int)col) & 0xff;
            int bgndIndex = (((int)col) >> 8) & 0xff;
            ColorMgr.SHARED_INSTANCE.setColors(bgndIndex, fgndIndex);
          }
          sb.append(chr);
        }
        if (sb.length() != 0) {
          pr("finished row, plotting:",sb);
          t.putString(sb.toString());
          sb.setLength(0);
        }
        prevColor = (char) -1;
      }

    } catch (IOException e) {
      throw asRuntimeException(e);
    }

    try {

      var cm = ColorMgr.SHARED_INSTANCE;
      cm.setRandom();

      var t = winMgr().terminal();

      t.setCursorPosition(10, 5);
      t.putCharacter('H');
      t.putCharacter('e');
      t.putCharacter('l');
      t.putCharacter('l');
      t.putCharacter('o');
      t.putCharacter('!');
      t.setCursorPosition(0, 0);

      cm.setDefault();

    } catch (Throwable tt) {
      throw asRuntimeException(tt);
    }
  }

  private void prepareToRender() {
    if (mPrepared)
      return;
    var r = Render.SHARED_INSTANCE;
    mClip = r.clipBounds();
    mCharGrid = new char[mClip.size().product() * 2];
    mPrepared = true;
  }

  private boolean mPrepared;
  private IRect mClip;


  @Override
  public boolean focusPossible() {
    return true;
  }

  @Override
  public boolean undoEnabled() {
    return true;
  }


  public void rebuild() {
//    mCurrentAccount = getCurrentRow();
//
//    clearEntries();
//    List<Account> sorted = storage().readAllAccounts();
//    sorted.sort(ACCOUNT_COMPARATOR);
//
//    for (var a : sorted) {
//      openEntry();
//
//      addHint(accountNumberWithNameString(a));
//      addHint(a.name());
//
//      add(new AccountNameField(a.number(), storage().accountName(a.number())));
//      long amount;
//      if (hasBudget(a))
//        amount = unspentBudget(a);
//      else
//        amount = a.balance();
//      add(new CurrencyField(amount));
//      closeEntry(a);
//    }
//
//    setCurrentRow(mCurrentAccount);
    repaint();
  }


  @Override
  public void processKeyEvent(KeyEvent k) {
//    Account a = getCurrentRow();

    switch (k.toString()) {

      case ":Q":
        winMgr().quit();
        break;

      case KeyEvent.ARROW_RIGHT:
        repaint();
        break;

      case KeyEvent.RETURN:
        //        if (a != null) {
//          mListener.viewAccount(a);
//        }
        break;

//      case ":T":
//        focusManager().pushAppend(new TransactionLedger(0, mTransListener));
//        break;
//
//      case ":R":
//        RuleManager.SHARED_INSTANCE.applyRulesToAllTransactions();
//        break;
//
//      case KeyEvent.ADD:
//        mListener.addAccount();
//        rebuild();
//        break;
//
//      case KeyEvent.DELETE_ACCOUNT:
//        if (a != null) {
//          mListener.deleteAccount(a);
//          rebuild();
//        }
//        break;
//
//      case KeyEvent.EDIT:
//        if (a != null) {
//          mListener.editAccount(a);
//          rebuild();
//        }
//        break;
//
//      case KeyEvent.PRINT:
//        if (a != null) {
//          PrintManager.SHARED_INSTANCE.printLedger(a);
//        }
//        break;
//      default:
//        super.processKeyEvent(k);
//        break;
    }
  }


  /**
   * Determine lexeme patterns, and rebuild DFA if necessary
   */
  public File getLexemeDefinitionFile() {
    return new File("example/example.rxp");
  }

  private DFA mDfa;

  public DFA getTextDFA() {
    if (mDfa != null) return mDfa;

    var f = getLexemeDefinitionFile();
    Files.assertExists(f, "Lexeme definition file");

    var txt = Files.readString(f);
    var lexDefinitions = extractLexemeDefinitions(txt);
    var dfa = DFACache.SHARED_INSTANCE.forTokenDefinitions(lexDefinitions);
    mDfa = dfa;
    return mDfa;
  }

  public String extractLexemeDefinitions(String text) {
    todo("!extract definitions without embedded instructions");
    return text;
  }


  private void prepareLexemes(String content) {
    mPlacedStrs.clear();
    var s = new Lexer(getTextDFA()).withNoSkip().withAcceptUnknownTokens().withText(content);
    mFrags.clear();
    var addr = s.filteredAddresses();
    for (var ad : addr) {
      var frag = buildTextFrag(ad);
      mFrags.add(frag);
    }

    layoutFrags(s.inputBytes(), s.tokenInfo());

    //pr("layoutFrags produced:");
    for (var x : mFrags) {
      var count = x.strCount;
      if (count == 0) continue;
//      pr("addr:",x.mAddress);
      int start = x.strStart;
      for (int i = start; i < start + count; i++) {
        var st = mPlacedStrs.get(i);
//        pr(INDENT,st.y,st.x,quote(st.str));
      }
    }
  }

  private static TextFrag buildTextFrag(int address) {
    var f = new TextFrag();
    f.mAddress = address;
    return f;
  }

  private void layoutFrags(byte[] inputBytes, int[] lexInfo) {
    var frags = mFrags;

    var sb = new StringBuilder();

    var x = 0;
    var y = 0;
    for (var f : frags) {
      f.mX = x;
      f.mY = y;
      f.strStart = mPlacedStrs.size();


      var ad = f.mAddress;

      var tokenId = lexInfo[ad + Lexer.F_TOKEN_ID];

      // Determine the visibility of this token type

      boolean visible = true;

      if (!visible) continue;

      var strStart = lexInfo[ad + Lexer.F_TOKEN_OFFSET];
      var strLen = lexInfo[ad + Lexer.TOKEN_INFO_REC_LEN + Lexer.F_TOKEN_OFFSET] - strStart;

      sb.setLength(0);

      for (int i = 0; i < strLen; i++) {
        var ch = inputBytes[strStart + i];
        if (ch == 0x0a) { // Linefeed?
          if (sb.length() != 0) {
            var ps = new PlacedStr();
            ps.str = sb.toString();
            ps.x = x;
            ps.y = y;
            //var index = sPlacedStrs.size();
            mPlacedStrs.add(ps);
            //pstrs.add(ps);
            sb.setLength(0);
          }
          y++;
          x = 0;
          // We may do special formatting later, e.g. for pretty printing or whatever
          continue;
        }
        sb.append((char) ch);
      }
      if (sb.length() != 0) {
        var ps = new PlacedStr();
        ps.str = sb.toString();
        ps.x = x;
        ps.y = y;
        mPlacedStrs.add(ps);
        x += sb.length();
      }
      f.strCount = mPlacedStrs.size() - f.strStart;
    }
  }


  private void plotString(String str, int wx, int sy) {

    var c = mClip;
    if (sy < c.y || sy >= c.endY()) return;

    var x0 = wx;
    var x1 = wx + str.length();

    var cx0 = Math.max(x0, 0);
    var cx1 = Math.min(x1, c.endX());
    if (cx0 >= cx1) return;

    int charsPerRow = c.width * 2;
    int gridIndex = sy * charsPerRow;
    var grid = mCharGrid;

    for (int k = cx0; k < cx1; k++) {
      grid[gridIndex] = 0; // color?
      grid[gridIndex + 1] = str.charAt(k - x0);
      gridIndex += 2;
    }
  }


  private List<PlacedStr> mPlacedStrs = arrayList();
  private List<TextFrag> mFrags = arrayList();


  private char[] mCharGrid;
}

