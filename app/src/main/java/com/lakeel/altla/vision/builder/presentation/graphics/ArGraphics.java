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
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.DefaultTextureBinder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Queue;
import com.lakeel.altla.android.log.Log;
import com.lakeel.altla.android.log.LogFactory;
import com.lakeel.altla.vision.builder.presentation.model.Axis;
import com.lakeel.altla.vision.model.Actor;
import com.lakeel.altla.vision.model.Asset;
import com.lakeel.altla.vision.model.AssetType;
import com.lakeel.altla.vision.model.ImageAsset;
import com.projecttango.tangosupport.TangoSupport;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.SimpleArrayMap;
import android.view.Display;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.badlogic.gdx.graphics.VertexAttributes.Usage.ColorPacked;
import static com.badlogic.gdx.graphics.VertexAttributes.Usage.Position;

public final class ArGraphics extends ApplicationAdapter implements GestureDetector.GestureListener {

    private static final Log LOG = LogFactory.getLog(ArGraphics.class);

    private static final int INVALID_TEXTURE_ID = 0;

    private static final float NEAR_PLANE_DISTANCE = 0.1f;

    private static final float FAR_PLANE_DISTANCE = 10f;

    private static final float TRANSLATION_COEFF = 0.001f;

    private static final float ROTATION_COEFF = 0.5f;

    private static final float SCALING_COEFF = 0.001f;

    private final FPSLogger fpsLogger = new FPSLogger();

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

    private PerspectiveCamera camera = new PerspectiveCamera();

    private final Quaternion tangoPoseRotation = new Quaternion();

    private RenderContext renderContext;

    private Environment environment;

    private ModelBatch modelBatch;

    private SpriteBatch spriteBatch;

    private final ColorObjectPicker picker = new ColorObjectPicker();

    private final SimpleArrayMap<String, Actor> actorMap = new SimpleArrayMap<>();

    private final Queue<ActorBuildRequest> actorBuildRequestQueue = new Queue<>();

    private final SimpleArrayMap<String, Model> modelMap = new SimpleArrayMap<>();

    private final Array<ActorObject> actorObjects = new Array<>();

    private final SimpleArrayMap<String, ActorObject> actorObjectMap = new SimpleArrayMap<>();

    private final Array<ModelInstance> visibleInstances = new Array<>();

    private final Array<ModelInstance> pickableInstances = new Array<>();

    private boolean debugFrameBuffersVisible;

    @Nullable
    private CursorBuildRequest cursorBuildRequest;

    @Nullable
    private Model cursorModel;

    @Nullable
    private CursorObject cursorObject;

    private boolean removeCursorRequested;

    private Model actorAxesModel;

    private ActorAxesObject actorAxesObject;

    @Nullable
    private ActorObject touchedActorObject;

    private boolean touchedActorObjectLocked;

    private boolean actorAxesObjectVisible;

    private boolean translationEnabled;

    private boolean rotationEnabled;

    private boolean scaleEnabled;

    @Nullable
    private Axis transformAxis;

    public ArGraphics(@NonNull Display display, @NonNull Listener listener) {
        this.display = display;
        this.listener = listener;
    }

    @Override
    public void create() {
        cameraPreview = new CameraPreview();

        camera.near = NEAR_PLANE_DISTANCE;
        camera.far = FAR_PLANE_DISTANCE;

        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

        renderContext = new RenderContext(new DefaultTextureBinder(DefaultTextureBinder.WEIGHTED, 1));

        modelBatch = new ModelBatch(renderContext);

        picker.init();

        actorAxesModel = new ModelBuilder().createXYZCoordinates(0.25f, new Material(), Position | ColorPacked);
        actorAxesObject = new ActorAxesObject(actorAxesModel);

        spriteBatch = new SpriteBatch();

        Gdx.input.setInputProcessor(new GestureDetector(this));
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
    public void dispose() {
        super.dispose();

        LOG.d("dispose");

        cameraPreview.close();
        modelBatch.dispose();
        spriteBatch.dispose();
        picker.dispose();

        for (int i = 0; i < modelMap.size(); i++) {
            modelMap.valueAt(i).dispose();
        }
        modelMap.clear();
    }

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        LOG.d("tap: x = %f, y = %f, count = %d, button = %d", x, y, count, button);
        return false;
    }

    @Override
    public boolean longPress(float x, float y) {
        LOG.d("longPress: x = %f, y = %f", x, y);
        return false;
    }

