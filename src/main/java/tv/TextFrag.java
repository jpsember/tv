package tv;

class TextFrag {
  TextFrag mParent;  // Lexeme that starts this one's paragraph
  int mAddress; // address within Lexer table
  int mY;       // row
  int mX;       // column
  PlacedStr[] strs; // If null, not visible
}
