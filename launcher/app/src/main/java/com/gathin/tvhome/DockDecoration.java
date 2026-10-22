package com.gathin.tvhome;

import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/** Draws the tvOS style lighter band behind the first (dock) row and adds a gap below it. */
public class DockDecoration extends RecyclerView.ItemDecoration {

    private final int dockSize;
    private final float density;
    private final Paint band = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint edge = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint topEdge = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint baseScrim = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();

    /** Extra band below the tiles for their names (always_show_names), in px. */
    private final float namesRoom;
    /** Light glass like the top bar's pills (config.xml dock_glass): a white tint that fades downwards. */
    private final boolean lightGlass;
    /** The band's top and bottom the glass gradient was made for; it is remade when they move. */
    private float shaderTop = Float.NaN, shaderBottom = Float.NaN;

    /**
     * @param translucent the cinematic shelf's band: the artwork and the glow show through it.
     * @param names       the dock tiles show their names, so the band reaches down over them.
     * @param opacity     the plain band's opacity in percent (dock_opacity), so a wallpaper can show through it.
     * @param lightGlass  a light frosted band like the pills, for a sky or wallpaper behind it.
     */
    DockDecoration(int dockSize, float density, boolean translucent, boolean names, int opacity, boolean lightGlass) {
        this.dockSize = dockSize;
        this.density = density;
        this.namesRoom = names ? 28 * density : 0;
        this.lightGlass = lightGlass;
        baseScrim.setColor(0x990A1622); // Deep translucent frosted glass base for crystal clear text legibility
        band.setColor(translucent ? 0x8C121014 : (Math.round(opacity * 2.55f) << 24) | 0x141416);
        edge.setStyle(Paint.Style.STROKE);
        edge.setStrokeWidth(density);
        edge.setColor(lightGlass ? 0x4AFFFFFF : translucent ? 0x26FFFFFF : 0x1FFFFFFF);
        topEdge.setStyle(Paint.Style.STROKE);
        topEdge.setStrokeWidth(1.8f * density);
        topEdge.setColor(0x8AFFFFFF);
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent,
                               @NonNull RecyclerView.State state) {
        int pos = parent.getChildAdapterPosition(view);
        if (pos >= 0 && pos < dockSize) {
            outRect.top = (int) (6 * density);
            outRect.bottom = (int) (12 * density);
        }
    }

    @Override
    public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        boolean any = false;
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            int pos = parent.getChildAdapterPosition(child);
            if (pos < 0 || pos >= dockSize) continue;
            RecyclerView.ViewHolder vh = parent.getChildViewHolder(child);
            View card = vh instanceof AppAdapter.Holder ? ((AppAdapter.Holder) vh).card : child.findViewById(R.id.card);
            if (card == null) continue;
            float l = child.getLeft() + card.getLeft();
            float t = child.getTop() + card.getTop() + child.getTranslationY();
            float r = l + card.getWidth();
            float b = t + card.getHeight();
            if (!any) {
                rect.set(l, t, r, b);
                any = true;
            } else {
                rect.union(l, t, r, b);
            }
        }
        if (!any) return;
        float pad = 14 * density;
        float sidePad = 22 * density;
        float side = Math.max(0f, Math.min(sidePad, Math.min(rect.left, parent.getWidth() - rect.right) - density));
        rect.inset(-side, -pad);
        rect.bottom += namesRoom;
        float radius = 26 * density;
        if (lightGlass && (rect.top != shaderTop || rect.bottom != shaderBottom)) {
            band.setShader(new LinearGradient(0, rect.top, 0, rect.bottom, 0x48FFFFFF, 0x14FFFFFF,
                    Shader.TileMode.CLAMP));
            shaderTop = rect.top;
            shaderBottom = rect.bottom;
        }
        if (lightGlass) {
            c.drawRoundRect(rect, radius, radius, baseScrim);
        }
        c.drawRoundRect(rect, radius, radius, band);
        c.drawRoundRect(rect, radius, radius, edge);
        if (lightGlass) {
            c.save();
            c.clipRect(rect.left, rect.top, rect.right, rect.top + 14 * density);
            c.drawRoundRect(rect, radius, radius, topEdge);
            c.restore();
        }
    }

}
