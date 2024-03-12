package edu.uob;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Grammar {
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
        LIKE("like");

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
        return str != null && isPlainText(str) && !isKeyword(str) && !idAttrName.equals(str.toLowerCase());
    }
}
