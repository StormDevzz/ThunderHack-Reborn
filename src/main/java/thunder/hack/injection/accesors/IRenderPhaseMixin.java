package thunder.hack.injection.accesors;

// RenderPhase was removed in 1.21.11, this accessor is disabled
public interface IRenderPhaseMixin {
    default String getName() {
        return "";
    }
}
