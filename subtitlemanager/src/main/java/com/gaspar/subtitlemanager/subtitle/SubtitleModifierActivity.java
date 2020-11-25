package com.gaspar.subtitlemanager.subtitle;

import android.view.View;
import android.widget.ListView;

import com.gaspar.subtitlemanager.conversion.Converter;

/**
 * Interface for activities that modify subtitle objects.
 */
public interface SubtitleModifierActivity {

    /**
     * Creates a listener that determines what happens when an individual component is clicked.
     * For example in the subtitle adjuster activity this creates a dialog that can adjust the component's
     * timings.
     */
    View.OnClickListener createComponentListener(SubtitleComponent component);

    ListView getSubtitleListView();

    Subtitle getSubtitle();

    void setSubtitle(Subtitle subtitle);

    void setConverter(Converter converter);

    void setUnsavedOperations(boolean unsavedOperations);
}
