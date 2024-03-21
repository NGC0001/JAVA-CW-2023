package edu.uob;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import static edu.uob.Grammar.compareValue;
import static edu.uob.Grammar.Keyword;
import static edu.uob.Grammar.Keyword.*;

public class ComparisonTests {
    public static void expect(String str1, Keyword op, String str2) {
        try {
            assertTrue(compareValue(str1, op, str2));
        } catch (DBException dbe) {
            fail(dbe.toString());
        }
    }

    public static void expectNot(String str1, Keyword op, String str2) {
        try {
            assertFalse(compareValue(str1, op, str2));
        } catch (DBException dbe) {
            fail(dbe.toString());
        }
    }

    @Test
    public void testNullAndOther() {
        expect("null", EQ, "null");
        expectNot("null", NEQ, "null");
        expectNot("null", GT, "null");
        expectNot("null", LT, "null");
        expectNot("null", GE, "null");
        expectNot("null", LE, "null");
        expectNot("null", LIKE, "null");

        expectNot("null", EQ, "false");
        expect("null", NEQ, "false");
        expectNot("null", GT, "false");
        expectNot("null", LT, "false");
        expectNot("null", GE, "false");
        expectNot("null", LE, "false");
        expectNot("null", LIKE, "false");
        expectNot("false", LE, "null");
        expectNot("false", LIKE, "null");

        expectNot("null", EQ, "''");
        expect("null", NEQ, "''");
        expectNot("null", GT, "''");
        expectNot("null", LT, "''");
        expectNot("null", GE, "''");
        expectNot("null", LE, "''");
        expectNot("null", LIKE, "''");
        expectNot("''", LE, "null");
        expectNot("''", LIKE, "null");
    }

    @Test
    public void testLike() {
        expect("'Good'", LIKE, "'Go'");
        expectNot("'Good'", LIKE, "'go'");
        expect("'Good'", LIKE, "'oo'");
        expectNot("'Go'", LIKE, "'Good'");
        expect("'Good'", LIKE, "'Good'");
        expect("'Hello world'", LIKE, "'o w'");
        expect("'False'", LIKE, "False");
        expect("False", LIKE, "'Fa'");
        expectNot("false", LIKE, "'Fa'");
        expect("false", LIKE, "false");
        expectNot("False", LIKE, "false");
        expect("''", LIKE, "''");
        expect("'a'", LIKE, "''");
        expectNot("''", LIKE, "' '");
        expect("2.0", LIKE, "2");
        expectNot("2", LIKE, "2.0");
        expectNot("2.0", LIKE, "+2");
        expect("'2.0'", LIKE, "2.0");
        expect("2.0", LIKE, "'2.'");
        expectNot("'2.0'", LIKE, "2.00");
        expect("2.000", LIKE, "'2.0'");
        expect("2.000", LIKE, "2.0");
    }

    @Test
    public void testKeywordAndKeyword() {
        expectNot("true", EQ, "FALSE");
        expectNot("TRUE", EQ, "False");
        expect("TRUE", EQ, "true");
        expect("false", EQ, "False");
        expect("true", NEQ, "FALSE");
        expect("TRUE", NEQ, "False");
        expectNot("TRUE", NEQ, "true");
        expectNot("false", NEQ, "False");

        expectNot("True", GT, "False");
        expectNot("True", GE, "False");
        expectNot("True", LT, "False");
        expectNot("True", LE, "False");
        expectNot("false", GT, "True");
        expectNot("false", GE, "True");
        expectNot("false", LT, "True");
        expectNot("false", LE, "True");
    }

    @Test
    public void testLongOrdering() {
        expect("+002", EQ, "2");
        expect("0020", EQ, "+20");
        expectNot("+002", EQ, "-2");
        expectNot("7", EQ, "-1");
        expect("7", NEQ, "-1");
        expect("7", GT, "-1");
        expect("7", GE, "-1");
        expectNot("7", LT, "-1");
        expectNot("7", LE, "-1");
        expect("3", EQ, "03");
        expectNot("3", NEQ, "03");
        expectNot("3", GT, "03");
        expectNot("3", LT, "03");
        expect("3", GE, "03");
        expect("3", LE, "03");
    }

    @Test
    public void testDoubleOrdering() {
        expect("+001.0", EQ, "1.0");
        expect("+001.0", EQ, "1");
        expect("1", EQ, "1.0");
        expect("1", GT, "-1.0");
        expect("1", GE, "-1.0");
        expect("-1", LT, "1.0");
        expect("-1", LE, "1.0");
        expect("1.5", NEQ, "1");
        expect("-1.5", EQ, "-1.5");
        expect("+1.5", GT, "-1.5");
        expect("+1.5", GE, "-1.5");
        expectNot("+1.5", LT, "-1.5");
        expectNot("+1.5", LE, "-1.5");
        expect("+0.0", EQ, "-0.0");
        expectNot("+0.0", NEQ, "-0.0");
        expectNot("+0.0", GT, "-0.0");
        expectNot("+0.0", LT, "-0.0");
        expect("+0.0", GE, "-0.0");
        expect("+0.0", LE, "-0.0");
    }

    @Test
    public void testStringOrdering() {
        expect("''", EQ, "''");
        expect("'a'", GT, "''");
        expect("'AB'", EQ, "'AB'");
        expect("'AB'", GE, "'AB'");
        expect("'AB'", LE, "'AB'");
        expectNot("'AB'", NEQ, "'AB'");
        expectNot("'AB'", GT, "'AB'");
        expectNot("'AB'", LT, "'AB'");
        expectNot("'B'", EQ, "'A'");
        expect("'B'", NEQ, "'A'");
        expect("'B'", GT, "'A'");
        expect("'B'", GE, "'A'");
        expectNot("'B'", LT, "'A'");
        expectNot("'B'", LE, "'A'");
        expect("'AB'", GT, "'A'");
        expect("'AB'", GE, "'A'");
        expect("'AB'", NEQ, "'A'");
        expectNot("'AB'", LT, "'A'");
        expectNot("'AB'", LE, "'A'");
        expectNot("'AB'", EQ, "'A'");

        expect("'false'", GT, "False");
        expect("'false'", EQ, "false");
        expect("FALSE", GT, "''");
        expect("'true'", GT, "0");
        expect("'true'", GT, "+1.0");
        expect("true", GT, "'0.0'");
    }
}
