grammar loan;

options {
	output = AST;
}

tokens {
	NUM_LIT = 'numeric';
	BOOL_LIT = 'boolean';
	STRING_LIT = 'string';
	URI = 'uri';
	CURIE = 'curie';
	PRODUCT = 'product';
	JUDGEMENT = 'judgement';
	GOAL = 'goal';
}

// $<Parser

document 
	:	base_rule? (at_rule | sentence)* EOF;
	
base_rule 
	:	AT_BASE^ IRI_REF DOT!
	;

at_rule :	AT_IMPORT^ IRI_REF DOT!
	|	AT_PREFIX^ PNAME_NS IRI_REF DOT!
	|	AT_DELAY^ LPAREN! INTEGER RPAREN! DOT!
	;

sentence 
	:	statement ( judgement
	                  | question
	                  | goal
	                  )
	;
	
judgement 
	:	DOT^ truthvalue?
	;
	
goal	:	EXCLAMATION^ truthvalue?
	;
	
question 
	:	QUESTION^
	;
	
truthvalue 
	:	PERCENT^ (DECIMAL | INTEGER) (SEMICOLON! DECIMAL)? PERCENT!;

statement 
	:	unary_statement ((CONJ^ | SEMICOLON^ | COMMA^ | DISJ^) unary_statement)*
	;
	
unary_statement 
	:	NOT simple_statement
	|	PAST simple_statement
	|	PRESENT simple_statement
	|	FUTURE simple_statement
	|	simple_statement
	;
	
simple_statement 
	:	term ((INHERITANCE^ | SIMILARITY^ | INSTANCE^ | PROPERTY^ | INSTANCE_PROPERTY^ | IMPLICATION^ | IMPLICATION_PRED^ | IMPLICATION_RETRO^ | IMPLICATION_CONC^ | EQUIVALENCE^ | EQUIVALENCE_PRED^ | EQUIVALENCE_CONC^) term)?
	;
	
	
term 	:	difference ((AMPERSAND^ | BAR^) difference)*
	;

ext_set :	OPEN_BRACE^ (term (COMMA! term)*)? CLOSE_BRACE!
	;
	
int_set :	LBRACKET^ (term (COMMA! term)*)? RBRACKET!
	;
	
ext_image 
	:	EXT_IMG^ LPAREN! term COMMA! term_or_wild (COMMA! term_or_wild)* RPAREN!
	;
	
int_image 
	:	INT_IMG^ LPAREN! term COMMA! term_or_wild (COMMA! term_or_wild)* RPAREN!
	;
	
term_or_wild 
	:	WILDCARD
	|	term
	;
	
difference 
	:	product ((MINUS^ | TILDE^) product)*
	;
	
product :	atomic_term (STAR atomic_term)* -> ^(PRODUCT atomic_term*)
	;

atomic_term 
	:	ext_set
	|	int_set
	|	ext_image
	|	int_image
	|	LPAREN! statement RPAREN!
	|	variable
	|	iriRef
	|	literal
	;
	
variable 
	:	query_variable
	|	statement_variable
	;
	
query_variable 
	:	QUERY_VAR
	;
	
statement_variable 
	:	STM_VAR
	;
	
	
	
literal
	:	numericLiteral -> ^(NUM_LIT numericLiteral)
	|	booleanLiteral -> ^(BOOL_LIT booleanLiteral)
	|	string -> ^(STRING_LIT string)
	;
	
numericLiteral
    : numericLiteralUnsigned | numericLiteralPositive | numericLiteralNegative
    ;

numericLiteralUnsigned
    : INTEGER
    | DECIMAL
    | DOUBLE
    ;

numericLiteralPositive
    : INTEGER_POSITIVE
    | DECIMAL_POSITIVE
    | DOUBLE_POSITIVE
    ;

numericLiteralNegative
    : INTEGER_NEGATIVE
    | DECIMAL_NEGATIVE
    | DOUBLE_NEGATIVE
    ;

booleanLiteral
    : TRUE
    | FALSE
    ;

string
    : STRING_LITERAL1
    | STRING_LITERAL2
    | STRING_LITERAL_LONG1
    | STRING_LITERAL_LONG2
    ;

iriRef
    : IRI_REF -> ^(URI IRI_REF)
    | prefixedName -> ^(CURIE prefixedName)
    ;

prefixedName
    : PNAME_LN
    | PNAME_NS
    ;


// $>

// $<Lexer

WS
    : (' '| '\t'| EOL)+ { $channel=HIDDEN; }
    ;

AT_IMPORT 
	:	'@import'
	;
	
AT_PREFIX 
	:	'@prefix'
	;
	
AT_BASE :	'@base'
	;
	
AT_DELAY 
	:	'@delay'
	;

protected    
INHERITANCE 
	:	'-->';

protected	
SIMILARITY
	:	'<->';
	
protected
INSTANCE 
	:	'}->'
	;
	
protected
PROPERTY
	:	'--['
	;
	
protected
INSTANCE_PROPERTY 
	:	'}-['
	;
	
protected
IMPLICATION 
	:	'==>'
	;
	
protected
IMPLICATION_PRED 
	:	'=/>'
	;
	
protected
IMPLICATION_RETRO 
	:	'=\\>'
	;
	
protected
IMPLICATION_CONC
	:	'=|>'
	;
	
protected
EQUIVALENCE 
	:	'<=>'
	;
	
protected
EQUIVALENCE_PRED 
	:	'</>'
	;
	
protected
EQUIVALENCE_CONC 
	:	'<|>'
	;
	
EXT_IMG	:	'ext'
	;
	
INT_IMG :	'int'
	;
	
WILDCARD 
	:	'_'
	;

	
NOT	:	'!!'
	;
	
