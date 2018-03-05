grammar BoolExpr;


parse
 : expression EOF
 ;

//������ʽ
expression
 : LPAREN expression RPAREN                       #parenExpression
 | NOT expression                                 #notExpression,not ���ʽ
 | left=expression op=comparator right=expression #comparatorExpression �Ƚϱ��ʽ
 | left=expression op=binary right=expression     #binaryExpression,�߼����ʽ
 | bool                                           #boolExpression ���ص���booleanֵ�ı��ʽ
 | IDENTIFIER                                     #identifierExpression ��ʾһ��key�ı��ʽ,�ñ��ʽ��[a-zA-Z_]��ͷ,Ȼ������ɸ�[a-zA-Z_0-9-],Ȼ���.,Ȼ��������ɸ�[a-zA-Z_0-9-],����org.apache.spark
 | DECIMAL                                        #decimalExpression ����ֵ��double���͵ı��ʽ
 | FIELD_REFERENCE                                #fieldReferenceExpression ������ʽ����,����${aa.bb.cc��}
 ;

//�ȽϷ���
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
IDENTIFIER : [a-zA-Z_] [a-zA-Z_0-9-]* ('.' [a-zA-Z_0-9-]+)*; //��ʾ��[a-zA-Z_]��ͷ,Ȼ������ɸ�[a-zA-Z_0-9-],Ȼ���.,Ȼ��������ɸ�[a-zA-Z_0-9-],����org.apache.spark
// field reference, example: ${var} //������ʽ����,����${aa.bb.cc��}
FIELD_REFERENCE : '${' [a-zA-Z_0-9]+ ('.' [a-zA-Z_0-9]+)* '}';

//����ո�
WS         : [ \r\t\u000C\n]+ -> skip;