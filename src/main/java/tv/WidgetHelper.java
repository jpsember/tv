package tv;

import static js.base.Tools.*;

import java.util.Map;

import js.base.BaseObject;

public abstract class WidgetHelper extends BaseObject {

  /**
   * Construct a hint for a prefix (which will be a nonempty lower case string)
   */
  public abstract String constructHint(String prefix);

  public String getHint(String prefix) {
    String hint = "";
    if (!prefix.isEmpty()) {
      var prefixLower = prefix.toLowerCase();
      hint = mHintResultsMap.get(prefix);
      if (hint == null) {
        hint = nullToEmpty(constructHint(prefixLower));
        mHintResultsMap.put(prefix, hint);
      }
    }
    return hint;
  }

  public static int compareLowerCase(String a, String b) {
    return String.CASE_INSENSITIVE_ORDER.compare(a, b);
  }

  public static boolean matchCaseInsens(String a, String b) {
    return compareLowerCase(a, b) == 0;
  }

  public static boolean hasPrefix(String string, String prefix) {
    if (string.length() < prefix.length())
      return false;
    return matchCaseInsens(string.substring(0, prefix.length()), prefix);
  }

  // A map of user prefix (in lower case) and the hint that should be shown for that prefix (which might be an empty string)
  private Map<String, String> mHintResultsMap = hashMap();

}
