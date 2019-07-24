package com.thunder.ktv.record2view.record;

/**
 * Created by chengkai on 19-1-8.
 */


public class AudioRecordInfo
{
    private int SamplesRate = 0;
    private int SamplesBits = 0;
    private int Channels = 0;
    public AudioRecordInfo(int SamplesRate, int SamplesBits, int Channels){
        setSamplesRate(SamplesRate);
        setSamplesBits(SamplesBits);
        setChannels(Channels);
    }

    public void setSamplesBits(int samplesBits) {
        if(samplesBits == 0){
            this.SamplesBits = AudioRecorder.getIstance().getAudioRecordInfo().getSamplesBits();
        }else{
            this.SamplesBits = samplesBits;
        }
    }
    public int getSamplesBits() {
        return SamplesBits;
    }

    public void setSamplesRate(int samplesRate) {
        if(samplesRate == 0){
            this.SamplesRate = getSamplesRate();
        }else{
            this.SamplesRate = samplesRate;
        }
    }
    public int getSamplesRate() {
        return SamplesRate;
    }

    public void setChannels(int channels) {
        if(channels == 0){
            this.Channels = AudioRecorder.getIstance().getAudioRecordInfo().getChannels();
        }else{
            this.Channels = channels;
        }
    }
    public int getChannels() {
        return Channels;
    }
}
