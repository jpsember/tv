package tv;

import static js.base.Tools.*;

import java.util.Random;

import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.screen.Screen;
import tv.gen.TvConfig;
import js.base.BasePrinter;
import js.base.DateTimeTools;
import js.json.JSMap;

public final class Util {

  public static final int BORDER_NONE = 0;
  public static final int BORDER_THIN = 1;
  public static final int BORDER_THICK = 2;
  public static final int BORDER_ROUNDED = 3;
  public static final int BORDER_TOTAL = 4;

  public static final int STYLE_NORMAL = 0;
  public static final int STYLE_INVERSE = 1;
  public static final int STYLE_MARKED = 2;
  public static final int STYLE_INVERSE_AND_MARK = 3;
  public static final int STYLE_TOTAL = 4;

  public final static void loadUtil() {
  }


  public static void sleepMs(int ms) {
    DateTimeTools.sleepForRealMs(ms);
  }

  public static WinMgr winMgr() {
    return WinMgr.SHARED_INSTANCE;
  }

  public static final JSMap db(Object obj) {
    var m = map();
    if (obj == null)
      m.put("", "NULL");
    else {
      m.put("str", obj.toString());
      m.put("class", obj.getClass().getName());
    }
    return m;
  }

  public static String randomText(int maxLength, boolean withLinefeeds) {
    StringBuilder sb = new StringBuilder();
    Random r = random();
    int len = (int) Math.abs(r.nextGaussian() * maxLength);
    while (sb.length() < len) {
      int wordSize = r.nextInt(8) + 2;
      if (withLinefeeds && r.nextInt(4) == 0)
        sb.append('\n');
      else
        sb.append(' ');
      String sample = "orhxxidfusuytelrcfdlordburswfxzjfjllppdsywgsw"
          + "kvukrammvxvsjzqwplxcpkoekiznlgsgjfonlugreiqvtvpjgrqotzu";
      int cursor = r.nextInt(sample.length() - wordSize);
      sb.append(sample.substring(cursor, cursor + wordSize));
    }
    return sb.toString().trim();
  }

  public static Random random() {
    return sRandom;
  }
  private static Random sRandom = new Random(1965);
  public static final FocusManager focusManager() {
    return FocusManager.SHARED_INSTANCE;
  }

  public static final FocusHandler focus() {
    return focusManager().focus();
  }




  public static boolean quitCommand(KeyEvent k) {
    alert("!can't seem to use command keys reliably, so have user ctrl-c out of program");
    return false;
  }

  private static TvConfig sConfig;

  public static void setUtilConfig(TvConfig config) {
    sConfig = config.build();
  }

  public static TvConfig tvConfig() {
    return sConfig;
  }

  public static MessageWindow sHeader, sFooter;


  private static long sPendingDuration;
  private static long sMessageExpTime;

  public static void setMessageDuration(long seconds) {
    sPendingDuration = seconds * 1000;
  }

  public static void setFooterMessage(Object... msg) {
    var s = BasePrinter.toString(msg);
    sMessageExpTime = 0;
    if (sPendingDuration > 0)
      sMessageExpTime = System.currentTimeMillis() + sPendingDuration;
    sPendingDuration = 0;
    if (sFooter != null) {
      sFooter.setMessageAt(MessageWindow.LEFT, s);
    } else {
      pr(s);
    }
  }

  public static void updateFooterMessage() {
    if (sMessageExpTime != 0 && System.currentTimeMillis() >= sMessageExpTime) {
      setFooterMessage();
    }
  }


  public static Screen mScreen;
  public static TextGraphics mTextGraphics;

  @Deprecated
  public static Screen screen() {
    return mScreen;
  }

  @Deprecated
  public static TextGraphics textGraphics() {
    return mTextGraphics;
  }

}
