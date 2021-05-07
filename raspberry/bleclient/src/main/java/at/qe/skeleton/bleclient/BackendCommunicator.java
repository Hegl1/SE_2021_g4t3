package at.qe.skeleton.bleclient;

import java.net.*;
import java.io.*;

// see https://tg.mtse.cc/ for overview of REST endpoints

/**
 * Class that contains methods to send HTTP requests to the backend.
 */
public final class BackendCommunicator {
	private String diceId;
	private String diceIdFileName;
	private String backendUrlPrefix;
	private String backendUrlFileName;

	public BackendCommunicator() throws NullPointerException {
		this.backendUrlFileName = "timeGuessBackendUrl.txt";
		this.backendUrlPrefix = readBackendUrlFromFile();
		if (backendUrlPrefix == null) {
			throw new NullPointerException("backend URL");
		}
		System.out.println("URL of backend: " + backendUrlPrefix);
		backendUrlPrefix += "/dice";
		this.diceIdFileName = "timeGuessDiceId.txt";
		this.diceId = getDiceId();
	}

	public String getBackendUrlPrefix() {
		return this.backendUrlPrefix;
	}

	public String getDiceIdFileName() {
		return this.diceIdFileName;
	}

	public void setDiceIdFileName(String diceIdFileName) {
		this.diceIdFileName = diceIdFileName;
	}

	/**
	 * Gets the ID that is needed to uniquely identify the rasbperry 
	 * and corresponding TimeFlip dice when starting a new TimeGuess game.
	 * Once registered with the backend, a raspberry keeps his ID and can easily be identified again.
	 * The ID is saved in and can then be read from a file upon request. 
	 * For registration an HTTP GET request is sent to receive a fresh ID.
	 * 
	 * @return the dice ID
	 */
	public String getDiceId() {
		if (this.diceId == null) { 
			String savedDiceId = readIdFromFile();
			if (savedDiceId == null) {
				String urlString = backendUrlPrefix + "/register";
				String newDiceId = sendGetRequest(urlString);
				if (newDiceId == null) {
					System.out.println("couldn't get an ID from the central backend");
				} else {
					this.diceId = newDiceId;
					System.out.println("new dice Id = " + newDiceId);
					saveIdToFile(newDiceId);
				}
			} else {
				this.diceId = savedDiceId;
			}
		}
		return this.diceId;
	}

	/**
	 * Reads the URL of the central backend from a file if that file exists. 
	 * NOTE:
	 * The backend URL file should always exist and contain the correct URL.
	 * The backend should already be up and running when this program gets executed on the raspberry.
	 * 
	 * @return the backend URL if a file with a backend URL exists, null otherwise
	 */
	public String readBackendUrlFromFile() {
		File file = new File(this.backendUrlFileName);
		if (file.exists()) {
			System.out.println("backend URL file exists");
			try { 
				BufferedReader reader = new BufferedReader(new FileReader(file));
    			String backendUrl = reader.readLine();
   				reader.close();
				System.out.println("backend URL successfully read from file");
				return backendUrl;
			} catch (IOException e) {
				System.out.println("backend URL couldn't be read from file");
				return null;
			}		
		} else {
			System.out.println("~~~~~~ WRONG USAGE OF PROGRAM DETECTED ~~~~~~");
			System.out.println("file 'timeGuessBackendUrl.txt' doesn't exist in folder 'bleclient'. please make sure the file exists and contains the correct URL of the central backend");
			System.out.println("(also make sure the central backend is up and running when running this program on the raspberry)");
			return null;
		}	
	}

	/**
	 * Reads the already registered raspberry/dice ID from a file if that file exists.
	 * 
	 * @return the registered ID if a file with an ID exists, null otherwise
	 */
	public String readIdFromFile() {	
		File file = new File(this.diceIdFileName);
		if (file.exists()) {
			System.out.println("ID file exists");
			try { 
				BufferedReader reader = new BufferedReader(new FileReader(file));
    			String savedDiceId = reader.readLine();
   				reader.close();
				System.out.println("ID successfully read from file");
				return savedDiceId;
			} catch (IOException e) {
				System.out.println("ID couldn't be read from file");
				return null;
			}		
		} else {
			System.out.println("ID file doesn't exist");
			return null;
		}	
	}

