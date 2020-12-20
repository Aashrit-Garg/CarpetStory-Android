package com.example.carpetstory;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.assets.RenderableSource;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

public class AR extends AppCompatActivity {

    private ArFragment arFragment;
    private String ASSET_3D = "http://c7438e369c88.ngrok.io/";
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar);

        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.arFragment);
        arFragment.setOnTapArPlaneListener((hitResult, plane, motionEvent) -> {

            dialog = ProgressDialog.show(AR.this, "",
                    "Loading...", true);
            placeModel(hitResult.createAnchor());
        });

    }

    private void placeModel(Anchor anchor) {



        Intent intent = getIntent();
        String name = intent.getStringExtra("carpetName");

        ModelRenderable
                .builder()
                .setSource(
                        this,
                        RenderableSource
                                .builder()
                                .setSource(this, Uri.parse(ASSET_3D + name + "/rug.gltf"), RenderableSource.SourceType.GLTF2)
                                .setScale(0.25f)
                                .setRecenterMode(RenderableSource.RecenterMode.ROOT)
                                .build()
                )
                .setRegistryId(ASSET_3D)
                .build()
                .thenAccept(modelRenderable -> addNodeToScene(modelRenderable, anchor))
                .exceptionally(throwable -> {
                    Toast.makeText(this, throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    return null;
                });
    }

    private void addNodeToScene(ModelRenderable modelRenderable, Anchor anchor) {

        AnchorNode anchorNode = new AnchorNode(anchor);
        TransformableNode transformableNode = new TransformableNode(arFragment.getTransformationSystem());
        transformableNode.setParent(anchorNode);
        transformableNode.setRenderable(modelRenderable);
        arFragment.getArSceneView().getScene().addChild(anchorNode);
        transformableNode.select();
        dialog.dismiss();

    }
}
