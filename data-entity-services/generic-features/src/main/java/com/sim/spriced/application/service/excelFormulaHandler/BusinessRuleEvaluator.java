package com.sim.spriced.application.service.excelFormulaHandler;

import java.util.*;
import java.util.stream.Collectors;

// ---------- Parser / Evaluator ----------
public class BusinessRuleEvaluator {
    private final Lexer lexer;
    private Token currentToken;
    private final Map<String, Double> variables;

    public BusinessRuleEvaluator(String formula, Map<String, Double> variables) {
        this.lexer = new Lexer(formula);
        this.currentToken = lexer.nextToken();
        this.variables = variables;
    }

    private void eat(Token.Type type) {
        if (currentToken.type == type)
            currentToken = lexer.nextToken();
        else
            throw new RuntimeException("Expected " + type + " but found " + currentToken.type + " (" + currentToken.value + ")");
    }

    // ---- Public entry point ----
    public double evaluate() {
        double result = parseExpression();
        if (currentToken.type != Token.Type.EOF)
            throw new RuntimeException("Unexpected token after expression: " + currentToken.value);
        return result;
    }

    // expression = comparison
    private double parseExpression() {
        return parseComparison();
    }

    // comparison = addition ( ( "<" | ">" | "<=" | ">=" | "=" | "<>" ) addition )*
    private double parseComparison() {
        double left = parseAddition();
        while (currentToken.type == Token.Type.OPERATOR && isComparisonOp(currentToken.value)) {
            String op = currentToken.value;
            eat(Token.Type.OPERATOR);
            double right = parseAddition();
            left = compare(op, left, right);
        }
        return left;
    }

    private boolean isComparisonOp(String op) {
        return op.equals("<") || op.equals(">") || op.equals("<=") || op.equals(">=") || op.equals("=") || op.equals("<>");
    }

    private double compare(String op, double a, double b) {
        boolean result;
        switch (op) {
            case "<":  result = a < b; break;
            case ">":  result = a > b; break;
            case "<=": result = a <= b; break;
            case ">=": result = a >= b; break;
            case "=":  result = a == b; break;
            case "<>": result = a != b; break;
            default: throw new RuntimeException("Unknown comparison: " + op);
        }
        return result ? 1.0 : 0.0;
    }

    // addition = multiplication ( ("+" | "-") multiplication )*
    private double parseAddition() {
        double left = parseMultiplication();
        while (currentToken.type == Token.Type.OPERATOR &&
                (currentToken.value.equals("+") || currentToken.value.equals("-"))) {
            String op = currentToken.value;
            eat(Token.Type.OPERATOR);
            double right = parseMultiplication();
            left = op.equals("+") ? left + right : left - right;
        }
        return left;
    }

    // multiplication = unary ( ("*" | "/") unary )*
    private double parseMultiplication() {
        double left = parseUnary();
        while (currentToken.type == Token.Type.OPERATOR &&
                (currentToken.value.equals("*") || currentToken.value.equals("/"))) {
            String op = currentToken.value;
            eat(Token.Type.OPERATOR);
            double right = parseUnary();
            left = op.equals("*") ? left * right : left / right;
        }
        return left;
    }

    // unary = ("+" | "-")? power
    private double parseUnary() {
        if (currentToken.type == Token.Type.OPERATOR &&
                (currentToken.value.equals("+") || currentToken.value.equals("-"))) {
            String op = currentToken.value;
            eat(Token.Type.OPERATOR);
            double value = parsePower();
            return op.equals("-") ? -value : value;
        }
        return parsePower();
    }

    // power = primary ("^" unary)*   (right‑associative)
    private double parsePower() {
        double left = parsePrimary();
        if (currentToken.type == Token.Type.OPERATOR && currentToken.value.equals("^")) {
            eat(Token.Type.OPERATOR);
            double right = parsePower();
            return Math.pow(left, right);
        }
        return left;
    }

    // primary = number | variable | function | "(" expression ")"
    private double parsePrimary() {
        if (currentToken.type == Token.Type.NUMBER) {
            double val = Double.parseDouble(currentToken.value);
            eat(Token.Type.NUMBER);
            return val;
        }
        if (currentToken.type == Token.Type.IDENTIFIER) {
            String name = currentToken.value;
            if (isFunction(name)) {
                return parseFunction(name);
            } else {
                eat(Token.Type.IDENTIFIER);
                Double val = variables.get(name);
                if (val == null) throw new RuntimeException("Undefined variable: " + name);
                return val;
            }
        }
        if (currentToken.type == Token.Type.LPAREN) {
            eat(Token.Type.LPAREN);
            double val = parseExpression();
            eat(Token.Type.RPAREN);
            return val;
        }
        throw new RuntimeException("Unexpected token: " + currentToken.type + " (" + currentToken.value + ")");
    }