	/**
	 * Saves the newly registered raspberry/dice ID into a file.
	 * 
	 * @param newDiceId the ID to be saved
	 */
	public void saveIdToFile(String newDiceId) {
		try {
			File file = new File(this.diceIdFileName);
    		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
    		writer.write(newDiceId);
    		writer.close();
			System.out.println("ID saved to file");
		} catch (IOException e) {
			System.out.println("dice ID couldn't be saved to file");
		}
	}

	/**
	 * Sends an HTTP POST request to the backend, containing information about the current dice position, i.e. about which facet is on top.
	 * The facet number should be a number from 0 to 11.
	 * 
	 * @param dicePosition the value of the facet on the top side of the dice
	 */
	public void postDicePosition(int dicePosition) {
		String body = String.valueOf(dicePosition);
		String urlString = backendUrlPrefix + "/" + diceId + "/update";
		sendPostRequest(urlString, body);
	}

	/**
	 * Sends an HTTP POST request to the backend, containing information about the current battery level. 
	 * The battery can be any number from 0 to 100.
	 * 
	 * @param batteryLevel the battery level
	 */
	public void postBatteryStatus(int batteryLevel) {
		String body = String.valueOf(batteryLevel);
		String urlString = backendUrlPrefix + "/" + diceId + "/notify/battery";
		sendPostRequest(urlString, body);
	}

	/**
	 * Sends an HTTP POST request to the backend, containing information about the current connection status. 
	 * 
	 * @param connectionStatus the connection status; true if connected, false if disconnected
	 */
	public void postConnectionStatus(boolean connectionStatus) {
		String body = String.valueOf(connectionStatus);
		String urlString = backendUrlPrefix + "/" + diceId + "/notify/connection";
		sendPostRequest(urlString, body);
	}

	/**
	 * Sends an HTTP POST request to the backend. 
	 * 
	 * @param urlString the url to send the POST request to
	 * @param body the request body that contains data needed by the backend
	 */
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
			int status = con.getResponseCode();
			System.out.println("POST request response status: " + status);
			if (status == 200) {	
				return;
			} else if (status == 404) {
				System.out.println(urlString + " not found (please check url prefix or dice id)");
			} else if (status == 400 && urlString.contains("update")) {
				System.out.println("dice position not in between 0-11");
			} else if (status == 204 && urlString.contains("connection")) {
				System.out.println("dice not in a game");
			} else {
				System.out.println("POST request response code is not mentioned in REST documentaion");
			} 
			con.disconnect();
		} catch (Exception e) {
			System.out.println("POST request failed. exception caught. please check if central backend is running properly.");
		}
	}

	/**
	 * Sends an HTTP GET request to the backend. 
	 * 
	 * @param urlString the url to send the POST request to
	 * @return the response body if the request was successful, null otherwise
	 */
	public String sendGetRequest(String urlString) {
		try {
			// setup
			URL url = new URL(urlString);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			
			// read the response
			int status = con.getResponseCode();
			System.out.println("GET request response status: " + status);
			if (status == 200) {
				try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
					StringBuilder response = new StringBuilder();
					String responseLine = null;
					while ((responseLine = br.readLine()) != null) {
						response.append(responseLine.trim());
					}
					return response.toString();
				} catch (Exception e) {
					System.out.println("GET request response exception caught. couldn't read answer.");
				}
			} else {
				System.out.println("GET request response code is not mentioned in REST documentaion");
			}
			con.disconnect();
		} catch (Exception e) {
			System.out.println("GET request failed. exception caught. please check if central backend is running properly.");
		}
		return null;
	}
}