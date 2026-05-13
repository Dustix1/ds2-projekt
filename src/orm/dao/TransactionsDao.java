package orm.dao;

import orm.dto.RunDto;
import orm.dto.RunSplitDto;
import orm.dto.SplitItem;

import java.sql.*;
import java.util.List;

public class TransactionsDao {

    // ── F12. FinishRun – Java transaction ────────────────────────────────
    public static boolean finishRun(
            Database pDb,
            int         p_id_user,
            int         p_id_category,
            int         p_id_platform,
            String      p_video_link,
            List<SplitItem> p_splits,
            int[]       p_id_run)          // out: generated run id
    {
        Database db = null;
        boolean ret = false;
        try {
            db = Database.connect(pDb);
            db.beginTransaction();

            // Krok 2 – Validace počtu mezičasů
            int expected = SegmentDao.countByCategory(db, p_id_category);
            if (p_splits.size() != expected) {
                db.rollback();
                return false;
            }

            // Krok 3 – Výpočet celkového času
            int finalTime = 0;
            for (SplitItem s : p_splits) finalTime += s.split_time_ms;

            // Krok PB – Validace osobního rekordu
            // Hráč nemůže odeslat run horší než jeho aktuální nejlepší (verifikovaný nebo čekající) čas.
            Integer pb = RunDao.getPersonalBest(db, p_id_user, p_id_category);
            if (pb != null && finalTime >= pb) {
                db.rollback();
                return false;
            }

            // Krok 4 – INSERT Run
            RunDto run = new RunDto();
            run.user_id      = p_id_user;
            run.category_id  = p_id_category;
            run.platform_id  = p_id_platform;
            run.final_time_ms = finalTime;
            run.video_link   = p_video_link;
            int idRun = RunDao.insert(db, run);
            p_id_run[0] = idRun;

            // Krok 5 – INSERT Run_Split × n
            for (SplitItem s : p_splits) {
                RunSplitDto rs = new RunSplitDto();
                rs.run_id        = idRun;
                rs.segment_id    = s.segment_id;
                rs.split_time_ms = s.split_time_ms;
                RunSplitDao.insert(db, rs);
            }

            db.endTransaction();
            ret = true;

        } catch (SQLException e) {
            e.printStackTrace();
            if (db != null) db.rollback();
        } finally {
            try { Database.close(pDb, db); } catch (SQLException ignored) {}
        }
        return ret;
    }

    // ── F12. FinishRun – volání uložené procedury ─────────────────────────
    // Splits jsou předány jako JSON: [{"segment_id":1,"split_time_ms":60000}, ...]
    public static boolean finishRun_sp(
            Database pDb,
            int         p_id_user,
            int         p_id_category,
            int         p_id_platform,
            String      p_video_link,
            List<SplitItem> p_splits,
            int[]       p_id_run)
    {
        Database db = null;
        boolean ret = false;
        try {
            db = Database.connect(pDb);

            // Serialize splits to JSON
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < p_splits.size(); i++) {
                SplitItem s = p_splits.get(i);
                if (i > 0) json.append(",");
                json.append("{\"segment_id\":").append(s.segment_id)
                    .append(",\"split_time_ms\":").append(s.split_time_ms).append("}");
            }
            json.append("]");

            try (CallableStatement cs = db.prepareCall(
                "{call FinishRun(?, ?, ?, ?, ?, ?, ?)}")) {
                cs.setInt(1, p_id_user);
                cs.setInt(2, p_id_category);
                cs.setInt(3, p_id_platform);
                cs.setString(4, p_video_link);
                cs.setString(5, json.toString());

                cs.registerOutParameter(6, Types.INTEGER);  // p_id_run
                cs.registerOutParameter(7, Types.INTEGER);  // p_ret

                cs.execute();

                int pRet = cs.getInt(7);
                ret = (pRet == 1);
                if (ret) p_id_run[0] = cs.getInt(6);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try { Database.close(pDb, db); } catch (SQLException ignored) {}
        }
        return ret;
    }
}
