
package com.kekousoft.flashnote;

public class Note {
    public static final String TABLE_NAME = "notes";

    public static final String COL_ID = "_id";

    public static final String COL_DESC = "description";

    public static final String COL_DUEDATE = "due_date";

    public static final String COL_VOICE = "voice_record";

    public static final String COL_PRIO = "priority";

    public static final int PRIO_LOW = 3;

    public static final int PRIO_NORMAL = 2;

    public static final int PRIO_HIGH = 1;

    public Note() {
    }

    public Note(long id, String description, long dueDate, String voiceRecord, int priority) {
        this.id = id;
        this.description = description;
        this.dueDate = dueDate;
        this.voiceRecord = voiceRecord;
        this.priority = priority;
    }

    public long id;

    public String description;

    public long dueDate;

    public String voiceRecord;

    public int priority;
}
