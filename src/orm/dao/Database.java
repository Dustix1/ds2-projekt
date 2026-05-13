package orm.dao;

import java.sql.*;

public class Database {

    private Connection conn;
    private boolean ownsConnection;

    private Database(Connection conn, boolean ownsConnection) {
        this.conn = conn;
        this.ownsConnection = ownsConnection;
    }

    // If pDb already has an active transaction, reuse it; otherwise open new connection.
    public static Database connect(Database pDb) throws SQLException {
        if (pDb != null && pDb.conn != null && !pDb.conn.getAutoCommit()) {
            return new Database(pDb.conn, false);
        }
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Oracle JDBC Driver not found in classpath.", e);
        }
        String url  = "jdbc:oracle:thin:@//127.0.0.1:1521/FREEPDB1";
        String user = "speedrun";
        String pass = "speedrun";
        Connection c = DriverManager.getConnection(url, user, pass);
        c.setAutoCommit(true);
        return new Database(c, true);
    }

    public static void close(Database pDb, Database db) throws SQLException {
        if (db != null && db.ownsConnection && db.conn != null) {
            db.conn.close();
            db.conn = null;
        }
    }

    public void beginTransaction() throws SQLException {
        conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        conn.setAutoCommit(false);
    }

    public void endTransaction() throws SQLException {
        conn.commit();
        conn.setAutoCommit(true);
    }

    public void rollback() {
        try {
            if (conn != null) {
                conn.rollback();
                conn.setAutoCommit(true);
            }
        } catch (SQLException ignored) {}
    }

    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return conn.prepareStatement(sql);
    }

    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        return conn.prepareStatement(sql, columnNames);
    }

    // Returns generated key (RETURNING id INTO ?) via getGeneratedKeys.
    public int executeScalar(PreparedStatement ps) throws SQLException {
        ps.executeUpdate();
        try (ResultSet rs = ps.getGeneratedKeys()) {
            if (rs.next()) return rs.getInt(1);
        }
        throw new SQLException("No generated key returned.");
    }

    // Returns single int value (e.g. COUNT).
    public int executeScalarInt(PreparedStatement ps) throws SQLException {
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    public ResultSet select(PreparedStatement ps) throws SQLException {
        return ps.executeQuery();
    }

    public void executeNonQuery(PreparedStatement ps) throws SQLException {
        ps.executeUpdate();
    }

    public CallableStatement prepareCall(String sql) throws SQLException {
        return conn.prepareCall(sql);
    }
}
