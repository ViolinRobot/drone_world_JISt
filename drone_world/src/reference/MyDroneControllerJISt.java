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
import java.util.LinkedList;
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
	
	private LinkedList<String> route = new LinkedList<String>();

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
		Place currentLocation = drone.getDestination();
		if (currentLocation.getName().equals(route.getFirst()))
			route.removeFirst();

		// Get all the possible places to go
		ArrayList<Place> listOfPlaces = new ArrayList<Place>();
		listOfPlaces.addAll(getAllPossiblePlaces(currentLocation));

		// Get all the people riding the drone
		ArrayList<Person> currentPassengers = new ArrayList<Person>();
		currentPassengers.addAll(drone.getPassengers());

		// Get all nearby places to go
		ArrayList<Place> listOfNearbyPlaces = new ArrayList<Place>();
		listOfNearbyPlaces.addAll(getInRangePlaces(currentLocation, drone.getDischargeRate()));

		// Obtain a random number bounded by the size of the nearby-places-list
		//int r = (int)(Math.random() * listOfNearbyPlaces.size() + 1);

		// Figure out where people want to go if there is not already a pre-planned route
		if (currentPassengers.size() > 0 && route.size() < 1)
		{
			
		}
		else
		{
			route.clear();
			route.addFirst(goGetSomePassengersForCryingOutLoud(currentLocation, drone.getDischargeRate()));
		}		

		// Specify the next routed destination
		

		// Tell the passengers where the drone is going
		//getSimulator().setDroneManifest(drone, nextPlace);

		// Send the drone to a new location
		getSimulator().routeDrone(drone, route.getFirst());
	}

	private String goGetSomePassengersForCryingOutLoud(Place currentLocation, double dischargeRate)
	{
		// Get in range places
		ArrayList<Place> inRangePlaces = new ArrayList<Place>();
		inRangePlaces.addAll(getInRangePlaces(currentLocation, dischargeRate));

		// Return a random location to go get passengers from
		Random r = getSimulator().getSimulationController().getRandom();
		Integer nextRandom = r.nextInt(inRangePlaces.size());
		return inRangePlaces.get(nextRandom).getName();
	}

	private TreeSet<Place> getAllPossiblePlaces(Place currentLocation)
	{
		TreeSet<Place> places = getSimulator().getPlaces();
		places.remove(currentLocation);
		return places;
	}

	private ArrayList<Place> getInRangePlaces(Place currentLocation, double dischargeRate)
	{
		ArrayList<Place> listOfPlaces = new ArrayList<Place>();
		listOfPlaces.addAll(getAllPossiblePlaces(currentLocation));

		for (int j = 0; j < listOfPlaces.size(); j++) 
		{
			double distance = DistanceCalculator.distance(
								currentLocation.getPosition().getLatitude(),
								currentLocation.getPosition().getLongitude(),		
								listOfPlaces.get(j).getPosition().getLatitude(),
								listOfPlaces.get(j).getPosition().getLongitude()
								);
			if (distance*dischargeRate < 1)
				listOfPlaces.add(listOfPlaces.get(j));
		}

		return listOfPlaces;
	}
}