package me.olliem5.ferox.api.hud;

import git.littledraily.eventsystem.Listener;
import git.littledraily.eventsystem.event.Priority;
import me.olliem5.ferox.api.traits.Minecraft;
import me.olliem5.ferox.impl.events.GameOverlayRenderEvent;
import me.olliem5.ferox.impl.hud.InventoryComponent;
import me.olliem5.ferox.impl.hud.PlayerComponent;
import me.olliem5.ferox.impl.hud.WatermarkComponent;
import me.olliem5.ferox.impl.hud.WelcomerComponent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HudManager implements Minecraft {
    private static List<HudComponent> components = new ArrayList<>();

    public static void init() {
        components.addAll(Arrays.asList(
                new WelcomerComponent(),
                new InventoryComponent(),
                new WatermarkComponent(),
                new PlayerComponent()
        ));
    }

    public static List<HudComponent> getComponents() {
        return components;
    }

    @Listener(priority = Priority.LOWEST)
    public void onGameOverlayRender(GameOverlayRenderEvent event) {
        if (mc.world == null || mc.player == null) return;

        for (HudComponent hudComponent : components) {
            if (hudComponent.isVisible()) {
                hudComponent.render();
            }
        }
    }
}
