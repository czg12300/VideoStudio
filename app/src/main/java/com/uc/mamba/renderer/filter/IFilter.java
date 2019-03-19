package com.uc.mamba.renderer.filter;

public interface IFilter {
    void onCreate();

    void onSizeChanged(int width, int height);

    void onDraw(int textureId);
}
