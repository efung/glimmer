package com.github.efung.glimmer;

import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import org.andengine.engine.LimitedFPSEngine;
import org.andengine.engine.camera.Camera;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.particle.BatchedSpriteParticleSystem;
import org.andengine.entity.particle.Particle;
import org.andengine.entity.particle.initializer.ColorParticleInitializer;
import org.andengine.entity.particle.initializer.RotationParticleInitializer;
import org.andengine.entity.particle.modifier.AlphaParticleModifier;
import org.andengine.entity.particle.modifier.ExpireParticleInitializer;
import org.andengine.entity.particle.modifier.IParticleModifier;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.UncoloredSprite;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.entity.util.AverageFPSCounter;
import org.andengine.extension.ui.livewallpaper.BaseLiveWallpaperService;
import org.andengine.input.sensor.SensorDelay;
import org.andengine.input.sensor.orientation.IOrientationListener;
import org.andengine.input.sensor.orientation.OrientationData;
import org.andengine.input.sensor.orientation.OrientationSensorOptions;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.bitmap.BitmapTextureFormat;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.util.HorizontalAlign;
import org.andengine.util.color.Color;
import org.andengine.util.color.ColorUtils;
import org.andengine.util.math.MathUtils;

public class GlimmerLiveWallpaper extends BaseLiveWallpaperService
{
    private static final int CAMERA_WIDTH = 480;
    private static final int CAMERA_HEIGHT = 800;

    private static final float PARTICLE_LIFETIME = 9.0f;
    private static final int MAX_FRAMES_PER_SECOND = 24;

    private Camera mCamera;
    private BitmapTextureAtlas mBitmapTextureAtlas;
    private ITextureRegion mParticleTextureRegion;
    private BatchedSpriteParticleSystem mParticleSystem;

    // Prefs
    private int mMode;
    private float mColourChangePeriod;
    private int mDotSize;
    private int mStaticColour; // ARGB packed
    private boolean mShowFps;

    private float mCurrentPitch; // rotation around X-axis, screen's horizontal axis (tilting forward and backward)
    private float mCurrentRoll; // rotation around Y-axis, screen's vertical axis (tilting left and right)
    private Font mFont;


