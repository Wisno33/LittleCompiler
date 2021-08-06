/**
 * Define the grammar for the Little language.
 */
 grammar Little;


//Parser (Non-Terminal) rules 

//Program structure rules.
program : 'PROGRAM' id 'BEGIN' pgm_body 'END' + EOF;
id : IDENTIFIER;
pgm_body : decl func_declarations;
decl : string_decl decl | var_decl decl | ;

//String Declaration rules
string_decl : 'STRING' id ':=' str ';';
str : STRINGLITERAL;

//Variable Declaration Rules (Floating point and Integer)
var_decl: var_type id_list ';';
var_type: 'FLOAT' | 'INT';
any_type: var_type | 'VOID';
id_list: id id_tail;
id_tail: ',' id id_tail | ;

//Function Parameter Rules
param_decl_list: param_decl param_decl_tail | ;
param_decl: var_type id;
param_decl_tail: ',' param_decl param_decl_tail | ;

//Function Declaration rules
func_declarations: func_decl func_declarations | ;
func_decl: 'FUNCTION' any_type id '(' param_decl_list ')' 'BEGIN' func_body 'END';
func_body: decl stmt_list;

//High level (general) statement Rules
stmt_list: stmt stmt_list | ;
stmt: base_stmt | if_stmt | while_stmt;
base_stmt: assign_stmt | read_stmt | write_stmt | return_stmt;

//Base Statement rules
assign_stmt: assign_expr ';';
assign_expr: id ':=' expr;
read_stmt: 'READ' '(' id_list ')' ';';
write_stmt: 'WRITE' '(' id_list ')' ';';
return_stmt: 'RETURN' expr ';';

//Expression Rules
expr: expr_prefix factor;
expr_prefix: expr_prefix factor addop | ;
factor: factor_prefix postfix_expr;
factor_prefix: factor_prefix postfix_expr mulop | ;
postfix_expr: primary | call_expr;
call_expr: id '(' expr_list ')';
expr_list: expr expr_list_tail | ;
expr_list_tail: ',' expr expr_list_tail | ;
primary: '(' expr ')' | id| INTLITERAL | FLOATLITERAL;
addop: '+' | '-';
mulop: '*' | '/';

//Complex Statements and Conditional Statements.
if_stmt: 'IF' '(' cond ')' decl stmt_list else_part 'ENDIF';
else_part: 'ELSE' decl stmt_list | ;
cond: expr compop expr;
compop: '<' | '>' | '=' | '!=' | '<=' | '>=';

//While statement
while_stmt: 'WHILE' '(' cond ')' decl stmt_list 'ENDWHILE';



//Lexer (Token / Terminals) rules:

//Reserved words in the Little language.
KEYWORD: ('PROGRAM'|'BEGIN'|'END'|'FUNCTION'|'READ'|'WRITE'|
	'IF'|'ELSE'|'ENDIF'|'WHILE'|'ENDWHILE'|'CONTINUE'|'BREAK'|
	'RETURN'|'INT'|'VOID'|'STRING'|'FLOAT'); 
	
//Assignment, arithmetic, and boolean operators available in the Little language.
OPERATOR: ( ':=' | '+' | '-' | '*' | '/' | '=' | '!=' | '<' | '>' |
		'(' | ')' | ';' | ',' | '<=' | '>=') ;

/*
 * Specifies the name of a source code variable. 
 * IDENTIFIER: token will begin with a letter, and be followed by any number of letters and numbers.
 */
IDENTIFIER : [a-zA-Z][a-zA-Z0-9]*;

/*
 * Data types available in the Little language.
 * INTLITERAL: integer number.
 * FLOATLITERAL: floating point number available in two different format yyyy.xxxxxx or .xxxxxxx.
 * STRINGLITERAL: any sequence of characters except '"' 
 */
INTLITERAL: [0-9]+;
FLOATLITERAL:  [0-9]*'.'[0-9]+;
STRINGLITERAL: '"'~('"')*?'"';

/*
 * Lines for documentation, not considered in the final output of the compiler.
 * COMMENT: Starts with "--" and lasts till the end of line
 */
COMMENT: '--'.*?[\r\n] -> skip;
	
//Whitespace
WS : [ \t\r\n]+ -> skip ;