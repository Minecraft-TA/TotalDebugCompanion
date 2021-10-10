package com.github.minecraft_ta.totalDebugCompanion.lsp;

import org.eclipse.xtext.xbase.lib.Pair;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BaseScript {

    private static final Pattern IMPORT_PATTERN = Pattern.compile("import\\s(.*?);");

    private static final String BASE_SCRIPT_IMPORTS = """
            import java.io.StringWriter;
            import java.util.Arrays;
            import java.util.List;
            import net.minecraft.entity.player.EntityPlayerMP;
            import net.minecraft.server.MinecraftServer;
            import net.minecraft.util.text.TextComponentString;
            import net.minecraft.world.World;
            import net.minecraft.world.WorldServer;
            import net.minecraftforge.fml.common.FMLCommonHandler;
            import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
            """;
    private static final String BASE_SCRIPT_TEXT = """
            abstract class BaseScript {
                        
                public MinecraftServer getServer() {
                    return FMLCommonHandler.instance().getMinecraftServerInstance();
                }
                        
                public void sendToAllPlayers(String message) {
                    getServerPlayers().forEach(p -> p.sendMessage(new TextComponentString(message)));
                }
                        
                public World getServerOverworld() {
                    return getServer().getWorld(0);
                }
                        
                public List<WorldServer> getServerWorlds() {
                    return Arrays.asList(getServer().worlds);
                }
                        
            	public List<EntityPlayerMP> getServerPlayers() {
            		return getServer().getPlayerList().getPlayers();
            	}
                        
                public <T> T getClientInstance() {
                    try {
                        return (T) ObfuscationReflectionHelper.findField(Class.forName("net.minecraft.client.Minecraft"), "R").get(null);
                    } catch (Throwable t) {
                        try {
                            return (T) Class.forName("net.minecraft.client.Minecraft").getMethod("getMinecraft").invoke(null);
                        } catch (Throwable t2) {
                        	return null;
                        }
                    }
                }
                        
                private StringWriter logWriter = new StringWriter();
                        
                public void logln(Object s) {
                    this.log(String.format("%s%n", s == null ? null : s.toString()));
                }
                        
                public void log(Object s) {
                    this.logWriter.append(s == null ? null : s.toString());
                }
                        
                public abstract void run() throws Throwable;
            }
            """.replace("    ", "\t");
    //language=Java
    private static final String BASE_SCRIPT = BASE_SCRIPT_IMPORTS + BASE_SCRIPT_TEXT;
    private final Path path;

    public BaseScript(Path path) {
        this.path = path;
    }

    public String mergeWithNormalScript(String scriptText) {
        var pair = extractImports(getText());
        return pair.getKey() + scriptText + pair.getValue();
    }

    public void writeToFileIfNotExists() {
        try {
            if (!Files.exists(this.path))
                Files.writeString(this.path, BASE_SCRIPT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getText() {
        try {
            return Files.readString(this.path).replace("\r\n", "\n");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Pair<String, String> extractImports(String code) {
        Matcher matcher = IMPORT_PATTERN.matcher(code);

        StringBuilder imports = new StringBuilder();
        while (matcher.find()) {
            imports.append(matcher.group());
        }

        return Pair.of(imports.toString(), code.replaceAll(IMPORT_PATTERN.pattern() + "(\\r\\n|\\r|\\n)", ""));
    }
}
