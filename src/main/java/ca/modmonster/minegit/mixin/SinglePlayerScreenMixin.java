package ca.modmonster.minegit.mixin;

import ca.modmonster.minegit.data.Config;
import ca.modmonster.minegit.data.ConfigManager;
import ca.modmonster.minegit.data.GitManager;
import ca.modmonster.minegit.gui.AccountLinkScreen;
import ca.modmonster.minegit.gui.CloneScreen;
import ca.modmonster.minegit.gui.EnableWorldSyncScreen;
import ca.modmonster.minegit.widget.WorldSyncButtonState;
import net.minecraft.client.gui.*;
import net.minecraft.client.resources.I18n;
import net.minecraft.world.storage.WorldSummary;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;
import java.util.List;

@Mixin(GuiWorldSelection.class)
public class SinglePlayerScreenMixin extends GuiScreen {
    @Unique
    private String mineGIT$cloneButtonTooltip;

    @Shadow
    private GuiListWorldSelection selectionList;

    @Shadow
    protected GuiTextField field_212352_g;

    @Unique
    private GuiButton mineGIT$cloneButton;

    @Unique
    private GuiButton mineGIT$worldSyncButton;

    @Unique
    private List<String> mineGIT$worldSyncButtonTooltip;

    @Unique
    private WorldSyncButtonState mineGIT$worldSyncButtonState = WorldSyncButtonState.SETUP;

    @Unique
    private WorldSummary mineGIT$hoveredLevel;

    @Unique
    private boolean mineGIT$altHeld;

    @Inject(at = @At("TAIL"), method = "initGui", remap = false)
	private void initGui(CallbackInfo info) {
        mineGIT$cloneButtonTooltip = I18n.format("minegit.clone.title");

        // Add world sync button
        mineGIT$worldSyncButton = new GuiButton(children.size(), width / 2 - 178, height - 52, 20, 20, "☁") {
            @Override
            public void onClick(double mouseX, double mouseY) {
                if (mineGIT$worldSyncButtonState == WorldSyncButtonState.SETUP || mineGIT$altHeld) {
                    mineGIT$altHeld = false;
                    mc.displayGuiScreen(new AccountLinkScreen(SinglePlayerScreenMixin.this, () -> {
                        if (selectionList != null) mineGIT$returnToScreen();
                        mineGIT$updateWorldSyncButton();
                    }));
                } else if (mineGIT$worldSyncButtonState == WorldSyncButtonState.ENABLE) {
                    if (mineGIT$hoveredLevel != null)
                        mc.displayGuiScreen(new EnableWorldSyncScreen(SinglePlayerScreenMixin.this, mineGIT$hoveredLevel, () -> {
                            if (selectionList != null) mineGIT$returnToScreen();
                            mineGIT$updateWorldSyncButton();
                        }));
                }
            }
        };
        mineGIT$worldSyncButton.enabled = false;
        addButton(mineGIT$worldSyncButton);

        // Add clone button
        mineGIT$cloneButton = new GuiButton(children.size(), width / 2 - 178, height - 28, 20, 20, "↓") {
            @Override
            public void onClick(double mouseX, double mouseY) {
                mc.displayGuiScreen(new CloneScreen(() -> {
                    if (selectionList != null) mineGIT$returnToScreen();
                    mineGIT$updateWorldSyncButton();
                }));
            }
        };
        addButton(mineGIT$cloneButton);

        mineGIT$updateWorldSyncButton();
	}

    @Inject(at = @At("TAIL"), method = "render", remap = false)
    public void render(int i, int j, float f, CallbackInfo ci) {
        if (mineGIT$cloneButton != null && mineGIT$cloneButton.isMouseOver()) drawHoveringText(mineGIT$cloneButtonTooltip, i, j);
        if (mineGIT$worldSyncButtonTooltip != null && mineGIT$worldSyncButton != null &&  mineGIT$worldSyncButton.isMouseOver()) drawHoveringText(mineGIT$worldSyncButtonTooltip, i, j);
    }

    @Inject(at = @At("TAIL"), method = "selectWorld", remap = false)
    private void selectWorld(GuiListWorldSelectionEntry entry, CallbackInfo ci) {
        if (mineGIT$worldSyncButton == null) return;
        mineGIT$hoveredLevel = ((WorldListEntryAccessor) (Object) entry).getSummary();
        mineGIT$updateWorldSyncButton();
    }

    @Unique
    private void mineGIT$updateWorldSyncButton() {
        if (mineGIT$worldSyncButton == null) return;
        if (mineGIT$altHeld) return;
        Config config = ConfigManager.getCurrentConfig();
        if (config.username.replace(" ", "").isEmpty() || config.getPat().replace(" ", "").isEmpty()) {
            // Set the world sync button to configuration state
            mineGIT$worldSyncButtonState = WorldSyncButtonState.SETUP;
            this.mineGIT$worldSyncButton.enabled = true;
        } else if (mineGIT$hoveredLevel != null && GitManager.syncEnabled(mc, mineGIT$hoveredLevel.getFileName())) {
            mineGIT$worldSyncButtonState = WorldSyncButtonState.WORLD_CONFIGURE;
            this.mineGIT$worldSyncButton.enabled = false;
        } else {
            mineGIT$worldSyncButtonState = WorldSyncButtonState.ENABLE;
            this.mineGIT$worldSyncButton.enabled = mineGIT$hoveredLevel != null;
        }

        mineGIT$worldSyncButton.displayString = mineGIT$worldSyncButtonState.message;
        mineGIT$worldSyncButtonTooltip = mineGIT$worldSyncButtonState.getTooltip();
        if (mineGIT$cloneButton != null) mineGIT$cloneButton.enabled = mineGIT$worldSyncButtonState != WorldSyncButtonState.SETUP;
    }

    @Inject(at = @At("HEAD"), method = "keyPressed")
    public void keyPressed(int i, int j, int k, CallbackInfoReturnable<Boolean> cir) {
        if (i == 342) {
            mineGIT$altHeld = true;
            if (mineGIT$worldSyncButton != null) {
                mineGIT$worldSyncButton.enabled = true;
                mineGIT$worldSyncButton.displayString = "☁";
                mineGIT$worldSyncButtonTooltip = Collections.singletonList(I18n.format("minegit.link.setup.open"));
            }
        }
    }

    @Override
    public boolean keyReleased(int i, int j, int k) {
        if (i == 342) {
            mineGIT$altHeld = false;
            mineGIT$updateWorldSyncButton();
        }
        return super.keyReleased(i, j, k);
    }

    @Unique
    private void mineGIT$returnToScreen() {
        GuiListWorldSelection list = ((SelectWorldScreenAccessor) this).getLevelList();
        ((WorldSelectionListInvoker) list).invokeReloadWorldList(() -> field_212352_g.getText(), true);
        mc.displayGuiScreen(this);
    }
}