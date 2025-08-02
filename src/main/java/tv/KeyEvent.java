package tv;

import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import static js.base.Tools.*;

/**
 * Wraps a KeyStroke to a string that includes information about Alt, Shift,
 * Control keys.
 * 
 * "[A][C][S]:{character or KeyType name}"
 */
public class KeyEvent {

  public static final String //
  RETURN = ":Enter" //
      , ESCAPE = ":Escape" //
      , DELETE_TRANSACTION = ":∂" //
      , DELETE_ACCOUNT = ":Î" //
      , CONTROL_ENTER = "C:Enter" //
      , ADD = ":A" //
      , EDIT = ":E" //   
      , OPTION_Z = ":Ω" //
      , PRINT = ":P" //
      , JUMP = ":j" //
      , JUMP_BACK = ":b" //
      , OPTION_SHIFT_Z = ":¸" //
      , MARK = ":." //
      , UNMARK_ALL = ":≥" //
      , ARROW_UP = ":ArrowUp" //
      , ARROW_DOWN = ":ArrowDown" //
      , ARROW_LEFT = ":ArrowLeft" //
      , ARROW_RIGHT = ":ArrowRight" //
      , QUIT = ":œ" //
      , UNDO = OPTION_Z // 
      , REDO = OPTION_SHIFT_Z //
  ;

  public KeyEvent(KeyStroke k) {
    mKeyStroke = k;
    var sb = new StringBuilder();
    if (k.isAltDown())
      sb.append('A');
    if (k.isCtrlDown())
      sb.append('C');
    if (k.isShiftDown())
      sb.append('S');
    sb.append(':');
    switch (k.getKeyType()) {
    case Character:
      sb.append(k.getCharacter());
      break;
    default:
      sb.append(k.getKeyType().name());
      break;
    }
    mString = sb.toString();
  }

  @Override
  public String toString() {
    return mString;
  }

  public boolean is(String str) {
    return mString.equals(str);
  }

  private String mString;
  private KeyStroke mKeyStroke;

  public KeyType keyType() {
    return mKeyStroke.getKeyType();
  }

  public boolean hasCtrlOrAlt() {
    return mKeyStroke.isAltDown() || mKeyStroke.isCtrlDown();
  }

  public Character getCharacter() {
    checkArgument(keyType() == KeyType.Character);
    return mKeyStroke.getCharacter();
  }

}
