package com.uc.mamba.renderer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import com.uc.mamba.renderer.gpuimage.OpenGlUtils;

import java.io.FileInputStream;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PictureDataSource {
    public static final int NO_TEXTURE = -1;
    private static final int MSG_DECODE = 0x1;
    List<String> pictures;
    private Handler threadHandler;
    private int frameIndex = 0;
    private int textureId = OpenGlUtils.getExternalOESTextureID();
    private long frameSpit;
    private int width = 720;
    private int height = 1280;
    private int frameRate = 30;
    private OnTextureAvailableListener onTextureAvailableListener;
    private Bitmap currentBitmap;
    private final Lock mRecordLock = new ReentrantLock();

    public PictureDataSource(List<String> pictures) {
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
    }

    public void start() {
        frameIndex = 0;
        threadHandler.sendEmptyMessage(MSG_DECODE);
        if (listener != null) {
            listener.onStart();
        }
    }

    public void stop() {
        threadHandler.removeMessages(MSG_DECODE);
        if (listener != null) {
            listener.onStop();
        }
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    private void decode() {
        threadHandler.removeMessages(MSG_DECODE);
        if (frameIndex >= pictures.size()) {
            if (listener != null) {
                listener.onFinish();
            }
            return;
        }
        Bitmap bitmap = decodeBitmap(pictures.get(frameIndex++));
        if (bitmap == null) {
            return;
        }
        mRecordLock.lock();
        currentBitmap=bitmap;
        mRecordLock.unlock();
        if (onTextureAvailableListener != null) {
            onTextureAvailableListener.onAvailable(bitmap, width, height);
        }
    }


   private int loadTexture(final Bitmap img, final int usedTexId, final boolean recycle) {
        int textures[] = new int[]{-1};
        if (usedTexId == NO_TEXTURE) {
            GLES20.glGenTextures(1, textures, 0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            //纹理也有坐标系，称UV坐标，或者ST坐标
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT); // S轴的拉伸方式为重复，决定采样值的坐标超出图片范围时的采样方式
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT); // T轴的拉伸方式为重复

            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, img, 0);
        } else {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, usedTexId);
            GLUtils.texSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, img);
            textures[0] = usedTexId;
        }
        if (recycle) {
            img.recycle();
        }
        return textures[0];
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

    public Bitmap scaleBitmap(Bitmap originalBitmap, int width, int height) {
        float realWidth = (float) originalBitmap.getWidth();
        float realHeight = (float) originalBitmap.getHeight();
        float scale = Math.min((float) width / realWidth, (float) height / realHeight);
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        int targetW = originalBitmap.getWidth();
        int targetH = originalBitmap.getHeight();

        try {
            Bitmap resizeBmp = Bitmap.createBitmap(originalBitmap, 0, 0, targetW, targetH, matrix, true);
//            if (!originalBitmap.isRecycled()) {
//                originalBitmap.recycle();
//            }

            originalBitmap = resizeBmp;
        } catch (Throwable var10) {
            var10.printStackTrace();
        }

        return originalBitmap;
    }

    public void setOnTextureAvailableListener(OnTextureAvailableListener onTextureAvailableListener) {
        this.onTextureAvailableListener = onTextureAvailableListener;
    }

    public int getTextureId() {
        return textureId;
    }

    public interface OnTextureAvailableListener {
        void onAvailable(Bitmap bitmap, int width, int height);
    }

    Listener listener;

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public interface Listener {
        void onStart();

        void onStop();

        void onFinish();
    }
}
