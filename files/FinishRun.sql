-- F12. FinishRun – uložená procedura (Oracle PL/SQL)
-- Splits jsou předány jako JSON: [{"segment_id":1,"split_time_ms":60000}, ...]

CREATE OR REPLACE PROCEDURE FinishRun(
    p_id_user      IN  NUMBER,
    p_id_category  IN  NUMBER,
    p_id_platform  IN  NUMBER,
    p_video_link   IN  VARCHAR2,
    p_splits_json  IN  VARCHAR2,
    p_id_run       OUT NUMBER,
    p_ret          OUT NUMBER
) AS
    v_expected    NUMBER;
    v_split_count NUMBER;
    v_final_time  NUMBER;
    v_pb          NUMBER;
BEGIN
    p_ret    := 0;
    p_id_run := NULL;

    -- Krok 2: Validace počtu mezičasů
    SELECT COUNT(*) INTO v_expected
    FROM Segment
    WHERE category_id = p_id_category;

    SELECT COUNT(*) INTO v_split_count
    FROM JSON_TABLE(p_splits_json, '$[*]'
         COLUMNS (segment_id NUMBER PATH '$.segment_id' NULL ON ERROR));

    IF v_split_count != v_expected THEN
        ROLLBACK;
        RETURN;
    END IF;

    -- Krok 3: Výpočet celkového času
    SELECT SUM(split_time_ms) INTO v_final_time
    FROM JSON_TABLE(p_splits_json, '$[*]'
         COLUMNS (split_time_ms NUMBER PATH '$.split_time_ms' NULL ON ERROR));

    -- Krok PB: Validace osobního rekordu
    -- Hráč nemůže odeslat run s horším časem než je jeho nejlepší verifikovaný nebo čekající run (Anti-spam).
    SELECT MIN(final_time_ms) INTO v_pb
    FROM Run
    WHERE user_id     = p_id_user
      AND category_id = p_id_category
      AND status      IN ('verified', 'pending');

    IF v_pb IS NOT NULL AND v_final_time >= v_pb THEN
        ROLLBACK;
        RETURN;
    END IF;

    -- Krok 4: INSERT Run
    INSERT INTO Run
        (user_id, category_id, platform_id, final_time_ms,
         video_link, submitted_at, status, verified_by)
    VALUES
        (p_id_user, p_id_category, p_id_platform, v_final_time,
         p_video_link, SYSDATE, 'pending', NULL)
    RETURNING id INTO p_id_run;

    -- Krok 5: INSERT Run_Split – jeden příkaz přes JSON_TABLE
    INSERT INTO Run_Split (run_id, segment_id, split_time_ms)
    SELECT p_id_run, jt.segment_id, jt.split_time_ms
    FROM JSON_TABLE(p_splits_json, '$[*]'
         COLUMNS (
             segment_id    NUMBER PATH '$.segment_id'    NULL ON ERROR,
             split_time_ms NUMBER PATH '$.split_time_ms' NULL ON ERROR
         )
    ) jt;

    COMMIT;
    p_ret := 1;

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        p_ret    := 0;
        p_id_run := NULL;
END;
/
