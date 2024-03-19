package edu.uob;

import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class CommandTests {
    private String dir;
    private DBServer server;

    // Random name generator
    private String generateRandomName() {
        String randomName = "";
        for(int i = 0; i < 10; ++i) {
            randomName += (char)(97 + 25.0 * Math.random());
        }
        return randomName;
    }

    private String sendCommandToServer(String command) {
        // Try to send a command to the server - this call will timeout if it takes too long
        return assertTimeoutPreemptively(Duration.ofMillis(500),
            () -> server.handleCommand(command), "server took too long to respond");
    }

    private void assertOk(String response) {
        assertTrue(response.startsWith("[OK]"), "expect OK");
        assertFalse(response.contains("[ERROR]"), "expect no ERROR");
    }

    private void assertError(String response) {
        assertTrue(response.startsWith("[ERROR]"), "expect ERROR");
        assertFalse(response.contains("[OK]"), "expect no OK");
    }

    private void assertOkHasRow(String response, String... row) {
        assertTrue(findFromOkResponse(response, row).size() >= 1);
    }

    private void assertOkUniqueRow(String response, String... row) {
        assertTrue(findFromOkResponse(response, row).size() == 1);
    }

    private void assertOkHeaderRow(String response, String... row) {
        List<Integer> rowIdx = findFromOkResponse(response, row);
        assertTrue(rowIdx.size() >= 1 && rowIdx.get(0).intValue() == 0);
    }

    private List<Integer> findFromOkResponse(String response, String[] row) {
        assertOk(response);
        List<Integer> rowIdx = new ArrayList<Integer>();
        String[] lines = response.trim().split("\\n");
        for (int i = 1; i < lines.length; ++i) {
            List<String> tokens = null;
            try {
                tokens = Grammar.getTokensFromString(lines[i]);
            } catch (Exception e) {
                fail(e);
            }
            if (stringListEqual(tokens, Arrays.asList(row))) {
                rowIdx.add(i - 1);
            }
        }
        return rowIdx;
    }

    private boolean stringListEqual(List<String> l1, List<String> l2) {
        if (l1 == null) {
            return l2 == null;
        }
        if (l1.size() != l2.size()) {
            return false;
        }
        for (int i = 0; i < l1.size(); ++i) {
            String s1 = l1.get(i);
            String s2 = l2.get(i);
            if (s1.length() < 2 || s1.charAt(0) != '\'' || s1.charAt(s1.length() - 1) != '\'') {
                s1 = s1.toLowerCase();
            }
            if (s2.length() < 2 || s2.charAt(0) != '\'' || s2.charAt(s2.length() - 1) != '\'') {
                s2 = s2.toLowerCase();
            }
            if (!s1.equals(s2)) {
                return false;
            }
        }
        return true;
    }

    private void assertOkCountDataRows(String response, int cnt) {
        assertOk(response);
        String[] lines = response.trim().split("\\n");
        assertTrue(lines.length - 2 == cnt);
    }

    // Create a new server _before_ every @Test
    @BeforeEach
    public void setup() {
        String subdir = System.currentTimeMillis() + "_" + generateRandomName();
        this.dir = Paths.get("databases", subdir).toString();
        this.server = new DBServer(this.dir);
    }

    @Test
    public void testInvalidCommand() {
        assertError(sendCommandToServer(""));
        assertError(sendCommandToServer("\t ; \n\t"));
        assertError(sendCommandToServer("create database newdb"));
        assertError(sendCommandToServer("command;"));
    }

    @Test
    public void testCreateDatabaseCommand() {
        assertOk(sendCommandToServer("\n Create  database \n  newdb1 \t;\t"));
        assertOk(sendCommandToServer("\n CREATE   dataBASE    NEWDB2 \t  ;"));
        assertError(sendCommandToServer("create database newdb2;"));
        assertError(sendCommandToServer("create database newdb3;;"));
        assertError(sendCommandToServer("create database table;"));
        assertError(sendCommandToServer("create database newdb4 newdb5;"));
        assertError(sendCommandToServer("create database newdb4, newdb5;"));
        assertOk(sendCommandToServer("create database newdb4;"));
    }

    @Test
    public void testUseCommand() {
        assertOk(sendCommandToServer("create database newdb;"));
        assertOk(sendCommandToServer("create database newdb2;"));
        assertOk(sendCommandToServer("use newdb;"));
        assertOk(sendCommandToServer("use newdb;"));
        assertOk(sendCommandToServer("\nuse\tNEWDB ;\n"));
        assertError(sendCommandToServer("use nosuchdb;"));
        assertError(sendCommandToServer("use newdb;;"));
        assertError(sendCommandToServer("use newdb newdb2;"));
        assertError(sendCommandToServer("use database;"));
    }

    @Test
    public void testCreateTableCommand() {
        assertOk(sendCommandToServer("create database newdb;"));
        assertError(sendCommandToServer("create table t1;"));
        assertOk(sendCommandToServer("use newdb;"));
        assertOk(sendCommandToServer("create table \tt1\t;"));
        assertOk(sendCommandToServer("create table t2;"));
        assertError(sendCommandToServer("create table T1;"));
        assertError(sendCommandToServer("create table t3;;"));
        assertError(sendCommandToServer("create table t5 t6;"));
        assertError(sendCommandToServer("create table t5, t6;"));
        assertError(sendCommandToServer("create table on;"));
        assertOk(sendCommandToServer("create database newdb2;"));
        assertError(sendCommandToServer("create table T1;"));
        assertError(sendCommandToServer("create table table1();"));
        assertOk(sendCommandToServer("create table table1(1,2,3);"));
        assertOk(sendCommandToServer("create table table2(attr1);"));
        assertOk(sendCommandToServer("create table table3 (1attr\n);"));
        assertOk(sendCommandToServer("create table table4 ( attr1, attr2);"));
        assertError(sendCommandToServer("create table table5 (a1, a2)(a3);"));
        assertError(sendCommandToServer("create table table6 (a1, a2,);"));
        assertError(sendCommandToServer("create table table7 (,);"));
        assertError(sendCommandToServer("create table table8 ,;"));
        assertOk(sendCommandToServer("create table table5 (a1, 2);"));
    }

    @Test
    public void testDropDatabaseCommand() {
        assertOk(sendCommandToServer("create database db1;"));
        assertOk(sendCommandToServer("create database db2;"));
        assertOk(sendCommandToServer("create database db3;"));
        assertOk(sendCommandToServer("use db3;"));
        assertOk(sendCommandToServer("create table t1;"));
        assertError(sendCommandToServer("drop database db2;;"));
        assertError(sendCommandToServer("drop database db1 db2;"));
        assertError(sendCommandToServer("drop database db1, db2;"));
        assertOk(sendCommandToServer("drop database DB2;"));
        assertError(sendCommandToServer("drop database db2;"));
        assertError(sendCommandToServer("use db2;"));
        assertOk(sendCommandToServer("create table t2;"));
        assertOk(sendCommandToServer("drop database db3;"));
        assertError(sendCommandToServer("create table t3;"));
        assertOk(sendCommandToServer("drop database db1;"));
    }

    @Test
    public void testInsertCommand() {
        assertOk(sendCommandToServer("create database db;"));
        assertError(sendCommandToServer("insert into t2 values (1, 2, 3);"));
        assertOk(sendCommandToServer("use db;"));
        assertOk(sendCommandToServer("create table t1;"));
        assertOk(sendCommandToServer("create table t2 (a1, a2, a3);"));
        assertError(sendCommandToServer("insert into t1 values ();"));
        assertError(sendCommandToServer("insert into t2 values (1);"));
        assertError(sendCommandToServer("insert into t2 values (1, 2);"));
        assertOk(sendCommandToServer("insert into t2 values (1, 2, 3);"));
        assertOk(sendCommandToServer("insert into t2 values (1, 2, 3);"));
        assertOk(sendCommandToServer("insert into t2 values (True, 2.0, ' s as ');"));
        assertOk(sendCommandToServer("insert into t2 values (false, 'or', '');"));
        assertOk(sendCommandToServer("insert into t2 values (-3, -2.3, nULL);"));
        assertOk(sendCommandToServer("insert into t2 values (+3, +2.3, ' ');"));
        assertOk(sendCommandToServer("insert into t2 values ('-3.0, 2(', 'false', '  {)}');"));
        assertError(sendCommandToServer("insert into t2 values (1, 2, 3) (1, 2, 3);"));
        assertError(sendCommandToServer("insert into t2 values (1, 2, 3), (1, 2, 3);"));
        assertError(sendCommandToServer("insert into t2 values (1, 2, or);"));
        assertError(sendCommandToServer("insert into t2 values (1, 2, 3.);"));
        assertError(sendCommandToServer("insert into t2 values (1, 2, .3);"));
        assertError(sendCommandToServer("insert into t2 values (1, 2, '\t');"));
        assertError(sendCommandToServer("insert into t2 values (1, 2, ++3);"));
        assertError(sendCommandToServer("insert into t2 values (1, 2, 3, 4);"));
        assertError(sendCommandToServer("insert into t2 values 1, 2, 3;"));
        assertError(sendCommandToServer("insert into t2 values (1, 2), 3;"));
        assertError(sendCommandToServer("insert into t2 values (1, 2, 3);;"));
        assertError(sendCommandToServer("insert into t2 values (a, 2, 3);"));
    }

    @Test
    public void testDropTableCommand() {
        assertOk(sendCommandToServer("create database db;"));
        assertError(sendCommandToServer("drop table t1;"));
        assertOk(sendCommandToServer("use db;"));
        assertOk(sendCommandToServer("create table t1;"));
        assertOk(sendCommandToServer("create table t2 (a1, a2, a3);"));
        assertOk(sendCommandToServer("insert into t2 values (1, 2, 3);"));
        assertError(sendCommandToServer("drop table t1;;"));
        assertError(sendCommandToServer("drop table t1 t2;"));
        assertError(sendCommandToServer("drop table t1, t2;"));
        assertOk(sendCommandToServer("drop table t1;"));
        assertError(sendCommandToServer("drop table t1;"));
        assertOk(sendCommandToServer("drop table t2;"));
        assertError(sendCommandToServer("insert into t2 values (1, 2, 3);"));
        assertError(sendCommandToServer("drop table t2;"));
    }

    @Test
    public void testAlterCommand() {
        assertOk(sendCommandToServer("create database db;"));
        assertError(sendCommandToServer("alter table t1 add attr1;"));
        assertOk(sendCommandToServer("use db;"));
        assertOk(sendCommandToServer("create table t1;"));
        assertOk(sendCommandToServer("create table t2 (a1, a2, a3);"));
        assertOk(sendCommandToServer("alter table t1 add 1;"));
        assertOk(sendCommandToServer("insert into t1 values (1);"));
        assertError(sendCommandToServer("insert into t1 values (1, 2);"));
        assertError(sendCommandToServer("alter table t1 add 1;"));
        assertOk(sendCommandToServer("alter table t1 add A;"));
        assertError(sendCommandToServer("insert into t1 values (1);"));
        assertOk(sendCommandToServer("insert into t1 values (1, 2);"));
        assertError(sendCommandToServer("alter table t1 add a;"));
        assertError(sendCommandToServer("alter table t1 add and;"));
        assertError(sendCommandToServer("alter table t1 add Id;"));
        assertError(sendCommandToServer("alter table t1 add c;;"));
        assertError(sendCommandToServer("alter table t1 add c d;"));
        assertError(sendCommandToServer("alter table t1 add c, d;"));
        assertOk(sendCommandToServer("alter table t1 drop a;"));
        assertOk(sendCommandToServer("insert into t1 values (1);"));
        assertError(sendCommandToServer("insert into t1 values (1, 2);"));
        assertError(sendCommandToServer("alter table t1 drop A;"));
        assertOk(sendCommandToServer("alter table t1 drop 1;"));
        assertError(sendCommandToServer("alter table t1 drop 1;"));
        assertError(sendCommandToServer("insert into t1 values ();"));
        assertError(sendCommandToServer("insert into t1 values (1);"));
        assertError(sendCommandToServer("insert into t1 values (1, 2);"));
    }

    @Test
    public void testSeletCommand() {
        String response = null;
        assertOk(sendCommandToServer("create database db;"));
        assertError(sendCommandToServer("select * from t;"));
        assertOk(sendCommandToServer("use db;"));
        assertOk(sendCommandToServer("create table t (a1, a2);"));
        assertOk(sendCommandToServer("insert into t values (1, 2);"));
        response = sendCommandToServer("   select *\t \tfrom t\n;");
        assertOkHeaderRow(response, "id", "a1", "a2");
        assertOkUniqueRow(response, "0", "1", "2");
        response = sendCommandToServer("   seLECT a2,\nId,id,\tA1 \tFrom t\n;");
        assertOkHeaderRow(response, "a2", "id", "id", "a1");
        assertOkUniqueRow(response, "2", "0", "0", "1");
        assertError(sendCommandToServer("select * from t;;"));
        assertError(sendCommandToServer("select * from t,t;"));
        assertError(sendCommandToServer("select * from t t;"));
        assertError(sendCommandToServer("select *, a1 from t;"));
        assertError(sendCommandToServer("select a2, * from t;"));
        assertError(sendCommandToServer("select a2, from t;"));
        assertError(sendCommandToServer("select (a2) from t;"));
        assertError(sendCommandToServer("select from t;"));
        assertError(sendCommandToServer("select a3 from t;"));
        assertOk(sendCommandToServer("alter table t drop a1;"));
        assertError(sendCommandToServer("select a1 from t;"));
        response = sendCommandToServer("select * from t;");
        assertOkHeaderRow(response, "id", "a2");
        assertOkUniqueRow(response, "0", "2");
        assertOk(sendCommandToServer("alter table t add a3;"));
        response = sendCommandToServer("select * from t;");
        assertOkHeaderRow(response, "id", "a2", "a3");
        assertOkUniqueRow(response, "0", "2", "null");
        assertOk(sendCommandToServer("insert into t values ('A1B2', +2.0);"));
        assertOkUniqueRow(sendCommandToServer("select * from t;"), "1", "'A1B2'", "+2.0");
        assertError(sendCommandToServer("select * from t where;"));
        assertError(sendCommandToServer("select * from t where ();"));
        assertError(sendCommandToServer("select * from t where (id == 0));"));
        assertError(sendCommandToServer("select * from t where (id = 0);"));
        assertError(sendCommandToServer("select * from t where (id and 0);"));
        assertError(sendCommandToServer("select * from t where (id 0 0);"));
        assertError(sendCommandToServer("select * from t where (id == id);"));
        assertError(sendCommandToServer("select * from t where (0 == id);"));
        assertError(sendCommandToServer("select * from t where (id 0);"));
        assertError(sendCommandToServer("select * from t where (id == 0) and;"));
        assertError(sendCommandToServer("select * from t where id == 0 and ();"));
        assertOkCountDataRows(sendCommandToServer("select * from t where (id == 0 and ((a3 == +2.0)));"), 0);
        assertOkCountDataRows(sendCommandToServer("select * from t where (id == 0 and ((a3 == +2.0))) or id == 1;"), 1);
        assertOkCountDataRows(sendCommandToServer("select * from t where id == 0 and (((a3 == +2.0)) or id == 1);"), 0);
    }
}
// <Update>
// <Delete>
// <Join>
// Persistence
