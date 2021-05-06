package at.qe.skeleton.bleclient;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.any;

public class ValueNotificationTest {

    @Test
    public void testFillListWithValues() {
        BackendCommunicator backendCommunicator = new BackendCommunicator();
        ValueNotification valueNotification = new ValueNotification(backendCommunicator);
        List<Integer> filledList = valueNotification.fillListWithValues(new ArrayList<>());
        Assert.assertEquals(12, filledList.size());
        for (int value : filledList) {
            Assert.assertTrue(value >= 0);
            Assert.assertTrue(value <= 11);
        }
    }

    @Test
    public void testGetMappedFacetValue() {
        BackendCommunicator backendCommunicator = new BackendCommunicator();
        ValueNotification valueNotification = new ValueNotification(backendCommunicator);
        Assert.assertNotNull(valueNotification);
        
        Assert.assertEquals(0, valueNotification.getFacetMapping().size());
        Assert.assertEquals(12, valueNotification.getAvailableFacetValues().size());

        int number = 14;
        int mappedFacetValue = valueNotification.getMappedFacetValue(number);

        Assert.assertEquals(1, valueNotification.getFacetMapping().size());
        Assert.assertEquals(11, valueNotification.getAvailableFacetValues().size());

        int mappedNumber = valueNotification.getFacetMapping().get(number);
        Assert.assertTrue(mappedNumber >= 0);
        Assert.assertTrue(mappedNumber <= 11);
    }

    @Test
    public void testRun() {
        BackendCommunicator backendCommunicator = new BackendCommunicator();
        ValueNotification valueNotification = new ValueNotification(backendCommunicator);
        Assert.assertNotNull(valueNotification);

        Assert.assertEquals(0, valueNotification.getFacetMapping().size());
        Assert.assertEquals(12, valueNotification.getAvailableFacetValues().size());

        byte[] mockData = { 0x11 };
        valueNotification.run(mockData);

        Assert.assertEquals(1, valueNotification.getFacetMapping().size());
        Assert.assertEquals(11, valueNotification.getAvailableFacetValues().size());
    }
}
