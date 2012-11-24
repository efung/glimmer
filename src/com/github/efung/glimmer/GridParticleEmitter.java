package com.github.efung.glimmer;

import android.util.FloatMath;
import org.andengine.entity.particle.emitter.BaseRectangleParticleEmitter;
import org.andengine.util.math.MathUtils;

import static org.andengine.util.Constants.VERTEX_INDEX_X;
import static org.andengine.util.Constants.VERTEX_INDEX_Y;

public class GridParticleEmitter extends BaseRectangleParticleEmitter
{
    protected float mGridWidth;
    protected float mGridHeight;
    protected int mGridTilesX;
    protected int mGridTilesY;

    public GridParticleEmitter(final float pCenterX, final float pCenterY, final float pWidth, final float pHeight, final float pGridWidth, final float pGridHeight) {
        super(pCenterX, pCenterY, pWidth, pHeight);
        this.mGridWidth = pGridWidth;
        this.mGridHeight = pGridHeight;
        this.mGridTilesX = Math.round(FloatMath.floor(pWidth / pGridWidth));
        this.mGridTilesY = Math.round(FloatMath.floor(pHeight / pGridHeight));
    }

    @Override
    public void getPositionOffset(final float[] pOffset) {
        pOffset[VERTEX_INDEX_X] = this.mCenterX - this.mWidthHalf + MathUtils.RANDOM.nextInt(this.mGridTilesX) * this.mGridWidth;
        pOffset[VERTEX_INDEX_Y] = this.mCenterY - this.mHeightHalf + MathUtils.RANDOM.nextInt(this.mGridTilesY) * this.mGridHeight;
    }

    public int getGridTilesY()
    {
        return mGridTilesY;
    }

    public int getGridTilesX()
    {
        return mGridTilesX;
    }

// ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}
