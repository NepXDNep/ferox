package me.olliem5.ferox.impl.modules.movement;

import me.olliem5.ferox.api.module.Category;
import me.olliem5.ferox.api.module.FeroxModule;
import me.olliem5.ferox.api.module.Module;
import me.olliem5.ferox.api.setting.Setting;
import me.olliem5.pace.annotation.PaceHandler;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemPotion;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import org.lwjgl.input.Keyboard;

/**
 * @author olliem5
 * @author novola
 */

@FeroxModule(name = "NoSlow", description = "Prevents using items from slowing you down", category = Category.MOVEMENT)
public final class NoSlow extends Module {
    public static final Setting<NoSlowModes> noSlowMode = new Setting<>("Mode", "The way no item slowdown is achieved", NoSlowModes.Normal);

    public static final Setting<Boolean> guiMove = new Setting<>("GUI Move", "Allows you to move in GUI's", true);
    public static final Setting<Boolean> arrowKeyLook = new Setting<>(guiMove, "Arrow Key Look", "Allows you to look around with the arrow keys", true);

    public NoSlow() {
        this.addSettings(
                noSlowMode,
                guiMove
        );
    }

    private boolean sneaking;

    public void onUpdate() {
        if (nullCheck()) return;

        if (noSlowMode.getValue() == NoSlowModes.Bypass) {
            Item item = mc.player.getActiveItemStack().getItem();

            if (sneaking && ((!mc.player.isHandActive() && item instanceof ItemFood || item instanceof ItemBow || item instanceof ItemPotion) || (!(item instanceof ItemFood) || !(item instanceof ItemBow) || !(item instanceof ItemPotion)))) {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
                sneaking = false;
            }
        }

        if (guiMove.getValue()) {
            if (mc.currentScreen != null && !(mc.currentScreen instanceof GuiChat) && guiMove.getValue()) {
                if (arrowKeyLook.getValue()) {
                    if (Keyboard.isKeyDown(200)) {
                        mc.player.rotationPitch -= 5;
                    }

                    if (Keyboard.isKeyDown(208)) {
                        mc.player.rotationPitch += 5;
                    }

                    if (Keyboard.isKeyDown(205)) {
                        mc.player.rotationYaw += 5;
                    }

                    if (Keyboard.isKeyDown(203)) {
                        mc.player.rotationYaw -= 5;
                    }

                    if (mc.player.rotationPitch > 90) {
                        mc.player.rotationPitch = 90;
                    }

                    if (mc.player.rotationPitch < -90) {
                        mc.player.rotationPitch = -90;
                    }
                }
            }
        }
    }

    @PaceHandler
    public void onUseItem(LivingEntityUseItemEvent event) {
        if (nullCheck()) return;

        if (noSlowMode.getValue() == NoSlowModes.Bypass) {
            if (!sneaking) {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
                sneaking = true;
            }
        }
    }

    @PaceHandler
    public void onInputUpdate(InputUpdateEvent event) {
        if (nullCheck()) return;

        if (noSlowMode.getValue() == NoSlowModes.Normal) {
            if (mc.player.isHandActive() && !mc.player.isRiding()) {
                event.getMovementInput().moveStrafe *= 5;
                event.getMovementInput().moveForward *= 5;
            }
        }
    }

    public String getArraylistInfo() {
        return noSlowMode.getValue().toString();
    }

    public enum NoSlowModes {
        Normal,
        Bypass
    }
}
