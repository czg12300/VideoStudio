package com.uc.mamba.datascrouce;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import com.uc.mamba.CameraV1;

import java.util.Map;

import javax.microedition.khronos.opengles.GL10;

public class CameraDataSource implements IRendererDataSource {
    private OnFrameAvailableListener onFrameAvailableListener;
    Activity activity;
    private SurfaceTexture mSurfaceTexture;
    private CameraV1 cameraV1;
    private int width = 1280;
    private int height = 720;

    public CameraDataSource(Activity activity) {
        this.activity = activity;
        cameraV1 = new CameraV1(activity);
    }

    public CameraV1 getCameraV1() {
        return cameraV1;
    }

    @Override
    public void onTextureIdCreated(int textureId) {

        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MIN_FILTER,GL10.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);


        mSurfaceTexture = new SurfaceTexture(textureId);
        cameraV1.openCamera(width, height, 0);
        cameraV1.setPreviewTexture(mSurfaceTexture);
        mSurfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                if (onFrameAvailableListener != null) {
                    onFrameAvailableListener.onFrameAvailable();
                }
            }
        });
        cameraV1.startPreview();
        if (onFrameAvailableListener != null) {
            onFrameAvailableListener.onFrameSizeChange(width, height);
        }
    }

    @Override
    public void onRunInDraw(int textureId, Map<String, Object> params) {
        mSurfaceTexture.updateTexImage();
//        GLES20.glActiveTexture(GLES20.GL_TEXTURE0+getTextureType());
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,textureId);
//        GLES20.glUniform1i(mHTexture,getTextureType());
    }


    @Override
    public void setOnFrameAvailableListener(OnFrameAvailableListener listener) {
        onFrameAvailableListener = listener;
    }
}
