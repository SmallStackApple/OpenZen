package shit.zen.modules.impl.world;

import com.sun.net.httpserver.HttpServer;
import java.awt.Desktop;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URI;
import java.net.URISyntaxException;
import shit.zen.modules.Category;
import shit.zen.modules.Module;
import shit.zen.network.webui.CategoriesHandler;
import shit.zen.network.webui.ModulesHandler;
import shit.zen.network.webui.SetSettingHandler;
import shit.zen.network.webui.SettingsHandler;
import shit.zen.network.webui.StaticFileHandler;
import shit.zen.network.webui.ToggleModuleHandler;
import shit.zen.settings.impl.ModeSetting;
import shit.zen.settings.impl.NumberSetting;
import shit.zen.utils.misc.ChatUtil;

public class WebUI extends Module {
    private HttpServer httpServer;

    public final ModeSetting portMode = new ModeSetting("Port Mode", "Custom", "Random").withDefault("Custom");
    public final NumberSetting customPort = new NumberSetting("Custom Port", 8089, 1024, 65535, 1, () -> portMode.is("Custom"));

    public WebUI() {
        super("WebUI", Category.WORLD);
        setEnabled(false);
    }

    @Override
    public void onEnable() {
        try {
            int port = this.resolvePort();
            this.httpServer = this.createHttpServer(port);
            ChatUtil.print("WebUI started at http://127.0.0.1:" + port);
            try {
                System.setProperty("java.awt.headless", "false");
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    Desktop.getDesktop().browse(new URI("http://127.0.0.1:" + port));
                }
            } catch (URISyntaxException | IOException ex) {
                ChatUtil.print("Failed to open browser: " + ex.getMessage());
            }
        } catch (IOException ioException) {
            ChatUtil.print("Failed to start http server because " + ioException.getMessage());
            ioException.printStackTrace();
            this.setEnabled(false);
        }
    }

    @Override
    public void onDisable() {
        if (this.httpServer != null) {
            this.httpServer.stop(0);
            this.httpServer = null;
            ChatUtil.print("WebUI stopped");
        }
    }

    private int resolvePort() throws IOException {
        if (portMode.is("Random")) {
            try (ServerSocket socket = new ServerSocket(0, 1, InetAddress.getByName("127.0.0.1"))) {
                return socket.getLocalPort();
            }
        }
        return customPort.getValue().intValue();
    }

    private HttpServer createHttpServer(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
        server.createContext("/api/modulesList", new ModulesHandler());
        server.createContext("/api/categoriesList", new CategoriesHandler());
        server.createContext("/api/setStatus", new ToggleModuleHandler());
        server.createContext("/api/setModuleSettingValue", new SetSettingHandler());
        server.createContext("/api/getModuleSetting", new SettingsHandler());
        server.createContext("/", new StaticFileHandler("/webui", "/"));
        server.start();
        return server;
    }
}
