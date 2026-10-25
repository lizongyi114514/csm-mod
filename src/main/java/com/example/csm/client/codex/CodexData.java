package com.example.csm.client.codex;

import com.example.csm.Csm;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import org.slf4j.Logger;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 图鉴内容来自 assets/csm/codex/entries_<语言>.json。
 * 找不到对应语言时退回 en_us；两份都缺就不显示内容（不崩）。
 */
public final class CodexData {

    public record Entry(String title, List<String> body) {
    }

    public record Section(String title, List<Entry> entries) {
    }

    private static final Logger LOGGER = Csm.LOGGER;
    private static String cachedLanguage;
    private static List<Section> cached;

    private CodexData() {
    }

    public static List<Section> sections() {
        Minecraft minecraft = Minecraft.getInstance();
        String language = minecraft.options.languageCode;
        if (cached != null && language.equals(cachedLanguage)) {
            return cached;
        }
        List<Section> sections = load(language);
        if (sections.isEmpty()) {
            sections = load("en_us");
        }
        cached = sections;
        cachedLanguage = language;
        return sections;
    }

    private static List<Section> load(String language) {
        ResourceLocation id = new ResourceLocation(Csm.MODID, "codex/entries_" + language + ".json");
        try {
            Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(id);
            if (resource.isEmpty()) {
                LOGGER.warn("[CSM] 找不到图鉴资源 {}", id);
                return List.of();
            }
            try (InputStream stream = resource.get().open()) {
                JsonObject root = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8))
                        .getAsJsonObject();
                JsonArray array = root.getAsJsonArray("sections");
                List<Section> sections = new ArrayList<>();
                for (JsonElement element : array) {
                    JsonObject object = element.getAsJsonObject();
                    List<Entry> entries = new ArrayList<>();
                    for (JsonElement entryElement : object.getAsJsonArray("entries")) {
                        JsonObject entryObject = entryElement.getAsJsonObject();
                        List<String> body = new ArrayList<>();
                        for (JsonElement line : entryObject.getAsJsonArray("body")) {
                            body.add(line.getAsString());
                        }
                        entries.add(new Entry(entryObject.get("title").getAsString(), body));
                    }
                    sections.add(new Section(object.get("title").getAsString(), entries));
                }
                return sections;
            }
        } catch (Exception exception) {
            LOGGER.error("[CSM] 读取图鉴失败 {}", id, exception);
            return List.of();
        }
    }
}
