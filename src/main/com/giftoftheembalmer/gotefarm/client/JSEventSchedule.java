package com.giftoftheembalmer.gotefarm.client;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.List;
import java.util.Date;

public class JSEventSchedule implements IsSerializable {
    public static final int REPEAT_NEVER = 0;
    public static final int REPEAT_DAILY = 1;
    public static final int REPEAT_WEEKLY = 2;
    public static final int REPEAT_MONTHLY = 3;

    public static final int REPEAT_BY_DAY_OF_MONTH = 0;
    public static final int REPEAT_BY_DAY_OF_WEEK = 1;

    public long esid;
    public long eid;

    public Date start_time;
    public Date orig_start_time;
    public int timezone_offset;
    public int duration;

    public int display_start;
    public int display_end;

    public int signups_start;
    public int signups_end;

    public int repeat_size;
    public int repeat_freq;
    public int day_mask;
    public int repeat_by;

    public boolean active;

    public JSEventSchedule() {
    }
}
