package com.gathin.tvhome;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

/**
 * A translucent glass slab: soft sheen fill, bright top specular, crisp rim and a faint outer glow.
 * One instance floats over the grid and is moved with spring animations between tiles.
 */
public class GlassView extends View {

    private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint specular = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bottomSheen = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint rim = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint glow = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private final float density;
    private float radius;
    /** Glow colour as 0xRRGGBB: Frutiger Aero cyan by default, or an app's/festival's accent when tinted. */
    private int glowRgb = 0x00E5FF;
    /** Glow strength: luminous cyan aura for skeuomorphic depth. */
    private int glowAlpha = 0x60;

    public GlassView(Context context, AttributeSet attrs) {
        super(context, attrs);
        density = getResources().getDisplayMetrics().density;
        radius = 20f * density;
        rim.setStyle(Paint.Style.STROKE);
        rim.setStrokeWidth(1.6f * density);
        rim.setColor(0xEEFFFFFF);
        glow.setStyle(Paint.Style.STROKE);
        // Cache the slab as a texture. The spring only changes translation, so the GPU can
        // move it without replaying clipPath and the glow on every frame. That path is
        // software-rendered on the MT5895 and is what makes the highlight crawl.
        setLayerType(LAYER_TYPE_HARDWARE, null);
    }

    /** Tints the rim and the outer glow; 0 restores the Frutiger Aero cyan glow. */
    void setAccent(int argb) {
        if (argb == 0) {
            glowRgb = 0x00E5FF;
            glowAlpha = 0x60;
            rim.setColor(0xEEFFFFFF);
        } else {
            glowRgb = argb & 0xFFFFFF;
            glowAlpha = 0x75;
            // The rim is the accent lifted towards white, so the edge still reads as glass, not paint.
            int r = (((glowRgb >> 16) & 0xFF) + 255 * 2) / 3, g = (((glowRgb >> 8) & 0xFF) + 255 * 2) / 3,
                    b = ((glowRgb & 0xFF) + 255 * 2) / 3;
            rim.setColor(0xE6000000 | (r << 16) | (g << 8) | b);
        }
        invalidate();
    }

    void setRadius(float px) {
        radius = px;
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        fill.setShader(new LinearGradient(0, 0, 0, h, 0x48FFFFFF, 0x14FFFFFF, Shader.TileMode.CLAMP));
        specular.setShader(new LinearGradient(0, 0, 0, h * 0.48f, 0x8CFFFFFF, 0x00FFFFFF, Shader.TileMode.CLAMP));
        bottomSheen.setShader(new LinearGradient(0, h * 0.72f, 0, h, 0x00FFFFFF, 0x2AFFFFFF, Shader.TileMode.CLAMP));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float inset = 6f * density;   // room for the glow
        rect.set(inset, inset, getWidth() - inset, getHeight() - inset);

        // Outer glow: thin rings across the inset, each a little fainter, so it fades out smoothly.
        int rings = 12;
        float step = inset / rings;
        glow.setStrokeWidth(step + 0.5f);
        for (int i = 0; i < rings; i++) {
            float o = step * (i + 0.5f);
            float t = 1f - (float) i / rings;
            glow.setColor(Math.round(glowAlpha * t) << 24 | glowRgb);
            canvas.drawRoundRect(rect.left - o, rect.top - o, rect.right + o, rect.bottom + o, radius + o, radius + o, glow);
        }

        canvas.drawRoundRect(rect, radius, radius, fill);

        // Top glossy specular reflection sheen (upper 48%)
        canvas.save();
        canvas.clipRect(rect.left, rect.top, rect.right, rect.top + rect.height() * 0.48f);
        canvas.drawRoundRect(rect, radius, radius, specular);
        canvas.restore();

        // Bottom glass bounce reflection (lower 28%)
        canvas.save();
        canvas.clipRect(rect.left, rect.top + rect.height() * 0.72f, rect.right, rect.bottom);
        canvas.drawRoundRect(rect, radius, radius, bottomSheen);
        canvas.restore();

        canvas.drawRoundRect(rect, radius, radius, rim);
    }

}
