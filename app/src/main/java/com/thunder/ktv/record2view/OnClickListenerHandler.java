package com.thunder.ktv.record2view;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;
import com.thunder.ktv.record2view.record.AudioRecorder;
/**
 * Created by chengkai on 18-12-5.
 */

public class OnClickListenerHandler {

    private Context context;

    public OnClickListenerHandler(Context context) {
        this.context = context;
    }

    public void switchRecord(CompoundButton compoundButton, boolean b) {
        MainViewModel.getInstance().setRecordOpen(b);
        if (b) {
            if (AudioRecorder.getIstance().start() == false) {
                Toast.makeText(context, "录音打开失败", Toast.LENGTH_LONG).show();
            }
        } else {
            AudioRecorder.getIstance().stop();
        }
    }
}
