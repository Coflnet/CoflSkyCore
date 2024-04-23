package CoflCore.misc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class SessionManager {

    public static Path mainPath;
    public static Gson gson = new GsonBuilder()  .registerTypeAdapter(ZonedDateTime.class, new TypeAdapter<ZonedDateTime>() {
                @Override
                public void write(JsonWriter out, ZonedDateTime value) throws IOException {
                    out.value(value.toString());
                }

                @Override
                public ZonedDateTime read(JsonReader in) throws IOException {
                    return ZonedDateTime.parse(in.nextString());
                }
            })
            .enableComplexMapKeySerialization().create();
    public static class CoflSession {

        public String SessionUUID;
        public ZonedDateTime timestampCreated;
        public CoflSession() {}
        public CoflSession(String sessionUUID, ZonedDateTime timestampCreated) {
            super();
            SessionUUID = sessionUUID;
            this.timestampCreated = timestampCreated;
        }

    }

    public static void setMainPath(Path p) {
        mainPath = p;
    }

    public static void UpdateCoflSessions() throws IOException {
        Map<String, CoflSession> sessions = GetCoflSessions();

        for (String username : sessions.keySet()) {
            if(!isValidSession(sessions.get(username))) {
                DeleteCoflSession(username);
            }
        }
    }

    public static Path GetTempFileFolder() {

        Path dataPath = mainPath;
        dataPath.toFile().mkdirs();

        return dataPath;
    }

    public static Map<String, CoflSession> GetCoflSessions() throws IOException{

        File[] sessions = GetTempFileFolder().toFile().listFiles();

        Map<String, CoflSession> map = new HashMap<>();

        for (int i= 0; i<sessions.length;i++) {
            map.put(sessions[i].getName(),  GetCoflSession(sessions[i].getName()));
        }

        return map;
    }

    public static boolean isValidSession(CoflSession session) {
        if(session.timestampCreated.plus(Duration.ofDays(180)).isAfter(ZonedDateTime.now())) {
            return true;
        }
        return false;
    }

    private static Path GetUserPath(String username) {
        return Paths.get(GetTempFileFolder().toString() + "/" + username);
    }

    public static void DeleteCoflSession(String username) {
        Path path = GetUserPath(username);
        path.toFile().delete();
    }
    public static void DeleteAllCoflSessions() {
        Path path =GetTempFileFolder();
        File[] sessions = path.toFile().listFiles();
        for(File f : sessions) {
            f.delete();
        }
    }

    public static CoflSession GetCoflSession(String username) throws IOException {
        Path path = GetUserPath(username);
        File file = path.toFile();

        if(!file.exists() || file.length() == 0) {
            CoflSession session = new CoflSession(UUID.randomUUID().toString(), ZonedDateTime.now());
            OverwriteCoflSession(username, session);
            return session;
        }

        BufferedReader reader = new BufferedReader( new InputStreamReader(new FileInputStream(file)));
        String raw = reader.lines().collect(Collectors.joining("\n"));

        reader.close();
        CoflSession session = gson.fromJson(raw, CoflSession.class);
        return session;
    }

    public static boolean OverwriteCoflSession(String username, CoflSession session) throws IOException {


        Path path = GetUserPath(username);
        File file = path.toFile();
        file.createNewFile();

        String data = gson.toJson(session);

        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
        bw.append(data);
        bw.flush();
        bw.close();

        return true;
    }
}