    private boolean isFunction(String name) {
        return Lexer.FUNCTIONS.contains(name.toUpperCase());
    }

    private double parseFunction(String funcName) {
        eat(Token.Type.IDENTIFIER); // consume function name
        eat(Token.Type.LPAREN);
        List<Double> args = new ArrayList<>();
        if (currentToken.type != Token.Type.RPAREN) {
            args.add(parseExpression());
            while (currentToken.type == Token.Type.COMMA) {
                eat(Token.Type.COMMA);
                args.add(parseExpression());
            }
        }
        eat(Token.Type.RPAREN);

        // ---- 58 function implementations ----
        switch (funcName.toUpperCase()) {
            // Basic aggregate
            case "SUM":     return args.stream().mapToDouble(d -> d).sum();
            case "AVERAGE": return args.stream().mapToDouble(d -> d).average().orElse(0);
            case "MAX":     return args.stream().mapToDouble(d -> d).max().orElse(0);
            case "MIN":     return args.stream().mapToDouble(d -> d).min().orElse(0);
            case "PRODUCT": return args.stream().reduce(1.0, (a, b) -> a * b);

            // Logical
            case "AND": return args.stream().allMatch(d -> d != 0.0) ? 1.0 : 0.0;
            case "OR":  return args.stream().anyMatch(d -> d != 0.0) ? 1.0 : 0.0;
            case "NOT":
                validateArgCount("NOT", args, 1);
                return args.get(0) == 0.0 ? 1.0 : 0.0;

            // Conditional
            case "IF":
                validateArgCount("IF", args, 3);
                return args.get(0) != 0.0 ? args.get(1) : args.get(2);

            // Math – single argument
            case "ABS":
                validateArgCount("ABS", args, 1);
                return Math.abs(args.get(0));
            case "SQRT":
                validateArgCount("SQRT", args, 1);
                return Math.sqrt(args.get(0));
            case "EXP":
                validateArgCount("EXP", args, 1);
                return Math.exp(args.get(0));
            case "LN":
                validateArgCount("LN", args, 1);
                return Math.log(args.get(0));
            case "LOG10":
                validateArgCount("LOG10", args, 1);
                return Math.log10(args.get(0));
            case "SIGN":
                validateArgCount("SIGN", args, 1);
                double s = args.get(0);
                return s > 0 ? 1 : (s < 0 ? -1 : 0);
            case "INT":
                validateArgCount("INT", args, 1);
                return Math.floor(args.get(0));   // Excel rounds down
            case "TRUNC":
                // optional second argument (num_digits) not implemented; truncates to integer
                validateArgCount("TRUNC", args, 1);
                double t = args.get(0);
                return t >= 0 ? Math.floor(t) : Math.ceil(t);
            case "FACT":
                validateArgCount("FACT", args, 1);
                return factorial((int) args.get(0).longValue());
            case "DEGREES":
                validateArgCount("DEGREES", args, 1);
                return Math.toDegrees(args.get(0));
            case "RADIANS":
                validateArgCount("RADIANS", args, 1);
                return Math.toRadians(args.get(0));

            // Trigonometry – single argument
            case "SIN":
                validateArgCount("SIN", args, 1);
                return Math.sin(args.get(0));
            case "COS":
                validateArgCount("COS", args, 1);
                return Math.cos(args.get(0));
            case "TAN":
                validateArgCount("TAN", args, 1);
                return Math.tan(args.get(0));
            case "ASIN":
                validateArgCount("ASIN", args, 1);
                return Math.asin(args.get(0));
            case "ACOS":
                validateArgCount("ACOS", args, 1);
                return Math.acos(args.get(0));
            case "ATAN":
                validateArgCount("ATAN", args, 1);
                return Math.atan(args.get(0));
            case "SINH":
                validateArgCount("SINH", args, 1);
                return Math.sinh(args.get(0));
            case "COSH":
                validateArgCount("COSH", args, 1);
                return Math.cosh(args.get(0));
            case "TANH":
                validateArgCount("TANH", args, 1);
                return Math.tanh(args.get(0));

            // Two‑argument math
            case "ATAN2":
                validateArgCount("ATAN2", args, 2);
                return Math.atan2(args.get(1), args.get(0)); // Excel order: ATAN2(x_num, y_num)
            case "POWER":
                validateArgCount("POWER", args, 2);
                return Math.pow(args.get(0), args.get(1));
            case "MOD":
                validateArgCount("MOD", args, 2);
                return args.get(0) % args.get(1);
            case "LOG":
                validateArgCount("LOG", args, 2); // LOG(number, base)
                return Math.log(args.get(0)) / Math.log(args.get(1));
            case "ROUND":
                validateArgCount("ROUND", args, 2);
                return round(args.get(0), (int) args.get(1).longValue(), "ROUND");
            case "ROUNDUP":
                validateArgCount("ROUNDUP", args, 2);
                return round(args.get(0), (int) args.get(1).longValue(), "ROUNDUP");
            case "ROUNDDOWN":
                validateArgCount("ROUNDDOWN", args, 2);
                return round(args.get(0), (int) args.get(1).longValue(), "ROUNDDOWN");
            case "CEILING":
                validateArgCount("CEILING", args, 2);
                return ceiling(args.get(0), args.get(1));
            case "FLOOR":
                validateArgCount("FLOOR", args, 2);
                return floor(args.get(0), args.get(1));
            case "COMBIN":
                validateArgCount("COMBIN", args, 2);
                return combin((int) args.get(0).longValue(), (int) args.get(1).longValue());
            case "PERMUT":
                validateArgCount("PERMUT", args, 2);
                return permut((int) args.get(0).longValue(), (int) args.get(1).longValue());
            case "EVEN":
                validateArgCount("EVEN", args, 1);
                return even(args.get(0));
            case "ODD":
                validateArgCount("ODD", args, 1);
                return odd(args.get(0));

            // Random
            case "RAND":
                validateArgCount("RAND", args, 0);
                return Math.random();
            case "RANDBETWEEN":
                validateArgCount("RANDBETWEEN", args, 2);
                int low = (int) args.get(0).longValue();
                int high = (int) args.get(1).longValue();
                return low + Math.random() * (high - low + 1);

            // Statistical – multi‑arg
            case "MEDIAN":
                if (args.isEmpty()) throw new RuntimeException("MEDIAN requires at least 1 argument");
                List<Double> sorted = args.stream().sorted().collect(Collectors.toList());
                int n = sorted.size();
                if (n % 2 == 1) return sorted.get(n / 2);
                else return (sorted.get(n / 2 - 1) + sorted.get(n / 2)) / 2.0;

            case "MODE":
                if (args.isEmpty()) throw new RuntimeException("MODE requires at least 1 argument");
                Map<Double, Long> freq = args.stream().collect(Collectors.groupingBy(d -> d, Collectors.counting()));
                // Find the entry with the highest frequency; ties: choose the highest value.
                double mode = freq.entrySet().stream()
                        .max(Comparator.comparingLong(Map.Entry<Double, Long>::getValue)
                                .thenComparing(Map.Entry::getKey))
                        .get().getKey();
                return mode;

            case "STDEV":
                if (args.size() < 2) throw new RuntimeException("STDEV requires at least 2 arguments");
                double mean = args.stream().mapToDouble(d -> d).average().getAsDouble();
                double sumSq = args.stream().mapToDouble(d -> Math.pow(d - mean, 2)).sum();
                return Math.sqrt(sumSq / (args.size() - 1));   // sample stdev

            case "VAR":
                if (args.size() < 2) throw new RuntimeException("VAR requires at least 2 arguments");
                double meanV = args.stream().mapToDouble(d -> d).average().getAsDouble();
                double sumSqV = args.stream().mapToDouble(d -> Math.pow(d - meanV, 2)).sum();
                return sumSqV / (args.size() - 1);            // sample variance

            case "COUNT":
                return args.size();     // number of numeric arguments passed
            case "COUNTA":
                return args.size();     // in our numeric world, same as COUNT

            // LARGE(array..., k) – last argument is k, rest are values
            case "LARGE":
                if (args.size() < 2) throw new RuntimeException("LARGE requires at least 2 arguments (values, k)");
                int k = (int) args.get(args.size() - 1).longValue();
                List<Double> largeList = new ArrayList<>(args.subList(0, args.size() - 1));
                largeList.sort(Collections.reverseOrder());
                if (k < 1 || k > largeList.size()) throw new RuntimeException("LARGE k out of range");
                return largeList.get(k - 1);

            case "SMALL":
                if (args.size() < 2) throw new RuntimeException("SMALL requires at least 2 arguments (values, k)");
                int kk = (int) args.get(args.size() - 1).longValue();
                List<Double> smallList = new ArrayList<>(args.subList(0, args.size() - 1));
                Collections.sort(smallList);
                if (kk < 1 || kk > smallList.size()) throw new RuntimeException("SMALL k out of range");
                return smallList.get(kk - 1);

            case "PERCENTILE":
                if (args.size() < 2) throw new RuntimeException("PERCENTILE requires at least 2 arguments (values, percentile)");
                double p = args.get(args.size() - 1);
                if (p < 0 || p > 1) throw new RuntimeException("PERCENTILE percentile must be between 0 and 1");
                List<Double> percValues = new ArrayList<>(args.subList(0, args.size() - 1));
                Collections.sort(percValues);
                int nP = percValues.size();
                if (p == 0) return percValues.get(0);
                if (p == 1) return percValues.get(nP - 1);
                double rank = p * (nP - 1);
                int lowIdx = (int) Math.floor(rank);
                double frac = rank - lowIdx;
                return percValues.get(lowIdx) + frac * (percValues.get(lowIdx + 1) - percValues.get(lowIdx));

            case "QUARTILE":
                if (args.size() < 2) throw new RuntimeException("QUARTILE requires at least 2 arguments (values, quart)");
                int quart = (int) args.get(args.size() - 1).longValue();
                if (quart < 0 || quart > 4) throw new RuntimeException("QUARTILE quart must be 0,1,2,3,4");
                // reuse PERCENTILE logic
                double percentile = quart / 4.0;
                List<Double> qValues = new ArrayList<>(args.subList(0, args.size() - 1));
                Collections.sort(qValues);
                int qN = qValues.size();
                if (percentile == 0) return qValues.get(0);
                if (percentile == 1) return qValues.get(qN - 1);
                double qRank = percentile * (qN - 1);
                int qLowIdx = (int) Math.floor(qRank);
                double qFrac = qRank - qLowIdx;
                return qValues.get(qLowIdx) + qFrac * (qValues.get(qLowIdx + 1) - qValues.get(qLowIdx));

            // Number theory
            case "GCD":
                if (args.isEmpty()) return 0;
                return args.stream()
                        .mapToLong(d -> d.longValue())            // unbox Double → long
                        .reduce(this::gcd)
                        .orElse(0);                               // returns long, auto‑widened to double

            case "LCM":
                if (args.isEmpty()) return 0;
                return args.stream()
                        .mapToLong(d -> d.longValue())
                        .reduce(1, this::lcm);                    // returns long, auto‑widened to double

            default:
                throw new RuntimeException("Unknown function: " + funcName);
        }
    }

