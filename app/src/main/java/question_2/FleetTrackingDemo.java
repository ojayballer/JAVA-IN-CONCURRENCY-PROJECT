package question_2;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class FleetTrackingDemo {

    public static void main(String[] args) {

        Map<String, Point> initialLocations = new HashMap<>();
        initialLocations.put("Taxi1", new Point(2, 3));
        initialLocations.put("Truck1", new Point(5, 7));
        initialLocations.put("PoliceCar1", new Point(8, 2));

        VehicleTracker tracker = new VehicleTracker(initialLocations);

        Runnable gpsUpdater = () -> {
            Random random = new Random();
            while (true) {
                for (String vehicle : tracker.getLocations().keySet()) {
                    Point current = tracker.getLocation(vehicle);
                    tracker.setLocation(
                            vehicle,
                            current.getX() + random.nextInt(3) - 1,
                            current.getY() + random.nextInt(3) - 1);
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        };

        new Thread(gpsUpdater).start();

        while (true) {
            System.out.println("Vehicle Locations: " + tracker.getLocations());
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}