package orm.dao;

import orm.dto.RunSplitDto;
import java.sql.*;

public class RunSplitDao {

    private static final String SqlInsert =
        "INSERT INTO Run_Split (run_id, segment_id, split_time_ms) VALUES (?, ?, ?)";

    public static void insert(Database pDb, RunSplitDto rs) throws SQLException {
        Database db = Database.connect(pDb);
        try (PreparedStatement ps = db.prepareStatement(SqlInsert)) {
            ps.setInt(1, rs.run_id);
            ps.setInt(2, rs.segment_id);
            ps.setInt(3, rs.split_time_ms);
            db.executeNonQuery(ps);
        } finally {
            Database.close(pDb, db);
        }
    }
}
