grammar BoolExpr;


parse
 : expression EOF
 ;

//定义表达式
expression
 : LPAREN expression RPAREN                       #parenExpression
 | NOT expression                                 #notExpression,not 表达式
 | left=expression op=comparator right=expression #comparatorExpression 比较表达式
 | left=expression op=binary right=expression     #binaryExpression,逻辑表达式
 | bool                                           #boolExpression 返回的是boolean值的表达式
 | IDENTIFIER                                     #identifierExpression 表示一个key的表达式,该表达式以[a-zA-Z_]开头,然后接若干个[a-zA-Z_0-9-],然后接.,然后接入若干个[a-zA-Z_0-9-],比如org.apache.spark
 | DECIMAL                                        #decimalExpression 返回值是double类型的表达式
 | FIELD_REFERENCE                                #fieldReferenceExpression 属性链式引用,比如${aa.bb.cc等}
 ;

//比较符号
comparator
 : GT | GE | LT | LE | EQ
 ;

binary
 : AND | OR
 ;

bool
 : TRUE | FALSE
 ;

IF : 'if';
ELSE : 'else';

AND        : 'AND' ;
OR         : 'OR' ;
NOT        : 'NOT';
TRUE       : 'true' ;
FALSE      : 'false' ;
GT         : '>' ;
GE         : '>=' ;
LT         : '<' ;
LE         : '<=' ;
EQ         : '==' ;
LPAREN     : '(' ;
RPAREN     : ')' ;
// integer and float point number
DECIMAL    : '-'? [0-9]+ ( '.' [0-9]+ )? ;
// identifier that represents string starts with [a-zA-Z_] (so we can separate DECIMAL with IDENTIFIER),
// and support such format org.apache.spark
IDENTIFIER : [a-zA-Z_] [a-zA-Z_0-9-]* ('.' [a-zA-Z_0-9-]+)*; //表示以[a-zA-Z_]开头,然后接若干个[a-zA-Z_0-9-],然后接.,然后接入若干个[a-zA-Z_0-9-],比如org.apache.spark
// field reference, example: ${var} //属性链式引用,比如${aa.bb.cc等}
FIELD_REFERENCE : '${' [a-zA-Z_0-9]+ ('.' [a-zA-Z_0-9]+)* '}';

//定义空格
WS         : [ \r\t\u000C\n]+ -> skip;