package com.uc.mamba.datascrouce;

import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.view.Surface;

import java.util.Map;

public class PlayerDataSource implements IRendererDataSource {
    private MediaPlayer player ;
    private SurfaceTexture surfaceTexture;
    private OnFrameAvailableListener onFrameAvailableListener;
    private float[] matrix = new float[16];

    public void setPlayer(MediaPlayer player) {
        this.player = player;
    }

    @Override
    public void onTextureIdCreated(int textureId) {
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);
        surfaceTexture = new SurfaceTexture(textureId);
        Surface surface = new Surface(surfaceTexture);
        player.setSurface(surface);
        surfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                if (onFrameAvailableListener != null) {
                    onFrameAvailableListener.onFrameAvailable();
                }
            }
        });
    }

    public void onVideoSizeChanged(int width, int height) {
        if (onFrameAvailableListener != null) {
            onFrameAvailableListener.onFrameSizeChange(width, height);
        }
    }

    @Override
    public void onRunInDraw(int textureId, Map<String, Object> params) {
        surfaceTexture.updateTexImage();
        surfaceTexture.getTransformMatrix(matrix);
        params.put("matrix", matrix);

    }

    @Override
    public void setOnFrameAvailableListener(OnFrameAvailableListener listener) {
        onFrameAvailableListener = listener;
    }
}
