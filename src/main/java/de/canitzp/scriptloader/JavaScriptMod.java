package de.canitzp.scriptloader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author canitzp
 */
public class JavaScriptMod {

    public static final ScriptEngine JAVA_SCRIPT_ENGINE = new ScriptEngineManager().getEngineByName("nashorn");

    @Expose
    private String modid, name, version;
    @Expose
    private List<String> files = new ArrayList<>();
    private List<InputStream> fileStreams = new ArrayList<>();

    public JavaScriptMod(String modid, String name, String version) {
        this.modid = modid;
        this.name = name;
        this.version = version;
    }

    public static JavaScriptMod fromZip(ZipFile zip) throws IOException {
        List<ZipEntry> scripts = new ArrayList<>();
        InputStream mainMod = null;
        for(ZipEntry entry : Collections.list(zip.entries())){
            if(entry.getName().endsWith(".json")){
                mainMod = zip.getInputStream(entry);
            } else if(entry.getName().endsWith(".js")){
                scripts.add(entry);
            }
        }
        if(mainMod != null){
            Gson g = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
            JavaScriptMod mod = g.fromJson(new InputStreamReader(mainMod), JavaScriptMod.class);
            if(mod != null){
                mod.fileStreams = new ArrayList<>();
                scripts.stream().filter(entry -> mod.files.contains(entry.getName())).forEach(entry -> {
                    try {
                        mod.fileStreams.add(zip.getInputStream(entry));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                return mod;
            }
        }
        return null;
    }

    public void runScripts(String functionName, Object... args){
        System.out.println(fileStreams + "   " + JAVA_SCRIPT_ENGINE);
        try {
            for(InputStream is : fileStreams){
                JAVA_SCRIPT_ENGINE.eval(new InputStreamReader(is));
                ((Invocable)JAVA_SCRIPT_ENGINE).invokeFunction(functionName, this, args);
            }

        } catch (ScriptException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public Block createBlock(Material material, String name){
        Block block = new Block(material).setRegistryName(this.modid, name);
        return block;
    }

    @Override
    public String toString() {
        return String.format("JSMod{name=%s, id=%s, version=%s, files=%s}", this.name, this.modid, this.version, this.files);
    }
}
