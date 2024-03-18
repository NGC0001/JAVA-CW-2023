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

        public boolean equals(String otherStr) {
            if (otherStr == null) {
                return false;
            }
            return this.str.equals(otherStr.toLowerCase());
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

    // Does not throw DBException.
    // Only a thin layer around List<String> to avoid dynamic modification.
    private static class TokenList {
        private List<String> tokens;
        private int startIdx;
        private int endIdx;

        public TokenList(List<String> tokens) {
            this.tokens = tokens;
            this.startIdx = 0;
            this.endIdx = tokens.size();
        }

        public int size() {
            return this.endIdx - this.startIdx;
        }

        public boolean empty() {
            return size() <= 0;
        }

        public String front() {
            return this.tokens.get(this.startIdx);
        }

        public String popFront() {
            String token = this.tokens.get(this.startIdx);
            this.startIdx += 1;
            return token;
        }

        public String popBack() {
            this.endIdx -= 1;
            return this.tokens.get(this.endIdx);
        }

        @Override
        public String toString() {
            return this.tokens.subList(this.startIdx, this.endIdx).toString();
        }
    }

    @FunctionalInterface
    private static interface ElementExtractor<E> {
        E extract(TokenList tokens) throws GrammarException;
    }

    public static class AlwaysTrueCondition implements Condition {
        public AlwaysTrueCondition() {
        }
        public boolean evaluate(ValueMapper valueMapper) {
            return true;
        }
    }

    public static class CompoundCondition implements Condition {
        private Condition condOne;
        private boolean connectByAnd;
        private Condition condTwo;

        public CompoundCondition(Condition condOne) {
            this(condOne, false, null);
        }

        public CompoundCondition(Condition condOne, boolean connectByAnd, Condition condTwo) {
            this.condOne = condOne;
            this.connectByAnd = connectByAnd;
            this.condTwo = condTwo;
        }

        public void setNextCond(boolean connectByAnd, Condition nextCond) {
            this.connectByAnd = connectByAnd;
            this.condTwo = nextCond;
        }

        public boolean evaluate(ValueMapper valueMapper) throws DBException {
            if (valueMapper == null || this.condOne == null) {
                throw new DBException.NullObjectException("null value mapper or condition");
            }
            boolean condOneTrue = this.condOne.evaluate(valueMapper);
            if (this.condTwo == null) {
                return condOneTrue;
            }
            if (this.connectByAnd) {
                return condOneTrue && this.condTwo.evaluate(valueMapper);
            } else { // Connected by or
                return condOneTrue || this.condTwo.evaluate(valueMapper);
            }
        }
    }

    public static class Comparator implements Condition {
        private String key;
        private Keyword cmpOp;
        private String targetValue;

        public Comparator(String key, Keyword cmpOp, String targetValue) {
            this.key = key;
            this.cmpOp = cmpOp;
            this.targetValue = targetValue;
        }

        public boolean evaluate(ValueMapper valueMapper) throws DBException {
            if (valueMapper == null || this.cmpOp == null) {
                throw new DBException.NullObjectException("null arguments in condition evaluation");
            }
            String value = valueMapper.getValueByKey(this.key);
            if (value == null || this.targetValue == null) {
                return false;
            }
            if (this.cmpOp == Keyword.LIKE) {
                String valueStr = stripSingleQuote(value);
                String targetValueStr = stripSingleQuote(this.targetValue);
                return valueStr.contains(targetValueStr);
            }
            return arithmeticCompare(value, this.targetValue, this.cmpOp);
        }

        private static boolean arithmeticCompare(String value, String targetValue, Keyword op) {
            double diff = arithmeticDiff(value, targetValue);
            switch (op) {
                case EQ: return diff == 0.0;
                case GT: return diff > 0.0;
                case LT: return diff < 0.0;
                case GE: return diff >= 0.0;
                case LE: return diff <= 0.0;
                case NEQ: return diff != 0.0;
            }
            return false;
        }

        private static double arithmeticDiff(String value, String targetValue) {
            try { // Both integer
                long valueLong = Long.valueOf(value);
                long targetValueLong = Long.valueOf(targetValue);
                return (double)(valueLong - targetValueLong); // long -> double
            } catch (Exception notLong) {
            }
            try { // One double, the other double or integer
                double valueDouble = Double.valueOf(value);
                double targetValueDouble = Double.valueOf(targetValue);
                return valueDouble - targetValueDouble;
            } catch (Exception notDouble) {
            }
            if (isKeyword(value) && isKeyword(targetValue)) {
                value = value.toLowerCase();
                targetValue = targetValue.toLowerCase();
            }
            String valueStr = stripSingleQuote(value);
            String targetValueStr = stripSingleQuote(targetValue);
            return (double)(valueStr.compareTo(targetValueStr)); // int -> double
        }

        private static String stripSingleQuote(String str) {
            final char singleQuote = '\'';
            int strLen = str.length();
            if (strLen >= 2 && str.charAt(0) == singleQuote
                    && str.charAt(strLen - 1) == singleQuote) {
                return str.substring(1, strLen - 1);
            }
            return str;
        }
    }

    private static final String idAttrName = "id";
    private static final String allSymbols = "!#$%&()*+,-./:;>=<?@[\\]^_`{}~";

    public static Task parseCommand(String command) throws DBException {
        TokenList tokens = new TokenList(getTokensFromString(command));
        System.out.println("command tokens: " + tokens);
        ensureMoreTokens(tokens, "empty command");
        if (!isKeyword(Keyword.SEMICOLON, tokens.popBack())) {
            throw new GrammarException("command not closed by semicolon");
        }
        ensureMoreTokens(tokens);
        String cmdTypeStr = tokens.popFront();
        Keyword cmdType = Keyword.getByString(cmdTypeStr);
        if (cmdType == null) {
            throw new GrammarException("unknown command type " + cmdTypeStr);
        }
        return parseCommandType(cmdType, tokens);
    }

    private static Task parseCommandType(Keyword cmdType, TokenList tokens) throws GrammarException {
        switch (cmdType) {
            case USE:
                return parseUse(tokens);
            case CREATE:
                return parseCreate(tokens);
            case DROP: return parseDrop(tokens);
            case ALTER: return parseAlter(tokens);
            case INSERT: return parseInsert(tokens);
            case SELECT: return parseSelect(tokens);
            case UPDATE:
            case DELETE:
            case JOIN:
        }
        throw new GrammarException("unknown command type " + cmdType.toString());
    }

    private static Task parseUse(TokenList tokens) throws GrammarException {
        if (tokens.size() != 1) {
            throw new GrammarException("use command expect exactly one database name");
        }
        String databaseName = tokens.popFront();
        ensureValidDatabaseName(databaseName);
        return new Task.UseTask(databaseName);
    }

    private static Task parseCreate(TokenList tokens) throws GrammarException {
        ensureMoreTokens(tokens, "incomplete create command");
        String createTypeStr = tokens.popFront();
        Keyword createType = Keyword.getByString(createTypeStr);
        if (createType == Keyword.DATABASE) {
            return parseCreateDatabase(tokens);
        }
        if (createType == Keyword.TABLE) {
            return parseCreateTable(tokens);
        }
        throw new GrammarException("can not create " + createTypeStr);
    }

    private static Task parseCreateDatabase(TokenList tokens) throws GrammarException {
        if (tokens.size() != 1) {
            throw new GrammarException("expect exactly one database name to create");
        }
        String databaseName = tokens.popFront();
        ensureValidDatabaseName(databaseName);
        return new Task.CreateDatabaseTask(databaseName);
    }

    private static Task parseCreateTable(TokenList tokens) throws GrammarException {
        ensureMoreTokens(tokens, "expect a table name to create");
        String tableName = tokens.popFront();
        ensureValidTableName(tableName);
        Task.CreateTableTask task = new Task.CreateTableTask(tableName);
        if (tokens.empty()) {
            return task;
        }
        ensureIsKeyword(Keyword.LBRACKET, tokens.popFront());
        List<String> attrNames = parseList(tokens, (tokenList) -> {
            ensureMoreTokens(tokenList, "empty or incomplete attribute name list");
            String attrName = tokenList.popFront();
            ensureValidAttributeName(attrName);
            return attrName;
        });
        task.addAttrNames(attrNames);
        ensureMoreTokens(tokens, "expect a bracket to close attribute list");
        ensureIsKeyword(Keyword.RBRACKET, tokens.popFront());
        ensureNoMoreTokens(tokens);
        return task;
    }

    private static Task parseDrop(TokenList tokens) throws GrammarException {
        ensureMoreTokens(tokens, "incomplete drop command");
        String dropTypeStr = tokens.popFront();
        Keyword dropType = Keyword.getByString(dropTypeStr);
        if (dropType == Keyword.DATABASE) {
            return parseDropDatabase(tokens);
        }
        if (dropType == Keyword.TABLE) {
            return parseDropTable(tokens);
        }
        throw new GrammarException("can not drop " + dropTypeStr);
    }

    private static Task parseDropDatabase(TokenList tokens) throws GrammarException {
        if (tokens.size() != 1) {
            throw new GrammarException("expect exactly one database name to drop");
        }
        String databaseName = tokens.popFront();
        ensureValidDatabaseName(databaseName);
        return new Task.DropDatabaseTask(databaseName);
    }

    private static Task parseDropTable(TokenList tokens) throws GrammarException {
        if (tokens.size() != 1) {
            throw new GrammarException("expect exactly one table name to drop");
        }
        String tableName = tokens.popFront();
        ensureValidTableName(tableName);
        return new Task.DropTableTask(tableName);
    }

    private static Task parseAlter(TokenList tokens) throws GrammarException {
        if (tokens.size() != 4) {
            throw new GrammarException(
                    "expect alter command: ALTER TABLE [TableName] ADD/DROP [AttrName]");
        }
        ensureIsKeyword(Keyword.TABLE, tokens.popFront());
        String tableName = tokens.popFront();
        ensureValidTableName(tableName);
        String alterTypeStr = tokens.popFront();
        Keyword alterType = Keyword.getByString(alterTypeStr);
        if (alterType != Keyword.ADD && alterType != Keyword.DROP) {
            throw new GrammarException("unknown alter type " + alterTypeStr);
        }
        String attrName = tokens.popFront();
        ensureValidAttributeName(attrName);
        return new Task.AlterTask(tableName, attrName, alterType == Keyword.ADD);
    }

    private static Task parseInsert(TokenList tokens) throws GrammarException {
        ensurePopKeyword(Keyword.INTO, tokens);
        ensureMoreTokens(tokens, "expect table name for insertion");
        String tableName = tokens.popFront();
        ensureValidTableName(tableName);
        ensurePopKeyword(Keyword.VALUES, tokens);
        ensurePopKeyword(Keyword.LBRACKET, tokens);
        List<String> values = parseList(tokens, (tokenList) -> {
            ensureMoreTokens(tokens, "empty or incomplete attribute value list");
            String value = tokenList.popFront();
            ensureValidAttributeValue(value);
            return value;
        });
        ensurePopKeyword(Keyword.RBRACKET, tokens);
        ensureNoMoreTokens(tokens);
        return new Task.InsertTask(tableName, values);
    }

    private static Task parseSelect(TokenList tokens) throws GrammarException {
        ensureMoreTokens(tokens, "expect selection for select");
        List<String> selection = null;
        if (isKeyword(Keyword.STAR, tokens.front())) {
            tokens.popFront();
        } else {
            selection = parseList(tokens, (tokenList) -> {
                    ensureMoreTokens(tokenList, "incomplete selection list");
                    String attrName = tokenList.popFront();
                    ensureValidAttrOrIdName(attrName);
                    return attrName;
            });
        }
        ensurePopKeyword(Keyword.FROM, tokens);
        ensureMoreTokens(tokens, "expect table name for insertion");
        String tableName = tokens.popFront();
        ensureValidTableName(tableName);
        Task.SelectTask task = new Task.SelectTask(tableName, selection, new AlwaysTrueCondition());
        if (tokens.empty()) {
            return task;
        }
        ensurePopKeyword(Keyword.WHERE, tokens);
        task.setCondition(parseCondition(tokens));
        ensureNoMoreTokens(tokens);
        return task;
    }

    private static CompoundCondition parseCondition(TokenList tokens) throws GrammarException {
        ensureMoreTokens(tokens, "expect a condition");
        Condition condOne;
        if (isKeyword(Keyword.LBRACKET, tokens.front())) {
            tokens.popFront();
            condOne = parseCondition(tokens);
            ensurePopKeyword(Keyword.RBRACKET, tokens);
        } else {
            condOne = parseComparator(tokens);
        }
        CompoundCondition cond = new CompoundCondition(condOne);
        if (tokens.empty()) {
            return cond;
        }
        Keyword connection = Keyword.getByString(tokens.front());
        if (connection == Keyword.AND || connection == Keyword.OR) {
            tokens.popFront();
            Condition condTwo = parseCondition(tokens);
            cond.setNextCond(connection == Keyword.AND, condTwo);
        }
        return cond;
    }

    private static Comparator parseComparator(TokenList tokens) throws GrammarException {
        if (tokens.size() < 3) {
            throw new GrammarException("expect a comparator");
        }
        String attrName = tokens.popFront();
        ensureValidAttrOrIdName(attrName);
        String cmpOpStr = tokens.popFront();
        Keyword cmpOp = Keyword.getByString(cmpOpStr);
        if (cmpOp == null) {
            throw new GrammarException("invalid comparison op " + cmpOpStr);
        }
        String value = tokens.popFront();
        ensureValidAttributeValue(value);
        Comparator cmp = new Comparator(attrName, cmpOp, value);
        return cmp;
    }

    private static <E> List<E> parseList(TokenList tokens, ElementExtractor<E> extractor) throws GrammarException {
        ArrayList<E> list = new ArrayList<E>();
        E e = extractor.extract(tokens);
        list.add(e);
        while (!tokens.empty() && isKeyword(Keyword.COMMA, tokens.front())) {
            tokens.popFront();
            e = extractor.extract(tokens);
            list.add(e);
        }
        return list;
    }

    public static ArrayList<String> getTokensFromString(String str) throws DBException {
        if (str == null) {
            throw new DBException.NullObjectException("get tokens from null");
        }
        ArrayList<String> tokens = new ArrayList<String>();
        String[] fragments = (" " + str + " ").split(Pattern.quote("'"));
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
        final Keyword[] symbolKeywords = { // No GE and LE here
                // The order here: EQ -> NEQ -> GT/LT
                Keyword.STAR, Keyword.EQ, Keyword.NEQ, Keyword.GT, Keyword.LT,
                Keyword.LBRACKET, Keyword.RBRACKET, Keyword.COMMA, Keyword.SEMICOLON
        };
        for (int i = 0; i < symbolKeywords.length; ++i) {
            String keywordStr = symbolKeywords[i].toString();
            str = str.replace(keywordStr, " " + keywordStr + " ");
        }
        str = str.replace(" > =", " >= "); // GE
        str = str.replace(" < =", " <= "); // LE
        str = str.trim();
        if (str.length() == 0) {
            return new ArrayList<String>();
        }
        return Arrays.asList(str.split("\\s+"));
    }

    public static String getIdAttrName() {
        return idAttrName;
    }

    public static boolean isKeyword(String str) {
        return Keyword.getByString(str) != null;
    }

    public static boolean isKeyword(Keyword kw, String str) {
        return kw != null && kw.equals(str);
    }

    public static boolean isSymbol(char ch) {
        return allSymbols.indexOf(ch) >= 0;
    }

    public static boolean isCharLiteral(char ch) {
      if (ch > 256) {
        return false;
      }
      return ch == ' ' || Character.isLetterOrDigit(ch) || isSymbol(ch);
    }

    // String literal includes "'"
    public static boolean isStringLiteral(String str) {
        if (str == null) {
            return false;
        }
        int strLen = str.length();
        if (strLen < 2 || str.charAt(0) != '\'' || str.charAt(strLen - 1) != '\'') {
          return false;
        }
        for (int i = 1; i < strLen - 1; ++i) {
          if (!isCharLiteral(str.charAt(i))) {
            return false;
          }
        }
        return true;
    }

    public static boolean isBooleanLiteral(String str) {
        return isKeyword(Keyword.TRUE, str) || isKeyword(Keyword.FALSE, str);
    }

    public static boolean isFloatLiteral(String str) {
        if (str == null) {
            return false;
        }
        Pattern floatPattern = Pattern.compile("[+-]?[0-9]+\\.[0-9]+");
        Matcher floatMatcher = floatPattern.matcher(str);
        return floatMatcher.matches();
    }

    public static boolean isIntegerLiteral(String str) {
        if (str == null) {
            return false;
        }
        Pattern integerPattern = Pattern.compile("[+-]?[0-9]+");
        Matcher integerMatcher = integerPattern.matcher(str);
        return integerMatcher.matches();
    }

    public static boolean isNullLiteral(String str) {
        return isKeyword(Keyword.NULL, str);
    }

    public static boolean isPlainText(String str) {
        if (str == null) {
            return false;
        }
        Pattern plainTextPattern = Pattern.compile("[0-9A-Za-z]+");
        Matcher plainTextMatcher = plainTextPattern.matcher(str);
        return plainTextMatcher.matches();
    }

    public static boolean isIdAttrName(String str) {
        return str != null && getIdAttrName().equals(str.toLowerCase());
    }

    public static boolean isValidNameString(String str) {
        return str != null && isPlainText(str) && !isKeyword(str);
    }

    public static boolean isValidDatabaseName(String str) {
        return isValidNameString(str);
    }

    public static boolean isValidTableName(String str) {
        return isValidNameString(str);
    }

    // Is valid attribute name, but not "id"
    public static boolean isValidAttributeName(String str) {
        return isValidNameString(str) && !isIdAttrName(str);
    }

    // Is valid attribute name, including "id"
    public static boolean isValidAttrOrIdName(String str) {
        return isValidNameString(str);
    }

    public static boolean isValidAttributeValue(String str) {
        return isStringLiteral(str) || isBooleanLiteral(str)
          || isFloatLiteral(str) || isIntegerLiteral(str) || isNullLiteral(str);
    }

    private static void ensureValidDatabaseName(String databaseName) throws GrammarException {
        if (!isValidDatabaseName(databaseName)) {
            throw new GrammarException("invalid database name " + databaseName);
        }
    }

    private static void ensureValidTableName(String tableName) throws GrammarException {
        if (!isValidTableName(tableName)) {
            throw new GrammarException("invalid table name " + tableName);
        }
    }

    private static void ensureValidAttributeName(String attrName) throws GrammarException {
        if (!isValidAttributeName(attrName)) {
            throw new GrammarException("invalid attribute name " + attrName);
        }
    }

    private static void ensureValidAttrOrIdName(String attrName) throws GrammarException {
        if (!isValidAttrOrIdName(attrName)) {
            throw new GrammarException("invalid attribute name " + attrName);
        }
    }

    public static void ensureValidAttributeValue(String attrValue) throws GrammarException {
        if (!isValidAttributeValue(attrValue)) {
            throw new GrammarException("invalid attribute value " + attrValue);
        }
    }

    private static void ensureIsKeyword(Keyword kw, String str) throws GrammarException {
        if (!isKeyword(kw, str)) {
            throw new GrammarException("expect " + kw + " but found " + str);
        }
    }

    private static void ensureMoreTokens(TokenList tokens, String err) throws GrammarException {
        if (err == null) {
            err = "expect more tokens, command incomplete";
        }
        if (tokens.empty()) {
            throw new GrammarException(err);
        }
    }

    private static void ensureMoreTokens(TokenList tokens) throws GrammarException {
        ensureMoreTokens(tokens, null);
    }

    private static void ensureNoMoreTokens(TokenList tokens, String err) throws GrammarException {
        if (err == null) {
            err = "extra tokens in command";
        }
        if (!tokens.empty()) {
            throw new GrammarException(err);
        }
    }

    private static void ensureNoMoreTokens(TokenList tokens) throws GrammarException {
        ensureNoMoreTokens(tokens, null);
    }

    private static void ensurePopKeyword(Keyword kw, TokenList tokens) throws GrammarException {
        ensureMoreTokens(tokens, "missing keyword " + kw);
        ensureIsKeyword(kw, tokens.popFront());
    }
}
