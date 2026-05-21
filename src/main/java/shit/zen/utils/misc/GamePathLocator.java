package shit.zen.utils.misc;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.Generated;
import shit.zen.exception.SilentException;

public final class GamePathLocator {
    public static Path getGamePath() {
        if (GamePathLocator.isWindows()) {
            String userProfile = System.getenv("USERPROFILE");
            String miHoYoSubPath = "AppData/LocalLow/miHoYo";
            return Paths.get(userProfile, new String[0]).resolve(miHoYoSubPath);
        }
        throw new SilentException();
    }

    public static List<Path> getGamePaths() throws Exception {
        Path basePath = GamePathLocator.getGamePath();
        ArrayList<Path> paths = new ArrayList<>();
        Path logPath = null;
        String logRelative = "绝区零/Player.log";
        String dataMarker = "/ZenlessZoneZero_Data/";
        logPath = basePath.resolve("绝区零/Player.log");
        Optional<Path> exePath = GamePathLocator.findExecutable(logPath, "/ZenlessZoneZero_Data/");
        exePath.ifPresent(path -> paths.add(path));
        return paths;
    }

    private static Optional<Path> findExecutable(Path logPath, String marker) {
        try (Stream<String> stream = Files.lines(logPath, StandardCharsets.UTF_8)) {
            return stream.filter(line -> line.contains(marker)).map(line -> GamePathLocator.findGamePath(line, marker)).filter(Optional::isPresent).map(Optional::get).findFirst();
        } catch (Exception ex) {
            ex.printStackTrace();
            return Optional.empty();
        }
    }

    private static Optional<Path> findGamePath(String line, String marker) {
        try {
            int markerIdx = line.indexOf(marker);
            if (markerIdx != -1) {
                String exePath = line.substring(line.indexOf("at path ") + 8).replace("/UnitySubsystems", "").replace("/ZenlessZoneZero_Data", "/ZenlessZoneZero.exe").trim();
                return Optional.of(Paths.get(exePath, new String[0]));
            }
        } catch (InvalidPathException invalidPathException) {
            // empty catch block
        }
        return Optional.empty();
    }

    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    @Generated
    private GamePathLocator() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}