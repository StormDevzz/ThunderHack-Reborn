package thunder.hack.features.hud.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import thunder.hack.core.Managers;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.features.hud.HudElement;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.setting.Setting;
import thunder.hack.utility.Timer;
import thunder.hack.utility.render.Render2DEngine;

import java.awt.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class CommitsList extends HudElement {
    private static final Logger log = LogUtils.getLogger();
    private final Setting<Integer> maxCommits = new Setting<>("MaxCommits", 6, 1, 100);
    private final Setting<Boolean> showAuthor = new Setting<>("ShowAuthor", true);
    private final Setting<Boolean> showTime = new Setting<>("ShowTime", true);
    private final Setting<Boolean> showHash = new Setting<>("ShowHash", false);

    private final List<Commit> commits = new CopyOnWriteArrayList<>();
    private final Timer refreshTimer = new Timer();
    private volatile boolean fetched;
    private volatile boolean fetching;
    private volatile boolean fetchFailed;

    public CommitsList() {
        super("CommitsList", 180, 50);
    }

    @Override
    public void onEnable() {
        if (!fetching && !fetched) triggerFetch();
    }

    private synchronized void triggerFetch() {
        if (fetching) return;
        fetching = true;
        refreshTimer.reset();
        Managers.ASYNC.run(() -> {
            fetchCommits();
            fetchFailed = commits.isEmpty();
            fetched = true;
            fetching = false;
        });
    }

    private void triggerRefresh() {
        if (fetching) return;
        fetching = true;
        refreshTimer.reset();
        Managers.ASYNC.run(() -> {
            fetchCommits();
            fetching = false;
        });
    }

    private static class Commit {
        final String hash;
        final String author;
        final String message;
        final long timestamp;

        Commit(String hash, String author, String message, long timestamp) {
            this.hash = hash;
            this.author = author;
            this.message = message;
            this.timestamp = timestamp;
        }
    }

    private void fetchCommits() {
        try {
            log.info("CommitsList: fetching commits from GitHub...");
            HttpRequest req = HttpRequest.newBuilder(
                    URI.create("https://api.github.com/repos/StormDevzz/ThunderHack-Reborn/commits?per_page=20"))
                    .GET()
                    .header("Accept", "application/vnd.github.v3+json")
                    .header("User-Agent", "ThunderHack-Reborn")
                    .build();

            try (HttpClient client = HttpClient.newHttpClient()) {
                HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
                log.info("CommitsList: response status={}, bodyLength={}", resp.statusCode(), resp.body().length());

                if (resp.statusCode() != 200) {
                    log.warn("CommitsList: GitHub API returned status {}", resp.statusCode());
                    return;
                }

                JsonArray arr = JsonParser.parseString(resp.body()).getAsJsonArray();
                log.info("CommitsList: parsed {} commits from response", arr.size());

                List<Commit> newCommits = new ArrayList<>();

                for (int i = 0; i < arr.size(); i++) {
                    JsonObject obj = arr.get(i).getAsJsonObject();
                    String sha = obj.get("sha").getAsString().substring(0, 7);
                    JsonObject commitObj = obj.get("commit").getAsJsonObject();
                    String msg = commitObj.get("message").getAsString().split("\n")[0];
                    String author = commitObj.get("author").getAsJsonObject().get("name").getAsString();
                    String dateStr = commitObj.get("author").getAsJsonObject().get("date").getAsString();
                    long time = Instant.parse(dateStr).toEpochMilli();

                    newCommits.add(new Commit(sha, author, msg, time));
                }

                commits.clear();
                commits.addAll(newCommits);
                log.info("CommitsList: loaded {} commits successfully", newCommits.size());
            }
        } catch (Exception e) {
            log.error("CommitsList: failed to fetch commits", e);
        }
    }

    @Override
    public void onThread() {
        if (!isEnabled()) return;
        if (!fetched && !fetching) {
            triggerFetch();
            return;
        }
        if (fetched && refreshTimer.passedMs(30000) && !fetching)
            triggerRefresh();
    }

    @Override
    public void onRender2D(DrawContext context) {
        super.onRender2D(context);

        if (!fetched && !fetching) triggerFetch();

        int limit = Math.min(maxCommits.getValue(), commits.size());
        if (limit == 0) {
            String text = fetchFailed ? Formatting.RED + "Failed to load commits" : Formatting.GRAY + "Loading commits...";
            FontRenderers.getModulesRenderer().drawString(context.getMatrices(), text, getPosX(), getPosY(), HudEditor.getColor(1).getRGB());
            setBounds(getPosX(), getPosY(), FontRenderers.getModulesRenderer().getStringWidth(text) + 4, 10);
            return;
        }

        float maxWidth = 0;
        float yOff = 0;
        float lineH = 9;

        for (int i = 0; i < limit; i++) {
            Commit c = commits.get(i);
            String line = buildLine(c);
            float w = FontRenderers.getModulesRenderer().getStringWidth(line) + 4;
            if (w > maxWidth) maxWidth = w;
        }

        float startX = getPosX() > mc.getWindow().getScaledWidth() / 2f
                ? getPosX() - maxWidth : getPosX();

        for (int i = 0; i < limit; i++) {
            Commit c = commits.get(i);
            String line = buildLine(c);
            Color clr = HudEditor.getColor(i * 45);

            Render2DEngine.drawRound(context.getMatrices(), startX, getPosY() + yOff, maxWidth, lineH, 2, HudEditor.plateColor.getValue().getColorObject());
            FontRenderers.getModulesRenderer().drawString(context.getMatrices(), line, startX + 2, getPosY() + yOff + 1, clr.getRGB());
            yOff += lineH;
        }

        setBounds(startX, getPosY(), maxWidth, yOff);
    }

    private String buildLine(Commit c) {
        String timeStr = "";
        if (showTime.getValue()) {
            long diff = System.currentTimeMillis() - c.timestamp;
            if (diff < 60000) timeStr = "now";
            else if (diff < 3600000) timeStr = (diff / 60000) + "m";
            else if (diff < 86400000) timeStr = (diff / 3600000) + "h";
            else timeStr = (diff / 86400000) + "d";
        }

        StringBuilder sb = new StringBuilder();
        if (showHash.getValue())
            sb.append(Formatting.DARK_GRAY).append(c.hash).append(" ").append(Formatting.RESET);
        if (showAuthor.getValue())
            sb.append(Formatting.GRAY).append("[").append(c.author).append("] ").append(Formatting.RESET);
        sb.append(c.message);
        if (showTime.getValue())
            sb.append(Formatting.DARK_GRAY).append(" (").append(timeStr).append(")");

        return sb.toString();
    }
}
