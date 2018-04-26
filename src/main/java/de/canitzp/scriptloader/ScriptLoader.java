package de.canitzp.scriptloader;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author canitzp
 */
@Mod(modid = ScriptLoader.MODID, name = ScriptLoader.MODNAME, version = ScriptLoader.MODVERSION)
public class ScriptLoader {

    public static final String MODID = "scriptloader";
    public static final String MODNAME = "ScriptLoader";
    public static final String MODVERSION = "@Version@";

    public static File scriptDir;
    public static List<JavaScriptMod> javaScripts = new ArrayList<>();

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event){
        scriptDir = new File(event.getModConfigurationDirectory().getParentFile(), "scripts");
        if(!scriptDir.exists()){
            scriptDir.mkdirs();
        } else {
            for(File file : FileUtils.listFiles(scriptDir, new String[]{"zip"}, false)){
                try {
                    ZipFile zip = new ZipFile(file);
                    JavaScriptMod mod = JavaScriptMod.fromZip(zip);
                    if(mod != null){
                        javaScripts.add(mod);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        javaScripts.forEach(mod -> mod.runScripts("preInit", event));
    }

}
