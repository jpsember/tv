package tv;

public interface FocusHandler {

  default void gainFocus() {
  }

  default void loseFocus() {
  }

  default void processKeyEvent(KeyEvent k) {
  }

  default void repaint() {
  }

  /**
   * Client should return true only if having focus makes sense. An example
   * where it should return false is if it is a form editing an account that no
   * longer exists
   */
  boolean focusPossible();

  /**
   * Client should return true if the undo/redo commands are available (even if
   * the undo stack is empty)
   */
  boolean undoEnabled();
}