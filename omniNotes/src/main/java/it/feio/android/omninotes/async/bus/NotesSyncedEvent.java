package it.feio.android.omninotes.async.bus;

import it.feio.android.omninotes.helpers.LogDelegate;
import it.feio.android.omninotes.models.Note;
import java.util.List;


public class NotesSyncedEvent {
    private List<Note> notes;

    public NotesSyncedEvent(List<Note> notes) {
        LogDelegate.d(this.getClass().getName());
        this.notes = notes;
    }

    public List<Note> getNotes() {
        return notes;
    }
}
