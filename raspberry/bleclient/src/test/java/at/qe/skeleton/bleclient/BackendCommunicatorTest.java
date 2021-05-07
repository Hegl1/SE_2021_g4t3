package at.qe.skeleton.bleclient;

import org.junit.Assert;
import org.junit.Test;

public class BackendCommunicatorTest {

    @Test
    public void testBackendCommunicator() {
        BackendCommunicator backendCommunicator = new BackendCommunicator();
        Assert.assertNotNull(backendCommunicator);
        Assert.assertNotNull(backendCommunicator.getBackendUrlPrefix());
        Assert.assertNotNull(backendCommunicator.getDiceId());
        Assert.assertNotNull(backendCommunicator.getDiceIdFileName());
    }

    @Test
    public void testSendGetRequest() {
        BackendCommunicator backendCommunicator = new BackendCommunicator();
        String urlString = backendCommunicator.getBackendUrlPrefix() + "/register";
        String response = backendCommunicator.sendGetRequest(urlString);
        Assert.assertNotNull(response);
    }

    @Test
    public void testSaveIdToFile() {
        BackendCommunicator backendCommunicator = new BackendCommunicator();
        String testId = "testId";
        String testFileName = "testDiceIdFile.txt";
        backendCommunicator.setDiceIdFileName(testFileName);
        backendCommunicator.saveIdToFile(testId);
        String readId = backendCommunicator.readIdFromFile();
        Assert.assertEquals(testId, readId);
    }
}
