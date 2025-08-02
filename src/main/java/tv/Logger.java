package tv;

import static js.base.Tools.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;

import org.apache.commons.io.output.NullWriter;

import js.base.DateTimeTools;
import js.base.LoggerInterface;
import js.file.Files;

public class Logger implements LoggerInterface {

  public Logger(File logFile) {
    try {
      if (Files.empty(logFile))
        mWriter = NullWriter.NULL_WRITER;
      else
        mWriter = new BufferedWriter(new FileWriter(logFile));
      mWriter.append("\n\nBk Logger opened at: " + DateTimeTools.humanTimeString()
          + "\n--------------------------------------------------\n");
    } catch (Throwable t) {
      throw asRuntimeException(t);
    }
  }

  @Override
  public void println(String message) {
    try {
      mWriter.append(message);
      mWriter.write('\n');
      mWriter.flush();
    } catch (Throwable t) {
      throw asRuntimeException(t);
    }
  }

  private Writer mWriter;

}