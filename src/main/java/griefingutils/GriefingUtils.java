package griefingutils;

import com.mojang.logging.LogUtils;
import griefingutils.commands.*;
import griefingutils.hud.BrandHud;
import griefingutils.modules.*;
import griefingutils.modules.creative.DoomBoom;
import griefingutils.modules.creative.ExplosiveHands;
import griefingutils.modules.op.AutoScoreboard;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;

public class GriefingUtils extends MeteorAddon {
    public static final Logger LOG = LogUtils.getLogger();
    public static final MinecraftClient MC = MinecraftClient.getInstance();

    @Override
    public void onInitialize() {
        LOG.info("Initializing 0x06's Griefing Utils");
        registerModules();
        registerCommands();
        registerHUDs();
    }

    private static void registerModules() {
        Modules.get().add(new AntiFunny());
        Modules.get().add(new AutoLavacast());
        Modules.get().add(new AutoScoreboard());
        Modules.get().add(new AutoSignPlus());
        Modules.get().add(new ContainerPuke());
        Modules.get().add(new DoomBoom());
        Modules.get().add(new ExplosiveHands());
        Modules.get().add(new GameModeNotifier());
        Modules.get().add(new NoBlockEntities());
        Modules.get().add(new NoFall());
        Modules.get().add(new NoItem());
        Modules.get().add(new VanillaFlight());
        Modules.get().add(new WitherAdvertise());
    }

    private static void registerCommands() {
        Commands.add(new ClipboardGive());
        Commands.add(new CrackedKick());
        Commands.add(new CreativeFunnies());
        Commands.add(new HologramCommand());
        Commands.add(new PurpurCrash());
    }

    private static void registerHUDs() {
        Hud.get().register(BrandHud.INFO);
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(Categories.DEFAULT);
    }

    @Override
    public String getPackage() {
        return "griefingutils";
    }
}