package com.github.efung.glimmer;

import org.andengine.entity.particle.BatchedSpriteParticleSystem;
import org.andengine.entity.particle.emitter.IParticleEmitter;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;


public class GlimmerParticleSystem extends BatchedSpriteParticleSystem
{
    private float mRateMaximum;

    public GlimmerParticleSystem(IParticleEmitter pParticleEmitter, float pRateMinimum, float pRateMaximum, int pParticlesMaximum, ITextureRegion pTextureRegion, VertexBufferObjectManager pVertexBufferObjectManager)
    {
        super(pParticleEmitter, pRateMinimum, pRateMaximum, pParticlesMaximum, pTextureRegion,
                pVertexBufferObjectManager);
        this.mRateMaximum = pRateMaximum;
    }

    public GlimmerParticleSystem(float pX, float pY, IParticleEmitter pParticleEmitter, float pRateMinimum, float pRateMaximum, int pParticlesMaximum, ITextureRegion pTextureRegion, VertexBufferObjectManager pVertexBufferObjectManager)
    {
        super(pX, pY, pParticleEmitter, pRateMinimum, pRateMaximum, pParticlesMaximum, pTextureRegion,
                pVertexBufferObjectManager);
        this.mRateMaximum = pRateMaximum;
    }

    @Override
    protected float determineCurrentRate() {
        // Stop particle spawning if we're close to maximum. Once we reach mParticlesMaximum,
        // we won't spawn any more, but dueToSpawn gets incremented, so whenever existing
        // particles expire, there will always be more due, so we end up in a repeated,
        // pulsing mode. That's my theory, anyway.
        if (this.mParticlesAlive == this.mParticlesMaximum)
        {
            return 0f;
        }
        return super.determineCurrentRate();
    }

}
