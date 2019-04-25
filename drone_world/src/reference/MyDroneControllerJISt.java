/**
 * Authored by: Josiah Swanson & Isaac Siebelink - JISt
 * Drone World Part 3
 * Last edit: 4/16/19
 */

package reference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;

import simulator.DistanceCalculator;
import simulator.Drone;
import simulator.Person;
import simulator.Place;

public class MyDroneControllerJISt extends DroneControllerSkeleton 
{	
	//@Override
	//public String getNextDroneName() 
	//{
	//	return "JISt Drone #"+incrementDroneCounter();
	//}

	@Override
	public String getCompanyName() { return "JISt";	}

	// Add getters during Drone transit over-ride
	// But, do all calculations while idling...
	
	@Override
	public void droneIdling(Drone drone) 
	{				
		// Notify the parent class of the current event
		super.droneIdling(drone);

		// Find out where the drone currently is (idling at it's last destination)
		Place x = drone.getDestination();
		drone.getPassengers();

		// Get all the possible places to go
		TreeSet<Place> places = getSimulator().getPlaces();
		places.remove(x);
		ArrayList<Place> listOfPlaces = new ArrayList<Place>();
		listOfPlaces.addAll(places);
		Collections.sort(listOfPlaces);

		// Get all the people riding the drone
		Set<Person> people = drone.getPassengers();
		ArrayList<Person> listOfPeople = new ArrayList<Person>();
		listOfPeople.addAll(people);

		// Get all nearby places to go
		ArrayList<Place> listOfNearbyPlaces = new ArrayList<Place>();
		for (int j=0; j < listOfPlaces.size(); j++) {
			double distance = DistanceCalculator.distance(x.getPosition().getLatitude(),x.getPosition().getLongitude(),
					listOfPlaces.get(j).getPosition().getLatitude(),listOfPlaces.get(j).getPosition().getLongitude());
			if (distance*drone.getDischargeRate() <= 0.9) {
				listOfNearbyPlaces.add(listOfPlaces.get(j));
			}
		}

		// Obtain a random number bounded by the size of the nearby-places-list
		int r = (int)(Math.random() * listOfNearbyPlaces.size() + 1);

		// Figure out where people want to go
		String nextPlace = listOfNearbyPlaces.get(r - 1).getName();
		Map<String, Integer> locationMap = new HashMap<>();
			for (Person candidate : x.getWaitingToEmbark()) {
			Integer val = locationMap.get(candidate.getDestination());
			locationMap.put(candidate.getDestination(), val == null ? 1 : val + 1);
		}

		Entry<String, Integer> max =null;

		for (Entry<String, Integer> e : locationMap.entrySet()) {
			if (max == null || e.getValue() > max.getValue()) {
				max = e;
			}
		}

		nextPlace = max.getKey();

		// Get the places with the most ninjas
		ArrayList<Place> listOfNinjaPlaces = new ArrayList<Place>();
		for (int j=0; j < listOfNearbyPlaces.size(); j++) {
			if (listOfNearbyPlaces.get(j).getWaitingToEmbark().size() > 7) {
				listOfNinjaPlaces.add(listOfNearbyPlaces.get(j));
			}
		}
		
		// Find the unload place (I would like to make the unload place the most common destination amongst passengers!!!
		String unloadPlace = listOfPeople.get(0).getDestination();
		

		// Specify the next routed destination
		Place routePlace = listOfNearbyPlaces.get(r - 1);
		if (listOfNinjaPlaces.size() > 5) {
			routePlace = listOfNinjaPlaces.get((int)(Math.random() * listOfNinjaPlaces.size() - 1));
		}

		// Tell the passengers where the drone is going
		getSimulator().setDroneManifest(drone, nextPlace);

		// Send the drone to a new location
		if (listOfPeople.size() > 7)
			getSimulator().routeDrone(drone, listOfNearbyPlaces.get(r - 1));
		else 
			getSimulator().routeDrone(drone, routePlace);
	}
}