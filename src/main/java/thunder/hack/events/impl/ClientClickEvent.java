package thunder.hack.events.impl;

import net.minecraft.text.ClickEvent;

/*When using a clickable text client, you should create this object instead of the usual ClickEvent.
If not, a vulnerability could occur as mentioned in this GitHub issue: https://github.com/MeteorDevelopment/meteor-client/pull/4399.*/
public class ClientClickEvent implements ClickEvent {
    private final Action action;
    private final String value;

    public ClientClickEvent(Action action, String value) {
        this.action = action;
        this.value = value;
    }

    public Action getAction() {
        return action;
    }

    public String getValue() {
        return value;
    }
}