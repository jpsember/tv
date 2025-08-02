package tv;

import static js.base.Tools.*;

import java.util.List;

import js.base.BasePrinter;

public class FooterWindow extends JWindow {

  public FooterWindow setMessageAt(int row, Object... message) {
    var m = BasePrinter.toString(message);
    while (mMessages.size() <= row) {
      mMessages.add("");
      setLayoutInvalid();
      int minSize = row + 1;
      if (minSize > 1)
        minSize += 1;
      setSizeChars(row + 1);
    }
    mMessages.set(row, m);

    repaint();
    return this;
  }

  @Override
  public void paint() {
    var r = Render.SHARED_INSTANCE;
    var b = r.clipBounds();

    int y = b.y;
    if (b.height > 1) {
      plotHorzLine(y);
      y++;
    }

    for (var msg : mMessages) {
      if (msg.isEmpty())
        continue;
      r.drawString(b.x + 1, y, b.width - 2, msg);
      y++;
    }
  }

  private List<String> mMessages = arrayList();

}
