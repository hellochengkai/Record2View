package com.thunder.ktv.record2view;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import com.android.databinding.library.baseAdapters.BR;


/**
 * Created by chengkai on 18-12-5.
 */

public class MainViewModel extends BaseObservable{
    private static final String TAG = "MainViewModel";
    private static MainViewModel instance;
    private boolean recordOpen = false;

    @Bindable
    public boolean isRecordOpen() {
        return recordOpen;
    }

    public void setRecordOpen(boolean recordOpen) {
        this.recordOpen = recordOpen;
        notifyPropertyChanged(BR.recordOpen);
    }

    private MainViewModel() {
    }

    public static synchronized MainViewModel getInstance() {
        if(instance == null){
            instance = new MainViewModel();
        }
        return instance;
    }
}
