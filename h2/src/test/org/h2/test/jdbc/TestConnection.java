/*
 * Copyright 2004-2014 H2 Group. Multiple-Licensed under the MPL 2.0,
 * and the EPL 1.0 (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package org.h2.test.jdbc;

import org.h2.api.ErrorCode;
import org.h2.jdbc.JdbcConnectionBackwardsCompat;
import org.h2.test.TestBase;
import java.sql.Connection;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Tests the client info
 */
public class TestConnection extends TestBase {

    /**
     * Run just this test.
     *
     * @param a ignored
     */
    public static void main(String... a) throws Exception {
        TestBase.createCaller().init().test();
    }

    @Override
    public void test() throws Exception {
        testSetSupportedClientInfo();
        testSetUnsupportedClientInfo();
        testGetUnsupportedClientInfo();
        testSetSupportedClientInfoProperties();
        testSetUnsupportedClientInfoProperties();
        testSetInternalProperty();
        testSetGetSchema();
    }

    private void testSetInternalProperty() throws SQLException {
        // Use MySQL-mode since this allows all property names
        // (apart from h2 internal names).
        Connection conn = getConnection("clientInfoMySQL;MODE=MySQL");

        assertThrows(SQLClientInfoException.class, conn).setClientInfo("numServers", "SomeValue");
        assertThrows(SQLClientInfoException.class, conn).setClientInfo("server23", "SomeValue");
    }

    private void testSetUnsupportedClientInfoProperties() throws SQLException {
        Connection conn = getConnection("clientInfo");

        Properties properties = new Properties();
        properties.put("ClientUser", "someUser");

        assertThrows(SQLClientInfoException.class, conn).setClientInfo(properties);
    }

    private void testSetSupportedClientInfoProperties() throws SQLException {
        Connection conn = getConnection("clientInfoDB2;MODE=DB2");
        conn.setClientInfo("ApplicationName", "Connection Test");

        Properties properties = new Properties();
        properties.put("ClientUser", "someUser");
        conn.setClientInfo(properties);
        // old property should have been removed
        assertNull(conn.getClientInfo("ApplicationName"));
        // new property has been set
        assertEquals(conn.getClientInfo("ClientUser"), "someUser");
    }

    private void testSetSupportedClientInfo() throws SQLException {
        Connection conn = getConnection("clientInfoDB2;MODE=DB2");
        conn.setClientInfo("ApplicationName", "Connection Test");

        assertEquals(conn.getClientInfo("ApplicationName"), "Connection Test");
    }

    private void testSetUnsupportedClientInfo() throws SQLException {
        Connection conn = getConnection("clientInfoDB2;MODE=DB2");
        assertThrows(SQLClientInfoException.class, conn).setClientInfo(
                "UnsupportedName", "SomeValue");
    }

    private void testGetUnsupportedClientInfo() throws SQLException {
        Connection conn = getConnection("clientInfo");
        assertNull(conn.getClientInfo("UnknownProperty"));
    }

    private void testSetGetSchema() throws SQLException {
        if (config.networked) {
            return;
        }
        deleteDb("schemaSetGet");
        Connection conn = getConnection("schemaSetGet");
        Statement s = conn.createStatement();
        s.executeUpdate("create schema my_test_schema");
        s.executeUpdate("create table my_test_schema.my_test_table(id uuid, nave varchar)");
        JdbcConnectionBackwardsCompat connx = (JdbcConnectionBackwardsCompat) conn;
        assertEquals("PUBLIC", connx.getSchema());
        assertThrows(ErrorCode.TABLE_OR_VIEW_NOT_FOUND_1, s, "select * from my_test_table");
        assertThrows(ErrorCode.SCHEMA_NOT_FOUND_1, connx).setSchema("my_test_table");
        connx.setSchema("MY_TEST_SCHEMA");
        assertEquals("MY_TEST_SCHEMA", connx.getSchema());
        s.executeQuery("select * from my_test_table");
        assertThrows(ErrorCode.SCHEMA_NOT_FOUND_1, connx).setSchema("NON_EXISTING_SCHEMA");
        assertEquals("MY_TEST_SCHEMA", connx.getSchema());
        s.executeUpdate("create schema \"otheR_schEma\"");
        connx.setSchema("otheR_schEma");
        assertEquals("otheR_schEma", connx.getSchema());
        s.close();
        conn.close();
    }
}
