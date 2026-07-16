package com.sim.spriced.application.service.excelFormulaHandler;

import java.util.Set;

class Lexer {
    private final String input;
    private int pos = 0;

    // All recognised functions (case‑insensitive) – 58 in total
    public static final Set<String> FUNCTIONS = Set.of(
            // basic
            "SUM", "AVERAGE", "MAX", "MIN", "IF", "AND", "OR", "NOT",
            // math & trig
            "ABS", "ROUND", "ROUNDUP", "ROUNDDOWN", "SQRT", "POWER", "MOD",
            "EXP", "LN", "LOG", "LOG10",
            "SIN", "COS", "TAN", "ASIN", "ACOS", "ATAN", "ATAN2",
            "SINH", "COSH", "TANH",
            "DEGREES", "RADIANS",
            "CEILING", "FLOOR", "TRUNC", "INT",
            "SIGN", "FACT", "PRODUCT", "COMBIN", "PERMUT",
            // rounding / even‑odd
            "EVEN", "ODD",
            // statistical
            "MEDIAN", "MODE", "STDEV", "VAR",
            "LARGE", "SMALL", "PERCENTILE", "QUARTILE",
            "COUNT", "COUNTA",
            // random
            "RAND", "RANDBETWEEN",
            // number theory
            "GCD", "LCM"
    );

    Lexer(String input) {
        this.input = input.replaceAll("\\s+", "");
    }

    Token nextToken() {
        if (pos >= input.length()) return new Token(Token.Type.EOF, "");
        char c = input.charAt(pos);
        // Numbers
        if (Character.isDigit(c) || (c == '.' && pos + 1 < input.length() && Character.isDigit(input.charAt(pos + 1)))) {
            StringBuilder sb = new StringBuilder();
            while (pos < input.length() && (Character.isDigit(input.charAt(pos)) || input.charAt(pos) == '.'))
                sb.append(input.charAt(pos++));
            return new Token(Token.Type.NUMBER, sb.toString());
        }
        // Identifiers (variables + function names)
        if (Character.isLetter(c) || c == '_') {
            StringBuilder sb = new StringBuilder();
            while (pos < input.length() && (Character.isLetterOrDigit(input.charAt(pos)) || input.charAt(pos) == '_'))
                sb.append(input.charAt(pos++));
            String name = sb.toString().toUpperCase();
            if (FUNCTIONS.contains(name))
                return new Token(Token.Type.IDENTIFIER, name);  // function name
            else
                return new Token(Token.Type.IDENTIFIER, sb.toString()); // variable (keep original case)
        }
        // Multi‑character operators (>=, <=, <>)
        if (c == '>' || c == '<' || c == '=' || c == '!') {
            String op = String.valueOf(c);
            pos++;
            if (pos < input.length()) {
                char next = input.charAt(pos);
                if ((c == '>' || c == '<' || c == '!' || c == '=') && next == '=' ||
                        (c == '<' && next == '>')) {
                    op += next;
                    pos++;
                }
            }
            return new Token(Token.Type.OPERATOR, op);
        }
        // Single‑character tokens
        pos++;
        switch (c) {
            case '+':
            case '-':
            case '*':
            case '/':
            case '^':
                return new Token(Token.Type.OPERATOR, String.valueOf(c));
            case '(':
                return new Token(Token.Type.LPAREN, "(");
            case ')':
                return new Token(Token.Type.RPAREN, ")");
            case ',':
                return new Token(Token.Type.COMMA, ",");
            default:
                throw new RuntimeException("Unknown character: " + c);
        }
    }
}
