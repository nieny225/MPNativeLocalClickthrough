package com.example.vchau.MPNativeLocalClickthrough;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mopub.common.UrlAction;
import com.mopub.common.UrlHandler;
import com.mopub.common.util.Drawables;
import com.mopub.nativeads.BaseNativeAd;
import com.mopub.nativeads.MoPubAdRenderer;
import com.mopub.nativeads.MoPubNative;
import com.mopub.nativeads.MoPubStaticNativeAdRenderer;
import com.mopub.nativeads.NativeAd;
import com.mopub.nativeads.NativeErrorCode;
import com.mopub.nativeads.NativeImageHelper;
import com.mopub.nativeads.StaticNativeAd;
import com.mopub.nativeads.ViewBinder;

public class NativeStaticActivity extends AppCompatActivity {

    private final String TAG = this.getClass().getName();

    private MoPubNative moPubNative;
    private MoPubNative.MoPubNativeNetworkListener moPubNativeNetworkListener;
    private RelativeLayout nativeAdView;
    private ViewBinder viewBinder;
    private FrameLayout parentView;
    private NativeAd.MoPubNativeEventListener moPubNativeEventListener;
    private MoPubAdRenderer moPubAdRenderer;
    private ImageView privacyInformationIconImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_native);

        nativeAdView = (RelativeLayout) findViewById(R.id.nativeAdView);
        parentView = (FrameLayout) findViewById(R.id.parentView);

        viewBinder = new ViewBinder.Builder(0).build();

        moPubNativeEventListener = new NativeAd.MoPubNativeEventListener() {

            @Override
            public void onImpression(View view) {
                Log.d(TAG, "onImpression");
            }

            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick");
            }
        };

        moPubNativeNetworkListener = new MoPubNative.MoPubNativeNetworkListener() {

            @Override
            public void onNativeLoad(final NativeAd nativeAd) {
                Log.d(TAG, "onNativeLoad");

                nativeAd.setMoPubNativeEventListener(moPubNativeEventListener);

                BaseNativeAd baseNativeAd = nativeAd.getBaseNativeAd();

                if (baseNativeAd instanceof StaticNativeAd) {
                    StaticNativeAd staticNativeAd = (StaticNativeAd) baseNativeAd;

                    String adTitle = staticNativeAd.getTitle();
                    String cTA = staticNativeAd.getCallToAction();
                    String iconImageUrl = staticNativeAd.getIconImageUrl();
                    String mainImageUrl = staticNativeAd.getMainImageUrl();
                    String adText = staticNativeAd.getText();
                    final String privacyInformationIconClickThroughUrl = staticNativeAd.getPrivacyInformationIconClickThroughUrl();
                    String privacyInformationIconImageUrl = staticNativeAd.getPrivacyInformationIconImageUrl();

                    privacyInformationIconImageView = new ImageView(getApplicationContext());

                    if (privacyInformationIconImageUrl == null) {
                        privacyInformationIconImageView.setImageDrawable(
                                Drawables.NATIVE_PRIVACY_INFORMATION_ICON.createDrawable(getApplicationContext()));
                    } else {
                        NativeImageHelper.loadImageView(privacyInformationIconImageUrl,
                                privacyInformationIconImageView);
                    }

                    TextView adTextTV = (TextView) findViewById(R.id.adTextTV);
                    TextView adTitleTV = (TextView) findViewById(R.id.adTitleTV);
                    Button cTABtn = (Button) findViewById(R.id.cTABtn);
                    ImageView iconImageIV = (ImageView) findViewById(R.id.iconImageIV);
                    ImageView mainImageIV = (ImageView) findViewById(R.id.mainImageIV);

                    adTextTV.setText(adText);
                    adTitleTV.setText(adTitle);
                    cTABtn.setText(cTA);

                    NativeImageHelper.loadImageView(iconImageUrl,
                            iconImageIV);

                    NativeImageHelper.loadImageView(mainImageUrl,
                            mainImageIV);

                    privacyInformationIconImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(final View v) {
                            new UrlHandler.Builder()
                                    .withSupportedUrlActions(
                                            UrlAction.IGNORE_ABOUT_SCHEME,
                                            UrlAction.OPEN_NATIVE_BROWSER,
                                            UrlAction.OPEN_IN_APP_BROWSER,
                                            UrlAction.HANDLE_SHARE_TWEET,
                                            UrlAction.FOLLOW_DEEP_LINK_WITH_FALLBACK,
                                            UrlAction.FOLLOW_DEEP_LINK)
                                    .build().handleUrl(getApplicationContext(), privacyInformationIconClickThroughUrl);
                        }
                    });

                    staticNativeAd.prepare(parentView);

                    prepareOverlay();
                }
            }

            @Override
            public void onNativeFail(NativeErrorCode errorCode) {
                Log.d(TAG, "onNativeFail: " + errorCode.toString());
            }
        };

        moPubNative = new MoPubNative(this, "11a17b188668469fb0412708c3d16813", moPubNativeNetworkListener);

        moPubAdRenderer = new MoPubStaticNativeAdRenderer(viewBinder);

        moPubNative.registerAdRenderer(moPubAdRenderer);

        moPubNative.makeRequest();
    }


    public void prepareOverlay() {

        ViewTreeObserver vto = nativeAdView.getViewTreeObserver();

        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    nativeAdView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    nativeAdView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }

                // Geting the dimensions of the native ad, in this case it is fullscreen.

                int width = nativeAdView.getMeasuredWidth();
                int height = nativeAdView.getMeasuredHeight();

                View overlayLayout = findViewById(R.id.overlayLayout);

                ViewGroup.LayoutParams params = overlayLayout.getLayoutParams();

                params.height = height;
                params.width = width;

                // Making the overlay the exact same size as the native ad
                overlayLayout.setLayoutParams(params);

                overlayLayout.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        return true; // absorb touch events and do nothing
                    }
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        moPubNative.destroy();
        moPubNative = null;

        moPubNativeNetworkListener = null;
        moPubNativeEventListener = null;
    }
}