    @Override
    public boolean fling(float velocityX, float velocityY, int button) {
        LOG.d("fling: velocityX = %f, velocityY = %f, button = %d", velocityX, velocityY, button);
        return false;
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        LOG.d("pan: x = %f, y = %f, deltaX = %f, deltaY = %f", x, y, deltaX, deltaY);
        if (touchedActorObject != null) {
            if (translationEnabled && transformAxis != null) {
                float distance;
                switch (transformAxis) {
                    case X:
                        distance = deltaX;
                        break;
                    case Y:
                        distance = -deltaY;
                        break;
                    case Z:
                        distance = deltaY;
                        break;
                    default:
                        throw new IllegalStateException("An unexpected axis: " + transformAxis);
                }
                distance *= TRANSLATION_COEFF;
                touchedActorObject.translate(transformAxis, distance);
                return true;
            } else if (rotationEnabled && transformAxis != null) {
                float degrees;
                switch (transformAxis) {
                    case X:
                        degrees = deltaY;
                        break;
                    case Y:
                        degrees = deltaX;
                        break;
                    case Z:
                        degrees = -deltaX;
                        break;
                    default:
                        throw new IllegalStateException("An unexpected axis: " + transformAxis);
                }
                degrees *= ROTATION_COEFF;
                touchedActorObject.rotate(transformAxis, degrees);
            } else if (scaleEnabled) {
                float delta = (Math.abs(deltaY) <= Math.abs(deltaX)) ? deltaX : -deltaY;
                delta *= SCALING_COEFF;
                touchedActorObject.scale(delta);
            }
        }
        return false;
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        LOG.d("panStop: x = %f, y = %f, pointer = %d, button = %d");
        if (touchedActorObject != null) {
            if (translationEnabled && transformAxis != null) {
                touchedActorObject.stopTranslate();
                listener.onActorChanged(touchedActorObject.actor);
            } else if (rotationEnabled && transformAxis != null) {
                touchedActorObject.stopRotate();
                listener.onActorChanged(touchedActorObject.actor);
            } else if (scaleEnabled) {
                touchedActorObject.stopScale();
                listener.onActorChanged(touchedActorObject.actor);
            }
        }
        return false;
    }

