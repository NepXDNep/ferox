package me.olliem5.ferox.impl.modules.combat;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.olliem5.ferox.api.module.Category;
import me.olliem5.ferox.api.module.FeroxModule;
import me.olliem5.ferox.api.module.Module;
import me.olliem5.ferox.api.setting.NumberSetting;
import me.olliem5.ferox.api.setting.Setting;
import me.olliem5.ferox.api.util.client.MessageUtil;
import me.olliem5.ferox.api.util.player.InventoryUtil;
import me.olliem5.ferox.api.util.player.PlayerUtil;
import me.olliem5.ferox.api.util.player.TargetUtil;
import me.olliem5.ferox.api.util.render.draw.RenderUtil;
import me.olliem5.pace.annotation.PaceHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import org.lwjgl.opengl.GL11;

import java.awt.*;

/**
 * @author olliem5
 *
 * TODO: Fix placing 2 webs, maybe add it as other mode
 * TODO: Move off target if they have a web
 */

@FeroxModule(name = "AutoWeb", description = "Places webs at your or enemies feet", category = Category.COMBAT)
public final class AutoWeb extends Module {
    public static final Setting<TargetModes> targetMode = new Setting<>("Target", "The target to go for when placing webs", TargetModes.Self);
    public static final NumberSetting<Double> targetRange = new NumberSetting<>("Target Range", "The range for a target to be found", 1.0, 4.4, 10.0, 1);

    public static final Setting<Boolean> renderPlace = new Setting<>("Render", "Allows the web placements to be rendered", true);
    public static final Setting<RenderModes> renderMode = new Setting<>(renderPlace, "Render Mode", "The type of box to render", RenderModes.Full);
    public static final NumberSetting<Double> outlineWidth = new NumberSetting<>(renderPlace, "Outline Width", "The width of the outline", 1.0, 2.0, 5.0, 1);
    public static final Setting<Color> renderColour = new Setting<>(renderPlace, "Render Colour", "The colour for the web placements", new Color(15, 60, 231, 201));

    public AutoWeb() {
        this.addSettings(
                targetMode,
                targetRange,
                renderPlace
        );
    }

    private int webSlot;

    private BlockPos renderBlock = null;
    private EntityPlayer webTarget = null;

    @Override
    public void onEnable() {
        if (nullCheck()) return;

        webSlot = InventoryUtil.getHotbarBlockSlot(Blocks.WEB);

        if (webSlot == -1) {
            MessageUtil.sendClientMessage("No Webs, " + ChatFormatting.RED + "Disabling!");
            this.toggle();
        }
    }

    @Override
    public void onDisable() {
        if (nullCheck()) return;

        renderBlock = null;
        webTarget = null;
    }

    public void onUpdate() {
        if (nullCheck()) return;

        if (targetMode.getValue() == TargetModes.Self) {
            webTarget = mc.player;
        } else {
            webTarget = TargetUtil.getClosestPlayer(targetRange.getValue());
        }

        if (webTarget != null) {
            if (!hasWeb(webTarget)) {
                final int oldInventorySlot = mc.player.inventory.currentItem;

                if (webSlot != -1) {
                    mc.player.inventory.currentItem = webSlot;
                }

                if (mc.player.getHeldItemMainhand().getItem() == Item.getItemFromBlock(Blocks.WEB)) {
                    mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(new BlockPos(PlayerUtil.getCenter(webTarget.posX, webTarget.posY, webTarget.posZ)), EnumFacing.UP, EnumHand.MAIN_HAND, 0, 0, 0));
                }

                renderBlock = new BlockPos(PlayerUtil.getCenter(webTarget.posX, webTarget.posY, webTarget.posZ));

                mc.player.inventory.currentItem = oldInventorySlot;
            }
        }
    }

    private boolean hasWeb(EntityPlayer entityPlayer) {
        return mc.world.getBlockState(entityPlayer.getPosition()).getBlock() == Blocks.WEB;
    }

    @PaceHandler
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (nullCheck()) return;

        GL11.glLineWidth(outlineWidth.getValue().floatValue());

        if (renderPlace.getValue()) {
            if (renderBlock != null) {
                switch (renderMode.getValue()) {
                    case Box:
                        RenderUtil.draw(renderBlock, true, false, 0, 0, renderColour.getValue());
                        break;
                    case Outline:
                        RenderUtil.draw(renderBlock, false, true, 0, 0, renderColour.getValue());
                        break;
                    case Full:
                        RenderUtil.draw(renderBlock, true, true, 0, 0, renderColour.getValue());
                        break;
                }
            }
        }
    }

    public String getArraylistInfo() {
        if (webTarget != null) {
            return webTarget.getName();
        }

        return "";
    }

    public enum TargetModes {
        Self,
        Enemy
    }

    public enum RenderModes {
        Box,
        Outline,
        Full
    }
}
