package tv;

import static js.base.Tools.*;

import js.app.App;

public class TvMain extends App {

  public static void main(String[] args) {
    loadTools();
    TvMain app = new TvMain();
    //app.setCustomArgs("log_file bk_log.txt close_accounts 2024/6/15 -v");

    app.startApplication(args);
    app.exitWithReturnCode();
  }

  @Override
  public String getVersion() {
    return "1.0";
  }

  @Override
  protected void registerOperations() {
    registerOper(new TvOper());
  }

}
