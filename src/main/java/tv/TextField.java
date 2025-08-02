package tv;

public class TextField implements LedgerField {

  public TextField(String text) {
    mText = text;
  }

  @Override
  public String toString() {
    return mText;
  }

  private String mText;
}
