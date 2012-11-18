package com.github.efung.nexus4wp;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.particle.BatchedSpriteParticleSystem;
import org.andengine.entity.particle.initializer.ColorParticleInitializer;
import org.andengine.entity.particle.modifier.AlphaParticleModifier;
import org.andengine.entity.particle.modifier.ExpireParticleInitializer;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.UncoloredSprite;
import org.andengine.extension.ui.livewallpaper.BaseLiveWallpaperService;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.bitmap.BitmapTextureFormat;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.util.math.MathUtils;

public class Nexus4LiveWallpaper extends BaseLiveWallpaperService implements ITimerCallback
{
    private static final int CAMERA_WIDTH = 480;
    private static final int CAMERA_HEIGHT = 800;

    private static final float DURATION = 10.0f;

    private Camera mCamera;
    private BitmapTextureAtlas mBitmapTextureAtlas;
    private ITextureRegion mParticleTextureRegion;
    private BatchedSpriteParticleSystem mParticleSystem;

    private float mCurrentR, mCurrentG, mCurrentB;
    private ColorParticleInitializer<UncoloredSprite> mColorParticleInitializer;

    @Override
    public EngineOptions onCreateEngineOptions()
    {
        this.mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);

        return new EngineOptions(true, ScreenOrientation.PORTRAIT_FIXED, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), this.mCamera);
    }

    @Override
    public void onCreateResources(OnCreateResourcesCallback pOnCreateResourcesCallback) throws
            Exception
    {
        BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
        this.mBitmapTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 22, 22, BitmapTextureFormat.RGB_565, TextureOptions.BILINEAR);
        this.mParticleTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "particle.png", 0, 0);
        this.mBitmapTextureAtlas.load();
        pOnCreateResourcesCallback.onCreateResourcesFinished();
    }

    @Override
    public void onCreateScene(OnCreateSceneCallback pOnCreateSceneCallback) throws Exception
    {
        this.mEngine.registerUpdateHandler(new TimerHandler(3.0f, true, this));
        final Scene scene = new Scene();

        final GridParticleEmitter particleEmitter = new GridParticleEmitter(CAMERA_WIDTH * 0.5f,  CAMERA_HEIGHT * 0.5f, CAMERA_WIDTH, CAMERA_HEIGHT, 16, 16);
        final int maxParticles = particleEmitter.getGridTilesX() * particleEmitter.getGridTilesY();
        this.mParticleSystem = new BatchedSpriteParticleSystem(particleEmitter, maxParticles * 0.25f, maxParticles * 0.25f, maxParticles,
                this.mParticleTextureRegion, this.getVertexBufferObjectManager());

        this.mCurrentR = MathUtils.RANDOM.nextFloat();
        this.mCurrentG = MathUtils.RANDOM.nextFloat();
        this.mCurrentB = MathUtils.RANDOM.nextFloat();
        this.mColorParticleInitializer = new ColorParticleInitializer<UncoloredSprite>(this.mCurrentR, this.mCurrentG, this.mCurrentB);
        this.mParticleSystem.addParticleInitializer(this.mColorParticleInitializer);
        this.mParticleSystem.addParticleInitializer(new ExpireParticleInitializer<UncoloredSprite>(DURATION));

        float hsv[] = new float[3];
        hsv[0] = MathUtils.RANDOM.nextFloat() * 360f;
        hsv[1] = 0.5f;
        hsv[2] = 0.5f;
//            int packedARGB1 = android.graphics.Color.HSVToColor(hsv);
//            int r1 = android.graphics.Color.red(packedARGB1);
//            int g1 = android.graphics.Color.green(packedARGB1);
//            int b1 = android.graphics.Color.blue(packedARGB1);

        this.mParticleSystem.addParticleModifier(new AlphaParticleModifier<UncoloredSprite>(0, DURATION / 2, 0, 1));
        this.mParticleSystem.addParticleModifier(new AlphaParticleModifier<UncoloredSprite>(DURATION / 2, DURATION, 1, 0));

        scene.attachChild(this.mParticleSystem);

        pOnCreateSceneCallback.onCreateSceneFinished(scene);
    }

    @Override
    public void onPopulateScene(Scene pScene, OnPopulateSceneCallback pOnPopulateSceneCallback) throws Exception
    {
        pOnPopulateSceneCallback.onPopulateSceneFinished();
    }

    @Override
    public void onTimePassed(TimerHandler pTimerHandler)
    {
        this.mCurrentR = MathUtils.RANDOM.nextFloat();
        this.mCurrentG = MathUtils.RANDOM.nextFloat();
        this.mCurrentB = MathUtils.RANDOM.nextFloat();
        ColorParticleInitializer<UncoloredSprite> initializer = new ColorParticleInitializer<UncoloredSprite>(this.mCurrentR, this.mCurrentG, this.mCurrentB);

        this.mParticleSystem.addParticleInitializer(initializer);
        this.mParticleSystem.removeParticleInitializer(this.mColorParticleInitializer);
        this.mColorParticleInitializer = initializer;
    }
}
