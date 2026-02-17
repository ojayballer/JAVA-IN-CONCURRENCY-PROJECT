package question_2;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class VehicleTracker {

    private final ConcurrentHashMap<String, Point> locations;

    public VehicleTracker(Map<String, Point> initialLocations) {
        this.locations = new ConcurrentHashMap<>(initialLocations);
    }

    // Returns an unmodifiable view of all vehicle locations
    public Map<String, Point> getLocations() {
        return Collections.unmodifiableMap(locations);
    }

    // Returns the location of a specific vehicle
    public Point getLocation(String id) {
        return locations.get(id);
    }

    // Updates the location of a vehicle
    public void setLocation(String id, int x, int y) {
        if (!locations.containsKey(id)) {
            throw new IllegalArgumentException("Invalid vehicle name: " + id);
        }
        locations.put(id, new Point(x, y));
    }
}