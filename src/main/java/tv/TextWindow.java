package tv;

import com.googlecode.lanterna.TextColor;
import js.file.Files;
import js.geometry.IPoint;
import js.geometry.IRect;
import js.parsing.DFA;
import js.parsing.DFACache;
import js.parsing.Lexer;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static tv.Util.*;

import static js.base.Tools.*;

public class TextWindow extends JWindow implements FocusHandler {

  public TextWindow() {
    setBorder(BORDER_THICK);

    // Load the sample text file
    var f = tvConfig().textFile();
    Files.assertExists(f,"text_file");
    String content  = Files.readString(f);
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
    try {
      prepareToRender();

      todo("!more clever way of clipping, scrolling to particular start row etc");

      // Render the fragments into our grid
      //
      for (var frag : mFrags) {
        for (int j = 0; j < frag.strCount; j++) {
          int sa = j + frag.strStart;
          var ps = mPlacedStrs.get(sa);
          plotString(frag.colorCode, ps.str, ps.x, ps.y);
        }
      }

      // Render the grid
      //
      var terminal = winMgr().terminal();
      pr("...rendering grid");
      // We only render string when the color has changed, or we've reached the end of a row
      final char UNKNOWN_COLOR_CODE = Character.MAX_VALUE;

      char prevColorCode = UNKNOWN_COLOR_CODE;
      var gridIndex = 0;
      var sb = new StringBuilder();

      for (int y = 0; y < mGridSize.y; y++) {

        terminal.setCursorPosition(mClip.x, mClip.y + y);

        for (int x = 0; x < mGridSize.x; x++) {

          var colorCode = mCharGrid[gridIndex + 0];
          var charCode = mCharGrid[gridIndex + 1];
          gridIndex += 2;

          if (colorCode != prevColorCode) {
            if (sb.length() != 0) {
              terminal.putString(sb.toString());
              sb.setLength(0);
            }
            prevColorCode = colorCode;
            int fgndIndex = ((int) colorCode) & 0xff;
            int bgndIndex = (((int) colorCode) >> 8) & 0xff;
            ColorMgr.SHARED_INSTANCE.setColors(bgndIndex, fgndIndex);
          }
          sb.append(charCode);
        }

        if (sb.length() != 0) {
          terminal.putString(sb.toString());
          sb.setLength(0);
        }
      }

      pr("GRID PLOTTED");
    } catch (IOException e) {
      throw asRuntimeException(e);
    }
  }

  private void prepareToRender() {
    if (mPrepared)
      return;
    todo("!do we need to rebuild the mCharGrid each time?");
    var r = Render.SHARED_INSTANCE;
    mClip = r.clipBounds();
    mGridSize = mClip.size();
    mCharGrid = new char[mGridSize.product() * 2];
    mPrepared = true;
  }

  private boolean mPrepared;
  private IRect mClip;
  private IPoint mGridSize;


  @Override
  public boolean focusPossible() {
    return true;
  }

  @Override
  public boolean undoEnabled() {
    return true;
  }

  @Override
  public void processKeyEvent(KeyEvent k) {

    switch (k.toString()) {

      case ":Q":
        winMgr().quit();
        break;

      case KeyEvent.ARROW_RIGHT:
        repaint();
        break;

      case KeyEvent.RETURN:
        break;
    }
  }

  private char colorCodeForToken(int tokenId) {
    int fgnd = tokenId;
    int bgnd = (tokenId + mOurStandardColors.size() / 2) % mOurStandardColors.size();
    return (char) ((fgnd << 8) | bgnd);
  }

  /**
   * Determine lexeme patterns, and rebuild DFA if necessary
   */
  public File getLexemeDefinitionFile() {
    var f = tvConfig().tokenFile();
    Files.assertExists(f,"token_file");
    return f;
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

  private static String defColorStr = "#EF8B10 #EF1021 #EA15AC #B60EF1 #4F0DF2 #149CEB #4DAAB2 #42BD88 #A9CD32 #C48F3B #AC537C";

  private List<TextColor> mOurStandardColors = arrayList();
  private List<TextColor> mTokenIdColorCodes;

  private void prepareColors() {

    for (var s : split(defColorStr, ' ')) {
      s = chompPrefix(s, "#");
      if (s.isEmpty()) continue;
      var col = ColorMgr.SHARED_INSTANCE.parseColor(s);
      mOurStandardColors.add(col);
    }
ColorMgr.SHARED_INSTANCE.defineColors(mOurStandardColors);
  }

  private void prepareLexemes(String content) {
    mPlacedStrs.clear();
    var dfa = getTextDFA();

    prepareColors();

    // determine color codes for each token id
    mTokenIdColorCodes = arrayList();
    for (int i = 0; i < dfa.tokenNames().length; i++) {
      var j = mOurStandardColors.get(i % mOurStandardColors.size());
      mTokenIdColorCodes.add(j);
    }

    var s = new Lexer(dfa).withNoSkip().withAcceptUnknownTokens().withText(content);
    mFrags.clear();
    var addr = s.filteredAddresses();
    for (var ad : addr) {
      var frag = buildTextFrag(ad);
      mFrags.add(frag);
    }
    layoutFrags(s.inputBytes(), s.tokenInfo());
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

      f.colorCode = colorCodeForToken(tokenId);
pr("tokenId:",tokenId,"color:",f.colorCode);

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
            pr("...adding str:",ps.str);
            mPlacedStrs.add(ps);
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
        pr("...adding str:",ps.str);
        ps.x = x;
        ps.y = y;
        mPlacedStrs.add(ps);
        x += sb.length();
      }
      f.strCount = mPlacedStrs.size() - f.strStart;
    }
  }


  /**
   * Plot a string, in a particular color, into the view
   */
  private void plotString(char colorCode, String str, int sx, int sy) {

    // Do we need to clip the rectangle?
    var c = mGridSize;

    if (sy < 0 || sy >= c.y) return;

    var x0 = sx;
    var x1 = sx + str.length();

    if (x0 < 0) x0 = 0;
    if (x1 > c.x)
      x1 = c.x;
    if (x0 >= x1) return;

    int charsPerRow = c.x * 2;
    int gridIndex = sy * charsPerRow;
    var grid = mCharGrid;

    for (int k = x0; k < x1; k++) {
      grid[gridIndex] = colorCode;
      grid[gridIndex + 1] = str.charAt(k - x0);
      gridIndex += 2;
    }
  }


  private List<PlacedStr> mPlacedStrs = arrayList();
  private List<TextFrag> mFrags = arrayList();

  private char[] mCharGrid;
}

