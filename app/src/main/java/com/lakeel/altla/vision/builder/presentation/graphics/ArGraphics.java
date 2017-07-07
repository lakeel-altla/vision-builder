package com.lakeel.altla.vision.builder.presentation.graphics;

import com.google.atap.tangoservice.Tango;
import com.google.atap.tangoservice.TangoCameraIntrinsics;
import com.google.atap.tangoservice.TangoException;
import com.google.atap.tangoservice.TangoPoseData;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.DefaultTextureBinder;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Queue;
import com.lakeel.altla.android.log.Log;
import com.lakeel.altla.android.log.LogFactory;
import com.lakeel.altla.vision.model.Actor;
import com.projecttango.tangosupport.TangoSupport;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Display;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.disposables.CompositeDisposable;

public final class ArGraphics extends ApplicationAdapter {

    private static final Log LOG = LogFactory.getLog(ArGraphics.class);

    private static final int INVALID_TEXTURE_ID = 0;

    private final FPSLogger fpsLogger = new FPSLogger();

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private final Listener listener;

    private final Display display;

    private Tango tango;

    private boolean tangoConnected;

    private boolean cameraConfigured;

    private int connectedTextureId = INVALID_TEXTURE_ID;

    private final AtomicBoolean frameAvailable = new AtomicBoolean(false);

    private double cameraColorTimestamp;

    private CameraPreview cameraPreview;

    private int displayRotation;

    private PerspectiveCamera camera;

    private final Quaternion tangoPoseRotation = new Quaternion();

    private final Vector3 cameraInitialDirection = new Vector3();

    private final Vector3 cameraInitialUp = new Vector3();

    private RenderContext renderContext;

    private Environment environment;

    private ModelBatch modelBatch;

    private SpriteBatch spriteBatch;

    private final ColorObjectPicker picker = new ColorObjectPicker();

    private final Map<String, Actor> actorMap = new HashMap<>();

    private final Queue<ActorModelBuilder> actorModelBuilderQueue = new Queue<>();

    private final Map<String, Model> modelMap = new HashMap<>();

    private final Array<ArObject> arObjects = new Array<>();

    private final Map<String, ArObject> arObjectMap = new HashMap<>();

    public ArGraphics(@NonNull Display display, @NonNull Listener listener) {
        this.display = display;
        this.listener = listener;
    }

    @Override
    public void create() {
        cameraPreview = new CameraPreview();

        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

        renderContext = new RenderContext(new DefaultTextureBinder(DefaultTextureBinder.WEIGHTED, 1));

        modelBatch = new ModelBatch(renderContext);

        picker.init();

        spriteBatch = new SpriteBatch();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);

