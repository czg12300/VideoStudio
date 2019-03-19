package com.uc.mamba;

import android.media.MediaPlayer;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.uc.mamba.datascrouce.CameraDataSource;
import com.uc.mamba.datascrouce.PictureDataSource;
import com.uc.mamba.datascrouce.PlayerDataSource;
import com.uc.mamba.renderer.GLRenderer;
import com.uc.mamba.renderer.ImageRenderer;
import com.uc.mamba.renderer.drawer.CameraDrawer;
import com.uc.mamba.renderer.drawer.PictureDrawer;
import com.uc.mamba.renderer.drawer.PlayerDrawer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PreviewActivity extends AppCompatActivity {
    private GLSurfaceView surfaceView;
    private MediaPlayer player;
    private CameraV1 cameraV1;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        surfaceView = findViewById(R.id.preview);
        int type = getIntent().getIntExtra("type", 1);
        switch (type) {
            case 1:
                picture();
                break;
            case 2:
                camera();
                break;
            case 3:
                player();
                break;
        }


    }

    private void camera() {
        GLRenderer glRenderer = new GLRenderer(surfaceView);
        CameraDrawer drawer = new CameraDrawer();
        glRenderer.setDrawer(drawer);
        CameraDataSource cameraDataSource = new CameraDataSource(this);
        glRenderer.setRendererDataSource(cameraDataSource);
        cameraDataSource.setOnFrameAvailableListener(glRenderer);
    }

    private void player() {
        GLRenderer glRenderer = new GLRenderer(surfaceView);
        PlayerDrawer drawer = new PlayerDrawer();
        glRenderer.setDrawer(drawer);
        final PlayerDataSource dataSource = new PlayerDataSource();
        glRenderer.setRendererDataSource(dataSource);
        dataSource.setOnFrameAvailableListener(glRenderer);
        player = new MediaPlayer();
        dataSource.setPlayer(player);
        try {
            player.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
                @Override
                public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                    dataSource.onVideoSizeChanged(width,height);
                }
            });
            player.setDataSource("/sdcard/0A/ak.mp4");
            player.prepare();
            player.setLooping(true);
            player.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void picture() {
        File file = new File("/sdcard/0Ajake");
        List<String> list = new ArrayList<>();
        if (file.exists()) {
            for (File path : file.listFiles()) {
                list.add(path.getAbsolutePath());
                Log.d("jake", "path=" + path.getAbsolutePath());
            }
        }
        PictureDataSource dataSource = new PictureDataSource(list);
        GLRenderer glRenderer = new GLRenderer(surfaceView);
        PictureDrawer drawer = new PictureDrawer();
        glRenderer.setDrawer(drawer);
        glRenderer.setRendererDataSource(dataSource);

        dataSource.setOnFrameAvailableListener(glRenderer);
        dataSource.autoStartWhenReady(true);
        dataSource.setWidth(720);
        dataSource.setHeight(1280);
        dataSource.setFrameRate(2);
        dataSource.setOnCompletionListener(new PictureDataSource.OnCompletionListener() {
            @Override
            public void onCompletion(PictureDataSource dataSource) {
                dataSource.start();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.stop();
            player.release();
        }
    }
}
