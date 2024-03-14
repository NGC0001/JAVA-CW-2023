package edu.uob;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Grammar {
    public static class GrammarException extends DBException {
        @Serial
        private static final long serialVersionUID = 1;

        public GrammarException(String message) {
            super(message);
        }
    }

    public static enum Keyword {
        USE("use"),
        CREATE("create"),
        DATABASE("database"),
        TABLE("table"),
        DROP("drop"),
        ALTER("alter"),
        INSERT("insert"),
        INTO("into"),
        VALUES("values"),
        SELECT("select"),
        FROM("from"),
        WHERE("where"),
        UPDATE("update"),
        SET("set"),
        DELETE("delete"),
        JOIN("join"),
        AND("and"),
        ON("on"),
        ADD("add"),
        TRUE("true"),
        FALSE("false"),
        OR("or"),
        LIKE("like"),
        NULL("null"),

        STAR("*"),

        EQ("=="),
        NEQ("!="),
        GT(">"),
        GE(">="),
        LT("<"),
        LE("<="),

        LBRACKET("("),
        RBRACKET(")"),
        COMMA(","),
        SEMICOLON(";");

        public static final Keyword[] symbolKeywords = {
            STAR, EQ, NEQ, GT, GE, LT, LE, LBRACKET, RBRACKET, COMMA, SEMICOLON
        };
        private static HashMap<String, Keyword> strKwMap;
        static {
            strKwMap = new HashMap<String, Keyword>();
            for (Keyword kw : Keyword.values()) {
                strKwMap.put(kw.toString(), kw);
            }
        }

        private String str;

        private Keyword(String str) {
            this.str = str;
        }

        public static Keyword getByString(String str) {
            if (str == null) {
                return null;
            }
            return strKwMap.get(str.toLowerCase());
        }

        @Override
        public String toString() {
            return str;
        }
    }

    private static final String idAttrName = "id";

    public static Task parseCommand(String command) throws DBException {
        ArrayList<String> tokens = getTokensFromString(command);
        System.out.println("user command: " + tokens);
        int numTokens = tokens.size();
        // if (numTokens < 2 || Keyword.SEMICOLON.equals()) {
        // }
        return new Task();
    }

    public static ArrayList<String> getTokensFromString(String str) throws DBException {
        if (str == null) {
            throw new DBException.NullObjectException("get tokens from null");
        }
        ArrayList<String> tokens = new ArrayList<String>();
        String[] fragments = (" " + str + " ").split("'");
        if (fragments.length % 2 != 1) {
            throw new GrammarException("unclosed string literal");
        }
        for (int i = 0; i < fragments.length; ++i) {
            if (i % 2 == 1) {
                tokens.add("'" + fragments[i] + "'");
            } else {
                tokens.addAll(getTokensFromTrivialString(fragments[i]));
            }
        }
        return tokens;
    }

    protected static List<String> getTokensFromTrivialString(String str) throws DBException {
        if (str == null) {
            throw new DBException.NullObjectException("get tokens from null");
        }
        for(int i = 0; i < Keyword.symbolKeywords.length; ++i) {
            String symbolKeywordStr = Keyword.symbolKeywords[i].toString();
            str = str.replace(symbolKeywordStr, " " + symbolKeywordStr + " ");
        }
        str = str.trim();
        if (str.length() == 0) {
            return new ArrayList<String>();
        }
        return Arrays.asList(str.split("\\s+"));
    }

    public static boolean isAllLowerCase(String str) {
        return str != null && str.equals(str.toLowerCase());
    }

    public static String getIdAttrName() {
        return idAttrName;
    }

    public static boolean isKeyword(String str) {
        return Keyword.getByString(str) != null;
    }

    public static boolean isValue(String str) {
        return false;
    } // TODO

    public static boolean isSymbol(String str) {
        return false;
    } // TODO

    public static boolean isPlainText(String str) {
        if (str == null) {
            return false;
        }
        Pattern plainTextPattern = Pattern.compile("[0-9A-Za-z]+");
        Matcher plainTextMatcher = plainTextPattern.matcher(str);
        return plainTextMatcher.matches();
    }

    public static boolean isValidDatabaseName(String str) {
        return str != null && isPlainText(str) && !isKeyword(str);
    }

    public static boolean isValidTableName(String str) {
        return str != null && isPlainText(str) && !isKeyword(str);
    }

    public static boolean isValidAttributeName(String str) {
        return str != null && isPlainText(str) && !isKeyword(str)
                && !getIdAttrName().equals(str.toLowerCase());
    }

    // TODO: implement
    public static boolean isValidAttributeValue(String str) {
        return str != null;
    }
}
