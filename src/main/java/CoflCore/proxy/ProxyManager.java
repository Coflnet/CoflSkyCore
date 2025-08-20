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
        int waitMs = request.getWaitMs() > 0 ? request.getWaitMs() : 1000; // unless this is a loadtest, wait 1 second
        CompletableFuture<String> req = this.doRequest(request.getUrl(), userAgent, waitMs);
        if(request.getUploadTo() != null) {
            req.thenAcceptAsync(res -> this.uploadData(res,request.getId(), request.getUploadTo(), request.getRegex()));
        }
    }

    public void resetChromeData(){
        File userDataDir = getChromeDataDir();
        if (userDataDir.exists()) {
            try {
                java.nio.file.Path root = userDataDir.toPath();
                if (java.nio.file.Files.exists(root)) {
                    java.nio.file.Files.walkFileTree(root, new java.nio.file.SimpleFileVisitor<java.nio.file.Path>() {
                        @Override
                        public java.nio.file.FileVisitResult visitFile(java.nio.file.Path file, java.nio.file.attribute.BasicFileAttributes attrs) throws java.io.IOException {
                            java.nio.file.Files.deleteIfExists(file);
                            return java.nio.file.FileVisitResult.CONTINUE;
                        }

                        @Override
                        public java.nio.file.FileVisitResult postVisitDirectory(java.nio.file.Path dir, java.io.IOException exc) throws java.io.IOException {
                            java.nio.file.Files.deleteIfExists(dir);
                            return java.nio.file.FileVisitResult.CONTINUE;
                        }
                    });
                }
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
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

    public void uploadData(String data, String id, String uploadTo, String selectRegex){
        this.requestExecutor.submit(new Runnable() {
            @Override
            public void run() {
                try{
                    String uploadData = data;

                    if(selectRegex != null && !selectRegex.isEmpty()){
                        try {
                            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(selectRegex, java.util.regex.Pattern.DOTALL);
                            java.util.regex.Matcher matcher = pattern.matcher(uploadData != null ? uploadData : "");
                            if (matcher.find()) {
                                uploadData = matcher.groupCount() >= 1 ? matcher.group(1) : matcher.group(0);
                            }
                        } catch (java.util.regex.PatternSyntaxException e) {
                            System.err.println("Invalid selectRegex: " + e.getMessage());
                        }
                    }
                    URL url = new URL(uploadTo);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("POST");

                    con.setRequestProperty("X-Request-Id", id);
                    con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    con.setRequestProperty("Accept", "application/json");
                    con.setRequestProperty("User-Agent", "CoflMod");
                    con.setRequestProperty("Content-Encoding", "gzip");

                    con.setDoOutput(true);
                    con.setDoInput(true);

                    // Use chunked streaming so we don't need to know compressed length up-front
                    con.setChunkedStreamingMode(0);

                    try (OutputStream os = con.getOutputStream();
                         java.io.BufferedOutputStream bos = new java.io.BufferedOutputStream(os);
                         java.util.zip.GZIPOutputStream gos = new java.util.zip.GZIPOutputStream(bos)) {
                        if (uploadData != null && !uploadData.isEmpty()) {
                            byte[] contentBytes = uploadData.getBytes(StandardCharsets.UTF_8);
                            gos.write(contentBytes);
                        }
                        gos.finish();
                    }

                    String response = getString(con);
                    int responseCode = con.getResponseCode();
                    System.out.println("Response code=" + responseCode + " Response=" + response);
                }catch (Exception exception){
                    exception.printStackTrace();
                }
            }
        });
    }


    private CompletableFuture<String> doRequest(String targetUrl, String userAgent, int waitMs){
        CompletableFuture<String> future = new CompletableFuture<>();

        this.requestExecutor.submit(new Runnable() {
            @Override
            public void run() {
                try{
                    String chromeExecutable = findChromeExecutable();
                    if (chromeExecutable == null) {
                        chromeExecutable = "chromium";
                    }
                    Thread.sleep(waitMs); // wait for the specified time before starting the process

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
                File userDataDir = getChromeDataDir();

                ProcessBuilder pb = new ProcessBuilder(
                        chromeExecutable,
                        "--headless",
                        "--disable-gpu",
                        "--user-data-dir=" + userDataDir.getAbsolutePath(),
                        "--dump-dom",
                        "--user-agent=" + userAgent,
                        targetUrl
                );

                System.out.println("Running command: " + String.join(" ", pb.command()));

                return pb.start();
            }
        });

        return future;
    }

    private static File getChromeDataDir() {
        File configDir = CoflCore.configFile.getParentFile();
        File userDataDir = new File(configDir, "chrome-profile");
        if (!userDataDir.exists()) {
            userDataDir.mkdirs();
        }
        return userDataDir;
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
