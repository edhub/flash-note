
package com.kekousoft.flashnote;

public class Note {
    public static final String TABLE_NAME = "notes";

    public static final String COL_ID = "_id";

    public static final String COL_DESC = "description";

    public static final String COL_DUEDATE = "due_date";

    public static final String COL_VOICE = "voice_record";

    public static final String COL_COLOR = "color";

    public static final String COL_FINISHED_ON = "finished_on";

    public Note() {
    }

    public Note(long id, String description, long dueDate, String voiceRecord, int color, long finishedOn) {
        this.id = id;
        this.description = description;
        this.dueDate = dueDate;
        this.voiceRecord = voiceRecord;
        this.color = color;
        this.finishedOn = finishedOn;
    }

    public long id;

    public String description;

    public long dueDate;

    public String voiceRecord;

    public int color;

    public long finishedOn;
}
