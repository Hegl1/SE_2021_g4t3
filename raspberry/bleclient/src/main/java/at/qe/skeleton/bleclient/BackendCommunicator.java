package at.qe.skeleton.bleclient;

import java.net.*;
import java.io.*;

// see https://tg.mtse.cc/ for overview of REST endpoints

/**
 * Class that contains methods to send HTTP requests to the backend.
 */
public final class BackendCommunicator {
	private String urlPrefix;
	private String diceId;

	public BackendCommunicator() {
		this.urlPrefix = "http://192.168.0.220:8080/dice";
	}

	// TODO GET dice id from backend (response: a string)
	public String getDiceId() {
		// if id not found in file:
		String urlString = urlPrefix + "/register";
		String newDiceId = sendGetRequest(urlString); // put get request here
		this.diceId = newDiceId;
		// maybe also request new id if some other method fails due to FileNotFoundException or something received from backend?
		// else:
		return diceId;
	}

	// dice position (number from 0-11), upon chance (put it into run() in ValueNotification)
	public void postDicePosition(int dicePosition) {
		String body = String.valueOf(dicePosition);
		String urlString = urlPrefix + "/" + diceId + "/update";
		sendPostRequest(urlString, body);
	}

	public void postBatteryStatus(int batteryLevel) {
		String body = String.valueOf(batteryLevel);
		String urlString = urlPrefix + "/" + diceId + "/notify/battery";
		sendPostRequest(urlString, body);
	}

	public void postConnectionStatus(boolean connectionStatus) {
		String body = String.valueOf(connectionStatus);
		String urlString = urlPrefix + "/" + diceId + "/notify/connection";
		sendPostRequest(urlString, body);
	}

	public void sendPostRequest(String urlString, String body) {
		/* with code from https://www.baeldung.com/httpurlconnection-post */
		try {
			// setup
			URL url = new URL(urlString);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/json; utf-8");
			con.setRequestProperty("Accept", "application/json");
			con.setDoOutput(true);

			// send request
			String jsonInputString = body;
			try (OutputStream os = con.getOutputStream()) {
				byte[] input = jsonInputString.getBytes("utf-8");
				os.write(input, 0, input.length);
			}

			// read the response
			//int status = con.getResponseCode(); // is this sufficient?
			try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
				StringBuilder response = new StringBuilder();
				String responseLine = null;
				while ((responseLine = br.readLine()) != null) {
					response.append(responseLine.trim());
				}
				System.out.println(response.toString());
			} catch (Exception e) {
				System.out.println("POST request response code exception caught");
				// TODO handle different responses - catch all possible exceptions?
				// maybe throw them so the calling method can hande it accordingly?
			}
			con.disconnect(); // do I need this?
		} catch (Exception e) {
			System.out.println("POST request method exception caught");
			// TODO catch all possible exceptions?
			// this might be a temporary catch phrase and will be removed later
		}
	
	}

	// TODO check if it works at all
	public String sendGetRequest(String urlString) {
		/* with code from https://www.baeldung.com/httpurlconnection-post */
		try {
			// setup
			URL url = new URL(urlString);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			//con.setRequestProperty("Content-Type", "application/json; utf-8");
			//con.setRequestProperty("Accept", "application/json");
			//con.setDoOutput(true);

			// send request
			//String jsonInputString = "body";
			//try (OutputStream os = con.getOutputStream()) {
			//	byte[] input = jsonInputString.getBytes("utf-8");
			//	os.write(input, 0, input.length);
			//}

			String contentType = con.getHeaderField("Content-Type");
			System.out.println(contentType);
			
			// read the response
			//int status = con.getResponseCode();
			try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
				StringBuilder response = new StringBuilder();
				String responseLine = null;
				while ((responseLine = br.readLine()) != null) {
					response.append(responseLine.trim());
				}
				System.out.println(response.toString());
				return response.toString();
			} catch (Exception e) {
				System.out.println("POST request response code exception caught");
				// TODO handle different responses - catch all possible exceptions?
				// maybe throw them so the calling method can hande it accordingly?
			}
			con.disconnect(); // do I need this?
		} catch (Exception e) {
			System.out.println("POST request method exception caught");
			// TODO catch all possible exceptions?
			// this might be a temporary catch phrase and will be removed later
		}
		return "testResponseString"; // TODO
	
	}
}