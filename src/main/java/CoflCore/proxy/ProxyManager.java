package CoflCore.proxy;

import CoflCore.CoflCore;
import CoflCore.commands.models.ProxyRequest;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProxyManager {
    private final ExecutorService requestExecutor = Executors.newSingleThreadExecutor();

    public void handleRequestAsync(ProxyRequest request){
        String userAgent = request.getUserAgent() != null ? request.getUserAgent() : "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36";
        CompletableFuture<String> req = this.doRequest(request.getUrl(), userAgent);
        if(request.getUploadTo() != null) {
            req.thenAcceptAsync(res -> this.uploadData(res,request.getId(), request.getUploadTo()));
        }
    }

    private String getString(HttpURLConnection con) {
        try {
            InputStream in = new BufferedInputStream(con.getInputStream());
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            for (int length; (length = in.read(buffer)) != -1; ) {
                result.write(buffer, 0, length);
            }
            String resString = result.toString("UTF-8");
            return resString;
        } catch(IOException e){
            return null;
        }
    }

    public void uploadData(String data, String id, String uploadTo){
        this.requestExecutor.submit(new Runnable() {
            @Override
            public void run() {
                try{
                    URL url = new URL(uploadTo);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("POST");

                    con.setRequestProperty("X-Request-Id", id);

                    con.setDoOutput(true);
                    con.setDoInput(true);

                    OutputStream os = con.getOutputStream();
                    os.write(data.getBytes(StandardCharsets.UTF_8));
                    os.close();
                    String response = getString(con);
                    System.out.println("Response=" + response);
                }catch (Exception exception){
                    exception.printStackTrace();
                }
            }
        });
    }


    private CompletableFuture<String> doRequest(String targetUrl, String userAgent){
        CompletableFuture<String> future = new CompletableFuture<>();

        this.requestExecutor.submit(new Runnable() {
            @Override
            public void run() {
                try{
                    String chromeExecutable = findChromeExecutable();
                    if (chromeExecutable == null) {
                        chromeExecutable = "chromium";
                    }
                    System.out.println("Using chrome executable: " + chromeExecutable);

                    Process process = runChrome(chromeExecutable);

                    // Read the output from the process
                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    StringBuilder output = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        output.append(line).append(System.lineSeparator());
                    }

                    int exitCode = process.waitFor();
                    if (exitCode == 0) {
                        future.complete(output.toString());
                    } else {
                        // Read error stream for debugging
                        BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                        StringBuilder errorOutput = new StringBuilder();
                        while ((line = errorReader.readLine()) != null) {
                            errorOutput.append(line).append(System.lineSeparator());
                        }
                        future.complete(output.toString());
                        System.err.println("Chromium process exited with code " + exitCode + " output " + output.toString().length() + ": " + errorOutput.toString());
                        future.completeExceptionally(new RuntimeException("Chromium process exited with code " + exitCode + ": " + errorOutput.toString()));
                    }
                }catch (Exception exception){
                    exception.printStackTrace();
                }
            }

            private Process runChrome(String chromeExecutable) throws IOException {
                File configDir = CoflCore.configFile.getParentFile();
                File userDataDir = new File(configDir, "chrome-profile");
                if (!userDataDir.exists()) {
                    userDataDir.mkdirs();
                }


                ProcessBuilder pb = new ProcessBuilder(
                        '"' + chromeExecutable + '"',
                        "--headless",
                        "--disable-gpu",
                        "--user-data-dir=\"" + userDataDir.getAbsolutePath()  + '"',
                        "--dump-dom",
                        "--user-agent=\"" + userAgent + '"',
                        '"' + targetUrl + '"'
                );

                System.out.println("Running command: " + String.join(" ", pb.command()));

                return pb.start();
            }
        });

        return future;
    }


    private String findChromeExecutable() {
        String os = System.getProperty("os.name").toLowerCase();
        try {
            if (os.contains("win")) {
                // Common Windows install locations
                String pf = System.getenv("ProgramFiles");
                String pf86 = System.getenv("ProgramFiles(x86)");
                String local = System.getenv("LocalAppData");
                String[] paths = new String[] {
                        pf == null ? null : pf + "\\Google\\Chrome\\Application\\chrome.exe",
                        pf86 == null ? null : pf86 + "\\Google\\Chrome\\Application\\chrome.exe",
                        local == null ? null : local + "\\Google\\Chrome\\Application\\chrome.exe",
                        "chrome.exe"
                };
                for (String p : paths) {
                    if (p == null) continue;
                    File f = new File(p);
                    if (f.exists()) return f.getAbsolutePath();
                }
                // try 'where' to consult PATH
                try {
                    Process pr = new ProcessBuilder("where", "chrome.exe").start();
                    BufferedReader r = new BufferedReader(new InputStreamReader(pr.getInputStream()));
                    String line = r.readLine();
                    if (line != null && !line.isEmpty()) return line.trim();
                } catch (Exception ignored) {}
                return null;
            } else if (os.contains("mac")) {
                String[] paths = new String[] {
                        "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome",
                        "/Applications/Chromium.app/Contents/MacOS/Chromium",
                        "/Applications/Google Chrome Canary.app/Contents/MacOS/Google Chrome Canary"
                };
                for (String p : paths) {
                    if (new File(p).exists()) return p;
                }
                // fallback to which
                String[] candidates = new String[] {"google-chrome", "chromium", "chrome"};
                for (String c : candidates) {
                    try {
                        Process pr = new ProcessBuilder("which", c).start();
                        BufferedReader r = new BufferedReader(new InputStreamReader(pr.getInputStream()));
                        String line = r.readLine();
                        if (line != null && !line.isEmpty()) return line.trim();
                    } catch (Exception ignored) {}
                }
                return null;
            } else {
                // Linux/Unix
                String[] candidates = new String[] {"google-chrome", "google-chrome-stable", "chromium-browser", "chromium", "chrome"};
                for (String c : candidates) {
                    try {
                        Process pr = new ProcessBuilder("which", c).start();
                        BufferedReader r = new BufferedReader(new InputStreamReader(pr.getInputStream()));
                        String line = r.readLine();
                        if (line != null && !line.isEmpty()) return line.trim();
                    } catch (Exception ignored) {}
                }
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }
}
