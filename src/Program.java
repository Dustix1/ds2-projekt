import orm.dao.TransactionsDao;
import orm.dto.SplitItem;

import java.util.Arrays;
import java.util.List;

public class Program {

    public static void main(String[] args) throws Exception {

        // Splits pro kategorii "16 Star" (2 segmenty v mock DB)
        List<SplitItem> splits = Arrays.asList(
            new SplitItem(1, 60000),   // Bob-omb Battlefield
            new SplitItem(2, 120000)   // Whomps Fortress
        );

        // Horší čas – nebude přijat pokud hráč má PB
        List<SplitItem> worseSplits = Arrays.asList(
            new SplitItem(1, 900000),
            new SplitItem(2, 600000)
        );

        // Špatný počet segmentů (např. pouze 1 místo 2) – očekáváme odmítnutí
        List<SplitItem> invalidSplits = Arrays.asList(
            new SplitItem(1, 60000)
        );

        int[] idRun = {0};
        boolean ret;

        int totalTimeSplits = splits.stream().mapToInt(s -> s.split_time_ms).sum();
        int totalTimeWorse  = worseSplits.stream().mapToInt(s -> s.split_time_ms).sum();
        int totalTimeInvalid = invalidSplits.stream().mapToInt(s -> s.split_time_ms).sum();

        // ── Java transakce ──────────────────────────────────────────────
        
        // 1. Nový run (žádný PB ještě neexistuje) – očekáváme úspěch
        idRun[0] = 0;
        ret = TransactionsDao.finishRun(null, 2, 1, 3, "https://youtube.com/watch?v=ex1", splits, idRun);
        System.out.printf("FinishRun:    ret: %-5s id: %-5s time: %d ms%n", ret, ret ? idRun[0] : "null", totalTimeSplits);

        // 2. Run s horším časem – očekáváme odmítnutí (PB validation)
        idRun[0] = 0;
        ret = TransactionsDao.finishRun(null, 2, 1, 3, "https://youtube.com/watch?v=ex2", worseSplits, idRun);
        System.out.printf("FinishRun:    ret: %-5s id: %-5s time: %d ms%n", ret, ret ? idRun[0] : "null", totalTimeWorse);

        // 2b. Run s chybným počtem segmentů – očekáváme odmítnutí
        idRun[0] = 0;
        ret = TransactionsDao.finishRun(null, 2, 1, 3, "https://youtube.com/watch?v=ex2b", invalidSplits, idRun);
        System.out.printf("FinishRun:    ret: %-5s id: %-5s time: %d ms (invalid seg count)%n", ret, ret ? idRun[0] : "null", totalTimeInvalid);

        // ── Uložená procedura ───────────────────────────────────────────

        // 3. Nový run přes SP – očekáváme úspěch
        idRun[0] = 0;
        ret = TransactionsDao.finishRun_sp(null, 3, 3, 1, "https://twitch.tv/ex3", splits, idRun);
        System.out.printf("FinishRun_sp: ret: %-5s id: %-5s time: %d ms%n", ret, ret ? idRun[0] : "null", totalTimeSplits);

        // 4. Run s horším časem přes SP – očekáváme odmítnutí
        idRun[0] = 0;
        ret = TransactionsDao.finishRun_sp(null, 3, 3, 1, "https://twitch.tv/ex4", worseSplits, idRun);
        System.out.printf("FinishRun_sp: ret: %-5s id: %-5s time: %d ms%n", ret, ret ? idRun[0] : "null", totalTimeWorse);

        // 4b. Run s chybným počtem segmentů přes SP – očekáváme odmítnutí
        idRun[0] = 0;
        ret = TransactionsDao.finishRun_sp(null, 3, 3, 1, "https://twitch.tv/ex4b", invalidSplits, idRun);
        System.out.printf("FinishRun_sp: ret: %-5s id: %-5s time: %d ms (invalid seg count)%n", ret, ret ? idRun[0] : "null", totalTimeInvalid);

    }
}
