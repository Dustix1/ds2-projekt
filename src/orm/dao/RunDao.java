package orm.dao;

import orm.dto.RunDto;
import java.sql.*;

public class RunDao {

    private static final String SqlInsert =
        "INSERT INTO Run (user_id, category_id, platform_id, final_time_ms, " +
        "                 video_link, submitted_at, status, verified_by) " +
        "VALUES (?, ?, ?, ?, ?, SYSDATE, 'pending', NULL)";

    private static final String SqlGetPersonalBest =
        "SELECT MIN(final_time_ms) FROM Run " +
        "WHERE user_id = ? AND category_id = ? AND status IN ('verified', 'pending')";

    public static int insert(Database pDb, RunDto r) throws SQLException {
        Database db = Database.connect(pDb);
        try (PreparedStatement ps = db.prepareStatement(SqlInsert, new String[]{"ID"})) {
            ps.setInt(1, r.user_id);
            ps.setInt(2, r.category_id);
            ps.setInt(3, r.platform_id);
            ps.setInt(4, r.final_time_ms);
            ps.setString(5, r.video_link);
            return db.executeScalar(ps);
        } finally {
            Database.close(pDb, db);
        }
    }

    // Returns null if user has no verified run in this category.
    public static Integer getPersonalBest(Database pDb, int userId, int categoryId) throws SQLException {
        Database db = Database.connect(pDb);
        try (PreparedStatement ps = db.prepareStatement(SqlGetPersonalBest)) {
            ps.setInt(1, userId);
            ps.setInt(2, categoryId);
            try (ResultSet rs = db.select(ps)) {
                if (rs.next()) {
                    int val = rs.getInt(1);
                    if (!rs.wasNull()) return val;
                }
            }
            return null;
        } finally {
            Database.close(pDb, db);
        }
    }
}
