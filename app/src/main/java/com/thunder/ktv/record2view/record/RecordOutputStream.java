package com.thunder.ktv.record2view.record;

import com.thunder.ktv.record2view.view.SineWavesView;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by chengkai on 18-12-5.
 */

public class RecordOutputStream extends OutputStream {

    SineWavesView sineWavesView;

    public RecordOutputStream(SineWavesView sineWavesView) {
        this.sineWavesView = sineWavesView;
    }

    @Override
    public void write(int b) throws IOException {
    }

    @Override
    public void write(byte b[]) {
        sineWavesView.addData(b);
    }

    @Override
    public void flush() {
        sineWavesView.clearData();
    }
}
