package ca.modmonster.minegit.event;

import ca.modmonster.minegit.data.Config;
import ca.modmonster.minegit.data.ConfigManager;
import ca.modmonster.minegit.data.GitManager;
import ca.modmonster.minegit.data.SyncResult;
import ca.modmonster.minegit.gui.*;
import ca.modmonster.minegit.widget.WorldSyncButtonState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.resources.I18n;
import net.minecraft.world.storage.WorldSummary;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.util.Collections;
import java.util.List;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class SinglePlayerScreenEvents {
    private static String cloneButtonTooltip;
    private static GuiButton cloneButton;
    private static GuiButton worldSyncButton;
    private static List<String> worldSyncButtonTooltip;
    private static WorldSyncButtonState worldSyncButtonState = WorldSyncButtonState.SETUP;
    public static WorldSummary hoveredLevel;
    private static boolean altHeld;
    private static String lastSelectedWorldId = null;
    private static boolean showGitBeforeJoin = true;
    private static GuiListWorldSelection list;
    private static GuiTextField searchBox;

    @SubscribeEvent
	public static void onGuiInit(GuiScreenEvent.InitGuiEvent.Post event) {
        if (!(event.getGui() instanceof GuiWorldSelection)) return;
        GuiWorldSelection gui = (GuiWorldSelection) event.getGui();
        Minecraft minecraft = gui.mc;
        list = ObfuscationReflectionHelper.getPrivateValue(GuiWorldSelection.class, gui, "selectionList");
        searchBox = ObfuscationReflectionHelper.getPrivateValue(GuiWorldSelection.class, gui, "field_212352_g");
        cloneButtonTooltip = I18n.format("minegit.clone.title");

        // Add world sync button
        worldSyncButton = new GuiButton(gui.getChildren().size(), gui.width / 2 - 178, gui.height - 52, 20, 20, "☁") {
            @Override
            public void onClick(double mouseX, double mouseY) {
                if (worldSyncButtonState == WorldSyncButtonState.SETUP || altHeld) {
                    altHeld = false;
                    minecraft.displayGuiScreen(new AccountLinkScreen(gui, () -> {
                        if (list != null) returnToScreen(gui, searchBox, list);
                        updateWorldSyncButton(gui.mc);
                    }));
                } else if (worldSyncButtonState == WorldSyncButtonState.ENABLE) {
                    if (hoveredLevel != null) minecraft.displayGuiScreen(new EnableWorldSyncScreen(gui, hoveredLevel, () -> {
                        if (list != null) returnToScreen(gui, searchBox, list);
                        updateWorldSyncButton(gui.mc);
                    }));
                }
            }
        };
        worldSyncButton.enabled = false;
        event.addButton(worldSyncButton);

        // Add clone button
        cloneButton = new GuiButton(gui.getChildren().size(), gui.width / 2 - 178, gui.height - 28, 20, 20, "↓") {
            @Override
            public void onClick(double mouseX, double mouseY) {
                minecraft.displayGuiScreen(new CloneScreen(() -> {
                    if (list != null) returnToScreen(gui, searchBox, list);
                    updateWorldSyncButton(gui.mc);
                }));
            }
        };
        event.addButton(cloneButton);

        updateWorldSyncButton(gui.mc);
	}

    @SubscribeEvent
    public static void onDrawScreen(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (!(event.getGui() instanceof GuiWorldSelection)) return;
        GuiWorldSelection gui = (GuiWorldSelection) event.getGui();
        int i = event.getMouseX();
        int j = event.getMouseY();

        if (cloneButton != null && cloneButton.isMouseOver()) gui.drawHoveringText(cloneButtonTooltip, i, j);
        if (worldSyncButtonTooltip != null && worldSyncButton != null &&  worldSyncButton.isMouseOver()) gui.drawHoveringText(worldSyncButtonTooltip, i, j);

        // Detect when a new world is clicked
        if (list != null) {
            GuiListWorldSelectionEntry entry = list.getSelectedWorld();

            if (entry != null) {
                WorldSummary summary = ObfuscationReflectionHelper.getPrivateValue(GuiListWorldSelectionEntry.class, entry, "worldSummary");
                String worldId = summary.getFileName();

                if (!worldId.equals(lastSelectedWorldId)) {
                    lastSelectedWorldId = worldId;
                    onSelectWorld(gui.mc, summary);
                }
            }
        }
    }

    private static void onSelectWorld(Minecraft mc, WorldSummary summary) {
        if (worldSyncButton == null) return;
        hoveredLevel = summary;
        updateWorldSyncButton(mc);
    }

    private static void updateWorldSyncButton(Minecraft mc) {
        if (worldSyncButton == null) return;
        if (altHeld) return;
        Config config = ConfigManager.getCurrentConfig();
        if (config.username.replace(" ", "").isEmpty() || config.getPat().replace(" ", "").isEmpty()) {
            // Set the world sync button to configuration state
            worldSyncButtonState = WorldSyncButtonState.SETUP;
            worldSyncButton.enabled = true;
        } else if (hoveredLevel != null && GitManager.syncEnabled(mc, hoveredLevel.getFileName())) {
            worldSyncButtonState = WorldSyncButtonState.WORLD_CONFIGURE;
            worldSyncButton.enabled = false;
        } else {
            worldSyncButtonState = WorldSyncButtonState.ENABLE;
            worldSyncButton.enabled = hoveredLevel != null;
        }

        worldSyncButton.displayString = worldSyncButtonState.message;
        worldSyncButtonTooltip = worldSyncButtonState.getTooltip();
        if (cloneButton != null) cloneButton.enabled = worldSyncButtonState != WorldSyncButtonState.SETUP;
    }

    @SubscribeEvent
    public static void keyPressed(GuiScreenEvent.KeyboardKeyPressedEvent.Pre event) {
        if (!(event.getGui() instanceof GuiWorldSelection)) return;
        if (event.getKeyCode() == 342) {
            altHeld = true;
            if (worldSyncButton != null) {
                worldSyncButton.enabled = true;
                worldSyncButton.displayString = "☁";
                worldSyncButtonTooltip = Collections.singletonList(I18n.format("minegit.link.setup.open"));
            }
        }
    }

    @SubscribeEvent
    public static void keyReleased(GuiScreenEvent.KeyboardKeyReleasedEvent.Pre event) {
        if (!(event.getGui() instanceof GuiWorldSelection)) return;
        if (event.getKeyCode() == 342) {
            altHeld = false;
            updateWorldSyncButton(event.getGui().mc);
        }
    }

    private static void returnToScreen(GuiScreen gui, GuiTextField searchBox, GuiListWorldSelection list) {
        list.func_212330_a(searchBox::getText, true);
        gui.mc.displayGuiScreen(gui);
    }

    @SubscribeEvent
    public static void onLoadWorld(GuiOpenEvent event) {
        if (!(event.getGui() instanceof GuiScreenWorking)) return;
        GuiScreenWorking gui = (GuiScreenWorking) event.getGui();
        String message = ObfuscationReflectionHelper.getPrivateValue(GuiScreenWorking.class, gui, "title");;
        if (!message.equals(I18n.format("connect.joining"))) return;
        event.setCanceled(true);

        Minecraft minecraft = gui.mc;

        if (!showGitBeforeJoin) return;
        String worldId = hoveredLevel.getFileName();
        if (!GitManager.syncEnabled(minecraft, worldId)) return;
        event.setCanceled(true);
        GitProgressScreen progressScreen = new GitProgressScreen(I18n.format("minegit.sync.status.git_pull"));
        minecraft.displayGuiScreen(progressScreen);
        new Thread(() -> {
            SyncResult status = GitManager.pull(GitManager.getPath(minecraft, worldId), progressScreen);
            GitManager.makeWritable(minecraft, worldId);
            switch (status) {
                case SUCCESS:
                    // Success; load world as normal
                    doLoadWorld(minecraft, list.getSelectedWorld());
                    break;
                case FAIL_GENERIC:
                    // Generic error; show option to keep local or cloud
                    minecraft.addScheduledTask(() -> minecraft.displayGuiScreen(new GitConflictScreen(
                            () -> doLoadWorld(minecraft, list.getSelectedWorld()),
                            () -> returnToScreen(gui, searchBox, list),
                            GitManager.getPath(minecraft, worldId)
                    )));
                    break;
                case FAIL_NETWORK:
                    // Network error; show unreachable screen
                    minecraft.addScheduledTask(() -> minecraft.displayGuiScreen(new TwoChoiceScreen(
                            I18n.format("minegit.sync.pull_unreachable.title"),
                            I18n.format("minegit.sync.pull_unreachable.description"),
                            I18n.format("minegit.sync.pull_unreachable.continue"),
                            I18n.format("minegit.sync.pull_unreachable.cancel"),
                            () -> doLoadWorld(minecraft, list.getSelectedWorld()), // continue
                            () -> returnToScreen(gui, searchBox, list) // cancel
                    )));
                    break;
            }
        }).start();
    }

    private static void doLoadWorld(Minecraft mc, GuiListWorldSelectionEntry entry) {
        if (entry == null) return;
        mc.addScheduledTask(() -> {
            showGitBeforeJoin = false;
            entry.joinWorld();
            showGitBeforeJoin = true;
        });
    }
}