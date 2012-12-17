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
        final int gridTilesX = Math.round(FloatMath.floor(pWidth / pGridWidth));
        final int gridTilesY = Math.round(FloatMath.floor(pHeight / pGridHeight));
        this.mGridTiles = gridTilesX * gridTilesY;
        this.mRandomFill = pRandomFill;

        this.mGridCoordsX = new float[this.mGridTiles];
        this.mGridCoordsY = new float[this.mGridTiles];
        this.mIndices = new Integer[this.mGridTiles];

        for (int i = 0; i < gridTilesX; i++)
        {
            for (int j = 0; j < gridTilesY; j++)
            {
                this.mGridCoordsX[gridTilesY * i + j] = this.mCenterX - this.mWidthHalf + i * pGridWidth;
                this.mGridCoordsY[gridTilesY * i + j] = this.mCenterY - this.mHeightHalf + j * pGridHeight;
                this.mIndices[gridTilesY * i + j] = gridTilesY * i + j;
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
