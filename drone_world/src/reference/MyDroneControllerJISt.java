/**
 * Authored by: Josiah Swanson & Isaac Siebelink - JISt
 * Drone World Part 3
 * Last edit: 4/16/19
 */

package reference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
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
import simulator.interfaces.SimulationController;

public class MyDroneControllerJISt extends DroneControllerSkeleton 
{
	// @Override
	// public String getNextDroneName()
	// {
	// return "JISt Drone #"+incrementDroneCounter();
	// }

	private LinkedList<String> route = new LinkedList<String>();

	@Override
	public String getCompanyName() 
	{
		return "JISt";
	}

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
		listOfNearbyPlaces.addAll(getInRangePlaces(drone, currentLocation));

		// Obtain a random number bounded by the size of the nearby-places-list
		// int r = (int)(Math.random() * listOfNearbyPlaces.size() + 1);

		// Figure out where people want to go if there is not already a pre-planned
		// route
		if (currentPassengers.size() > 0 && route.size() < 1) 
		{
			// Got passengers but no route. Go get one!
			Map<String, Integer> whereTheyGo = new HashMap<String, Integer>();
			for (Person passenger : currentPassengers) 
			{
				whereTheyGo.put(passenger.getDestination(), whereTheyGo.get(passenger.getDestination()) == null ? 1
						: whereTheyGo.get(passenger.getDestination()) + 1);
			}

			String theDestination = "";
			int theMostVotes = -1;

			for (String key : whereTheyGo.keySet()) 
			{
				if (whereTheyGo.get(key) > theMostVotes) 
				{
					theDestination = key;
					theMostVotes = whereTheyGo.get(key);
				}
			}

			Place thePlace = null;
			for (Place place : getAllPossiblePlaces(currentLocation))
			{
				if (place.getName().equals(theDestination))
					thePlace = place;
			}
			if(thePlace != null)
				route.addAll(routeTo(drone, currentLocation, thePlace, new ArrayList<String>()));
			else
				System.out.println("We have a problem...");
		}
		else if (currentPassengers.size() < 1)
		{
			// Got no passengers. Go get some!
			route.clear();
			route.addFirst(goGetSomePassengersForCryingOutLoud(drone, currentLocation));
		}
		// The final case is that the drone has both a passenger and a route. Just proceed.

		// Set the manifest
		Set<String> manifest = new HashSet<String>();
		manifest.addAll(route);
		getSimulator().setDroneManifest(drone, manifest);

		// Send the drone to the next location on its route
		getSimulator().routeDrone(drone, route.getFirst());
	}

	private ArrayList<String> routeTo(Drone drone, Place start, Place sink, ArrayList<String> route) 
	{
		ArrayList<Place> nextJump = getInRangePlaces(drone, start);

		if(nextJump.contains(sink))
		{
			route.add(sink.getName());
			return route;
		}
		
		for (Place place : nextJump)
		{
			route.add(place.getName());

			if (place.equals(sink))
				return route;
			else
				return routeTo(drone, place, sink, route);
		}
	}

	private Object generatePath(Drone drone, ArrayList<Place> neighbors, Place sink)
	{

	}

	private String goGetSomePassengersForCryingOutLoud(Drone drone, Place currentLocation)
	{
		// Get in-range places
		ArrayList<Place> inRangePlaces = new ArrayList<Place>();
		inRangePlaces.addAll(getInRangePlaces(drone, currentLocation));

		// Return an arbitrary location to go get passengers from
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

	private Boolean isInRange(Drone drone, Place start, Place sink)
	{
		double distance = DistanceCalculator.distance(
								start.getPosition().getLatitude(),
								start.getPosition().getLongitude(),		
								sink.getPosition().getLatitude(),
								sink.getPosition().getLongitude()
								);

		if (distance*drone.getDischargeRate() < 1)
			return true;
		else
			return false;
	}

	private Boolean isInRange(Drone drone, Place start, Place sink, Double currentDroneCharge)
	{
		double distance = DistanceCalculator.distance(
								start.getPosition().getLatitude(),
								start.getPosition().getLongitude(),		
								sink.getPosition().getLatitude(),
								sink.getPosition().getLongitude()
								);

		if (distance*drone.getDischargeRate() < currentDroneCharge)
			return true;
		else
			return false;
	}

	private ArrayList<Place> getInRangePlaces(Drone drone, Place currentLocation)
	{
		ArrayList<Place> listOfPlaces = new ArrayList<Place>();
		listOfPlaces.addAll(getAllPossiblePlaces(currentLocation));

		for (int i = 0; i < listOfPlaces.size(); i++) 
		{
			if (isInRange(drone, currentLocation, listOfPlaces.get(i)))
				listOfPlaces.add(listOfPlaces.get(i));
		}

		return listOfPlaces;
	}
}