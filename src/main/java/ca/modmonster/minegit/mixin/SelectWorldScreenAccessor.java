package ca.modmonster.minegit.mixin;

import net.minecraft.client.gui.GuiListWorldSelection;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.GuiWorldSelection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GuiWorldSelection.class)
public interface SelectWorldScreenAccessor {
    @Accessor("selectionList")
    GuiListWorldSelection getLevelList();

    @Accessor("field_212352_g")
    GuiTextField getEditBox();
}
