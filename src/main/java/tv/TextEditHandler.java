package tv;

public interface TextEditHandler extends FocusHandler {

  default String validate(String text) {
    return text;
  }

}
