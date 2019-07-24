package com.thunder.ktv.record2view.record;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.concurrent.locks.LockSupport;

/**
 * Created by chengkai on 19-1-8.
 */


public class AudioRecorder {
    static final String TAG = AudioRecorder.class.getName();
    static private AudioRecorder instance = null;

    private AudioRecord audioRecord = null;
    private int MinBufferSize = 0;
    private AudioRecorder.RecordThread recordThread = null;

    private AudioRecordInfo audioRecordInfo = null;

    public AudioRecordInfo getAudioRecordInfo() {
        return audioRecordInfo;
    }

    public void setOutputStream(OutputStream outputStream) {
        recordThread.setOutputStream(outputStream);
    }

    public static synchronized AudioRecorder getIstance() {
        if (instance == null) {
            instance = new AudioRecorder();
        }
        return instance;
    }


    private AudioRecorder() {
        recordThread = new AudioRecorder.RecordThread();
        audioRecordInfo = new AudioRecordInfo(48000, AudioFormat.ENCODING_PCM_16BIT, AudioFormat.CHANNEL_IN_STEREO);
        MinBufferSize = AudioRecord.getMinBufferSize(audioRecordInfo.getSamplesRate(), audioRecordInfo.getChannels(), audioRecordInfo.getSamplesBits());
        audioRecord = new AudioRecord(
                MediaRecorder.AudioSource.DEFAULT,
                audioRecordInfo.getSamplesRate(), audioRecordInfo.getChannels(), audioRecordInfo.getSamplesBits(),
                MinBufferSize);
        audioRecordInfo.setSamplesRate(audioRecord.getSampleRate());
        Log.d(TAG, "" + audioRecord);
        Log.d(TAG, "getState  " + audioRecord.getState());
        if (audioRecord.getState() == 0) {
            audioRecord.release();
            audioRecord = null;
        }
    }

    static boolean flag = false;

    public boolean start() {
        if (!recordThread.isAlive()) {
            recordThread.start();
        }
        flag = true;
        LockSupport.unpark(recordThread);
        return true;
    }

    public void stop() {
        flag = false;
    }

    class RecordThread extends Thread {
        OutputStream outputStream = null;

        public void setOutputStream(OutputStream outputStream) {
            this.outputStream = outputStream;
        }

        @Override
        public void run() {
            AudioRecord audioRecord = null;
            audioRecord = AudioRecorder.this.audioRecord;
            if (audioRecord == null)
                return;
            Log.d(TAG, "getState  " + audioRecord.getState());
            audioRecord.startRecording();
            byte[] bytes = new byte[MinBufferSize];
            byte[] bytesStereo = null;
            int readLen = 0;
            PcmData pcmData = null;
            while (true) {
                Log.d(TAG, "flag  " + flag);

                LockSupport.park();

//                while(flag == false){
//                    if(flag == true){
//                            break;
//                    }
//                    try {
//                        Thread.sleep(20);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
                while (flag) {
                    readLen = audioRecord.read(bytes, 0, bytes.length);
                    if (readLen <= 0) {
                        continue;
                    }
                    if (audioRecordInfo.getChannels() == AudioFormat.CHANNEL_IN_STEREO) {
                        bytesStereo = bytes;
                        //拆分左右声道
                        pcmData = splitPcmData(Arrays.copyOf(bytesStereo, readLen));
                        if (pcmData == null)
                            continue;
                    }
                    try {
                        outputStream.write(bytesStereo);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    outputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private PcmData splitPcmData(byte[] buffer) {
        if (buffer == null)
            return null;
        int len = buffer.length;
        byte[] outRightByts = new byte[len / 2];
        byte[] outLeftByts = new byte[len / 2];
        int i = 0;
        switch (getIstance().audioRecordInfo.getSamplesBits()) {
            case AudioFormat.ENCODING_PCM_16BIT:
                for (i = 0; i < len; i += 4) {
                    outLeftByts[i / 2 + 0] = buffer[i + 0];
                    outLeftByts[i / 2 + 1] = buffer[i + 1];
                    outRightByts[i / 2 + 0] = buffer[i + 2];
                    outRightByts[i / 2 + 1] = buffer[i + 3];
                }
                break;
            case AudioFormat.ENCODING_PCM_8BIT:
                for (i = 0; i < len; i += 2) {
                    outLeftByts[i / 2 + 0] = buffer[i + 0];
                    outRightByts[i / 2 + 0] = buffer[i + 1];
                }
                break;
            default:
                return null;
        }
        return new PcmData(outRightByts, outLeftByts);
    }

    //辗转相除获取分子分母
    private static int GetMaxCommonDivisor(int maxnum, int minnum) {
        int temp = 0;
        /*使得max中存放较大的数,min存放较小的数*/
        if (maxnum < minnum) {
            temp = minnum;
            minnum = maxnum;
            maxnum = temp;
        }
        while (maxnum % minnum != 0) {
            temp = minnum;
            minnum = maxnum % minnum;
            maxnum = temp;
        }
        return minnum;
    }


    byte[] reSamplePcm(byte[] buffer, int reSampleHz, int channelConfig) {
        if (buffer == null)
            return null;
        int len = buffer.length;
        int maxCommonDivisor = GetMaxCommonDivisor(getIstance().audioRecordInfo.getSamplesBits(), reSampleHz);
        int rate_out = reSampleHz / maxCommonDivisor;//倍率  分子
        int rate_in = getIstance().audioRecordInfo.getSamplesBits() / maxCommonDivisor;//倍率  分母
        //Logger.debug(TAG,"aaaa rate_out  " + rate_out + "   rate_in " + rate_in);
        if (rate_out >= rate_in) {
            return buffer;
        }
        int bits = 0;
        switch (channelConfig) {
            case AudioFormat.CHANNEL_IN_LEFT:
            case AudioFormat.CHANNEL_IN_RIGHT:
            case AudioFormat.CHANNEL_IN_MONO:
                bits = 2;
                break;
            case AudioFormat.CHANNEL_IN_STEREO:
                bits = 4;
                break;
            default:
                return null;
        }
        int dstPos = 0;
        byte tmpBytes[] = new byte[len];
        for (int i = 0; i < len; i += (bits * rate_in)) {
            if ((i + rate_out * bits) < buffer.length) {
                System.arraycopy(buffer, i, tmpBytes, dstPos, rate_out * bits);
                dstPos += rate_out * bits;
            }
        }
        return Arrays.copyOf(tmpBytes, dstPos);
    }
}

