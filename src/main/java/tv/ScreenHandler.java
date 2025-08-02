package tv;

import com.googlecode.lanterna.input.KeyStroke;

import js.geometry.IPoint;

@Deprecated // These should be handled by the active window
public interface ScreenHandler {
  
  void processKey(KeyStroke keyStroke);

  void repaint();
  
  void processNewSize(IPoint size);
}