PAST	:	'\\>'
	;
	
PRESENT	:	'|>'
	;
	
FUTURE 	:	'/>'
	;
	
CONJ	:	'&&'
	;

DISJ	:	'||'
	;

OPEN_BRACE
	:	'{'
	;

CLOSE_BRACE
	:	'}'
	;
	
LPAREN	:	'('
	;
	
RPAREN	:	')'
	;
	
LBRACKET 
	:	'['
	;

RBRACKET 
	:	']'
	;
	


PNAME_NS
    : p=PN_PREFIX? ':'
    ;

PNAME_LN
    : PNAME_NS PN_LOCAL
    ;

TRUE
    : ('T'|'t')('R'|'r')('U'|'u')('E'|'e')
    ;

FALSE
    : ('F'|'f')('A'|'a')('L'|'l')('S'|'s')('E'|'e')
    ;

IRI_REF
    : LANGLE ( options {greedy=false;} : ~(LANGLE | RANGLE | '"' | OPEN_BRACE | CLOSE_BRACE | '|' | '^' | '\\' | '`' | ('\u0000'..'\u0020')) )* RANGLE { setText($text.substring(1, $text.length() - 1)); }
    ;

LANGTAG
    : '@' PN_CHARS_BASE+ (MINUS (PN_CHARS_BASE DIGIT)+)*
    ;
    
QUERY_VAR 
	:	'?' PN_LOCAL
	;
	
STM_VAR :	'#' PN_LOCAL ('(' (PN_LOCAL (',' PN_LOCAL)*)? ')')?
	;

INTEGER
    : DIGIT+
    ;

DECIMAL
    : DIGIT+ DOT DIGIT*
    | DOT DIGIT+
    ;

DOUBLE
    : DIGIT+ DOT DIGIT* EXPONENT
    | DOT DIGIT+ EXPONENT
    | DIGIT+ EXPONENT
    ;

INTEGER_POSITIVE
    : PLUS INTEGER
    ;

DECIMAL_POSITIVE
    : PLUS DECIMAL
    ;

DOUBLE_POSITIVE
    : PLUS DOUBLE
    ;

INTEGER_NEGATIVE
    : MINUS INTEGER
    ;

DECIMAL_NEGATIVE
    : MINUS DECIMAL
    ;

DOUBLE_NEGATIVE
    : MINUS DOUBLE
    ;

fragment
EXPONENT
    : ('e'|'E') (PLUS|MINUS)? DIGIT+
    ;

STRING_LITERAL1
    : '\'' ( options {greedy=false;} : ~('\u0027' | '\u005C' | '\u000A' | '\u000D') | ECHAR )* '\''
    ;

STRING_LITERAL2
    : '"'  ( options {greedy=false;} : ~('\u0022' | '\u005C' | '\u000A' | '\u000D') | ECHAR )* '"'
    ;

STRING_LITERAL_LONG1
    :   '\'\'\'' ( options {greedy=false;} : ( '\'' | '\'\'' )? ( ~('\''|'\\') | ECHAR ) )* '\'\'\''
    ;

STRING_LITERAL_LONG2
    :   '"""' ( options {greedy=false;} : ( '"' | '""' )? ( ~('"'|'\\') | ECHAR ) )* '"""'
    ;

fragment
ECHAR
    : '\\' ('t' | 'b' | 'n' | 'r' | 'f' | '\\' | '"' | '\'')
    ;

fragment
PN_CHARS_U
    : PN_CHARS_BASE | '_'
    ;

fragment
VARNAME
    : ( PN_CHARS_U | DIGIT ) ( PN_CHARS_U | DIGIT | '\u00B7' | '\u0300'..'\u036F' | '\u203F'..'\u2040' )*
    ;

fragment
PN_CHARS
    : PN_CHARS_U
    | MINUS
    | DIGIT
    | '\u00B7' 
    | '\u0300'..'\u036F'
    | '\u203F'..'\u2040'
    ;

fragment
PN_PREFIX
    : PN_CHARS_BASE ((PN_CHARS|DOT)* PN_CHARS)?
    ;

fragment
PN_LOCAL
    : ( PN_CHARS_U | DIGIT ) ((PN_CHARS)* PN_CHARS)?
    ;

fragment
PN_CHARS_BASE
    : 'A'..'Z'
    | 'a'..'z'
    | '\u00C0'..'\u00D6'
    | '\u00D8'..'\u00F6'
    | '\u00F8'..'\u02FF'
    | '\u0370'..'\u037D'
    | '\u037F'..'\u1FFF'
    | '\u200C'..'\u200D'
    | '\u2070'..'\u218F'
    | '\u2C00'..'\u2FEF'
    | '\u3001'..'\uD7FF'
    | '\uF900'..'\uFDCF'
    | '\uFDF0'..'\uFFFD'
    ;

fragment
DIGIT
    : '0'..'9'
    ;

COMMENT 
    : '//' ( options{greedy=false;} : .)* EOL { $channel=HIDDEN; }
    ;

fragment
EOL
    : '\n' | '\r'
    ;

REFERENCE
    : '^^';
 
EXCLAMATION
	:	'!';
	
QUESTION 
	:	'?';
 
DOT	:	'.'
	;
	
COMMA	:	','
	;
	
//COLON	:	':'
//	;
	
SEMICOLON
	:	';'
	;
	
AMPERSAND
	:	'&';
	
BAR	:	'|'
	;
	
LANGLE	:	'<'
	;
	
RANGLE 	:	'>'
	;
	
PERCENT	:	'%'
	;
	
PLUS	:	'+'
	;
	
MINUS	:	'-'
	;
	
STAR	:	'*'
	;
	
TILDE 	:	'~'
	;
