package thunder.hack.utility.discord;

import java.util.Random;

public final class DiscordRPC {
    public static final DiscordRPC INSTANCE = new DiscordRPC();

    private DiscordIPC ipc;
    private volatile boolean running;
    private Thread readerThread;
    private String applicationId;
    private volatile boolean handshakeDone;

    private DiscordRPC() {}

    public void Discord_Initialize(String applicationId, boolean autoRegister, String steamId) {
        Discord_Shutdown();
        this.applicationId = applicationId;
        handshakeDone = false;
        ipc = new DiscordIPC();
        if (!ipc.connect()) {
            System.err.println("[RPC] Failed to connect to Discord IPC");
            ipc = null;
            return;
        }
        sendHandshake();
        readerThread = new Thread(this::readLoop, "TH-RPC-Reader");
        readerThread.setDaemon(true);
        readerThread.start();
    }

    public void Discord_UpdatePresence(DiscordRichPresence presence) {
        if (ipc == null || !ipc.isConnected()) return;
        String nonce = generateNonce();
        String activity = presence.toJson();
        String payload = "{\"cmd\":\"SET_ACTIVITY\",\"args\":{\"pid\":" + ProcessHandle.current().pid()
                + ",\"activity\":" + activity + "},\"nonce\":\"" + nonce + "\"}";
        ipc.send(1, payload);
    }

    public void Discord_RunCallbacks() {
    }

    public void Discord_Shutdown() {
        running = false;
        if (readerThread != null) {
            readerThread.interrupt();
            readerThread = null;
        }
        if (ipc != null) {
            ipc.close();
            ipc = null;
        }
        handshakeDone = false;
    }

    public void Discord_ClearPresence() {
        if (ipc == null || !ipc.isConnected()) return;
        String nonce = generateNonce();
        String payload = "{\"cmd\":\"SET_ACTIVITY\",\"args\":{\"pid\":" + ProcessHandle.current().pid()
                + ",\"activity\":null},\"nonce\":\"" + nonce + "\"}";
        ipc.send(1, payload);
    }

    private void sendHandshake() {
        String payload = "{\"v\":1,\"client_id\":\"" + applicationId + "\"}";
        ipc.send(0, payload);
    }

    private void readLoop() {
        running = true;
        while (running && ipc != null && ipc.isConnected()) {
            DiscordIPC.Frame frame = ipc.read();
            if (frame == null) {
                try { Thread.sleep(100); } catch (InterruptedException e) { break; }
                continue;
            }
            if (frame.op() == 0 && frame.data() != null) {
                if (frame.data().contains("\"cmd\":\"DISPATCH\"")
                        && frame.data().contains("\"evt\":\"READY\"")) {
                    handshakeDone = true;
                    System.out.println("[RPC] Handshake complete");
                }
            }
        }
        running = false;
    }

    private static String generateNonce() {
        byte[] bytes = new byte[16];
        new Random().nextBytes(bytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
