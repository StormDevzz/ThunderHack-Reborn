package thunder.hack.utility.render.animation;

public class CaptureMark {
    private static float espValue = 1f, prevEspValue;
    private static float espSpeed = 1f;
    private static boolean flipSpeed;

    public static void render(Object target) {
        // stubbed for 1.21.9
    }

    public static void tick() {
        prevEspValue = espValue;
        espValue += espSpeed;
        if (espSpeed > 25) flipSpeed = true;
        if (espSpeed < -25) flipSpeed = false;
        espSpeed = flipSpeed ? espSpeed - 0.5f : espSpeed + 0.5f;
    }
}
