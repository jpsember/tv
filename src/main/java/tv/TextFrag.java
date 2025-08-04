package tv;

class TextFrag {
  TextFrag mParent;  // Lexeme that starts this one's paragraph
  int mAddress; // address within Lexer table
  int mY;       // row
  int mX;       // column

  // Index into the buffer where this fragment's strings start
  int strStart;
  // Number of strings
  int strCount;
}
