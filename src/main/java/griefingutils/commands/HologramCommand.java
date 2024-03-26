package griefingutils.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import griefingutils.GriefingUtils;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.command.CommandSource;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.CompletableFuture;

public class HologramCommand extends BetterCommand {
    private String lastImagePath = null;

    public HologramCommand() {
        super("hologram", "Place holograms (in creative mode)", "holo");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes((ctx) -> execute(false))
            .then(
                literal("last")
                    .executes((ctx) -> execute(true))
            );
    }

    private int execute(boolean last) {
        if (!isCreative()) {
            warning("You're not in creative mode!");
            return SUCCESS;
        }
        if (last && lastImagePath == null) {
            ChatUtils.warning("There is no last image!");
            return SUCCESS;
        }
        PointerBuffer filter;
        if(!last) filter = BufferUtils.createPointerBuffer(1)
            .put(MemoryUtil.memASCII("*jpg;*jpeg;*.png;*.bmp;*.gif")).rewind();
        else filter = null;
        CompletableFuture.runAsync(() -> {
            String imagePath;
            if (!last) {
                imagePath = TinyFileDialogs.tinyfd_openFileDialog(
                    "Select Image", null, filter, null, false);
                if (imagePath == null) {
                    ChatUtils.info("Canceled");
                    return;
                }
                lastImagePath = imagePath;
            } else imagePath = lastImagePath;

            try {
                ChatUtils.info("Selected: " + imagePath);
                File file = new File(imagePath);
                BufferedImage image = ImageIO.read(file);
                ChatUtils.info("Original Resolution: %sx%s", image.getWidth(), image.getHeight());
                if (image.getWidth() * image.getHeight() > 128*128) {
                    ChatUtils.warning("Image is too big, scaling down to 128x128");
                    image = scaleImage(image, BufferedImage.TYPE_INT_RGB, 128, 128);
                }

                int width = image.getWidth();
                int height = image.getHeight();

                ItemStack lastStack = mc.player.getMainHandStack();
                for (int y = 0; y < height; y++) {
                    BlockHitResult bhr = bhrAbovePlayer();
                    ItemStack hologram = new ItemStack(Items.COD_SPAWN_EGG);
                    NbtCompound entityTag = new NbtCompound();
                    hologram.setSubNbt("EntityTag", entityTag);
                    entityTag.putString("id", "minecraft:armor_stand");
                    entityTag.putBoolean("CustomNameVisible", true);
                    entityTag.putBoolean("Marker", true);
                    entityTag.putBoolean("Invisible", true);
                    NbtList pos = new NbtList();
                    pos.add(NbtDouble.of(mc.player.getX()));
                    pos.add(NbtDouble.of(mc.player.getY() + (height - y) * 0.23));
                    pos.add(NbtDouble.of(mc.player.getZ()));
                    entityTag.put("Pos", pos);
                    StringBuilder JSON = new StringBuilder("[");

                    int lastColor = image.getRGB(0, y);
                    StringBuilder tmp = new StringBuilder();
                    for (int x = 0; x < width; x++) {
                        int color = image.getRGB(x, y);
                        if (color == lastColor && (x != width - 1)) {
                            tmp.append("█");
                            continue;
                        }
                        appendToBuilder(JSON, lastColor, tmp, true);
                        tmp = new StringBuilder("█");
                        lastColor = color;
                    }
                    appendToBuilder(JSON, lastColor, tmp, false);

                    entityTag.putString("CustomName", JSON + "]");

                    mc.interactionManager.clickCreativeStack(hologram, 36 + mc.player.getInventory().selectedSlot);
                    mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, bhr);
                }
                mc.interactionManager.clickCreativeStack(lastStack, 36 + mc.player.getInventory().selectedSlot);
                ChatUtils.info("Loaded Image", image.getWidth(), image.getHeight());

            } catch (Exception e) {
                ChatUtils.info("exception: %s", e);
                GriefingUtils.LOG.error("", e);
            }

        });
        return SUCCESS;
    }

    public static BufferedImage scaleImage(BufferedImage image, int imageType, int width, int height) {
        BufferedImage resized = new BufferedImage(width, height, imageType);
        Graphics2D g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.drawImage(image, 0, 0, width, height, 0, 0, image.getWidth(), image.getHeight(), null);
        g.dispose();
        return resized;
    }

    public static void appendToBuilder(StringBuilder JSON, int lastColor, StringBuilder tmp, boolean appendComma) {
        String r = Integer.toHexString(lastColor >> 16 & 0xFF);
        r = "0".repeat(2 - r.length()) + r;
        String g = Integer.toHexString(lastColor >> 8 & 0xFF);
        g = "0".repeat(2 - g.length()) + g;
        String b = Integer.toHexString(lastColor & 0xFF);
        b = "0".repeat(2 - b.length()) + b;
        String colorString = "#" + r + g + b;
        JSON.append("{\"text\":\"")
            .append(tmp)
            .append("\", \"color\": \"")
            .append(colorString)
            .append("\"}");
        if(appendComma) JSON.append(", ");
    }
}
