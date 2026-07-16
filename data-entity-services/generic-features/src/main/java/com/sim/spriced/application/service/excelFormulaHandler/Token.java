package com.sim.spriced.application.service.excelFormulaHandler;

// ---------- Token & Lexer ----------
public class Token {
    enum Type {NUMBER, IDENTIFIER, OPERATOR, LPAREN, RPAREN, COMMA, EOF}

    Type type;
    String value;

    Token(Type type, String value) {
        this.type = type;
        this.value = value;
    }
}
