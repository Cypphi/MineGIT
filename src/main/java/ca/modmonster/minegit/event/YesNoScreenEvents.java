package ca.modmonster.minegit.event;

import ca.modmonster.minegit.data.GitManager;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class YesNoScreenEvents {
    @SubscribeEvent
    public static void onGuiInit(GuiScreenEvent.InitGuiEvent.Post event) {
        if (!(event.getGui() instanceof GuiYesNo)) return;
        GuiYesNo gui = (GuiYesNo) event.getGui();
        String message = ObfuscationReflectionHelper.getPrivateValue(GuiYesNo.class, gui, "messageLine1");
        if (!message.equals(I18n.format("selectWorld.deleteQuestion"))) return;

        GitManager.makeWritable(gui.mc, SinglePlayerScreenEvents.hoveredLevel.getFileName());
    }
}
