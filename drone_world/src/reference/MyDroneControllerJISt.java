/**
 * Authored by: Josiah Swanson & Isaac Siebelink - JISt
 * Drone World Part 2
 * Last edit: 4/12/19
 */

package reference;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;

import simulator.DistanceCalculator;
import simulator.Drone;
import simulator.Person;
import simulator.Place;

public class MyDroneControllerJISt extends DroneControllerSkeleton 
{	
	@Override
	public String getCompanyName() 
	{
		return "JISt";
	}
	
	@Override
	public void droneIdling(Drone drone) 
	{
		// Notify the parent class of the current event
		super.droneIdling(drone);
		
		//Find out where the drone currently is (idling at it's last destination)
		Place x = drone.getDestination();
		
		//Get all the possible places to go
		TreeSet<Place> places = getSimulator().getPlaces();
		places.remove(x);		
		
		ArrayList<Place> listOfPlaces = new ArrayList<Place>();
		listOfPlaces.addAll(places);
		
		// Find the place with the most ninjas
		Place max = listOfPlaces.get(0);
		for (int i = 0; i < listOfPlaces.size(); i++) 
		{
			if (listOfPlaces.get(i).getWaitingToEmbark().size() > max.getWaitingToEmbark().size())
				max = listOfPlaces.get(i);
		}
		
		// Not implemented this installation. Maybe for the final installation... 		
		// Get places in order of distance from idling spot
		ArrayList<Place> listOfNearby = new ArrayList<Place>();
		for (int j = 1; j < listOfPlaces.size(); j++) {
			Place closestPlace = listOfPlaces.get(0);
			for (int i = j; i > 0; i--) 
			{
				if (DistanceCalculator.distance(x.getPosition().getLatitude(), x.getPosition().getLongitude(), listOfPlaces.get(i).getPosition().getLatitude(), listOfPlaces.get(i).getPosition().getLongitude()) <
						DistanceCalculator.distance(x.getPosition().getLatitude(), x.getPosition().getLongitude(), closestPlace.getPosition().getLatitude(), closestPlace.getPosition().getLongitude()))
					closestPlace = listOfPlaces.get(i);
			}
			listOfNearby.add(closestPlace);
		}
		
		
		String nextPlace = "";		
		
		/*
		If there is a person at current location:
			Check each person at the current location to see...
				If the person's destination equals the place with the most ninjas
				Or else if a person's destination equals one of the places that has one or more passengers
			If no destination has been set by this time, set the destination equal to that of the first passenger
		Else if there are no people (the nextPlace having not been updated), go to the destination with the most passengers
		*/
		
		if (x.getWaitingToEmbark().size() > 0)
		{			
			for (Person passenger : x.getWaitingToEmbark())
			{					
				if (passenger.getDestination() == max.getName())
				{
					nextPlace = passenger.getDestination();
					break;
				}
				
				for (Place dest : listOfPlaces)
				{
					if (passenger.getDestination() == dest.getName() && dest.getWaitingToEmbark().size() > 0)
						nextPlace = passenger.getDestination();					
				}
			}
			
			if (nextPlace.length() < 1)
				nextPlace = x.getWaitingToEmbark().get(0).getDestination();
		}	
		if (nextPlace.length() < 1)
			nextPlace = max.getName();
		
		// Tell the passengers where the drone is going
		getSimulator().setDroneManifest(drone, nextPlace);
			
		// Send the drone to it's location
		if (drone.getPassengers().size() < 1) 
			getSimulator().routeDrone(drone, max);
		
		getSimulator().routeDrone(drone, nextPlace);
		
	}
}
