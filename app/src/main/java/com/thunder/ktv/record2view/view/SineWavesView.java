package com.thunder.ktv.record2view.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.thunder.ktv.record2view.R;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by 成凯 on 2017/7/25.
 */

public class SineWavesView extends SurfaceView implements SurfaceHolder.Callback {

    MySurfaceViewThread mysurfaceviewThread;
    static final String TAG = SineWavesView.class.getName();
    SurfaceHolder holder;
    private List<byte[]> linkedList;
    String attrText = null;
    private static boolean arrDrawLine = true;

    public static void setArrDrawLine(boolean arrDrawLine) {
        SineWavesView.arrDrawLine = arrDrawLine;
    }

    public SineWavesView(Context context, AttributeSet attrs)
    {
        super(context,attrs);
        holder = getHolder();//<span style="color:#ff0000;">获取SurfaceHolder对象，同时指定callback</span>
        holder.addCallback(this);
        //setBackgroundResource(R.drawable.background);
        setZOrderOnTop(true);//使surfaceview放到最顶层
        getHolder().setFormat(PixelFormat.TRANSLUCENT);//使窗口支持透明度
        linkedList = Collections.synchronizedList(new LinkedList<byte[]>());

        Log.d(TAG,"getAttributeCount" + attrs.getAttributeCount());
        for (int i = 0;i < attrs.getAttributeCount();i++){
            if(attrs.getAttributeName(i).equals("text")){
                String s = attrs.getAttributeValue(i);
                if(s.substring(0,1).equals("@")){
                    attrText = getResources().getString(Integer.parseInt(s.substring(1)));
                }else{
                    attrText = attrs.getAttributeValue(i);
                }
            }
            Log.d(TAG,"getAttributeValue " + attrs.getAttributeValue(i));
            Log.d(TAG,"getAttributeName " + attrs.getAttributeName(i));
        }
    }

    public void addData(byte[] bytes)
    {
        if(linkedList.size() > 10)
            return;
        if(linkedList != null)
            linkedList.add(bytes);
    }
    public void clearData()
    {
        if(linkedList != null)
            linkedList.clear();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mysurfaceviewThread = new MySurfaceViewThread();
        mysurfaceviewThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if(mysurfaceviewThread != null){
            mysurfaceviewThread.exit();
            mysurfaceviewThread = null;
        }
    }

    void drawBGLine(Canvas canvas)
    {
        if(canvas == null)
            return;
        //width  940 ,height 250
        int height = canvas.getHeight();
        int width = canvas.getWidth();
        Paint p = new Paint();
        p.setAlpha(255);
        p.setStrokeWidth(2);
        p.setColor(getResources().getColor(R.color.common_yellow));
        p.setAntiAlias(true);
        canvas.drawLine(1, 1, 1,height,p);
        //canvas.drawLine(1, 0 + 5, width,0 + 5,p);
        canvas.drawLine(0, height/2, width,height/2,p);
        canvas.drawLine(0, 10, width,10,p);
        canvas.drawLine(0, height - 10, width,height - 10,p);
        //canvas.drawLine(1, height - 5, width,height - 5,p);
        p.setColor(getResources().getColor(R.color.common_blue));
        p.setTextSize(15);
        if(attrText !=null)
            canvas.drawText(attrText, p.getTextSize()/2, p.getTextSize(), p);// 画文本
    }
    int[] old_r = {0,SineWavesView.this.getHeight()/2};
    int[] old_l = {0,SineWavesView.this.getHeight()/2};
    int drawWavesPoint(int draw_x, Canvas canvas, byte[] bytes)
    {
        if(canvas == null || bytes == null)
            return draw_x;
        int height = canvas.getHeight();
        int width = canvas.getWidth();

        Paint p = new Paint();
        p.setStrokeWidth(2);
        p.setColor(Color.WHITE);// 设置红色
        p.setAntiAlias(true);
        Paint p1 = new Paint();
        p1.setStrokeWidth(2);
        p1.setColor(Color.RED);// 设置红色

        p1.setAntiAlias(true);
        //bytes.length == 4096;

        for (int i = 0;i < bytes.length ; i+=4)
        {
            byte[] bytes1 = new byte[2];
            byte[] bytes2 = new byte[2];
            bytes1[0] = bytes[i];
            bytes1[1] = bytes[i + 1];
            bytes2[0] = bytes[i + 2];
            bytes2[1] = bytes[i + 3];
            int a = 0;
            int b = 0;
            double da = (double)byte2Toint(bytes1)/(double)65536;
            if(da > 0){
                a = height/2 - (int) (da * height);
            }else{
                a = height + (int) (da * height);
            }
            double db = (double)byte2Toint(bytes2)/(double)65536;
            if(db > 0){
                b = height/2 - (int) (db * height);
            }else{
                b = height + (int) (db * height);
            }

            if(arrDrawLine){
                if(old_r[0] < draw_x)
                    canvas.drawLine(old_r[0],old_r[1], draw_x,a, p);
                old_r[0] = draw_x;  old_r[1] = a;
                if(old_l[0] < (draw_x+1))
                    canvas.drawLine(old_l[0],old_l[1], draw_x + 1,b, p1);
                old_l[0] = draw_x+1;  old_l[1] = b;
            }else{
                canvas.drawPoint(draw_x,a, p);
                canvas.drawPoint(draw_x + 1,b, p1);
            }
            draw_x++;
            if(draw_x > width){
                draw_x=0;
            }
        }
        return draw_x;
    }

    public static int byte2Toint(byte[] b) {
        int c =     ((b[0] & 0xFF) |
                    ((b[1] & 0x7F) << 8));
        if((b[1] & 0x80 )== 0x80) {
            c = -c;
        }
        return c;
    }

    class MySurfaceViewThread extends Thread
    {
        private boolean done = false;
        public MySurfaceViewThread() {
            super();
            this.done = false;
        }

        public void exit() {
            // <span style="color:#ff0000;">将done设置为true 使线程中的while循环结束。</span>
            done = true;
            try {
                join();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        @Override
        public void run() {
            super.run();
            SurfaceHolder surfaceHolder = holder;
            int a = 0;
            byte[] bytes = null;
            int draw_x = 0;
            while (!done)
            {
                synchronized (surfaceHolder){
                    //Canvas canvas = surfaceHolder.lockCanvas();
                    Canvas canvas  = surfaceHolder.lockCanvas(
                            new Rect(0, 0, SineWavesView.this.getWidth(), SineWavesView.this.getHeight()));// 关键:获取画布
                    if(canvas==null){
                        continue;
                    }
                    canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);//绘制透明色
                    if(linkedList.size() > 0){
                        bytes = linkedList.remove(0);
                        //canvas.drawARGB(0, 0, 0, 0);
                        draw_x = drawWavesPoint(draw_x,canvas,bytes);
                    }else{
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    drawBGLine(canvas);
                    surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
            synchronized (surfaceHolder) {
                //Canvas canvas = surfaceHolder.lockCanvas();
                Canvas canvas = surfaceHolder.lockCanvas(
                        new Rect(0, 0, SineWavesView.this.getWidth(), SineWavesView.this.getHeight()));// 关键:获取画布
                if (canvas == null) {
                    return;
                }
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);//绘制透明色
                drawBGLine(canvas);
                surfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
    }
}
