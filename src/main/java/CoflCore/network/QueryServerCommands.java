package CoflCore.network;

import CoflCore.CoflCore;
import CoflCore.misc.SessionManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.net.HttpURLConnection;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class QueryServerCommands {
    private static final String[] DESCRIPTION_ENDPOINTS = {
            "https://sky.coflnet.com/api/mod/description/modifications",
            "https://sky-commands.coflnet.com/api/mod/description/modifications",
            "https://sky-mod.coflnet.com/api/mod/description/modifications"
    };
    static volatile int preferredDescriptionEndpoint = -1;
	
	private static Gson gson = new GsonBuilder().create();
	
	public static String QueryCommands() {
		
		String queryResult = GetRequest(CoflCore.CommandUri);
		
		if(queryResult != null) {
			CommandInfo[] commands = gson.fromJson(queryResult, CommandInfo[].class);
			
			System.out.println(">>> "+Arrays.toString(commands));
			
			StringBuilder sb = new StringBuilder();
			
			if(commands.length>0) {
				for(CommandInfo cm : commands) {
					sb.append(cm + "\n");
				}
			}
			return sb.toString().trim();
			
		}
		
		return "§4ERROR: Could not connect to command server!";
	}
	
	private static class CommandInfo {
		
		public String subCommand;
		public String description;
		
		public CommandInfo() {}
		
		public CommandInfo(String subCommand, String description) {
			super();
			this.subCommand = subCommand;
			this.description = description;
		}

		@Override
		public String toString() {
			return subCommand + ": " + description;
		}
		
		
		
	}
	private static String GetRequest(String uri) {
		if (!NetworkUtils.isSSLInitialized()) {
			System.err.println("SSL keystore not initialized. Cannot make request to " + uri);
			return null;
		}
		
		try {
			URL url = new URL(uri);
			HttpURLConnection con = NetworkUtils.setupConnection(url);
			con.setRequestMethod("GET");
			con.setRequestProperty("Accept", "application/json");
			con.setDoInput(true);

			// ...

			/*OutputStream os = con.getOutputStream();
			byte[] bytes = ("[\"" + getUsername() + "\"]").getBytes("UTF-8");
			os.write(bytes);
			os.close();
			*/
			InputStream in = new BufferedInputStream(con.getInputStream());
			ByteArrayOutputStream result = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			for (int length; (length = in.read(buffer)) != -1; ) {
			    result.write(buffer, 0, length);
			}
			String resString = result.toString("UTF-8");
			return resString;
		} catch (javax.net.ssl.SSLException sslEx) {
			System.err.println("SSL validation failed for " + uri);
			sslEx.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
    public static String PostRequest(String uri, String data, String username) {
        if (!NetworkUtils.isSSLInitialized()) {
            System.err.println("SSL keystore not initialized. Cannot make request to " + uri);
            return null;
        }
        if (DESCRIPTION_ENDPOINTS[0].equals(uri)) {
            int connected = -1;
            WSClientWrapper wrapper = CoflCore.Wrapper;
            WSClient socket = wrapper == null ? null : wrapper.socket;
            if (wrapper != null && wrapper.isRunning && socket != null && socket.uri != null) {
                String host = socket.uri.getHost();
                if ("sky-commands.coflnet.com".equalsIgnoreCase(host)) connected = 1;
                if ("sky-mod.coflnet.com".equalsIgnoreCase(host)) connected = 2;
            }
            return postDescriptionWithFallback(DESCRIPTION_ENDPOINTS, data, username, connected);
        }
        try {
            return postOnce(uri, data, username, false);
        } catch (javax.net.ssl.SSLException sslEx) {
            System.err.println("SSL validation failed for " + uri);
            sslEx.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    static String postDescriptionWithFallback(String[] endpoints, String data, String username, int connected) {
        int first = preferredDescriptionEndpoint >= 0 ? preferredDescriptionEndpoint : connected;
        IOException lastFailure = null;
        for (int pass = -1; pass < endpoints.length; pass++) {
            int index = pass < 0 ? first : pass;
            if (index < 0 || (pass >= 0 && index == first)) continue;
            try {
                String response = postOnce(endpoints[index], data, username, true);
                preferredDescriptionEndpoint = index;
                return response;
            } catch (HttpStatusException e) {
                if (e.status < 500) {
                    System.err.println("[descriptions] Request failed without further fallback: " + e);
                    return null;
                }
                lastFailure = e;
            } catch (IOException e) {
                lastFailure = e;
            }
        }
        if (lastFailure != null) System.err.println("[descriptions] All endpoints failed: " + lastFailure);
        return null;
    }

    private static String postOnce(String uri, String data, String username, boolean description) throws IOException {
        HttpURLConnection con = NetworkUtils.setupConnection(new URL(uri));
        if (description) con.setInstanceFollowRedirects(false);
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        con.setRequestProperty("Accept", "application/json");
        con.setRequestProperty("conId", SessionManager.GetCoflSession(username).SessionUUID);
        con.setRequestProperty("uuid", username);
        con.setDoInput(true);
        con.setDoOutput(true);
        try (OutputStream os = con.getOutputStream()) {
            os.write(data.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            if (description) {
                try {
                    int status = con.getResponseCode();
                    if (status >= 300 && status < 500) {
                        drainAndClose(con.getErrorStream());
                        throw new HttpStatusException(status);
                    }
                } catch (HttpStatusException status) {
                    throw status;
                } catch (IOException ignored) {
                    // No response arrived; the next endpoint can handle this transport failure.
                }
            }
            throw e;
        }
        int responseCode = con.getResponseCode();
        if (responseCode < 200 || responseCode >= (description ? 300 : 400)) {
            drainAndClose(con.getErrorStream());
            throw new HttpStatusException(responseCode);
        }
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        try (InputStream in = new BufferedInputStream(con.getInputStream())) {
            byte[] buffer = new byte[1024];
            for (int length; (length = in.read(buffer)) != -1; ) result.write(buffer, 0, length);
        }
        return result.toString("UTF-8");
    }

    private static class HttpStatusException extends IOException {
        final int status;
        HttpStatusException(int status) {
            super("HTTP error code: " + status);
            this.status = status;
        }
    }

	// Reads an error stream to EOF and closes it so its socket can be reused (keep-alive). Never
	// throws - reconnecting on the next request is the harmless fallback if draining fails.
	private static void drainAndClose(InputStream stream) {
		if (stream == null)
			return;
		try (InputStream in = stream) {
			byte[] buffer = new byte[1024];
			while (in.read(buffer) != -1) {
				// discard
			}
		} catch (IOException ignored) {
		}
	}
}
