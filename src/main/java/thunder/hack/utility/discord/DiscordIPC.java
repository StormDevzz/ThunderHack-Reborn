package thunder.hack.utility.discord;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public final class DiscordIPC {
    private SocketChannel channel;
    private boolean connected;

    public boolean connect() {
        try {
            String path = findPipePath();
            if (path == null) return false;
            UnixDomainSocketAddress addr = UnixDomainSocketAddress.of(path);
            channel = SocketChannel.open(addr);
            channel.configureBlocking(true);
            connected = true;
            return true;
        } catch (Exception e) {
            connected = false;
            return false;
        }
    }

    public void close() {
        connected = false;
        try {
            if (channel != null) channel.close();
        } catch (IOException ignored) {}
        channel = null;
    }

    public boolean isConnected() {
        return connected && channel != null && channel.isOpen();
    }

    public boolean send(int op, String payload) {
        if (!isConnected()) return false;
        try {
            byte[] payloadBytes = payload.getBytes(StandardCharsets.UTF_8);
            ByteBuffer header = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
            header.putInt(op);
            header.putInt(payloadBytes.length);
            header.flip();
            channel.write(header);
            channel.write(ByteBuffer.wrap(payloadBytes));
            return true;
        } catch (IOException e) {
            connected = false;
            return false;
        }
    }

    public Frame read() {
        if (!isConnected()) return null;
        try {
            ByteBuffer header = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
            int read = channel.read(header);
            if (read < 8) return null;
            header.flip();
            int op = header.getInt();
            int len = header.getInt();
            if (len <= 0 || len > 65536) return null;
            ByteBuffer payload = ByteBuffer.allocate(len);
            read = channel.read(payload);
            if (read < len) return null;
            payload.flip();
            byte[] bytes = new byte[len];
            payload.get(bytes);
            return new Frame(op, new String(bytes, StandardCharsets.UTF_8));
        } catch (IOException e) {
            connected = false;
            return null;
        }
    }

    private static String findPipePath() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return "\\\\?\\pipe\\discord-ipc-0";
        }
        if (os.contains("mac")) {
            String tmp = System.getenv("TMPDIR");
            if (tmp == null) tmp = "/tmp";
            Path p = Path.of(tmp, "discord-ipc-0");
            if (Files.exists(p)) return p.toString();
            try {
                return Files.list(Path.of(tmp).getParent())
                    .filter(f -> f.getFileName().toString().startsWith("com.apple.launchd"))
                    .map(f -> f.resolve("discord-ipc-0"))
                    .filter(Files::exists)
                    .map(Path::toString)
                    .findFirst().orElse(null);
            } catch (IOException e) {
                return null;
            }
        }
        String xdg = System.getenv("XDG_RUNTIME_DIR");
        if (xdg != null) {
            Path p = Path.of(xdg, "discord-ipc-0");
            if (Files.exists(p)) return p.toString();
        }
        Path snap = Path.of("/run/user/1000/snap.discord/discord-ipc-0");
        if (Files.exists(snap)) return snap.toString();
        return null;
    }

    public record Frame(int op, String data) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Frame frame)) return false;
            return op == frame.op && Objects.equals(data, frame.data);
        }

        @Override
        public int hashCode() {
            return Objects.hash(op, data);
        }
    }
}
