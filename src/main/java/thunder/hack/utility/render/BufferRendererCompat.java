package thunder.hack.utility.render;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BuiltBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;

public class BufferRendererCompat {
    public static void draw(BuiltBuffer built) {
        draw(built, VertexFormat.DrawMode.QUADS);
    }

    public static void draw(BuiltBuffer built, VertexFormat.DrawMode mode) {
        if (built == null) return;
        built.close();
    }

    public static void endDraw(BufferBuilder builder) {
        BuiltBuffer built = builder.endNullable();
        if (built != null) built.close();
    }
}
