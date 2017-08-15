package com.lakeel.altla.vision.builder.presentation.graphics;

import com.google.atap.tango.mesh.TangoMesh;
import com.google.atap.tangoservice.Tango;
import com.google.atap.tangoservice.TangoCameraIntrinsics;
import com.google.atap.tangoservice.TangoException;
import com.google.atap.tangoservice.TangoPoseData;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.DefaultTextureBinder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.lakeel.altla.android.log.Log;
import com.lakeel.altla.android.log.LogFactory;
import com.lakeel.altla.vision.api.VisionService;
import com.lakeel.altla.vision.builder.presentation.graphics.asset.AssetLoader;
import com.lakeel.altla.vision.builder.presentation.graphics.model.ActorAxesInstance;
import com.lakeel.altla.vision.builder.presentation.graphics.model.ActorCursorInstance;
import com.lakeel.altla.vision.builder.presentation.graphics.model.ActorNode;
import com.lakeel.altla.vision.builder.presentation.graphics.model.ActorNodeFactory;
import com.lakeel.altla.vision.builder.presentation.graphics.model.ComponentInstance;
import com.lakeel.altla.vision.builder.presentation.graphics.model.ShapeModelLoader;
import com.lakeel.altla.vision.builder.presentation.graphics.shader.FillColorShader;
import com.lakeel.altla.vision.builder.presentation.model.Axis;
import com.lakeel.altla.vision.model.Actor;
import com.lakeel.altla.vision.model.AssetMeshComponent;
import com.lakeel.altla.vision.model.PrimitiveMeshComponent;
import com.projecttango.tangosupport.TangoSupport;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.SimpleArrayMap;
import android.view.Display;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.badlogic.gdx.graphics.VertexAttributes.Usage.ColorPacked;
import static com.badlogic.gdx.graphics.VertexAttributes.Usage.Position;

public final class ArGraphics extends ApplicationAdapter implements GestureDetector.GestureListener {

    private static final Log LOG = LogFactory.getLog(ArGraphics.class);

    private static final int INVALID_TEXTURE_ID = 0;

    private static final float NEAR_PLANE_DISTANCE = 0.1f;

    // See the tango 3D reconstruction config 'max_depth'.
    private static final float FAR_PLANE_DISTANCE = 5f;

    private static final float TRANSLATION_COEFF = 0.001f;

    private static final float ROTATION_COEFF = 0.5f;

    private static final float SCALING_COEFF = 0.001f;

    private static final int DEBUG_FRAME_BUFFER_VIEW_SIZE = 512;

    private final FPSLogger fpsLogger = new FPSLogger();

    private final Display display;

    private final Listener listener;

    private final VisionService visionService;

    private Tango tango;

    private boolean tangoConnected;

    private boolean cameraConfigured;

    private int connectedTextureId = INVALID_TEXTURE_ID;

    private final AtomicBoolean frameAvailable = new AtomicBoolean(false);

    private double cameraColorTimestamp;

    private CameraPreview cameraPreview;

    private int displayRotation;

    private TangoCamera camera = new TangoCamera();

    private RenderContext renderContext;

    private Environment environment;

    private ModelBatch modelBatch;

    private SpriteBatch spriteBatch;

    private FrameBuffer sceneFrameBuffer;

    private FrameBuffer tangoMeshesFrameBuffer;

    private TangoMeshRenderer tangoMeshRenderer;

    private ColorObjectPicker picker;

    private final AssetLoader assetLoader;

    private final ShapeModelLoader shapeModelLoader = new ShapeModelLoader();

    private final ActorNodeFactory actorNodeFactory;

    private final SimpleArrayMap<String, ActorNode> actorNodeMap = new SimpleArrayMap<>();

    private final Array<ModelInstance> visibleInstances = new Array<>();

    private final Array<ModelInstance> pickableInstances = new Array<>();

    private boolean debugFrameBuffersVisible;

    private boolean debugTangoMeshesVisible;

    private boolean debugCameraPreviewVisible = true;

    private final FillColorShader fillColorShader = new FillColorShader();

    @Nullable
    private ActorCursorInstance actorCursorInstance;

    private Model actorAxesModel;

    private ActorAxesInstance actorAxesInstance;

    @Nullable
    private ActorNode touchedActorNode;

    private final Vector3 originalTouchedActorObjectScale = new Vector3();

    private boolean touchedActorLocked;

    private boolean translationEnabled;

    private boolean rotationEnabled;

    private boolean scaleEnabled;

    @Nullable
    private Axis transformAxis;

