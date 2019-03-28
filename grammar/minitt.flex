package org.ice1000.tt;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import static org.ice1000.tt.psi.MiniTTTokenType.*;
import static org.ice1000.tt.psi.MiniTTTypes.*;

import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static com.intellij.psi.TokenType.WHITE_SPACE;

%%

%{
  public MiniTTLexer() { this((java.io.Reader)null); }

  private int commentStart = 0;
  private int commentDepth = 0;
%}

%public
%class MiniTTLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode
%ignorecase
%eof{ return;
%eof}

WHITE_SPACE=[\ \t\f\r\n]+
IDENTIFIER=[a-z_'\\][a-zA-Z_0-9'\\]*
CONSTRUCTOR=[A-Z][a-zA-Z_0-9'\\]*
MULTIPLY = \*|\\times|\xd7|\u2716
UNIVERSE = Type[0-9]*

%%

= { return EQ; }
: { return COMMA; }
, { return COLON; }
_ { return META_VAR; }
; { return SEMICOLON; }
1 { return ONE_KEYWORD; }
0 { return UNIT_KEYWORD; }
\. { return DOT; }
\| { return SEP; }
\( { return LEFT_PAREN; }
\{ { return LEFT_BRACE; }
\) { return RIGHT_PAREN; }
\} { return RIGHT_BRACE; }
\.1 { return DOT_ONE; }
\.2 { return DOT_TWO; }
let { return LET_KEYWORD; }
rec { return REC_KEYWORD; }
sum { return SUM_KEYWORD; }
"++"  { return CONCAT; }
split { return SPLIT_KEYWORD; }
const { return CONST_KEYWORD; }
->|\u21d2 { return ARROW; }
=>|\u2192 { return DOUBLE_ARROW; }
{MULTIPLY} { return MUL; }
{UNIVERSE} { return TYPE_UNIVERSE; }
{IDENTIFIER}  { return IDENTIFIER; }
{CONSTRUCTOR} { return CONSTRUCTOR_NAME; }
{WHITE_SPACE} { return WHITE_SPACE; }
\\Pi|\u03A0    { return PI; }
\\Sigma|\u03A3 { return SIGMA; }
\\lambda|\u03BB { return LAMBDA; }
// \\forall|\u2200 { return FORALL; }

[^] { return BAD_CHARACTER; }
