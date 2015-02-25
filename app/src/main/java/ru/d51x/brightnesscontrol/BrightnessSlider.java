package ru.d51x.brightnesscontrol;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.Message;
import android.os.IPowerManager;
import android.os.IPowerManager.Stub;
import android.os.ServiceManager;
import android.provider.Settings.System;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class BrightnessSlider extends LinearLayout implements OnClickListener, OnSeekBarChangeListener {
    private Context mContext;
    private Handler mHandler;
    private View mMax;
    private View mMin;
    private BrightnessSettingsObserver mObserver;
    private SeekBar mSlider;
    private BRModeTWUtil mBRModeUtil;
    private BRModeHandler mBRModeHandler;
    private RadioGroup mBrightnessModeGroup;
    private RadioButton radioButtonAuto;
    private RadioButton radioButtonLight;
    private RadioButton radioButtonDark;
    private OnClickListener brModeGroupListener;
	public int brightnessMode;

    public BrightnessSlider(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mObserver = null;
        this.mContext = context;
        this.mHandler = new Handler();
        this.mObserver = new BrightnessSettingsObserver(this.mHandler);
        this.mBRModeUtil = null;
        this.mBRModeHandler = new BRModeHandler(this);
	    this.brightnessMode = -1;
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mSlider = (SeekBar) findViewWithTag("slider");
        this.mSlider.setOnSeekBarChangeListener(this);
        this.mSlider.setMax(255);
        this.mMin = findViewWithTag("br_min");
        this.mMin.setOnClickListener(this);
        this.mMax = findViewWithTag("br_max");
        this.mMax.setOnClickListener(this);

        this.mBrightnessModeGroup = (RadioGroup) findViewById(R.id.brModeGroup);
        this.radioButtonAuto = (RadioButton) findViewById(R.id.brModeAuto);
        this.radioButtonLight = (RadioButton) findViewById(R.id.brModeLight);
        this.radioButtonDark = (RadioButton) findViewById(R.id.brModeDark);

        this.mBRModeUtil = BRModeTWUtil.open();
        this.mBRModeUtil.addHandler("BrightnessMode", this.mBRModeHandler);
        this.mBRModeUtil.write(258, 255);

        mBrightnessModeGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.brModeAuto:
                        mBRModeUtil.write(258, 1, 0);
                        setDBMode(0);
                        break;
                    case R.id.brModeLight:
                        mBRModeUtil.write(258, 1, 1);
                        setDBMode(1);
                        break;
                    case R.id.brModeDark:
                        mBRModeUtil.write(258, 1, 2);
                        setDBMode(2);
                        break;
                    default:
                        break;
                }
            }
        });
       updateState();
	   updateModeState();
    }



    public void updateState() {
        this.mSlider.setProgress(getCurrBrightness());
        if (System.getInt(getContext().getContentResolver(), "screen_brightness_mode", 0) == 0) {
            setVisibility(VISIBLE);
        } else {
            setVisibility(GONE);
        }
    }

	public void updateModeState() {
		switch ( this.brightnessMode ) {
			case 0:
				this.radioButtonAuto.setChecked( true );
				break;
			case 1:
				this.radioButtonLight.setChecked( true );
				break;
			case 2:
				this.radioButtonDark.setChecked( true );
				break;
			default:
				break;
		}
	}

    private int getCurrBrightness() {
        return System.getInt(this.mContext.getContentResolver(), "screen_brightness", 0);
    }

    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        setBrightness(progress);
    }

    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    public void onStopTrackingTouch(SeekBar seekBar) {
        setDB(this.mSlider.getProgress());
    }

    public void setDB(int br) {
        try {
            System.putInt(this.mContext.getContentResolver(), "screen_brightness", br);
        } catch (Exception exc) {
            Log.e("BrightnessSlider", exc.getLocalizedMessage());
        }
    }

    public void setDBMode(int br) {

//        try {
//            System.putInt(this.mContext.getContentResolver(), "blmode", br);
//        } catch (Exception exc) {
//            Log.e("BrightnessSlider", exc.getLocalizedMessage());
//        }
    }

    private void setBrightness(int brightness) {
        try {
            IPowerManager power = Stub.asInterface(ServiceManager.getService("power"));
            if (power != null) {
                power.setBacklightBrightness(brightness);
            }
        } catch (Exception exc) {
            Log.e("BrightnessSlider", exc.getLocalizedMessage());
        }
    }



    protected void onAttachedToWindow() {
        if (this.mObserver != null) {
            this.mObserver.observe();
        }
    }

    protected void onDetachedFromWindow() {
        if (this.mObserver != null) {
            this.mObserver.unobserve();
        }
    }

    public void onClick(View v) {
        if (v == this.mMin) {
            setDB(0);
        }
        if (v == this.mMax) {
            setDB(255);
        }
    }


    private class BrightnessSettingsObserver extends ContentObserver {
        public BrightnessSettingsObserver(Handler handler) {
            super(handler);
        }

        public void observe() {
            ContentResolver resolver = BrightnessSlider.this.mContext.getContentResolver();
            resolver.registerContentObserver(System.getUriFor("screen_brightness"), false, this);
            resolver.registerContentObserver(System.getUriFor("screen_brightness_mode"), false, this);
            //resolver.registerContentObserver(System.getUriFor("blmode"), false, this);
        }

        public void unobserve() {
            BrightnessSlider.this.mContext.getContentResolver().unregisterContentObserver(this);
        }

        public void onChange(boolean selfChange) {
            BrightnessSlider.this.updateState();
        }
    }


    private class BRModeHandler extends Handler {
        final BrightnessSlider brightnessSlider;

        BRModeHandler(BrightnessSlider brightnessSlider) {
            this.brightnessSlider = brightnessSlider;
            brightnessMode = -1;
        }

        public void handleMessage(Message message) {
            switch (message.what) {
                case 258:
                    brightnessMode = message.arg2;
	                switch ( brightnessMode ) {
		                case 0:
			                this.brightnessSlider.radioButtonAuto.setChecked ( true );
			                break;
		                case 1:
			                this.brightnessSlider.radioButtonLight.setChecked ( true );
			                break;
		                case 2:
			                this.brightnessSlider.radioButtonDark.setChecked ( true );
			                break;
		                default:
			                break;
	                }

                default:
                    break;
            }
        }

    }
}

