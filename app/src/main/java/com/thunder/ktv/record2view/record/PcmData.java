package com.thunder.ktv.record2view.record;

/**
 * Created by chengkai on 19-1-8.
 */
class PcmData{
    byte[] outRightByts;
    byte[] outLeftByts;
    PcmData(byte[] outRightByts,byte[] outLeftByts)
    {
        this.outRightByts = outRightByts;
        this.outLeftByts = outLeftByts;
    }
}