    public ArGraphics(@NonNull VisionService visionService, @NonNull Display display, @NonNull Listener listener) {
        this.visionService = visionService;
        this.display = display;
        this.listener = listener;
        assetLoader = new AssetLoader(visionService);
        actorNodeFactory = new ActorNodeFactory(assetLoader, shapeModelLoader);
    }

    @Override
    public void create() {
        cameraPreview = new CameraPreview();

        camera.near = NEAR_PLANE_DISTANCE;
        camera.far = FAR_PLANE_DISTANCE;

        environment = new Environment();
        // TODO: Make lights managed in each area.
        environment.add(new DirectionalLight().set(1, 1, 1, 0, -1, 0));
//        environment.setTangoPoseData(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1));

        renderContext = new RenderContext(new DefaultTextureBinder(DefaultTextureBinder.WEIGHTED, 1));

        modelBatch = new ModelBatch(renderContext);

        picker = new ColorObjectPicker();

        fillColorShader.init();

        actorAxesModel = new ModelBuilder().createXYZCoordinates(0.25f, new Material(), Position | ColorPacked);

        spriteBatch = new SpriteBatch();

        tangoMeshRenderer = new TangoMeshRenderer();

        Gdx.input.setInputProcessor(new GestureDetector(this));
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);

        if (tangoMeshesFrameBuffer != null) {
            tangoMeshesFrameBuffer.dispose();
            tangoMeshesFrameBuffer = null;
        }
        tangoMeshesFrameBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, true);

        if (sceneFrameBuffer != null) {
            sceneFrameBuffer.dispose();
            sceneFrameBuffer = null;
        }
        sceneFrameBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);
        // Share the depth buffer.
        Gdx.gl.glBindFramebuffer(GL20.GL_FRAMEBUFFER, sceneFrameBuffer.getFramebufferHandle());
        Gdx.gl.glFramebufferRenderbuffer(GL20.GL_FRAMEBUFFER,
                                         GL20.GL_DEPTH_ATTACHMENT,
                                         GL20.GL_RENDERBUFFER,
                                         tangoMeshesFrameBuffer.getDepthBufferHandle());
        Gdx.gl.glBindFramebuffer(GL20.GL_FRAMEBUFFER, 0);

        picker.resize(width, height);

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

        cameraPreview.dispose();
        modelBatch.dispose();
        spriteBatch.dispose();
        picker.dispose();

        fillColorShader.dispose();

        shapeModelLoader.dispose();

        if (sceneFrameBuffer != null) {
            sceneFrameBuffer.dispose();
            sceneFrameBuffer = null;
        }

        if (tangoMeshesFrameBuffer != null) {
            tangoMeshesFrameBuffer.dispose();
            tangoMeshesFrameBuffer = null;
        }

        tangoMeshRenderer.dispose();

        // TODO: dispose models for mesh components.

        // TODO: dispose the model for the actor's axes.
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
        if (touchedActorNode != null) {
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
                touchedActorNode.translate(transformAxis, distance);
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
                touchedActorNode.rotate(transformAxis, degrees);
            } else if (scaleEnabled) {
                float delta = (Math.abs(deltaY) <= Math.abs(deltaX)) ? deltaX : -deltaY;
                delta *= SCALING_COEFF;
                touchedActorNode.scaleByExtent(delta);
            }
        }
        return false;
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        LOG.d("panStop: x = %f, y = %f, pointer = %d, button = %d");
        if (touchedActorNode != null) {
            if (translationEnabled && transformAxis != null) {
                touchedActorNode.savePositionToActor();
                listener.onActorChanged(touchedActorNode.actor);
            } else if (rotationEnabled && transformAxis != null) {
                touchedActorNode.saveOrientationToActor();
                listener.onActorChanged(touchedActorNode.actor);
            } else if (scaleEnabled) {
                touchedActorNode.saveScaleToActor();
                listener.onActorChanged(touchedActorNode.actor);
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

    public void updateTangoMeshes(@NonNull List<TangoMesh> tangoMeshes) {
        tangoMeshRenderer.update(tangoMeshes);
    }

    public void setDebugFrameBuffersVisible(boolean debugFrameBuffersVisible) {
        this.debugFrameBuffersVisible = debugFrameBuffersVisible;
    }

    public void setDebugTangoMeshesVisible(boolean debugTangoMeshesVisible) {
        this.debugTangoMeshesVisible = debugTangoMeshesVisible;
    }

    public void setDebugCameraPreviewVisible(boolean debugCameraPreviewVisible) {
        this.debugCameraPreviewVisible = debugCameraPreviewVisible;
    }

    public void addActorCursor(@NonNull Actor actor) {
        final AssetMeshComponent assetMeshComponent = actor.findComponent(AssetMeshComponent.class);
        if (assetMeshComponent != null) {
            final String assetId = assetMeshComponent.getRequiredAssetId();
            final String assetType = assetMeshComponent.getRequiredAssetType();

            LOG.v("Loading a model: assetId = %s, assetType = %s", assetId, assetType);

            assetLoader.load(Model.class, assetId, assetType, model -> {
                Gdx.app.postRunnable(() -> {
                    actorCursorInstance = new ActorCursorInstance(model, actor, camera);
                });
            }, e -> {
                LOG.e("Failed to load the model: assetId = %s, assetType = %s", assetId, assetType);
            });

            return;
        } else {
            final PrimitiveMeshComponent primitiveMeshComponent = actor.findComponent(PrimitiveMeshComponent.class);
            if (primitiveMeshComponent != null) {
                final Model model = shapeModelLoader.load(primitiveMeshComponent.getClass());
                actorCursorInstance = new ActorCursorInstance(model, actor, camera);
            }
        }
    }

    public void removeActorCursor() {
        actorCursorInstance = null;
    }

    public void addActor(@NonNull Actor actor) {
        final ActorNode actorNode = actorNodeFactory.create(actor);
        actorNodeMap.put(actor.getId(), actorNode);
    }

    public void removeActor(@NonNull Actor actor) {
        actorNodeMap.remove(actor.getId());
    }

    public void setTouchedActorLocked(boolean touchedActorObjectLocked) {
        this.touchedActorLocked = touchedActorObjectLocked;
    }

    public void setActorAxes(@Nullable Actor actor) {
        if (actor == null) {
            actorAxesInstance = null;
        } else {
            final ActorNode node = actorNodeMap.get(actor.getId());
            actorAxesInstance = new ActorAxesInstance(actorAxesModel, node);
        }
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
                    camera.setTangoCameraIntrinsics(intrinsics);
                    camera.resetTransform();
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
                        camera.setTangoPoseData(poseData);
                        camera.update();
                    } else if (poseData.statusCode == TangoPoseData.POSE_INVALID) {
                        LOG.v("The tango pose is invalid:  cameraColorTimestamp = %f, displayRotation = %d",
                              cameraColorTimestamp, displayRotation);
                    } else if (poseData.statusCode == TangoPoseData.POSE_UNKNOWN) {
                        LOG.d("The tango pose is unknown.");
                    }
                }
            }
        } catch (TangoException e) {
            LOG.e("A tango API call error occured.", e);
        }

        if (actorCursorInstance != null) {
            actorCursorInstance.update();
        }

        if (actorAxesInstance != null) {
            actorAxesInstance.update();
        }

        for (int i = 0; i < actorNodeMap.size(); i++) {
            final ActorNode actorNode = actorNodeMap.valueAt(i);
            actorNode.update();
        }
    }

    private void draw() {
        if (!cameraConfigured) return;

        // TODO: Do the frustum culling.
        visibleInstances.clear();
        for (int i = 0; i < actorNodeMap.size(); i++) {
            final ActorNode actorNode = actorNodeMap.valueAt(i);
            if (touchedActorNode == null || touchedActorNode != actorNode) {
                actorNode.collectVisibleInstances(visibleInstances);
            }
        }

        renderContext.begin();

        renderTangoMeshesDepth();

        // Draw the scene.
        renderScene();

        // Draw and handle the object picker.
        renderColorObjectPicker();

        renderContext.end();

        // Draw the frame buffer for the scene to the screen
        spriteBatch.begin();
        spriteBatch.disableBlending();
        final Texture texture = sceneFrameBuffer.getColorBufferTexture();
        spriteBatch.draw(texture,
                         0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight(),
                         0, 0, texture.getWidth(), texture.getHeight(),
                         false, true);
        spriteBatch.end();

        // Draw frame buffers for debug.
        if (debugFrameBuffersVisible) {
            renderDebugFrameBuffers();
        }
    }

    private void renderTangoMeshesDepth() {
        tangoMeshesFrameBuffer.begin();

        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        renderContext.setDepthMask(true);
        renderContext.setDepthTest(GL20.GL_LEQUAL);

        tangoMeshRenderer.renderDepth(camera);

        tangoMeshesFrameBuffer.end();
    }

    private void renderScene() {
        sceneFrameBuffer.begin();

        if (!debugCameraPreviewVisible) {
            final Color color = Color.SKY;
            Gdx.gl.glClearColor(color.r, color.g, color.b, color.a);
        }

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        renderContext.setDepthMask(false);
        renderContext.setDepthTest(0);

        // Draw the camera preview.
        if (debugCameraPreviewVisible) {
            cameraPreview.render();
        }

        if (debugTangoMeshesVisible) {
            tangoMeshRenderer.renderWireframe(camera);
        }

        renderContext.setDepthMask(true);
        renderContext.setDepthTest(GL20.GL_LEQUAL);

        // Draw models.
        modelBatch.begin(camera);
        modelBatch.render(visibleInstances, environment);
        if (touchedActorNode != null) {
            originalTouchedActorObjectScale.set(touchedActorNode.scale);
            touchedActorNode.scaleByExtent(0.1f);
            touchedActorNode.update();

            renderContext.setDepthMask(false);
            renderContext.setBlending(true, GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            // ORANGE: 0xffa500ff
            // Transparent 50%: 0xffa50088
            fillColorShader.color.set(0xffa50088);
            modelBatch.render(touchedActorNode.getRequiredMainComponentInstance(), environment, fillColorShader);

            touchedActorNode.scale.set(originalTouchedActorObjectScale);
            touchedActorNode.transformDirty = true;
            touchedActorNode.update();

            renderContext.setDepthMask(true);
            modelBatch.render(touchedActorNode.getRequiredMainComponentInstance(), environment);
        }
        modelBatch.end();

        if (actorCursorInstance != null) {
            Gdx.gl.glLineWidth(5);
            modelBatch.begin(camera);
            modelBatch.render(actorCursorInstance);
            modelBatch.end();
            Gdx.gl.glLineWidth(1);
        }

        if (actorAxesInstance != null) {
            modelBatch.begin(camera);
            modelBatch.render(actorAxesInstance);
            modelBatch.end();
        }

        sceneFrameBuffer.end();
    }

    private void renderColorObjectPicker() {
        pickableInstances.clear();

        if (actorCursorInstance != null) {
            pickableInstances.add(actorCursorInstance);
        } else {
            // In the non-cursor mode.
            for (int i = 0; i < actorNodeMap.size(); i++) {
                // TODO: optimize
                actorNodeMap.valueAt(i).collectVisibleInstances(pickableInstances);
            }
        }

        picker.begin(camera, renderContext);
        picker.render(pickableInstances);

        // Check a picked object.
        if (Gdx.input.justTouched()) {
            final ModelInstance instance = picker.pick(pickableInstances, Gdx.input.getX(), Gdx.input.getY());

            if (instance == null) {
                if (actorCursorInstance == null) {
                    if (!touchedActorLocked) {
                        touchedActorNode = null;
                        listener.onActorTouched(null);
                    }
                }
            } else if (instance instanceof ComponentInstance) {
                if (!touchedActorLocked) {
                    final ComponentInstance componentInstance = (ComponentInstance) instance;
                    touchedActorNode = componentInstance.node;
                    listener.onActorTouched(touchedActorNode.actor);
                }
            } else if (instance instanceof ActorCursorInstance) {
                final ActorCursorInstance object = (ActorCursorInstance) instance;

                object.saveTransform();
                listener.onActorCursorTouched(object.actor);

                removeActorCursor();
            } else {
                LOG.e("Detected an unexpected model instance.");
            }
        }

        picker.end();
    }

    private void renderDebugFrameBuffers() {
        spriteBatch.begin();
        spriteBatch.disableBlending();
        renderDebugColorBuffer(picker.getColorBufferTexture(), 0);
        renderDebugColorBuffer(tangoMeshesFrameBuffer.getColorBufferTexture(), 1);
        spriteBatch.enableBlending();
        spriteBatch.end();
    }

    private void renderDebugColorBuffer(@Nullable final Texture texture, final int index) {
        final int x = index * DEBUG_FRAME_BUFFER_VIEW_SIZE;
        if (texture != null) {
            // Flip the texture because its origin in OpenGL is the buttom left.
            spriteBatch.draw(texture,
                             x, 0, DEBUG_FRAME_BUFFER_VIEW_SIZE, DEBUG_FRAME_BUFFER_VIEW_SIZE,
                             0, 0, texture.getWidth(), texture.getHeight(),
                             false, true);
        }
    }

    public interface Listener {

        void onActorTouched(@Nullable Actor actor);

        void onActorChanged(@NonNull Actor actor);

        void onActorCursorTouched(@NonNull Actor actor);
    }

}
