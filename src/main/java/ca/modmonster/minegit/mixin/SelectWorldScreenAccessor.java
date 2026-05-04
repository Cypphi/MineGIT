package ca.modmonster.minegit.mixin;

import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.screen.world.WorldSelectionList;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SelectWorldScreen.class)
public interface SelectWorldScreenAccessor {
    @Accessor("worldList")
    WorldSelectionList getLevelList();

    @Accessor("f_33034138")
    TextFieldWidget getEditBox();
}
