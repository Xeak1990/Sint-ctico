package codigo;
import java_cup.runtime.Symbol;
%%

%public
%class Lexer
%unicode
%cup
%line
%column

%%

// Palabras clave
"P_R_O_G"             { return new Symbol(sym.inicio_programa, yyline, yycolumn, yytext()); }
"E_N_D"               { return new Symbol(sym.fin_programa, yyline, yycolumn, yytext()); }
"clase"               { return new Symbol(sym.palabra_clase, yyline, yycolumn, yytext()); }
"funcion"             { return new Symbol(sym.palabra_funcion, yyline, yycolumn, yytext()); }
"entero"              { return new Symbol(sym.tipo_entero, yyline, yycolumn, yytext()); }
"decimal"             { return new Symbol(sym.tipo_decimal, yyline, yycolumn, yytext()); }
"cadena"              { return new Symbol(sym.tipo_cadena, yyline, yycolumn, yytext()); }
"booleano"            { return new Symbol(sym.tipo_booleano, yyline, yycolumn, yytext()); }
"devolver"            { return new Symbol(sym.palabra_devolver, yyline, yycolumn, yytext()); }
"si"                  { return new Symbol(sym.palabra_si, yyline, yycolumn, yytext()); }
"sino si"             { return new Symbol(sym.palabra_sino_si, yyline, yycolumn, yytext()); }
"sino"                { return new Symbol(sym.palabra_sino, yyline, yycolumn, yytext()); }

// Operadores
"="                   { return new Symbol(sym.igual, yyline, yycolumn, yytext()); }
"=="                  { return new Symbol(sym.operador_igual, yyline, yycolumn, yytext()); }
"<"                   { return new Symbol(sym.operador_menor, yyline, yycolumn, yytext()); }
">"                   { return new Symbol(sym.operador_mayor, yyline, yycolumn, yytext()); }
"<="                  { return new Symbol(sym.operador_menor_igual, yyline, yycolumn, yytext()); }
">="                  { return new Symbol(sym.operador_mayor_igual, yyline, yycolumn, yytext()); }
"&&"                  { return new Symbol(sym.operador_and, yyline, yycolumn, yytext()); }
"||"                  { return new Symbol(sym.operador_or, yyline, yycolumn, yytext()); }
"+"                   { return new Symbol(sym.operador_suma, yyline, yycolumn, yytext()); }
"-"                   { return new Symbol(sym.operador_resta, yyline, yycolumn, yytext()); }
"*"                   { return new Symbol(sym.operador_multiplicacion, yyline, yycolumn, yytext()); }
"/"                   { return new Symbol(sym.operador_division, yyline, yycolumn, yytext()); }

// Símbolos
"{"                   { return new Symbol(sym.llave_apertura, yyline, yycolumn, yytext()); }
"}"                   { return new Symbol(sym.llave_cierre, yyline, yycolumn, yytext()); }
"("                   { return new Symbol(sym.parentesis_apertura, yyline, yycolumn, yytext()); }
")"                   { return new Symbol(sym.parentesis_cierre, yyline, yycolumn, yytext()); }
";"                   { return new Symbol(sym.punto_coma, yyline, yycolumn, yytext()); }
","                   { return new Symbol(sym.coma, yyline, yycolumn, yytext()); }

// Literales
\"([^\"\\]|\\.)*\" {
    return new Symbol(sym.cadena, yyline, yycolumn, yytext());
}

-?([0-9]+(\.[0-9]+)?|\.[0-9]+) {
    // Parseamos el número como double porque puede tener decimales y signo
    Double valor = Double.parseDouble(yytext());
    return new Symbol(sym.numero, yyline, yycolumn, valor);
}

[a-zA-Z][a-zA-Z0-9_]* { return new Symbol(sym.identificador, yyline, yycolumn, yytext()); }

// Espacios y saltos de línea
[ \t\r\n\f]+          { /* ignorar espacios */ }

// Comentarios (opcionales)
"//".*                { /* comentario de una línea */ }
"/*"[^*]*"*"+([^/*][^*]*"*"+)*"/"  { /* comentario multilinea */ }

// Cualquier otro carácter inválido
.                     { return new Symbol(sym.error, yyline, yycolumn, yytext()); }

