package orm.dao;

import java.sql.*;

public class SegmentDao {

    private static final String SqlCountByCategory =
        "SELECT COUNT(*) FROM Segment WHERE category_id = ?";

    public static int countByCategory(Database pDb, int categoryId) throws SQLException {
        Database db = Database.connect(pDb);
        try (PreparedStatement ps = db.prepareStatement(SqlCountByCategory)) {
            ps.setInt(1, categoryId);
            return db.executeScalarInt(ps);
        } finally {
            Database.close(pDb, db);
        }
    }
}
