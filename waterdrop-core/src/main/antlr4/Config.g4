grammar Config;

// [done] STRING lexer rule定义过于简单，不满足需求, 包括其中包含Quote(',")的String
// [done] nested bool expression中的字段引用
// [done] nested bool expression
// [done] Filter, Output 允许if else, 包含nested if..else
// [done] 允许key不包含双引号
// [done] 允许多行配置没有"，"分割
// [done] 允许plugin中不包含任何配置

// notes: lexer rule vs parser rule
// notes: don't let two lexer rule match the same token

import BoolExpr;

//总配置文件
config
    : COMMENT* 'spark' sparkBlock COMMENT* 'input' inputBlock COMMENT* 'filter' filterBlock COMMENT* 'output' outputBlock COMMENT* EOF
    ;

sparkBlock
    : entries
    ;

inputBlock
    : '{' (plugin | COMMENT)* '}'
    ;

filterBlock
    : '{' statement* '}'
    ;

outputBlock
    : '{' statement* '}'
    ;

//表示可以是插件,也可以是判断语句,也可以是备注
statement
    : plugin
    | ifStatement
    | COMMENT
    ;

//if表达式
ifStatement
    : IF expression '{' statement* '}' (ELSE IF expression '{' statement* '}')* (ELSE '{' statement* '}')?
    ;

//表示插件名字以及对应的配置内容
plugin
    : IDENTIFIER entries
//    : plugin_name entries
    ;

//一个实体内容由{}包含,里面分别是由键值对配置组成,或者备注组测
entries
    : '{' (pair | COMMENT)* '}'
    ;

// entries
//    : '{' pair (','? pair)* '}'
//    | '{' '}'
//    ;

//一个key=value的键值对配置
pair
    : IDENTIFIER '=' value
    ;

//表示一个数组值,即[value,value]或者[]
array
    : '[' value (',' value)* ']'
    | '[' ']'
    ;

//具体存储的值
value
    : DECIMAL
    | QUOTED_STRING
//   | entries
    | array
    | TRUE
    | FALSE
    | NULL
    ;

//表示备注
COMMENT
    : ('#' | '//') ~( '\r' | '\n' )* -> skip
    ;

// double and single quoted string support
fragment BSLASH : '\\';
fragment DQUOTE : '"';
fragment SQUOTE : '\'';
fragment DQ_STRING_ESC : BSLASH ["\\/bfnrt] ;
fragment SQ_STRING_ESC : BSLASH ['\\/bfnrt] ;
fragment DQ_STRING : DQUOTE (DQ_STRING_ESC | ~["\\])* DQUOTE ;
fragment SQ_STRING : SQUOTE (SQ_STRING_ESC | ~['\\])* SQUOTE ;
QUOTED_STRING : DQ_STRING | SQ_STRING ;

NULL
    : 'null'
    ;

//表示空格
WS
    : [ \t\n\r]+ -> skip
    ;

