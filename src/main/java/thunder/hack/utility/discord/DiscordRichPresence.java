package thunder.hack.utility.discord;

public final class DiscordRichPresence {
    public String state;
    public String details;
    public long startTimestamp;
    public long endTimestamp;
    public String largeImageKey;
    public String largeImageText;
    public String smallImageKey;
    public String smallImageText;
    public String partyId;
    public int partySize;
    public int partyMax;
    public String partyPrivacy;
    public String matchSecret;
    public String joinSecret;
    public String spectateSecret;
    public String button_label_1;
    public String button_url_1;
    public String button_label_2;
    public String button_url_2;
    public int instance;

    public String toJson() {
        StringBuilder json = new StringBuilder("{");

        append(json, "state", state);
        append(json, "details", details);
        if (startTimestamp > 0) append(json, "timestamps", "{\"start\":" + startTimestamp + "}");
        append(json, "assets", buildAssets());
        append(json, "party", buildParty());
        append(json, "secrets", buildSecrets());
        append(json, "buttons", buildButtons());

        if (json.charAt(json.length() - 1) == ',')
            json.setLength(json.length() - 1);
        json.append("}");
        return json.toString();
    }

    private void append(StringBuilder json, String key, String value) {
        if (value != null && !value.isEmpty()) {
            json.append("\"").append(key).append("\":");
            if (value.startsWith("{") || value.startsWith("["))
                json.append(value);
            else
                json.append("\"").append(escape(value)).append("\"");
            json.append(",");
        }
    }

    private String buildAssets() {
        if (largeImageKey == null && smallImageKey == null) return null;
        StringBuilder a = new StringBuilder("{");
        if (largeImageKey != null) {
            a.append("\"large_image\":\"").append(escape(largeImageKey)).append("\"");
            if (largeImageText != null)
                a.append(",\"large_text\":\"").append(escape(largeImageText)).append("\"");
        }
        if (smallImageKey != null) {
            if (largeImageKey != null) a.append(",");
            a.append("\"small_image\":\"").append(escape(smallImageKey)).append("\"");
            if (smallImageText != null)
                a.append(",\"small_text\":\"").append(escape(smallImageText)).append("\"");
        }
        a.append("}");
        return a.toString();
    }

    private String buildParty() {
        if (partyId == null) return null;
        return "{\"id\":\"" + escape(partyId) + "\",\"size\":[" + partySize + "," + partyMax + "]}";
    }

    private String buildSecrets() {
        if (matchSecret == null && joinSecret == null && spectateSecret == null) return null;
        StringBuilder s = new StringBuilder("{");
        if (joinSecret != null) s.append("\"join\":\"").append(escape(joinSecret)).append("\"");
        if (spectateSecret != null) {
            if (joinSecret != null) s.append(",");
            s.append("\"spectate\":\"").append(escape(spectateSecret)).append("\"");
        }
        if (matchSecret != null) {
            if (joinSecret != null || spectateSecret != null) s.append(",");
            s.append("\"match\":\"").append(escape(matchSecret)).append("\"");
        }
        s.append("}");
        return s.toString();
    }

    private String buildButtons() {
        if (button_label_1 == null) return null;
        StringBuilder b = new StringBuilder("[");
        b.append("{\"label\":\"").append(escape(button_label_1)).append("\",\"url\":\"").append(escape(button_url_1 != null ? button_url_1 : "")).append("\"}");
        if (button_label_2 != null)
            b.append(",{\"label\":\"").append(escape(button_label_2)).append("\",\"url\":\"").append(escape(button_url_2 != null ? button_url_2 : "")).append("\"}");
        b.append("]");
        return b.toString();
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
