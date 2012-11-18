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

    private static final float PARTICLE_LIFETIME = 10.0f;

    private Camera mCamera;
    private BitmapTextureAtlas mBitmapTextureAtlas;
    private ITextureRegion mParticleTextureRegion;
    private BatchedSpriteParticleSystem mParticleSystem;

    private float mCurrentR, mCurrentG, mCurrentB;
    private float hsv[] = new float[3];
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
        this.mEngine.registerUpdateHandler(new TimerHandler(PARTICLE_LIFETIME, true, this));
        final Scene scene = new Scene();

        final GridParticleEmitter particleEmitter = new GridParticleEmitter(CAMERA_WIDTH * 0.5f,  CAMERA_HEIGHT * 0.5f, CAMERA_WIDTH, CAMERA_HEIGHT, 16, 16);
        final int maxParticles = particleEmitter.getGridTilesX() * particleEmitter.getGridTilesY();
        this.mParticleSystem = new BatchedSpriteParticleSystem(particleEmitter, maxParticles * 0.20f, maxParticles * 0.20f, maxParticles,
                this.mParticleTextureRegion, this.getVertexBufferObjectManager());

        this.mCurrentR = MathUtils.RANDOM.nextFloat();
        this.mCurrentG = MathUtils.RANDOM.nextFloat();
        this.mCurrentB = MathUtils.RANDOM.nextFloat();
        this.mColorParticleInitializer = new ColorParticleInitializer<UncoloredSprite>(this.mCurrentR, this.mCurrentG, this.mCurrentB);
        this.mParticleSystem.addParticleInitializer(this.mColorParticleInitializer);
        this.mParticleSystem.addParticleInitializer(new ExpireParticleInitializer<UncoloredSprite>(PARTICLE_LIFETIME));

        this.mParticleSystem.addParticleModifier(new AlphaParticleModifier<UncoloredSprite>(0, PARTICLE_LIFETIME / 2, 0f, 1f));
        this.mParticleSystem.addParticleModifier(new AlphaParticleModifier<UncoloredSprite>(PARTICLE_LIFETIME / 2,
                PARTICLE_LIFETIME, 1f, 0f));

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
        int r = Math.round(this.mCurrentR * 255f);
        int g = Math.round(this.mCurrentG * 255f);
        int b = Math.round(this.mCurrentB * 255f);
        android.graphics.Color.RGBToHSV(r, g, b, hsv);

        // Perturb values by hue, avoiding overly dark or bright colours
        hsv[0] = MathUtils.bringToBounds(0.0f, 360.f, hsv[0] + MathUtils.randomSign() * 20.0f);
        hsv[1] = MathUtils.bringToBounds(0.3f, 0.7f, hsv[1] + MathUtils.randomSign() * 0.1f);
        hsv[2] = MathUtils.bringToBounds(0.3f, 0.7f, hsv[2] + MathUtils.randomSign() * 0.1f);
        int packedARGB = android.graphics.Color.HSVToColor(hsv);

        this.mCurrentR = android.graphics.Color.red(packedARGB) / 255f;
        this.mCurrentG = android.graphics.Color.green(packedARGB) / 255f;
        this.mCurrentB = android.graphics.Color.blue(packedARGB) / 255f;

        ColorParticleInitializer<UncoloredSprite> initializer = new ColorParticleInitializer<UncoloredSprite>(this.mCurrentR, this.mCurrentG, this.mCurrentB);

        this.mParticleSystem.addParticleInitializer(initializer);
        this.mParticleSystem.removeParticleInitializer(this.mColorParticleInitializer);
        this.mColorParticleInitializer = initializer;
    }
}
