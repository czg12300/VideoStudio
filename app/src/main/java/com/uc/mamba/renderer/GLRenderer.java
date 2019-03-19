package com.uc.mamba.renderer;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.uc.mamba.datascrouce.IRendererDataSource;
import com.uc.mamba.renderer.drawer.IDrawer;

import java.util.HashMap;
import java.util.Map;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GLRenderer implements GLSurfaceView.Renderer, IRendererDataSource.OnFrameAvailableListener {

    private GLSurfaceView glSurfaceView;
    private IRendererDataSource rendererDataSource;
    private int textureId = -1;
    private IDrawer drawer;
    private Map<String,Object> params=new HashMap<>();

    public GLRenderer(GLSurfaceView glSurfaceView) {
        this.glSurfaceView = glSurfaceView;
        setupGLSurfaceView();
    }

    private void setupGLSurfaceView() {
        glSurfaceView.setEGLContextClientVersion(2); // 创建OpenGL ES 2.0 的上下文环境
        glSurfaceView.setRenderer(this);
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY); // 手动刷新
    }


    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        int[] tex = new int[1];
        GLES20.glGenTextures(1, tex, 0);
        textureId = tex[0];
        if (drawer != null) {
            drawer.onCreated();
        }
        if (rendererDataSource != null) {
            rendererDataSource.onTextureIdCreated(textureId);
        }
        Log.d("jake","textureId ="+textureId);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        if (drawer != null) {
            drawer.onOutputSizeChanged(width, height);
        }
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if (rendererDataSource != null) {
            rendererDataSource.onRunInDraw(textureId,params);
        }
        Log.d("jake","onDrawFrame onRunInDrawtextureId ="+textureId);
        if (drawer != null) {
            drawer.onDraw(textureId,params);
        }
        params.clear();
        Log.d("jake","onDrawFrame onDrawtextureId ="+textureId);
    }

    public void setRendererDataSource(IRendererDataSource rendererDataSource) {
        this.rendererDataSource = rendererDataSource;
    }

    public void setDrawer(IDrawer drawer) {
        this.drawer = drawer;
    }


    @Override
    public void onFrameAvailable() {
        if (glSurfaceView != null) {
            glSurfaceView.requestRender();
        }
    }

    @Override
    public void onFrameSizeChange(int width, int height) {
        if(drawer !=null){
            drawer.onInputSizeChanged(width,height);
        }
    }
}
