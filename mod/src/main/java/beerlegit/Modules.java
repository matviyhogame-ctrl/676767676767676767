package beerlegit;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import java.util.List;

/** Beer Visuals modules. */
public final class Modules {
    private Modules() {}
    public static void init(List<Module> list) {
        list.add(new Module("HUD", Category.RENDER) {{
            bool("Watermark", true); bool("Info Panel", true); bool("Coordinates", true);
            bool("FPS", true); bool("Armor Status", true); bool("Potion Effects", true);
            bool("Enabled Modules", true); bool("Armor Durability", true);
        }});
        list.add(new Module("Crosshair", Category.RENDER) {{
            num("Size", 6, 2, 16); num("Gap", 3, 0, 10); num("Thickness", 1, 1, 4);
            num("Red", 190, 0, 255); num("Green", 150, 0, 255); num("Blue", 255, 0, 255);
            bool("Outline", true);
        }});
        list.add(new Module("HitMarkers", Category.RENDER) {{
            bool("Sound", true); bool("Particles", true); num("Size", 8, 4, 20); num("Duration", 12, 4, 30);
        }});
        list.add(new Module("Fullbright", Category.RENDER) {
            private double oldGamma = 1.0;
            @Override public void onEnable(MinecraftClient mc) { if (mc.options != null) { oldGamma = mc.options.gamma; mc.options.gamma = 16.0; } }
            @Override public void onDisable(MinecraftClient mc) { if (mc.options != null) mc.options.gamma = oldGamma; }
            @Override public void onTick(MinecraftClient mc) { if (mc.options != null && mc.options.gamma < 15.0) mc.options.gamma = 16.0; }
        });
        list.add(new Module("NoHurtCam", Category.RENDER));
        list.add(new Module("Armor Durability", Category.PLAYER) {{ bool("Show Percent", true); bool("Low Durability Warning", true); num("Warning At", 20, 5, 80); }});
        list.add(new Module("Animations", Category.RENDER) {{ bool("Swing Animation", true); num("Swing Speed", 1.0, 0.5, 2.0); }});
        list.add(new Module("Chat Timestamp", Category.MISC) {{ bool("24-hour Clock", true); }});

        // Throws an ender pearl on a right-click when one is available in the hotbar.
        list.add(new Module("ClickPearl", Category.COMBAT) {
            private boolean wasDown;
            @Override public void onTick(MinecraftClient mc) {
                if (mc.player == null || mc.interactionManager == null || mc.currentScreen != null) return;
                boolean down = org.lwjgl.glfw.GLFW.glfwGetMouseButton(mc.getWindow().getHandle(), org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT) == org.lwjgl.glfw.GLFW.GLFW_PRESS;
                if (down && !wasDown) {
                    int old = mc.player.getInventory().selectedSlot, pearl = -1;
                    for (int i = 0; i < 9; i++) if (mc.player.getInventory().getStack(i).getItem() == Items.ENDER_PEARL) { pearl = i; break; }
                    if (pearl >= 0) {
                        mc.player.getInventory().selectedSlot = pearl;
                        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                        mc.player.getInventory().selectedSlot = old;
                    }
                }
                wasDown = down;
            }
        });

        // Keeps a totem in offhand; swaps an available inventory totem into slot 45.
        list.add(new Module("AutoTotem", Category.PLAYER) {
            private int cooldown;
            @Override public void onTick(MinecraftClient mc) {
                if (mc.player == null || mc.interactionManager == null || mc.currentScreen != null) return;
                if (cooldown > 0) { cooldown--; return; }
                if (mc.player.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING) return;
                int inv = -1;
                for (int i = 0; i < 36; i++) if (mc.player.getInventory().getStack(i).getItem() == Items.TOTEM_OF_UNDYING) { inv = i; break; }
                if (inv < 0) return;
                int slot = inv < 9 ? inv + 36 : inv;
                int sync = mc.player.playerScreenHandler.syncId;
                mc.interactionManager.clickSlot(sync, slot, 0, SlotActionType.PICKUP, mc.player);
                mc.interactionManager.clickSlot(sync, 45, 0, SlotActionType.PICKUP, mc.player);
                if (!mc.player.currentScreenHandler.getCursorStack().isEmpty()) mc.interactionManager.clickSlot(sync, slot, 0, SlotActionType.PICKUP, mc.player);
                cooldown = 8;
            }
        });
    }
}