    // ---- Helper methods ----

    private void validateArgCount(String name, List<Double> args, int expected) {
        if (args.size() != expected)
            throw new RuntimeException(name + " expects " + expected + " argument(s), got " + args.size());
    }

    private double round(double value, int digits, String type) {
        double factor = Math.pow(10, digits);
        switch (type) {
            case "ROUND":   return Math.round(value * factor) / factor;
            case "ROUNDUP": return Math.ceil(value * factor) / factor;
            case "ROUNDDOWN": return Math.floor(value * factor) / factor;
            default: throw new IllegalArgumentException("Unknown round type");
        }
    }

    private double ceiling(double number, double significance) {
        if (significance == 0) return 0;
        if (number > 0) return Math.ceil(number / significance) * significance;
        if (number < 0) return Math.floor(number / significance) * significance;
        return 0;
    }

    private double floor(double number, double significance) {
        if (significance == 0) return 0;
        return Math.floor(number / significance) * significance;
    }

    private double even(double x) {
        double sign = Math.signum(x);
        double abs = Math.abs(x);
        return sign * Math.ceil(abs / 2) * 2;
    }

    private double odd(double x) {
        double sign = Math.signum(x);
        double abs = Math.abs(x);
        double ceil = Math.ceil(abs);
        if (ceil % 2 == 0) ceil += 1;  // if already even, go to next odd
        else ceil = ceil;              // already odd
        return sign * ceil;
    }

    private long gcd(long a, long b) {
        return b == 0 ? a : gcd(b, a % b);
    }

    private long gcd(long a, long b, long... rest) {
        long result = gcd(a, b);
        for (long r : rest) result = gcd(result, r);
        return result;
    }

    private long lcm(long a, long b) {
        return a * (b / gcd(a, b));
    }

    private long lcm(long a, long b, long... rest) {
        long result = lcm(a, b);
        for (long r : rest) result = lcm(result, r);
        return result;
    }

    private long factorial(int n) {
        if (n < 0) throw new RuntimeException("FACT requires non‑negative argument");
        long res = 1;
        for (int i = 2; i <= n; i++) res *= i;
        return res;
    }

    private double combin(int n, int k) {
        if (k > n || k < 0) throw new RuntimeException("COMBIN: k must be between 0 and n");
        k = Math.min(k, n - k);
        double res = 1;
        for (int i = 1; i <= k; i++) {
            res = res * (n - k + i) / i;
        }
        return res;
    }

    private double permut(int n, int k) {
        if (k > n || k < 0) throw new RuntimeException("PERMUT: k must be between 0 and n");
        double res = 1;
        for (int i = 0; i < k; i++) res *= (n - i);
        return res;
    }
}