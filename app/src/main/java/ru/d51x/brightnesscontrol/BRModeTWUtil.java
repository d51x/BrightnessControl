package ru.d51x.brightnesscontrol;

import android.tw.john.TWUtil;




/**
 * Created by pyatyh_ds on 25.02.2015.
 */
public class BRModeTWUtil extends TWUtil {

    private static BRModeTWUtil twUtil;
    private static int mCount;

    static {
        twUtil = new BRModeTWUtil(17);
        mCount = 0;
    }

    public BRModeTWUtil(int i) {
        super(i);
    }

    public static BRModeTWUtil open() {
        int i = mCount;
        mCount = i + 1;
        if (i == 0) {
            if (twUtil.open(new short[]{(short) 258}) != 0) {
                mCount--;
                return null;
            }
            twUtil.start();
        }
        return twUtil;
    }

        public void close() {
            if (mCount > 0) {
                int i = mCount - 1;
                mCount = i;
                if (i == 0) {
                    stop();
                    super.close();
                }
            }
        }


}

