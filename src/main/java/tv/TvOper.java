package tv;

import static tv.Util.*;
import static js.base.Tools.*;

import js.app.AppOper;
import js.app.HelpFormatter;
import js.base.BasePrinter;
import tv.gen.TvConfig;

public class TvOper extends AppOper {

  @Override
  public String userCommand() {
    return "tv";
  }

  @Override
  protected String shortHelp() {
    return "Text Viewer program";
  }

  @Override
  protected void longHelp(BasePrinter b) {
    var hf = new HelpFormatter();
    hf.addItem("[ <filename> ]", "file to view");
    b.pr(hf);
  }

  @Override
  public TvConfig defaultArgs() {
    return TvConfig.DEFAULT_INSTANCE;
  }

  @Override
  public TvConfig config() {
    if (mConfig == null) {
      mConfig = super.config();
    }
    return mConfig;
  }

  @Override
  public void perform() {

    logger(new Logger(config().logFile()));

    setUtilConfig(config());

    var mgr = winMgr();

    try {
      mgr.open();
      mAccounts = new TextWindow();

      // Construct root container
      mgr.pushContainer();

      // Add a small header
      {
        var h = new MessageWindow();
        Util.sHeader = h;
        h.setMessageAt(MessageWindow.CENTER, "tv 1.0").setMessageAt(MessageWindow.RIGHT, "more to come");
        mgr.chars(1).window(h);
      }

      {
        // Create a container for the text file
        var c = mgr.horz().pushContainer();

        {
          mgr.pct(30);
          mgr.thickBorder();
          mgr.window(mAccounts);
          focusManager().setTopLevelContainer(c);
        }
//        {
//          mgr.pct(70);
//         var c = new JContainer();
//          mgr.pushContainer(c);
//          focusManager().setTopLevelContainer(c);
//          mgr.popContainer();
//        }

        mgr.popContainer();
      }

      // Add a small footer
      {
        var h = new MessageWindow();
        Util.sFooter = h;
        mgr.chars(1).window(h);
      }

      mgr.popContainer();
      mgr.doneConstruction();
      mgr.mainLoop();
    } catch (Throwable t) {
      setError(mgr.closeIfError(t));
    }
  }

  private TvConfig mConfig;

  private TextWindow mAccounts;
}
