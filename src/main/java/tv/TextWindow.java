package tv;


import js.file.Files;
import js.parsing.DFA;
import js.parsing.DFACache;
import js.parsing.Lexer;

import java.io.File;
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
    var r = Render.SHARED_INSTANCE;
    var clip = r.clipBounds();

    pr("clip:",clip);
  }

  private void prepareToRender() {
    if (mPrepared)
      return;

    mPrepared = true;
  }

  private boolean mPrepared;


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
    List<TextFrag> frags = arrayList();
    var addr = s.filteredAddresses();
    for (var ad : addr) {
      var frag = buildTextFrag(ad);
      frags.add(frag);
    }

    layoutFrags(s.inputBytes(), s.tokenInfo(), frags);

    //pr("layoutFrags produced:");
    for (var x : frags) {
      var count = x.strCount;
      if (count == 0) continue;
//      pr("addr:",x.mAddress);
      int start = x.strStart;
      for (int i = start; i < start+count; i++) {
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

  private void layoutFrags(byte[] inputBytes, int[] lexInfo, List<TextFrag> frags) {

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

      var strStart = lexInfo[ad+Lexer.F_TOKEN_OFFSET];
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
        sb.append((char)ch);
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

  private List<PlacedStr> mPlacedStrs = arrayList();

}