    @Override
    public EngineOptions onCreateEngineOptions()
    {
        this.mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
        return new EngineOptions(true, ScreenOrientation.PORTRAIT_FIXED, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), this.mCamera);
    }

    @Override
    public org.andengine.engine.Engine onCreateEngine(final EngineOptions pEngineOptions)
    {
        return new LimitedFPSEngine(pEngineOptions, MAX_FRAMES_PER_SECOND);
//        return new org.andengine.engine.Engine(pEngineOptions);
    }

    @Override
    public void onCreateResources(OnCreateResourcesCallback pOnCreateResourcesCallback) throws Exception
    {
        BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
        this.mBitmapTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 32, 32, BitmapTextureFormat.RGB_565, TextureOptions.BILINEAR);

        this.mFont = FontFactory.create(this.getFontManager(), this.getTextureManager(), 256, 256,
                TextureOptions.BILINEAR, Typeface.DEFAULT, 24, android.graphics.Color.WHITE);
        this.mFont.load();

        pOnCreateResourcesCallback.onCreateResourcesFinished();
    }

    @Override
    public void onCreateScene(OnCreateSceneCallback pOnCreateSceneCallback) throws Exception
    {
        final Scene scene = new Scene();
        pOnCreateSceneCallback.onCreateSceneFinished(scene);
    }

    @Override
    public void onPopulateScene(Scene pScene, OnPopulateSceneCallback pOnPopulateSceneCallback) throws Exception
    {
        pOnPopulateSceneCallback.onPopulateSceneFinished();
    }

    @Override
    public void onPauseGame()
    {
        super.onPauseGame();

        disableSensors();
    }

    @Override
    public void onResumeGame()
    {
        final boolean settingsChanged = readSettingsFromPreferences();

        if (mMode == GlimmerPreferenceActivity.PREFS_MODE_REFLECT_LIGHT)
        {
            enableSensors();
        }

        if (settingsChanged)
        {
            resetScene();
            buildScene(this.mEngine.getScene());
        }

        super.onResumeGame();
    }

    private boolean readSettingsFromPreferences()
    {
        boolean settingsChanged = false;
        int temp;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GlimmerLiveWallpaper.this);

        temp = Integer.valueOf(prefs.getString(this.getString(R.string.prefs_key_mode),
                String.valueOf(GlimmerPreferenceActivity.PREFS_MODE_DEFAULT)));
        if (this.mMode != temp)
        {
            this.mMode = temp;
            settingsChanged = true;
        }

        temp = Integer.valueOf(prefs.getString(this.getString(R.string.prefs_key_dot_size),
                String.valueOf(GlimmerPreferenceActivity.PREFS_DOT_SIZE_DEFAULT)));
        if (this.mDotSize != temp)
        {
            this.mDotSize = temp;
            settingsChanged = true;
        }

        temp = prefs.getInt(this.getString(R.string.prefs_key_colour_change_period), GlimmerPreferenceActivity.PREFS_COLOUR_CHANGE_PERIOD_DEFAULT);
        if (this.mColourChangePeriod != temp)
        {
            this.mColourChangePeriod = temp;
            settingsChanged = true;
        }

        temp = prefs.getInt(this.getString(R.string.prefs_key_single_colour), GlimmerPreferenceActivity.PREFS_SINGLE_COLOUR_DEFAULT);
        if (this.mStaticColour != temp)
        {
            this.mStaticColour = temp;
            settingsChanged = true;
        }

        boolean showFps = prefs.getBoolean(this.getString(R.string.prefs_key_display_fps), GlimmerPreferenceActivity.PREFS_SHOW_FPS_DEFAULT);
        if (this.mShowFps != showFps)
        {
            this.mShowFps = showFps;
            settingsChanged = true;
        }

        return settingsChanged;
    }

    private String getParticleFilename(final int prefsDotSize)
    {
        switch (prefsDotSize)
        {
            case GlimmerPreferenceActivity.PREFS_DOT_SIZE_L:
                return "nexusdot_l.png";
            case GlimmerPreferenceActivity.PREFS_DOT_SIZE_M:
            default:
                return "nexusdot_m.png";
            case GlimmerPreferenceActivity.PREFS_DOT_SIZE_S:
                return "nexusdot_s.png";
        }
    }

    private void loadParticleImage()
    {
        this.mParticleTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas,
                this, getParticleFilename(this.mDotSize), 0, 0);
        this.mBitmapTextureAtlas.load();
    }

    private void unloadParticleImage()
    {
        this.mBitmapTextureAtlas.unload();
        this.mParticleTextureRegion = null;
    }

    private void enableSensors()
    {
        this.mEngine.enableOrientationSensor(this, new IOrientationListener()
        {
            @Override
            public void onOrientationChanged(OrientationData pOrientationData)
            {
                GlimmerLiveWallpaper.this.mCurrentPitch = pOrientationData.getPitch();
                GlimmerLiveWallpaper.this.mCurrentRoll = pOrientationData.getRoll();
            }

            @Override
            public void onOrientationAccuracyChanged(OrientationData pOrientationData)
            {
            }
        },
                new OrientationSensorOptions(SensorDelay.GAME) // NORMAL makes it stutter, but GAME uses more battery...
        );
    }

    private void disableSensors()
    {
        this.mEngine.disableOrientationSensor(this);
    }

    private Color getRandomColor()
    {
        return new Color(MathUtils.RANDOM.nextInt(255) / 255f, MathUtils.RANDOM.nextInt(255) / 255f, MathUtils.RANDOM.nextInt(255) / 255f);
    }

    private void resetScene()
    {
        this.mEngine.clearUpdateHandlers();
        this.mEngine.getScene().detachChildren();
        unloadParticleImage();
    }

    private void buildScene(final Scene scene)
    {
        loadParticleImage();

        switch (mMode)
        {
            case GlimmerPreferenceActivity.PREFS_MODE_CHANGE_COLOUR:
            default:
                buildColourChangeScene(scene);
                break;
            case GlimmerPreferenceActivity.PREFS_MODE_REFLECT_LIGHT:
                buildReflectLightScene(scene);
                break;
            case GlimmerPreferenceActivity.PREFS_MODE_STATIC_COLOUR:
                buildStaticColourScene(scene);
                break;

        }

        if (mShowFps)
        {
            buildFpsDisplay(scene);
        }
    }

    private void buildFpsDisplay(final Scene scene)
    {
        final int FPS_X = CAMERA_WIDTH;
        final int FPS_Y = 600; // Above icon bar
        final Text fpsText = new Text(FPS_X, FPS_Y, this.mFont, "0.0", 8, new TextOptions(HorizontalAlign.RIGHT), this.getVertexBufferObjectManager());
        fpsText.setPosition(FPS_X - fpsText.getWidth(), FPS_Y);
        scene.attachChild(fpsText);

        this.mEngine.registerUpdateHandler( new AverageFPSCounter(5.0f)
        {
            @Override
            protected void onHandleAverageDurationElapsed(float pFPS)
            {
                fpsText.setText(String.valueOf(Math.round(pFPS * 10) / 10f)); // Round to one decimal
                fpsText.setPosition(FPS_X - fpsText.getWidth(), FPS_Y); // Bottom right corner
            }
        });
    }

    private void buildReflectLightScene(final Scene scene)
    {
        final GridParticleEmitter particleEmitter = new GridParticleEmitter(CAMERA_WIDTH * 0.5f,  CAMERA_HEIGHT * 0.5f, CAMERA_WIDTH, CAMERA_HEIGHT,
                this.mParticleTextureRegion.getWidth(), this.mParticleTextureRegion.getHeight(), false);
        final int maxParticles = particleEmitter.getGridTilesX() * particleEmitter.getGridTilesY();
        this.mParticleSystem = new BatchedSpriteParticleSystem(particleEmitter, maxParticles, maxParticles, maxParticles,
                this.mParticleTextureRegion, this.getVertexBufferObjectManager());

        this.mParticleSystem.addParticleInitializer(new ColorParticleInitializer<UncoloredSprite>(ColorUtils.convertARGBPackedIntToColor(
                mStaticColour)));
        this.mParticleSystem.addParticleInitializer(new RotationParticleInitializer<UncoloredSprite>(-90f, 90f));

        this.mParticleSystem.addParticleModifier(new IParticleModifier<UncoloredSprite>()
        {
            @Override
            public void onUpdateParticle(Particle<UncoloredSprite> pParticle)
            {
                UncoloredSprite sprite = pParticle.getEntity();
                sprite.setAlpha(getAlphaFromRotation(sprite.getRotation(), GlimmerLiveWallpaper.this.mCurrentPitch + GlimmerLiveWallpaper.this.mCurrentRoll));
            }

            @Override
            public void onInitializeParticle(Particle<UncoloredSprite> pParticle)
            {
                UncoloredSprite sprite = pParticle.getEntity();
                sprite.setAlpha(getAlphaFromRotation(sprite.getRotation(), 0));
            }


            private float getAlphaFromRotation(final float pParticleRotation, float pCombinedTilt)
            {
                // The closer the particle angle is to the tilt angle, the brighter it is.
                // Can't really see screen if tilt is more than 45 degrees, so we clamp it.
                // Maximum difference will be 180 degrees, so normalize then convert to alpha.
                pCombinedTilt = MathUtils.bringToBounds(-90f, 90f, pCombinedTilt);
                return MathUtils.bringToBounds(0.2f, 1f, 1f - Math.abs(pParticleRotation - pCombinedTilt) / 180f);
            }
        });

        scene.attachChild(this.mParticleSystem);
    }

    private void buildStaticColourScene(final Scene scene)
    {
        final GridParticleEmitter particleEmitter = new GridParticleEmitter(CAMERA_WIDTH * 0.5f,  CAMERA_HEIGHT * 0.5f, CAMERA_WIDTH, CAMERA_HEIGHT,
                this.mParticleTextureRegion.getWidth(), this.mParticleTextureRegion.getHeight(), false);
        final int maxParticles = particleEmitter.getGridTilesX() * particleEmitter.getGridTilesY();
        this.mParticleSystem = new BatchedSpriteParticleSystem(particleEmitter, maxParticles / PARTICLE_LIFETIME + 1, maxParticles / PARTICLE_LIFETIME - 1, maxParticles,
                this.mParticleTextureRegion, this.getVertexBufferObjectManager());

        ColorParticleInitializer<UncoloredSprite> colorParticleInitializer = new ColorParticleInitializer<UncoloredSprite>(ColorUtils.convertARGBPackedIntToColor(
                mStaticColour));
        this.mParticleSystem.addParticleInitializer(colorParticleInitializer);
        this.mParticleSystem.addParticleInitializer(new RotationParticleInitializer<UncoloredSprite>(-90f, 90f));
        this.mParticleSystem.addParticleInitializer(new ExpireParticleInitializer<UncoloredSprite>(PARTICLE_LIFETIME));

        this.mParticleSystem.addParticleModifier(new AlphaParticleModifier<UncoloredSprite>(0, PARTICLE_LIFETIME * 0.6666f, 0.3f, 1f));
        this.mParticleSystem.addParticleModifier(
                new AlphaParticleModifier<UncoloredSprite>(PARTICLE_LIFETIME * 0.6666f, PARTICLE_LIFETIME, 1f, 0.3f));

        scene.attachChild(this.mParticleSystem);
    }

    private void buildColourChangeScene(final Scene scene)
    {
        final GridParticleEmitter particleEmitter = new GridParticleEmitter(CAMERA_WIDTH * 0.5f,  CAMERA_HEIGHT * 0.5f, CAMERA_WIDTH, CAMERA_HEIGHT,
                this.mParticleTextureRegion.getWidth(), this.mParticleTextureRegion.getHeight(), false);
        final int maxParticles = particleEmitter.getGridTilesX() * particleEmitter.getGridTilesY();
        this.mParticleSystem = new BatchedSpriteParticleSystem(particleEmitter, maxParticles / PARTICLE_LIFETIME + 1, maxParticles / PARTICLE_LIFETIME - 1, maxParticles,
                this.mParticleTextureRegion, this.getVertexBufferObjectManager());

        Color initialColor = getRandomColor();
        MutableColorParticleInitializer<UncoloredSprite> colorParticleInitializer = new MutableColorParticleInitializer<UncoloredSprite>(initialColor);
        this.mParticleSystem.addParticleInitializer(colorParticleInitializer);
        this.mParticleSystem.addParticleInitializer(new RotationParticleInitializer<UncoloredSprite>(-90f, 90f));
        this.mParticleSystem.addParticleInitializer(new ExpireParticleInitializer<UncoloredSprite>(PARTICLE_LIFETIME));

        this.mParticleSystem.addParticleModifier(new AlphaParticleModifier<UncoloredSprite>(0, PARTICLE_LIFETIME * 0.6666f, 0.3f, 1f));
        this.mParticleSystem.addParticleModifier(new AlphaParticleModifier<UncoloredSprite>(PARTICLE_LIFETIME * 0.6666f, PARTICLE_LIFETIME, 1f, 0.3f));

        this.mEngine.registerUpdateHandler(new TimerHandler(mColourChangePeriod, true,
                new ChangingColorParticleInitializerTimerHandler(this.mParticleSystem, initialColor,
                        colorParticleInitializer)));

        scene.attachChild(this.mParticleSystem);
    }

    private class ChangingColorParticleInitializerTimerHandler implements ITimerCallback
    {
        private int mCurrentParticleColorARGB;
        private float hsv[] = new float[3];
        private MutableColorParticleInitializer<UncoloredSprite> mCurrentParticleInitializer;
        private BatchedSpriteParticleSystem mParticleSystem;

        public ChangingColorParticleInitializerTimerHandler(final BatchedSpriteParticleSystem pParticleSystem, final Color pColor, final MutableColorParticleInitializer<UncoloredSprite> pInitializer)
        {
            this.mParticleSystem = pParticleSystem;
            this.mCurrentParticleInitializer = pInitializer;
            this.mCurrentParticleColorARGB = pColor.getARGBPackedInt();
        }

        @Override
        public void onTimePassed(TimerHandler pTimerHandler)
        {
            android.graphics.Color.colorToHSV(this.mCurrentParticleColorARGB, hsv);

            // Perturb values by hue, avoiding overly dark or bright colours
            hsv[0] += MathUtils.randomSign() * (MathUtils.RANDOM.nextFloat() * 20.0f + 10.0f); // between 10 and 30 degrees
            if (hsv[0] > 360f) {
                hsv[0] -= 360f;
            }
            else if (hsv[0] < 0f) {
                hsv[0] += 360f;
            }
            hsv[1] = MathUtils.bringToBounds(0.4f, 0.7f, hsv[1] + MathUtils.randomSign() * 0.1f);
            hsv[2] = MathUtils.bringToBounds(0.4f, 0.7f, hsv[2] + MathUtils.randomSign() * 0.1f);

            this.mCurrentParticleColorARGB = android.graphics.Color.HSVToColor(hsv);

            final float r = android.graphics.Color.red(this.mCurrentParticleColorARGB) / 255f;
            final float g = android.graphics.Color.green(this.mCurrentParticleColorARGB) / 255f;
            final float b = android.graphics.Color.blue(this.mCurrentParticleColorARGB) / 255f;

            this.mCurrentParticleInitializer.setColor(r, r, g, g, b, b);
        }
    }
}
