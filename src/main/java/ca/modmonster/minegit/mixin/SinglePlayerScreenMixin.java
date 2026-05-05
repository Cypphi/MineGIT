package ca.modmonster.minegit.mixin;

import ca.modmonster.minegit.backport.ImageButton;
import ca.modmonster.minegit.data.Config;
import ca.modmonster.minegit.data.ConfigManager;
import ca.modmonster.minegit.data.GitManager;
import ca.modmonster.minegit.data.QuitState;
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

import java.util.Collections;
import java.util.List;

@Mixin(GuiWorldSelection.class)
public class SinglePlayerScreenMixin extends GuiScreen {
    @Unique
    private String mineGIT$cloneButtonTooltip;

    @Shadow
    private GuiListWorldSelection selectionList;

    @Unique
    private ImageButton mineGIT$cloneButton;

    @Unique
    private ImageButton mineGIT$worldSyncButton;

    @Unique
    private List<String> mineGIT$worldSyncButtonTooltip;

    @Unique
    private WorldSyncButtonState mineGIT$worldSyncButtonState = WorldSyncButtonState.SETUP;

    @Unique
    private WorldSummary mineGIT$hoveredLevel;

    @Unique
    private boolean mineGIT$prevAltState = false;

    @Inject(at = @At("TAIL"), method = "initGui")
	private void initGui(CallbackInfo info) {
        mineGIT$cloneButtonTooltip = I18n.format("minegit.clone.title");

        // Add world sync button
        mineGIT$worldSyncButton = new ImageButton(100, width / 2 - 178, height - 52, ImageButton.ImageButtonTex.CLOUD);
        mineGIT$worldSyncButton.enabled = false;
        addButton(mineGIT$worldSyncButton);

        // Add clone button
        mineGIT$cloneButton = new ImageButton(101, width / 2 - 178, height - 28, ImageButton.ImageButtonTex.CLONE);
        addButton(mineGIT$cloneButton);

        mineGIT$hoveredLevel = null;
        mineGIT$updateWorldSyncButton();
	}

    @Inject(at = @At("TAIL"), method = "actionPerformed")
    protected void actionPerformed(GuiButton button, CallbackInfo ci) {
        if (button.id == 100) {
            if (mineGIT$worldSyncButtonState == WorldSyncButtonState.SETUP || isAltKeyDown()) {
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
        } else if (button.id == 101) {
            mc.displayGuiScreen(new CloneScreen(() -> {
                if (selectionList != null) mineGIT$returnToScreen();
                mineGIT$updateWorldSyncButton();
            }));
        }
    }

    @Inject(at = @At("TAIL"), method = "drawScreen")
    public void drawScreen(int i, int j, float f, CallbackInfo ci) {
        if (mineGIT$cloneButton != null && mineGIT$cloneButton.isMouseOver()) drawHoveringText(mineGIT$cloneButtonTooltip, i, j);
        if (mineGIT$worldSyncButtonTooltip != null && mineGIT$worldSyncButton != null &&  mineGIT$worldSyncButton.isMouseOver()) drawHoveringText(mineGIT$worldSyncButtonTooltip, i, j);

        if (mineGIT$worldSyncButton != null && isAltKeyDown() != mineGIT$prevAltState) {
            mineGIT$prevAltState = isAltKeyDown();

            if (isAltKeyDown()) {
                QuitState.altQuit = true;
                mineGIT$worldSyncButton.enabled = true;
                mineGIT$worldSyncButton.texture = ImageButton.ImageButtonTex.CLOUD;
                mineGIT$worldSyncButtonTooltip = Collections.singletonList(I18n.format("minegit.link.setup.open"));
            } else {
                QuitState.altQuit = false;
                mineGIT$updateWorldSyncButton();
            }
        }
    }

    @Inject(at = @At("TAIL"), method = "selectWorld")
    private void selectWorld(GuiListWorldSelectionEntry entry, CallbackInfo ci) {
        if (mineGIT$worldSyncButton == null) return;
        mineGIT$hoveredLevel = ((WorldListEntryAccessor) entry).getSummary();
        mineGIT$updateWorldSyncButton();
    }

    @Unique
    private void mineGIT$updateWorldSyncButton() {
        if (mineGIT$worldSyncButton == null) return;
        if (isAltKeyDown()) return;
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

        mineGIT$worldSyncButton.texture = mineGIT$worldSyncButtonState.texture;
        mineGIT$worldSyncButtonTooltip = mineGIT$worldSyncButtonState.getTooltip();
        if (mineGIT$cloneButton != null) mineGIT$cloneButton.enabled = mineGIT$worldSyncButtonState != WorldSyncButtonState.SETUP;
    }

    @Unique
    private void mineGIT$returnToScreen() {
        mc.displayGuiScreen(this);
    }
}