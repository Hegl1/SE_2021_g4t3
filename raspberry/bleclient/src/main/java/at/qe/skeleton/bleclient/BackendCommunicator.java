package at.qe.skeleton.bleclient;

/**
 * Class that contains methods to send HTTP requests to the backend.
 */
public final class BackendCommunicator {
	/* see https://tg.mtse.cc/ for overview of REST endpoints */
	// TODO POST update dice position (number from 0-11) upon chance (put it into
	// run() in ValueNotification)
	// TODO GET retrieve the dice id (response: a string)
	// TODO POST update battery status
	// TODO POST update connection status

	public void postBatteryStatus(String diceId, int batteryLevel) {
		/* code from https://www.baeldung.com/httpurlconnection-post */

		// setup
		// TODO change the URL to whatever is correct
		URL url = new URL("http://localhost:8080/dice/" + diceId + "/update");
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("POST");
		con.setRequestProperty("Content-Type", "application/json; utf-8");
		con.setRequestProperty("Accept", "application/json");
		con.setDoOutput(true);

		// send request
		String jsonInputString = String.valueOf(batteryLevel);
		try (OutputStream os = con.getOutputStream()) {
			byte[] input = jsonInputString.getBytes("utf-8");
			os.write(input, 0, input.length);
		}

		// read the response
		try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
			StringBuilder response = new StringBuilder();
			String responseLine = null;
			while ((responseLine = br.readLine()) != null) {
				response.append(responseLine.trim());
			}
			System.out.println(response.toString());
		}

		// TODO handle different responses
	}
}