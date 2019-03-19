package com.uc.mamba.renderer;

import android.opengl.GLES20;

import com.uc.mamba.renderer.utils.OpenGlUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ImageRenderer {
    private final Lock mRecordLock = new ReentrantLock();
    private final Lock mUpdateLock = new ReentrantLock();
    private final LinkedList<Runnable> runOnDrawStart = new LinkedList<>();
    private final LinkedList<Runnable> runOnDrawEnd = new LinkedList<>();
    private FloatBuffer cubeBuffer;
    private FloatBuffer textureBuffer;
    private int outputWidth, outputHeight; // 窗口大小
    private int inputWidth, inputHeight; // bitmap图片实际大小
    private GLImageHandler imageHandler = new GLImageHandler();
    private VideoFps videoFps = new VideoFps();

    public void onCreated() {
        // 顶点数组缓冲器
        cubeBuffer = ByteBuffer.allocateDirect(OpenGlUtils.CUBE.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        cubeBuffer.put(OpenGlUtils.CUBE).position(0);
        // 纹理数组缓冲器
        textureBuffer = ByteBuffer.allocateDirect(OpenGlUtils.TEXTURE_NO_ROTATION.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        textureBuffer.put(OpenGlUtils.TEXTURE_NO_ROTATION).position(0);
        imageHandler.init();
    }

    public void onInputSizeChanged(int width, int height) {
        inputWidth = width;
        inputHeight = height;
    }

    public void onOutputSizeChanged(int width, int height) {
        outputWidth = width;
        outputHeight = height;
        GLES20.glViewport(0, 0, width, height); // 设置窗口大小
        adjustImageScaling(); // 调整图片显示大小。如果不调用该方法，则会导致图片整个拉伸到填充窗口显示区域
    }

    public void onDraw(int textureId) {
        videoFps.printFps();
        runPendingOnDrawStartTasks();
        imageHandler.onDraw(textureId, cubeBuffer, textureBuffer);
        runPendingOnDrawEndTasks();
    }

    // 调整图片显示大小为居中显示
    private void adjustImageScaling() {
        float outputWidth = this.outputWidth;
        float outputHeight = this.outputHeight;

        float ratio1 = outputWidth / inputWidth;
        float ratio2 = outputHeight / inputHeight;
        float ratioMax = Math.min(ratio1, ratio2);
        // 居中后图片显示的大小
        int imageWidthNew = Math.round(inputWidth * ratioMax);
        int imageHeightNew = Math.round(inputHeight * ratioMax);

        // 图片被拉伸的比例
        float ratioWidth = outputWidth / imageWidthNew;
        float ratioHeight = outputHeight / imageHeightNew;
        // 根据拉伸比例还原顶点
        float[] cube = new float[]{
                OpenGlUtils.CUBE[0] / ratioWidth, OpenGlUtils.CUBE[1] / ratioHeight,
                OpenGlUtils.CUBE[2] / ratioWidth, OpenGlUtils.CUBE[3] / ratioHeight,
                OpenGlUtils.CUBE[4] / ratioWidth, OpenGlUtils.CUBE[5] / ratioHeight,
                OpenGlUtils.CUBE[6] / ratioWidth, OpenGlUtils.CUBE[7] / ratioHeight,
        };

        cubeBuffer.clear();
        cubeBuffer.put(cube).position(0);
    }

    protected void runOnDrawStart(Runnable runnable) {
        synchronized (runOnDrawStart) {
            runOnDrawStart.offer(runnable);
        }
    }

    protected void runOnDrawEnd(Runnable runnable) {
        synchronized (runOnDrawStart) {
            runOnDrawStart.offer(runnable);
        }
    }

    private void runPendingOnDrawStartTasks() {
        synchronized (runOnDrawStart) {
            while (!runOnDrawStart.isEmpty()) {
                runOnDrawStart.poll().run();
            }
        }
    }

    private void runPendingOnDrawEndTasks() {
        synchronized (runOnDrawEnd) {
            while (!runOnDrawEnd.isEmpty()) {
                runOnDrawEnd.poll().run();
            }
        }
    }

}
