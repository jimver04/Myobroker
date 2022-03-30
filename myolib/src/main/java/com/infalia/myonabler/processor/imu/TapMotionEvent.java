package com.infalia.myonabler.processor.imu;

import com.infalia.myonabler.processor.DataPacket;

public class TapMotionEvent extends MotionEvent {
    private int mTapCount;

    public TapMotionEvent(DataPacket packet) {
        super(packet, Type.TAP);
    }

    public int getTapCount() {
        return mTapCount;
    }

    public void setTapCount(int tapCount) {
        mTapCount = tapCount;
    }
}
