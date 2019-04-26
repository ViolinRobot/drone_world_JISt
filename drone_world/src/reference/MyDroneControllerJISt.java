/**
 * Authored by: Josiah Swanson & Isaac Siebelink - JISt
 * Drone World Part 3
 * Last edit: 4/16/19
 */

package reference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

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
		Place x = drone.getDestination();
		drone.getPassengers();

		// Get all places
		TreeSet<Place> places = getSimulator().getPlaces();
		places.remove(x);
		LinkedList<Place> listOfPlaces = new LinkedList<Place>();
		listOfPlaces.addAll(places);
		Collections.sort(listOfPlaces);

		// Get all passengers
		Set<Person> people = drone.getPassengers();
		LinkedList<Person> listOfPeople = new LinkedList<Person>();
		listOfPeople.addAll(people);
		Collections.sort(listOfPeople);

		// Get all nearby places to go
		ArrayList<Place> listOfNearbyPlaces = new ArrayList<Place>();
		for (int j=0; j < listOfPlaces.size(); j++) {
			double distance = DistanceCalculator.distance(x.getPosition().getLatitude(),x.getPosition().getLongitude(),
					listOfPlaces.get(j).getPosition().getLatitude(),listOfPlaces.get(j).getPosition().getLongitude());
			if (distance*drone.getDischargeRate() <= 0.9) {
				listOfNearbyPlaces.add(listOfPlaces.get(j));
			}
		}
		listOfNearbyPlaces.remove(x);	

		// Get the places with the most ninjas
		ArrayList<String> ninjaStringPlaces = new ArrayList<String>();
		for (int i = 0; i < listOfNearbyPlaces.size(); i++) {
			if (listOfNearbyPlaces.get(i).getWaitingToEmbark().size() > 7) {
				ninjaStringPlaces.add(listOfNearbyPlaces.get(i).getName());
			}
		}

		// Turn list of passengers' destinations into a list of strings
		ArrayList<String> passPlaces = new ArrayList<String>();
		for (int j=0; j<listOfPeople.size(); j++) {
			passPlaces.add(listOfPeople.get(j).getDestination());
		}

		passPlaces.remove(x.getName());		
		passPlaces.retainAll(ninjaStringPlaces);

		// Turn list of nearby places into a set of strings
		Set<String> nearbyPlaceString = new TreeSet<String>();
		for (int j=0; j<listOfNearbyPlaces.size(); j++) {
			nearbyPlaceString.add(listOfNearbyPlaces.get(j).getName());
		}
		
		// Choose a random nearby place
		String routePlace = listOfNearbyPlaces.get((int)(Math.random() * listOfNearbyPlaces.size() + 1)).getName();
		
		// Cycle through the current passengers to see which is the most popular destination among them
		if (listOfPeople.size() > 0)
		{
			Map<String, Integer> whereTheyGo = new HashMap<String, Integer>();
			for (Person passenger : listOfPeople)
			{
				if (whereTheyGo.get(passenger.getDestination()) == null)
					whereTheyGo.put(passenger.getDestination(),  1);
				else
					whereTheyGo.put(passenger.getDestination(), whereTheyGo.get(passenger.getDestination()) + 1);
			}
			
			String theDestination = "";
			int theMostVotes = -1;
			
			for (String key : whereTheyGo.keySet())
			{
				if (whereTheyGo.get(key) < theMostVotes)
				{
					theDestination = key;
					theMostVotes = whereTheyGo.get(key);
				}
			}
		}

		// If the passenger destination list does not have any locations close, route to the random location aforedefined. Otherwise, go to the the most popular nearby destination
		if (passPlaces.size() < 1)
			getSimulator().routeDrone(drone, routePlace);
		else 
			getSimulator().routeDrone(drone, passPlaces.get(0));

		// Update the manifest for the passengers at the next destination
		

		// Get all the places the waiting people want to go to
		Set<String> waitingDestList = new TreeSet<String>();
		for (int i= 0; i < drone.getDestination().getWaitingToEmbark().size(); i++) 
		{
			Place theSink = null;
			
			for (Place place : listOfNearbyPlaces)
			{
				if (place.getName().equals(x.getWaitingToEmbark().get(i).getDestination()))
					theSink = place;					
			}
			
			if (theSink != null)
			{
				if (isInRange(drone, x, theSink))
					waitingDestList.add(x.getWaitingToEmbark().get(i).getDestination());
			}
		}

		waitingDestList.retainAll(getInRangePlaces(drone, drone.getDestination()));

		getSimulator().setDroneManifest(drone, waitingDestList);
	}

	private ArrayList<String> routeTo(Drone drone, Place start, Place sink, ArrayList<String> route) 
	{
		ArrayList<Place> nextJump = getInRangePlaces(drone, start);

		/*
		if(nextJump.contains(sink))
		{
			route.add(sink.getName());
			return route;
		}
		*/
		
		for (Place place : nextJump)
		{
			route.add(place.getName());

			if (place.equals(sink))
				return route;
			else
				return routeTo(drone, place, sink, route);
		}

		return null;
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