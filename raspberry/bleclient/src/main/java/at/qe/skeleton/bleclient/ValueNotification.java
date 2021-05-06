package at.qe.skeleton.bleclient;

import java.util.*;
import tinyb.*;

// with help from https://github.com/intel-iot-devkit/tinyb/blob/ac6d3082d06183c860eea97f451d5a92022348e0/examples/java/Notification.java#L66

/**
 * The ValueNotification class provides methods needed for TimeFlip dice facet value notifications.
 */
class ValueNotification implements BluetoothNotification<byte[]> {
    private BackendCommunicator backendCommunicator;
    private Map<Integer, Integer> facetMapping;
    private List<Integer> availableFacetValues;

    public ValueNotification(BackendCommunicator backendCommunicator) {
        super();
        this.backendCommunicator = backendCommunicator;
        this.facetMapping = new HashMap<>();
        this.availableFacetValues = fillListWithValues(new ArrayList<>());
    }

    /**
	 * Method that runs each time the TimeFlip dice sends a notification for new facet values.
     * 
	 * @param data notification data from the TimeFlip dice
	 */
    public void run(byte[] data) {
        int facetValue = Byte.toUnsignedInt(data[0]);
        int mappedFacetValue = getMappedFacetValue(facetValue);
        System.out.println("facet:(actual) " + facetValue + ", (mapped) " + mappedFacetValue); 
        backendCommunicator.postDicePosition(mappedFacetValue);
    }

    /**
	 * Maps an unpredictable TimeFlip dice facet value (1-48) to a reasonable output value (0-11) 
     * or retrieves such a value from the map.
     * The TimeFlip dice has 12 facets on which it should land when thrown. 
     * Ideally it only lands on facets until all facets have been on top of the dice at least once. 
     * This way the mapping works perfectly fine.
     * If it lands on an edge somehow, there will be more than 12 different values received from the dice. 
     * For every received "edge case" value, i.e. if we get more than 12 different values from the dice,
     * we assume that the facet mapping process is completed and we return the value modulo 12.
	 * 
	 * @return a reasonable facet value from 0 to 11
	 */
    public int getMappedFacetValue(int facetValue) {
        if (facetMapping.get(facetValue) == null) {
            if (availableFacetValues.isEmpty()) {
                return facetValue % 12; // dice could land on an edge
            } else {
                int randomAvailableValue = availableFacetValues.remove(0);
                facetMapping.put(facetValue, randomAvailableValue);
            }
        } 
        int mappedFacetValue = facetMapping.get(facetValue);  
        return mappedFacetValue;
    }

    /**
	 * Fills a list with values from 0 to 11 and shuffles said list.
	 * 
	 * @return a reasonable facet value from 0 to 11
	 */
    public List<Integer> fillListWithValues(List<Integer> list) {
        for(int i = 0; i < 12; i++) {
            list.add(i);
        }
        Collections.shuffle(list);
        //System.out.println(list); // spoiler alert
        return list;
    }
}