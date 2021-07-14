
grammar Minilang ;

/*@parser::members {

 boolean istype() {
   System.out.println("isType()" + getCurrentToken().getText());
   return true;
 }
}
*/

WS : [ \t\n\r]+ -> skip ;

FUZZY_SLOP : '~' NUM_CHAR? ;

AND : 'AND' | 'ET';
OR : 'OR' | 'OU' ;
LPAREN : '(';
RPAREN : ')';
GTE: '>=' ;
LTE: '<=' ;
GT: '>' ;
LT: '<' ;

STAR: '*';

NUM_CHAR : [0-9] ;

// TERM_LETTER : [a-zA-Z_] ;
// ID : TERM_LETTER (TERM_LETTER | NUM_CHAR)* ;


// Les TOkens ne prennent pas en comptes les caractères skippés.
// On peut donc tokeniser les dates sans qu'elles soient acceptées avec des espaces.
// fragment ISO_DATE : [12][09][0-9][0-9] '-' [01][0-9] '-' [0-9][0-9] ;
// fragment FR_DATE : [0-9][0-9] '/' [01][0-9] '/' [12][09][0-9][0-9] ;
// DATE: ISO_DATE ;
// date: DATE ;


// dans le lexique de Lucene les caractères - et + sont réservé en débuts de mots, mais
// pas sur la suite du mots...
// cela permet de prendre en compte les références (ou date) en temps de terme (par exemple 2020-10-01)
//
fragment TERM_START_CHAR: ~(' ' | '\t' | '\r'| '+' |
                   '-' | '!' | '(' | ')' | ':' |
                   '^' | '[' | ']' | '\\'| '{' |
                   '}' | '~' | '*' | '?' | '/' |
                   '>' | '<' ) ;


fragment TERM_CHAR : TERM_START_CHAR | '-' ;
fragment QUOTED_CHAR: ~( '"' ) ;

QUOTED : '"' QUOTED_CHAR* '"';
TERM: TERM_START_CHAR (TERM_CHAR)*;


// il vaut mieux définir le TERME après les block AND et OR sinon ils seraient catchés



// les nom des champs doivent respecter un identifiant valide

// https://stackoverflow.com/questions/35724082/syntactic-predicates-in-antlr-lexer-rules

// des suites comme 2020-10-10ABC~10 devraient être reconnues comme des term
// et pas comme des dates.
// ou alors carrement enlever le concept de date

// "multi-purpose" Term

query : expression EOF;

expression
    : LPAREN expression RPAREN                    # GroupingExpr
    | fieldedQuery                                # FieldedExpr
    | left=expression AND right=expression        # AndExpr
    | left=expression OR right=expression         # OrExpr
    | left=expression right=expression            # UnknownBoolExpr
    | '-' expression                              # MinusExpr
    | basicTerm                                   # TermExpr
    ;

fieldedQuery
   : left=id ':' (value=subTerm | LPAREN content=subQuery RPAREN)
   ;

// Les mini-expressions sont simplifiées, Elles ne permettent pas la ré-création
// de fieldExpr par exemple
subQuery
    : LPAREN subQuery RPAREN          # SubGroupingExpr
    | left=subQuery AND right=subQuery      # SubAndExpr
    | left=subQuery OR right=subQuery      # SubOrExpr
    | left=subQuery right=subQuery    # SubUnknownExpr
    | '-' subQuery                    # SubMinusExpr
    | subTerm                         # SubTermExp
    ;

// offre les recherches plus avancées sur les valeurs numériques
subTerm
    : QUOTED            # QuotedSubTerm
    | QUOTED FUZZY_SLOP # FuzzyQuotedSubTerm
    | TERM FUZZY_SLOP   # FuzzySubTerm
    | TERM '*'          # WildSubTerm
    | GTE TERM          # GreaterOrEqualSubTerm
    | GT  TERM          # GreaterSubTerm
    | LTE TERM          # LowerOrEqualSubTerm
    | LT TERM           # LowerSubTerm
    | TERM              # UniqueSubTerm
    ;

basicTerm
    : QUOTED            # QuotedTerm
    | QUOTED FUZZY_SLOP # FuzzyQuotedTerm
    | TERM FUZZY_SLOP   # FuzzyTerm
    | TERM '*'          # WildTerm
    | TERM              # UniqueTerm
    ;

// En réalité, il faudrait aller plus loin et ne matcher que les ID genre [a-zA-Z0-9_]
id
 : TERM
// : ID_CHARS
  ;

// ID_CHARS: [A-Za-z_][A-Za-z0-9_]* ;


