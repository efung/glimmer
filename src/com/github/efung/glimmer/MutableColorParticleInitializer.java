package com.github.efung.glimmer;

import org.andengine.entity.IEntity;
import org.andengine.entity.particle.initializer.ColorParticleInitializer;
import org.andengine.util.color.Color;

public class MutableColorParticleInitializer<T extends IEntity> extends ColorParticleInitializer<T>
{
    public MutableColorParticleInitializer(final Color pColor)
    {
        super(pColor);
    }

    public MutableColorParticleInitializer(final float pRed, final float pGreen, final float pBlue)
    {
        super(pRed, pGreen, pBlue);
    }

    public MutableColorParticleInitializer(final Color pMinColor, final Color pMaxColor)
    {
        super(pMinColor, pMaxColor);
    }

    public MutableColorParticleInitializer(final float pMinRed, final float pMaxRed, final float pMinGreen, final float pMaxGreen, final float pMinBlue, final float pMaxBlue)
    {
        super(pMinRed, pMaxRed, pMinGreen, pMaxGreen, pMinBlue, pMaxBlue);
    }

    public void setColor(final Color pColor)
    {
        setColor(pColor, pColor);
    }

    public void setColor(final Color pMinColor, final Color pMaxColor)
    {
        setColor(pMinColor.getRed(), pMaxColor.getRed(),
                pMinColor.getGreen(), pMaxColor.getGreen(),
                pMinColor.getBlue(), pMaxColor.getBlue());
    }

    public void setColor(final float pMinRed, final float pMaxRed, final float pMinGreen, final float pMaxGreen, final float pMinBlue, final float pMaxBlue)
    {
        this.mMinValue = pMinRed;
        this.mMaxValue = pMaxRed;
        this.mMinValueB = pMinGreen;
        this.mMaxValueB = pMaxGreen;
        this.mMinValueC = pMinBlue;
        this.mMaxValueC = pMaxBlue;
    }
}
