package com.uc.mamba.renderer.drawer;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;


import com.uc.mamba.renderer.gpuimage.OpenGlUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Map;

public class PlayerDrawer implements IDrawer {
    private int aPositionLocation;
    private int programId;
    private FloatBuffer vertexBuffer;
    private final float[] vertexData = {
            1f, -1f, 0f,
            -1f, -1f, 0f,
            1f, 1f, 0f,
            -1f, 1f, 0f
    };

    private final float[] projectionMatrix = new float[16];
    private int uMatrixLocation;

    private final float[] textureVertexData = {
            1f, 0f,
            0f, 0f,
            1f, 1f,
            0f, 1f
    };
    private FloatBuffer textureVertexBuffer;
    private int uTextureSamplerLocation;
    private int aTextureCoordLocation;
    private float[] mSTMatrix = new float[16];
    private int uSTMMatrixHandle;
    private int screenWidth, screenHeight;

    private static final String VERTEX = ""+
            "attribute vec4 position;\n" +
            "attribute vec4 inputTextureCoordinate;\n" +
            "\n" +
            "uniform mat4 textureTransform;\n" +
            "varying vec2 textureCoordinate;\n" +
            "\n" +
            "void main()\n" +
            "{\n" +
            "	textureCoordinate = (textureTransform * inputTextureCoordinate).xy;\n" +
            "	gl_Position = position;\n" +
            "}";
    private static final String FRAGMENT = ""+
            "#extension GL_OES_EGL_image_external : require\n" +
            "varying highp vec2 textureCoordinate;\n" +
            "\n" +
            "uniform samplerExternalOES inputImageTexture;\n" +
            "\n" +
            "void main()\n" +
            "{\n" +
            "	gl_FragColor = texture2D(inputImageTexture, textureCoordinate);\n" +
            "}";
    protected int mGLAttribPosition;
    protected int mGLUniformTexture;
    protected int mGLAttribTextureCoordinate;
    private int mTextureTransformMatrixLocation;

    @Override
    public void onCreated() {
        vertexBuffer = ByteBuffer.allocateDirect(vertexData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertexData);
        vertexBuffer.position(0);

        textureVertexBuffer = ByteBuffer.allocateDirect(textureVertexData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(textureVertexData);
        textureVertexBuffer.position(0);


        programId = OpenGlUtils.loadProgram(VERTEX, FRAGMENT);

        mGLAttribPosition = GLES20.glGetAttribLocation(programId, "position");
        mGLUniformTexture = GLES20.glGetUniformLocation(programId, "inputImageTexture");
        mGLAttribTextureCoordinate = GLES20.glGetAttribLocation(programId,"inputTextureCoordinate");
        mTextureTransformMatrixLocation = GLES20.glGetUniformLocation(programId, "textureTransform");
//
//        aPositionLocation = GLES20.glGetAttribLocation(programId, "aPosition");
//
//        uMatrixLocation = GLES20.glGetUniformLocation(programId, "uMatrix");
//        uSTMMatrixHandle = GLES20.glGetUniformLocation(programId, "uSTMatrix");
//        uTextureSamplerLocation = GLES20.glGetUniformLocation(programId, "sTexture");
//        aTextureCoordLocation = GLES20.glGetAttribLocation(programId, "aTexCoord");
    }

    private int inputWidth;
    private int inputHeight;
    @Override
    public void onInputSizeChanged(int width, int height) {
        inputWidth=width;
        inputHeight=height;
        updateProjection(width, height);
    }

    @Override
    public void onOutputSizeChanged(int width, int height) {
        screenWidth = width;
        screenHeight = height;
        updateProjection(inputWidth, inputHeight);
    }

    @Override
    public void onDraw(int textureId, Map<String, Object> params) {
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        mSTMatrix = (float[]) params.get("matrix");
        GLES20.glViewport(0, 0, screenWidth, screenHeight);
        GLES20.glUseProgram(programId);
        vertexBuffer.position(0);
        GLES20.glVertexAttribPointer(mGLAttribPosition, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer);
        GLES20.glEnableVertexAttribArray(mGLAttribPosition);
        textureVertexBuffer.position(0);
        GLES20.glVertexAttribPointer(mGLAttribTextureCoordinate, 2, GLES20.GL_FLOAT, false, 0, textureVertexBuffer);
        GLES20.glEnableVertexAttribArray(mGLAttribTextureCoordinate);
        GLES20.glUniformMatrix4fv(mTextureTransformMatrixLocation, 1, false, mSTMatrix, 0);

//        if(textureId != OpenGlUtils.NO_TEXTURE){
//            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
//            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
//            GLES20.glUniform1i(mGLUniformTexture, 0);
//        }

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glDisableVertexAttribArray(mGLAttribPosition);
        GLES20.glDisableVertexAttribArray(mGLAttribTextureCoordinate);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);





//
//
//        GLES20.glUseProgram(programId);
//        GLES20.glUniformMatrix4fv(uMatrixLocation, 1, false, projectionMatrix, 0);
//        GLES20.glUniformMatrix4fv(uSTMMatrixHandle, 1, false, mSTMatrix, 0);
//
//        vertexBuffer.position(0);
//        GLES20.glEnableVertexAttribArray(aPositionLocation);
//        GLES20.glVertexAttribPointer(aPositionLocation, 3, GLES20.GL_FLOAT, false,
//                12, vertexBuffer);
//
//        textureVertexBuffer.position(0);
//        GLES20.glEnableVertexAttribArray(aTextureCoordLocation);
//        GLES20.glVertexAttribPointer(aTextureCoordLocation, 2, GLES20.GL_FLOAT, false, 8, textureVertexBuffer);
//
//        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
//        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
//
//        GLES20.glUniform1i(uTextureSamplerLocation, 0);
//        GLES20.glViewport(0, 0, screenWidth, screenHeight);
//        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

    }




    private void updateProjection(int videoWidth, int videoHeight) {
        float screenRatio = (float) screenWidth / screenHeight;
        float videoRatio = (float) videoWidth / videoHeight;
        if (videoRatio > screenRatio) {
            Matrix.orthoM(projectionMatrix, 0, -1f, 1f, -videoRatio / screenRatio, videoRatio / screenRatio, -1f, 1f);
        } else
            Matrix.orthoM(projectionMatrix, 0, -screenRatio / videoRatio, screenRatio / videoRatio, -1f, 1f, -1f, 1f);
    }


}