    @Override
    public boolean zoom(float initialDistance, float distance) {
        LOG.d("zoom: initialDistance = %f, distance = %f", initialDistance, distance);
        return false;
    }

    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
        LOG.d("pinch: initialPointer1 = %s, initialPointer2 = %s, pointer1 = %s, pointer2 = %s",
              initialPointer1, initialPointer2, pointer1, pointer2);
        return false;
    }

    @Override
    public void pinchStop() {
        LOG.d("pinchStop");
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

    public void setDebugFrameBuffersVisible(boolean debugFrameBuffersVisible) {
        this.debugFrameBuffersVisible = debugFrameBuffersVisible;
    }

    public void setImageAssetCursor(@NonNull ImageAsset asset, @NonNull File imageCache) {
        final ImageAssetModelBuilder builder = new ImageAssetModelBuilder(imageCache);
        cursorBuildRequest = new CursorBuildRequest(asset, AssetType.IMAGE, builder);

        if (cursorObject != null) {
            // Remove a previous cursor if it exists.
            removeCursor();
        }
    }

    public void removeCursor() {
        removeCursorRequested = true;
    }

    public void addImageActor(@NonNull Actor actor, @NonNull File imageCache) {
        actorMap.put(actor.getId(), actor);

        final AssetModelBuilder builder = new ImageAssetModelBuilder(imageCache);
        final ActorBuildRequest request = new ActorBuildRequest(actor, builder);
        actorBuildRequestQueue.addLast(request);
    }

    public void removeActor(@NonNull Actor actor) {
        final ActorObject actorObject = actorObjectMap.remove(actor.getId());

        if (actorObject == null) throw new IllegalArgumentException("Invalid actor id: " + actor.getId());

        actorObjects.removeValue(actorObject, true);
    }

    public void setTouchedActorObjectLocked(boolean touchedActorObjectLocked) {
        this.touchedActorObjectLocked = touchedActorObjectLocked;
    }

    public void setActorAxesObjectVisible(boolean actorAxesObjectVisible) {
        this.actorAxesObjectVisible = actorAxesObjectVisible;
    }

    public void setTranslationEnabled(boolean translationEnabled, @Nullable Axis axis) {
        this.translationEnabled = translationEnabled;
        transformAxis = translationEnabled ? axis : null;
    }

    public void setRotationEnabled(boolean rotationEnabled, @Nullable Axis axis) {
        this.rotationEnabled = rotationEnabled;
        transformAxis = rotationEnabled ? axis : null;
    }

    public void setScaleEnabled(boolean scaleEnabled) {
        this.scaleEnabled = scaleEnabled;
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

                    // Update settings of the scene camera.
                    final TangoCameraIntrinsics intrinsics = TangoSupport.getCameraIntrinsicsBasedOnDisplayRotation(
                            TangoCameraIntrinsics.TANGO_CAMERA_COLOR, displayRotation);
                    camera.fieldOfView = MathHelper.verticalFieldOfView(intrinsics);
                    camera.viewportWidth = Gdx.graphics.getWidth();
                    camera.viewportHeight = Gdx.graphics.getHeight();

                    LOG.d("Camera parameters: fieldOfView = %f, viewportWidth = %f, viewportHeight = %f",
                          camera.fieldOfView, camera.viewportWidth, camera.viewportHeight);

                    // Initialize the pose of the scene camera.
                    camera.direction.set(0, 0, -1);
                    camera.up.set(0, 1, 0);
                    camera.update();

                    // Connect a new texture id for the camera preview to the tango.
                    if (connectedTextureId != cameraPreview.getTextureId()) {
                        tango.connectTextureId(TangoCameraIntrinsics.TANGO_CAMERA_COLOR, cameraPreview.getTextureId());
                        connectedTextureId = cameraPreview.getTextureId();
                        LOG.d("Texture connected: id = %d", connectedTextureId);
                    }

                    // Update the texture coordinate with the display rotation.
                    cameraPreview.updateTextureUv(displayRotation);

                    // Recreate the SpriteBatch instance
                    // because it decides the projection matrix with the viewport in its constructor.
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
                        camera.direction.set(0, 0, -1);
                        camera.up.set(0, 1, 0);
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

        while (0 < actorBuildRequestQueue.size) {
            final ActorBuildRequest request = actorBuildRequestQueue.removeFirst();
            final Actor actor = request.actor;
            final Model model = request.builder.build();

            modelMap.put(actor.getId(), model);

            final ActorObject actorObject = new ActorObject(model, actor);
            actorObjects.add(actorObject);
            actorObjectMap.put(actor.getId(), actorObject);

            LOG.d("Created an AR object: actorId = %s", actor.getId());
        }

        if (cursorModel != null && removeCursorRequested) {
            cursorModel.dispose();
            cursorModel = null;
            cursorObject = null;
            removeCursorRequested = false;
        }

        if (cursorBuildRequest != null) {
            cursorModel = cursorBuildRequest.builder.build();
            cursorObject = new CursorObject(cursorModel, cursorBuildRequest.asset, cursorBuildRequest.assetType);
            cursorBuildRequest = null;
        }

        if (cursorObject != null) {
            cursorObject.update(camera.position, tangoPoseRotation);
        }

        if (actorAxesObjectVisible && touchedActorObject != null) {
            actorAxesObject.update(touchedActorObject);
        }

        for (final ActorObject actorObject : actorObjects) {
            actorObject.update();
        }
    }

    private void draw() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        if (!cameraConfigured) return;

        renderContext.begin();

        // Draw the camera preview.
        cameraPreview.draw();

        // TODO: Do frustum culling.
        visibleInstances.clear();
        visibleInstances.addAll(actorObjects);
        if (cursorObject != null) visibleInstances.add(cursorObject);

        if (actorAxesObjectVisible) {
            visibleInstances.add(actorAxesObject);
        }

        pickableInstances.clear();
        if (cursorObject == null) {
            pickableInstances.addAll(actorObjects);
        } else {
            pickableInstances.add(cursorObject);
        }

        // Draw models.
        modelBatch.begin(camera);
        modelBatch.render(visibleInstances, environment);
        modelBatch.end();

        // Begin the picking section.
        picker.begin(camera, renderContext);
        picker.render(pickableInstances);

        // Check a picked object.
        if (Gdx.input.justTouched()) {
            final ModelInstance instance = picker.pick(pickableInstances, Gdx.input.getX(), Gdx.input.getY());

            if (instance == null) {
                if (cursorObject == null) {
                    if (!touchedActorObjectLocked) {
                        touchedActorObject = null;
                        listener.onActorObjectTouched(null);
                    }
                }
            } else if (instance instanceof ActorObject) {
                if (!touchedActorObjectLocked) {
                    touchedActorObject = (ActorObject) instance;
                    listener.onActorObjectTouched(touchedActorObject.actor);
                }
            } else if (instance instanceof CursorObject) {
                final CursorObject cursorObject = (CursorObject) instance;
                listener.onCursorObjectTouched(cursorObject.asset,
                                               cursorObject.assetType,
                                               new Vector3(cursorObject.position),
                                               new Quaternion(cursorObject.rotation),
                                               new Vector3(1, 1, 1));
                // Remove the cursor.
                removeCursor();
            } else {
                LOG.e("Detected an unexpected model instance.");
            }
        }

        picker.end();

        renderContext.end();

        // Draw frame buffers.
        if (debugFrameBuffersVisible) {
            spriteBatch.begin();
            spriteBatch.disableBlending();
            // Flip the texture because its origin in OpenGL is the buttom left.
            final Texture colorBufferTexture = picker.getColorBufferTexture();
            if (colorBufferTexture != null) {
                spriteBatch.draw(colorBufferTexture, 0, 0, 512, 512,
                                 0, 0, colorBufferTexture.getWidth(), colorBufferTexture.getHeight(), false, true);
            }
            spriteBatch.enableBlending();
            spriteBatch.end();
        }
    }

    public interface Listener {

        void onActorObjectTouched(@Nullable Actor actor);

        void onCursorObjectTouched(@NonNull Asset asset, @NonNull AssetType assetType,
                                   @NonNull Vector3 position, @NonNull Quaternion rotation, @NonNull Vector3 scale);

        void onActorChanged(@NonNull Actor actor);
    }
}
