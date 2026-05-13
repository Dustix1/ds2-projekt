package orm.dto;

public class SplitItem {
    public int segment_id;
    public int split_time_ms;

    public SplitItem(int segment_id, int split_time_ms) {
        this.segment_id    = segment_id;
        this.split_time_ms = split_time_ms;
    }
}
