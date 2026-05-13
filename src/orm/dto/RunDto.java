package orm.dto;

import java.sql.Date;

public class RunDto {
    public int     id;
    public int     user_id;
    public int     category_id;
    public int     platform_id;
    public int     final_time_ms;
    public String  video_link;
    public Date    submitted_at;
    public String  status;
    public Integer verified_by;   // nullable
}
