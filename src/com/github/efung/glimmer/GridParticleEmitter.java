package com.github.efung.glimmer;

import android.util.FloatMath;
import org.andengine.entity.particle.emitter.BaseRectangleParticleEmitter;
import org.andengine.util.math.MathUtils;

import java.util.Arrays;
import java.util.Collections;

import static org.andengine.util.Constants.VERTEX_INDEX_X;
import static org.andengine.util.Constants.VERTEX_INDEX_Y;

public class GridParticleEmitter extends BaseRectangleParticleEmitter
{
    protected float mGridWidth;
    protected float mGridHeight;
    protected int mGridTilesX;
    protected int mGridTilesY;
    protected int mGridTiles;
    private boolean mRandomFill = true;
    private float mGridCoordsX[];
    private float mGridCoordsY[];
    private Integer mIndices[];
    private int mCurrentIndex = 0;

    public GridParticleEmitter(final float pCenterX, final float pCenterY, final float pWidth, final float pHeight, final float pGridWidth, final float pGridHeight) {
        this(pCenterX, pCenterY, pWidth, pHeight, pGridWidth, pGridHeight, true);
    }

    public GridParticleEmitter(final float pCenterX, final float pCenterY, final float pWidth, final float pHeight, final float pGridWidth, final float pGridHeight, final boolean pRandomFill) {
        super(pCenterX, pCenterY, pWidth, pHeight);
        this.mGridWidth = pGridWidth;
        this.mGridHeight = pGridHeight;
        this.mGridTilesX = Math.round(FloatMath.floor(pWidth / pGridWidth));
        this.mGridTilesY = Math.round(FloatMath.floor(pHeight / pGridHeight));
        this.mGridTiles = this.mGridTilesX * this.mGridTilesY;
        this.mRandomFill = pRandomFill;

        this.mGridCoordsX = new float[this.mGridTiles];
        this.mGridCoordsY = new float[this.mGridTiles];
        this.mIndices = new Integer[this.mGridTiles];

        for (int i = 0; i < mGridTilesX; i++)
        {
            for (int j = 0; j < mGridTilesY; j++)
            {
                this.mGridCoordsX[this.mGridTilesY * i + j] = this.mCenterX - this.mWidthHalf + i * this.mGridWidth;
                this.mGridCoordsY[this.mGridTilesY * i + j] = this.mCenterY - this.mHeightHalf + j * this.mGridHeight;
                this.mIndices[this.mGridTilesY * i + j] = this.mGridTilesY * i + j;
            }
        }

        shuffleArray(this.mIndices);
    }

    @Override
    public void getPositionOffset(final float[] pOffset) {
        if (this.mRandomFill)
        {
            final int randomIndex = MathUtils.RANDOM.nextInt(this.mGridCoordsX.length);
            pOffset[VERTEX_INDEX_X] = this.mGridCoordsX[randomIndex];
            pOffset[VERTEX_INDEX_Y] = this.mGridCoordsY[randomIndex];
        }
        else
        {
            pOffset[VERTEX_INDEX_X] = this.mGridCoordsX[this.mIndices[this.mCurrentIndex]];
            pOffset[VERTEX_INDEX_Y] = this.mGridCoordsY[this.mIndices[this.mCurrentIndex]];

            this.mCurrentIndex++;

            if (this.mCurrentIndex == this.mIndices.length)
            {
                shuffleArray(this.mIndices);
                this.mCurrentIndex = 0;
            }
        }
    }

    private void shuffleArray(Integer pArray[])
    {
        Collections.shuffle(Arrays.asList(pArray));
    }

    public int getGridTiles()
    {
        return mGridTiles;
    }
}
