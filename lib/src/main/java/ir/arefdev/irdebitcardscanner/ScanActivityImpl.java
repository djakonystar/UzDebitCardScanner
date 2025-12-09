package ir.arefdev.irdebitcardscanner;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.List;

public class ScanActivityImpl extends ScanBaseActivity {

    private static final String TAG = "ScanActivityImpl";

    public static final String SCAN_CARD_TEXT = "scanCardText";
    public static final String POSITION_CARD_TEXT = "positionCardText";

    public static final String RESULT_CARD_NUMBER = "cardNumber";
    public static final String RESULT_EXPIRY_MONTH = "expiryMonth";
    public static final String RESULT_EXPIRY_YEAR = "expiryYear";

    private ImageView mDebugImageView;
    private boolean mInDebugMode = false;
    private static long startTimeMs = 0;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.irdcs_activity_scan_card);

        String scanCardText = getIntent().getStringExtra(SCAN_CARD_TEXT);
        if (!TextUtils.isEmpty(scanCardText)) {
            ((TextView) findViewById(R.id.scanCard)).setText(scanCardText);
        }

        String positionCardText = getIntent().getStringExtra(POSITION_CARD_TEXT);
        if (!TextUtils.isEmpty(positionCardText)) {
            ((TextView) findViewById(R.id.positionCard)).setText(positionCardText);
        }

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, 110);
            } else {
                mIsPermissionCheckDone = true;
            }
        } else {
            // no permission checks
            mIsPermissionCheckDone = true;
        }

        findViewById(R.id.closeButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mDebugImageView = findViewById(R.id.debugImageView);
        mInDebugMode = getIntent().getBooleanExtra("debug", false);
        if (!mInDebugMode) {
            mDebugImageView.setVisibility(View.INVISIBLE);
        }
        setViewIds(R.id.flashlightButton, R.id.cardRectangle, R.id.shadedBackground, R.id.texture,
                R.id.cardNumber, R.id.expiry);

        applyInsetsImeWithToolbar(findViewById(R.id.toolbar));
    }

    @Override
    protected void onCardScanned(String numberResult, String month, String year) {
        Intent intent = new Intent();
        intent.putExtra(RESULT_CARD_NUMBER, numberResult);
        intent.putExtra(RESULT_EXPIRY_MONTH, month);
        intent.putExtra(RESULT_EXPIRY_YEAR, year);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onPrediction(final String number, final Expiry expiry, final Bitmap bitmap,
                             final List<DetectedBox> digitBoxes, final DetectedBox expiryBox) {
        if (mInDebugMode) {
            mDebugImageView.setImageBitmap(ImageUtils.drawBoxesOnImage(bitmap, digitBoxes, expiryBox));
            Log.d(TAG, "Prediction (ms): " + (SystemClock.uptimeMillis() - mPredictionStartMs));
            if (startTimeMs != 0) {
                Log.d(TAG, "time to first prediction: " + (SystemClock.uptimeMillis() - startTimeMs));
                startTimeMs = 0;
            }
        }

        super.onPrediction(number, expiry, bitmap, digitBoxes, expiryBox);
    }

    public static void applyInsetsImeWithToolbar(@NonNull View view) {
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, windowInsets) -> {
            Insets statusInsets = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars());
            Insets systemInsets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets imeInsets = windowInsets.getInsets(WindowInsetsCompat.Type.ime());

            boolean isImeVisible = windowInsets.isVisible(WindowInsetsCompat.Type.ime());

            // Update padding
            int topPadding = statusInsets.top;
            int bottomPadding = isImeVisible ? imeInsets.bottom : systemInsets.bottom;

            v.setPadding(
                    v.getPaddingLeft(),
                    getStatusBarHeight(view.getContext()),
                    v.getPaddingRight(),
                    bottomPadding
            );

            // Indicate that you've fully handled the insets
            return WindowInsetsCompat.CONSUMED;
            // or return windowInsets; if you want to keep passing them on
        });
    }

    public static int getStatusBarHeight(Context context) {
        @SuppressLint("InternalInsetResource") int resourceId = context.getResources().getIdentifier(
                "status_bar_height",
                "dimen",
                "android"
        );

        if (resourceId > 0) {
            return context.getResources().getDimensionPixelSize(resourceId);
        }
        return 0;
    }


}
