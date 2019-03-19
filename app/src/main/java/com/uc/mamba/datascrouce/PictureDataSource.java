package com.uc.mamba.datascrouce;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import java.io.FileInputStream;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PictureDataSource implements IRendererDataSource {
    private static final int MSG_DECODE = 0x1;
    private List<String> pictures;
    private Handler threadHandler;
    private Handler uiHandler;
    private int frameIndex = 0;
    private long frameSpit;
    private int width = 720;
    private int height = 1280;
    private int frameRate = 30;
    private boolean autoStartWhenReady;
    private SafetyBitmapQueue bitmapQueue = new SafetyBitmapQueue();

    private OnFrameAvailableListener onFrameAvailableListener;
    private OnCompletionListener onCompletionListener;

    public PictureDataSource(List<String> pictures) {
        uiHandler = new Handler(Looper.getMainLooper());
        this.pictures = pictures;
        frameSpit = 1000 / frameRate;
        HandlerThread thread = new HandlerThread("picture");
        thread.start();
        threadHandler = new Handler(thread.getLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_DECODE:
                        decode();
                        break;
                }
                return true;
            }
        });
    }

    public void setFrameRate(int frameRate) {
        this.frameRate = frameRate;
        frameSpit = 1000 / frameRate;
    }

    public void start() {
        frameIndex = 0;
        threadHandler.removeMessages(MSG_DECODE);
        threadHandler.sendEmptyMessage(MSG_DECODE);
    }

    public void stop() {
        threadHandler.removeMessages(MSG_DECODE);
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    private PictureDataSource getThis() {
        return this;
    }

    private void decode() {
        threadHandler.removeMessages(MSG_DECODE);
        if (frameIndex >= pictures.size()) {
            if (onCompletionListener != null) {
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (onCompletionListener != null) {
                            onCompletionListener.onCompletion(getThis());
                        }
                    }
                });

            }
            return;
        }
        Bitmap bitmap = decodeBitmap(pictures.get(frameIndex++));
        bitmapQueue.offer(bitmap);
        threadHandler.sendEmptyMessageDelayed(MSG_DECODE, frameSpit);
        if (onFrameAvailableListener != null) {
            onFrameAvailableListener.onFrameAvailable();
        }
    }


    private Bitmap decodeBitmap(String filePath) {
        try {
            FileInputStream fis = new FileInputStream(filePath);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFileDescriptor(fis.getFD(), null, options);
            float srcWidth = options.outWidth;
            float srcHeight = options.outHeight;
            int inSampleSize = 1;

            if (srcHeight > height || srcWidth > width) {
                if (srcWidth > srcHeight) {
                    inSampleSize = Math.round(srcHeight / height);
                } else {
                    inSampleSize = Math.round(srcWidth / width);
                }
            }
            options.inJustDecodeBounds = false;
            options.inSampleSize = inSampleSize;
            Bitmap bitmap = BitmapFactory.decodeFileDescriptor(fis.getFD(), null, options);
            bitmap = scaleBitmap(bitmap, width, height);
            return bitmap;
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private Bitmap scaleBitmap(Bitmap originalBitmap, int width, int height) {
        float realWidth = (float) originalBitmap.getWidth();
        float realHeight = (float) originalBitmap.getHeight();
        float scale = Math.min((float) width / realWidth, (float) height / realHeight);
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        int targetW = originalBitmap.getWidth();
        int targetH = originalBitmap.getHeight();
        try {
            originalBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, targetW, targetH, matrix, true);
        } catch (Throwable var10) {
            var10.printStackTrace();
        }

        return originalBitmap;
    }


    public void autoStartWhenReady(boolean enable) {
        autoStartWhenReady = enable;
    }


    @Override
    public void onTextureIdCreated(int textureId) {
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        //纹理也有坐标系，称UV坐标，或者ST坐标
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT); // S轴的拉伸方式为重复，决定采样值的坐标超出图片范围时的采样方式
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT); // T轴的拉伸方式为重复
        if (onFrameAvailableListener != null) {
            onFrameAvailableListener.onFrameSizeChange(width, height);
        }
        if (autoStartWhenReady) {
            start();
        }
    }

    @Override
    public void onRunInDraw(int textureId, Map<String, Object> params) {
        Bitmap b = bitmapQueue.poll();
        if (b != null && !b.isRecycled()) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, b, 0);
            b.recycle();
        }
    }


    @Override
    public void setOnFrameAvailableListener(OnFrameAvailableListener listener) {
        onFrameAvailableListener = listener;
    }

    public void setOnCompletionListener(OnCompletionListener listener) {
        onCompletionListener = listener;
    }

    public interface OnCompletionListener {
        void onCompletion(PictureDataSource dataSource);
    }

    private static class SafetyBitmapQueue {
        Queue<Bitmap> queue = new LinkedBlockingDeque<>();
        private final Lock mRecordLock = new ReentrantLock();

        public void offer(Bitmap bitmap) {
            if (bitmap == null) {
                return;
            }
            mRecordLock.lock();
            while (!queue.isEmpty()) {//这里不做多个缓存，如果消费线程比较耗时，这里就直接跳过绘制这一帧数据
                Bitmap temp = queue.poll();
                if (temp != null && !temp.isRecycled()) {
                    temp.recycle();
                }
            }
            queue.offer(bitmap);
            mRecordLock.unlock();
        }

        Bitmap poll() {
            mRecordLock.lock();
            Bitmap result = null;
            if (!queue.isEmpty()) {
                result = queue.poll();

            }
            mRecordLock.unlock();
            return result;
        }


    }
}
