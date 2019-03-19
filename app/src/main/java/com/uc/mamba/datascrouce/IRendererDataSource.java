package com.uc.mamba.datascrouce;

import java.util.Map;

public interface IRendererDataSource {
    void onTextureIdCreated(int textureId);

    void onRunInDraw(int textureId, Map<String, Object> params);

    void setOnFrameAvailableListener(OnFrameAvailableListener listener);

    interface OnFrameAvailableListener {
        void onFrameAvailable();

        void onFrameSizeChange(int width, int height);
    }
}
