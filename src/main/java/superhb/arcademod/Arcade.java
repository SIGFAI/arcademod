package superhb.arcademod;

import com.google.gson.*;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.*;
import superhb.arcademod.init.ModRegistries;
import superhb.arcademod.util.PrizeList;
import superhb.arcademod.util.prizebox.PrizeHelper;
import superhb.arcademod.network.ArcadePacketHandler;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

@Mod(Reference.MODID)
public class Arcade {
    public static Arcade instance;
    public static final Logger logger = LogManager.getLogger(Reference.MODID);
    
    public static PrizeList[] prizeList;
    private static JsonObject json;
    private static File gameDir;
    
    public Arcade() {
        instance = this;
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        
        ModRegistries.register(modEventBus);
        
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);
        
        MinecraftForge.EVENT_BUS.register(this);
    }
    
    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            setupDirectories();
            loadPrizeList();
            initPrizeList();
            ArcadePacketHandler.register();
        });
    }
    
    private void clientSetup(final FMLClientSetupEvent event) {
    }

    private void setupDirectories() {
        gameDir = new File(FMLPaths.CONFIGDIR.get().toFile(), Reference.MODID + "/games");
        if (!gameDir.exists()) {
            logger.info("Games Addon directory doesn't exist. Creating empty folder...");
            gameDir.mkdirs();
        }
    }
    
    private void loadPrizeList() {
        File configDir = FMLPaths.CONFIGDIR.get().toFile();
        File prizeFile = new File(configDir, Reference.MODID + "/prizelist.json");
        
        if (!prizeFile.getParentFile().exists()) {
            prizeFile.getParentFile().mkdirs();
        }

        try {
            if (!prizeFile.exists()) {
                prizeFile.createNewFile();
                exportResource("prizelist.json", prizeFile.getAbsolutePath());
            }
            JsonElement element = JsonParser.parseReader(new FileReader(prizeFile));
            if (element != null && element.isJsonObject()) {
                json = element.getAsJsonObject();
            } else {
                json = new JsonObject();
                json.add("prizes", new JsonArray());
            }
        } catch (Exception e) {
            e.printStackTrace();
            json = new JsonObject();
            json.add("prizes", new JsonArray());
        }
    }
    
    private void initPrizeList() {
        if (json == null || !json.has("prizes")) return;
        JsonArray prizes = json.getAsJsonArray("prizes");
        
        prizeList = new PrizeList[prizes.size()];
        for (int i = 0; i < prizes.size(); i++) {
            try {
                prizeList[i] = new PrizeList(PrizeHelper.getItemStack(prizes.get(i).getAsJsonObject()), GsonHelper.getAsInt(prizes.get(i).getAsJsonObject(), "cost"));
            } catch (JsonSyntaxException e) {
                logger.error("Item is missing 'cost' member. Item will not be added to Prize Counter: " + e.getMessage());
            }
        }
    }
    
    private void exportResource(String name, String dir) throws Exception {
        InputStream in = getClass().getClassLoader().getResourceAsStream(name);
        if (in == null) {
            logger.warn(String.format("Cannot get resource '%s' from JAR file", name));
            return;
        }

        int readBytes;
        byte[] buffer = new byte[4096];
        try (OutputStream out = new FileOutputStream(dir)) {
            while ((readBytes = in.read(buffer)) > 0) {
                out.write(buffer, 0, readBytes);
            }
        }
        in.close();
    }
}
