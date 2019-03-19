package com.uc.mamba.renderer.drawer;

import java.util.Map;

public interface IDrawer {
    void onCreated();
    void onInputSizeChanged(int width, int height);
    void onOutputSizeChanged(int width, int height);
    void onDraw(int textureId, Map<String,Object> extParams);
}
