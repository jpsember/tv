package tv;

import static js.base.Tools.*;

import js.base.BasePrinter;

public class MessageWindow extends JWindow {

  public static final int TOPLEFT = 0, TOP = 1, TOPRIGHT = 2, LEFT = 3, CENTER = 4, RIGHT = 5, BOTTOMLEFT = 6,
      BOTTOM = 7, BOTTOMRIGHT = 8;
  private static final int POS_TOTAL = 9;

  public MessageWindow setMessageAt(int position, Object... message) {
    var m = BasePrinter.toString(message);
    mMessages[position] = m;
    repaint();
    return this;
  }

  @Override
  public void paint() {
    var r = Render.SHARED_INSTANCE;
    var b = r.clipBounds();
    b = b.withInset(1, 0);

    for (int row = 0; row < 3; row++) {
      if (b.height < 3 && row != 1)
        continue;
      int y = (row == 0) ? b.y : (row == 1 ? b.midY() : b.endY() - 1);

      var cursor = 0;
      var cursorMax = b.width;
      int posOffset = row * 3;

      var sb = new StringBuilder();
      for (int i = 0; i < 3; i++) {
        var m = msg(i + posOffset);

        // Determine where to start rendering this message
        int cursorTab = 0;
        switch (i) {
        case 1:
          cursorTab = (b.width - m.length()) / 2;
          break;
        case 2:
          cursorTab = cursorMax - m.length();
          break;
        }

        if (cursorTab < cursor)
          continue;
        sb.append(spaces(cursorTab - cursor));
        cursor = cursorTab;

        if (cursor + m.length() > cursorMax)
          continue;
        sb.append(m);
        sb.append("  ");
        cursor = sb.length();
      }

      r.drawString(b.x, y, b.width, sb.toString());
    }
  }

  private String msg(int position) {
    return nullToEmpty(mMessages[position]);
  }

  private String[] mMessages = new String[POS_TOTAL];
}
