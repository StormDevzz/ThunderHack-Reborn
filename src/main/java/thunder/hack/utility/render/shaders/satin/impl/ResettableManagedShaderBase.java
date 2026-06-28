package thunder.hack.utility.render.shaders.satin.impl;

import net.minecraft.util.Identifier;

public abstract class ResettableManagedShaderBase<S> {
    public ResettableManagedShaderBase(Identifier location) {
    }

    public void initializeOrLog(net.minecraft.resource.ResourceFactory mgr) {
    }

    public boolean isInitialized() {
        return false;
    }

    public void setup(int newWidth, int newHeight) {
    }

    public Identifier getLocation() {
        return null;
    }
}
