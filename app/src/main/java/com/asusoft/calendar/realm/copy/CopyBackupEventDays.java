package com.asusoft.calendar.realm.copy;

import java.util.ArrayList;

public class CopyBackupEventDays {
    ArrayList<CopyEventDay> items;

    public CopyBackupEventDays() { }

    public CopyBackupEventDays(ArrayList<CopyEventDay> items) {
        this.items = items;
    }

    public ArrayList<CopyEventDay> getItems() {
        return items;
    }

    public void setItems(ArrayList<CopyEventDay> items) {
        this.items = items;
    }
}
