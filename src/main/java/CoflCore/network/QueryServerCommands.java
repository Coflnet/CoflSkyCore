package CoflCore.network;

import CoflCore.CoflCore;
import CoflCore.misc.SessionManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.util.Arrays;

public class QueryServerCommands {
	
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
			HttpsURLConnection con = NetworkUtils.setupConnection(url);
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
	public static String PostRequest(String uri,  String data, String username) {
		if (!NetworkUtils.isSSLInitialized()) {
			System.err.println("SSL keystore not initialized. Cannot make request to " + uri);
			return null;
		}
		
		try {
			URL url = new URL(uri);
			HttpsURLConnection con = NetworkUtils.setupConnection(url);
			con.setRequestMethod("POST");

			con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
			con.setRequestProperty("Accept", "application/json");
			// User-Agent is already set by setupConnection
			con.setRequestProperty("conId", SessionManager.GetCoflSession(username).SessionUUID);
			con.setRequestProperty("uuid",username);
			con.setDoInput(true);
			con.setDoOutput(true);
			// ...

			OutputStream os = con.getOutputStream();
			byte[] bytes = data.getBytes("UTF-8");
			os.write(bytes);
			os.close();
			int responseCode = con.getResponseCode();
			if (responseCode < 200 || responseCode >= 400) {
				throw new IOException("HTTP error code: " + responseCode);
			}

			InputStream in = new BufferedInputStream(con.getInputStream());
			ByteArrayOutputStream result = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			for (int length; (length = in.read(buffer)) != -1; ) {
				result.write(buffer, 0, length);
			}
			String resString =  result.toString("UTF-8");
			return resString;
		} catch (javax.net.ssl.SSLException sslEx) {
			System.err.println("SSL validation failed for " + uri);
			sslEx.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}
}