        cameraConfigured = false;
    }

    @Override
    public void render() {
//        fpsLogger.log();

        update();
        draw();
    }

    @Override
    public void pause() {
        super.pause();

        compositeDisposable.clear();
    }

    @Override
    public void dispose() {
        super.dispose();

        LOG.d("dispose");

        cameraPreview.close();
        modelBatch.dispose();
        spriteBatch.dispose();
        picker.dispose();

        for (Model model : modelMap.values()) {
            model.dispose();
        }
    }

    // Invoked in Tango thread.
    public synchronized void onTangoConnected(@NonNull Tango tango) {
        this.tango = tango;
        tangoConnected = true;
    }

    // Invoked in UI thread.
    public synchronized void onTangoDisconnecting() {
        tango = null;
        tangoConnected = false;
        connectedTextureId = INVALID_TEXTURE_ID;
    }

    public void onFrameAvailable() {
        frameAvailable.set(true);
    }

    public void addImageActor(@NonNull Actor actor, @NonNull File imageCache) {
        actorMap.put(actor.getId(), actor);
        actorModelBuilderQueue.addLast(new ImageActorModelBuilder(actor, imageCache));
    }

    private void update() {
        //
        // Update the camera state and the texture for the camera preview.
        //
        try {
            synchronized (this) {
                if (!tangoConnected) {
                    return;
                }

                if (!cameraConfigured) {
                    displayRotation = display.getRotation();

                    // Create/recreate the scene camera.
                    final TangoCameraIntrinsics intrinsics = TangoSupport.getCameraIntrinsicsBasedOnDisplayRotation(
                            TangoCameraIntrinsics.TANGO_CAMERA_COLOR, displayRotation);

                    final int viewportWidth = Gdx.graphics.getWidth();
                    final int viewportHeight = Gdx.graphics.getHeight();
                    final float fovy = (float) Math.toDegrees(2f * Math.atan(0.5 * intrinsics.height / intrinsics.fy));

                    camera = new PerspectiveCamera(fovy, viewportWidth, viewportHeight);
                    camera.near = 1f;
                    camera.far = 300f;
                    camera.update();

                    cameraInitialDirection.set(camera.direction);
                    cameraInitialUp.set(camera.up);

                    if (connectedTextureId != cameraPreview.getTextureId()) {
                        tango.connectTextureId(TangoCameraIntrinsics.TANGO_CAMERA_COLOR, cameraPreview.getTextureId());
                        connectedTextureId = cameraPreview.getTextureId();
                        LOG.d("Texture connected: id = %d", connectedTextureId);
                    }

                    cameraPreview.updateTextureUv(displayRotation);

                    // Recreate the SpriteBatch instance because it decides the projection matrix with the current
                    // viewport in its constructor.
                    if (spriteBatch != null) {
                        spriteBatch.dispose();
                    }
                    spriteBatch = new SpriteBatch();

                    cameraConfigured = true;
                }

                if (frameAvailable.compareAndSet(true, false)) {
                    // Update the camera preview.
                    cameraColorTimestamp = tango.updateTexture(TangoCameraIntrinsics.TANGO_CAMERA_COLOR);

                    // Update the scene camera.
                    final TangoPoseData poseData = TangoSupport.getPoseAtTime(
                            cameraColorTimestamp,
                            TangoPoseData.COORDINATE_FRAME_AREA_DESCRIPTION,
                            TangoPoseData.COORDINATE_FRAME_CAMERA_COLOR,
                            TangoSupport.TANGO_SUPPORT_ENGINE_OPENGL,
                            TangoSupport.TANGO_SUPPORT_ENGINE_OPENGL,
                            displayRotation);
                    if (poseData.statusCode == TangoPoseData.POSE_VALID) {
                        camera.position.set((float) poseData.translation[0],
                                            (float) poseData.translation[1],
                                            (float) poseData.translation[2]);
                        tangoPoseRotation.set((float) poseData.rotation[0],
                                              (float) poseData.rotation[1],
                                              (float) poseData.rotation[2],
                                              (float) poseData.rotation[3]);
                        camera.direction.set(cameraInitialDirection);
                        camera.up.set(cameraInitialUp);
                        camera.rotate(tangoPoseRotation);
                        camera.update();
                    } else if (poseData.statusCode == TangoPoseData.POSE_INVALID) {
                        LOG.d("The tango pose is invalid:  cameraColorTimestamp = %f, displayRotation = %d",
                              cameraColorTimestamp, displayRotation);
                    } else if (poseData.statusCode == TangoPoseData.POSE_UNKNOWN) {
                        LOG.d("The tango pose is unknown.");
                    }
                }
            }
        } catch (TangoException e) {
            LOG.e("Tango API call error within the OpenGL render thread.", e);
        }

        while (0 < actorModelBuilderQueue.size) {
            final ActorModelBuilder actorModelBuilder = actorModelBuilderQueue.removeFirst();
            final Actor actor = actorModelBuilder.actor;
            final Model model = actorModelBuilder.build();

            modelMap.put(actor.getId(), model);

            final ArObject arObject = new ArObject(model, actor);

            // TODO: Adjust Z for tests.
            arObject.transform.translate((float) actor.getPositionX(),
                                         (float) actor.getPositionY(),
                                         (float) actor.getPositionZ() - 100);

            // TODO: use a tmp field.
            Quaternion rotation = new Quaternion((float) actor.getOrientationX(),
                                                 (float) actor.getOrientationY(),
                                                 (float) actor.getOrientationZ(),
                                                 (float) actor.getOrientationW());
            arObject.transform.rotate(rotation);

            arObject.transform.scale((float) actor.getScaleX() * arObject.transform.getScaleX(),
                                     (float) actor.getScaleY() * arObject.transform.getScaleY(),
                                     (float) actor.getScaleZ() * arObject.transform.getScaleZ());

            arObjects.add(arObject);
            arObjectMap.put(actor.getId(), arObject);

            LOG.d("Created an AR object: actorId = %s", actor.getId());
        }
    }

    private void draw() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        if (!cameraConfigured) return;

        renderContext.begin();

        // Draw the camera preview.
        cameraPreview.draw();

        // TODO: Do frustum culling.
        final Array<ModelInstance> instances = new Array<>(arObjects);

        // Draw models.
        modelBatch.begin(camera);
        modelBatch.render(instances, environment);
        modelBatch.end();

        // Begin the picking section.
        picker.begin(camera, renderContext);
        picker.render(instances);

        // Check a picked object.
        if (Gdx.input.justTouched()) {
            final ModelInstance instance = picker.pick(instances, Gdx.input.getX(), Gdx.input.getY());

            final Actor actor;
            if (instance == null) {
                actor = null;
            } else {
                final ArObject arObject = (ArObject) instance;
                actor = arObject.actor;
            }

            listener.onActorSelected(actor);
        }

        picker.end();

        renderContext.end();

        // Draw frame buffers.

        spriteBatch.begin();
        spriteBatch.disableBlending();
        // Flip the texture because its origin in OpenGL is the buttom left.
        final Texture colorBufferTexture = picker.getColorBufferTexture();
        if (colorBufferTexture != null) {
            spriteBatch.draw(colorBufferTexture, 0, Gdx.graphics.getHeight() - 512, 512, 512,
                             0, 0, colorBufferTexture.getWidth(), colorBufferTexture.getHeight(), false, true);
        }
        spriteBatch.enableBlending();
        spriteBatch.end();
    }

    public interface Listener {

        void onActorSelected(@Nullable Actor actor);
    }
}
