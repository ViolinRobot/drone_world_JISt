/**
 * Authored by: Josiah Swanson & Isaac Siebelink - JISt
 * Drone World Part 3
 * Last edit: 4/16/19
 */

package reference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;

import simulator.DistanceCalculator;
import simulator.Drone;
import simulator.Person;
import simulator.Place;

/**
 * This is the class that students should work with to create their drone controller
 * Rename this class to MyDroneControllerXXXXXXX where XXXXXX is the team name
 * 
 */
public class MyDroneControllerJISt extends DroneControllerSkeleton {
	
	
	//@Override
		//public String getNextDroneName() 
		//{
		//	return "JISt Drone #"+incrementDroneCounter();
		//}

		@Override
		public String getCompanyName() 
		{
			return "JISt";
		}

		// Add getters during Drone transit over-ride
		// But, do all calculations while idling...
		// 
		
		@Override
		public void droneIdling(Drone drone) 
		{	
			
			// Notify the parent class of the current event
			super.droneIdling(drone);

			//Find out where the drone currently is (idling at it's last destination)
			Place x = drone.getDestination();
			drone.getPassengers();

			//Get all the possible places to go
			TreeSet<Place> places = getSimulator().getPlaces();
			places.remove(x);

			ArrayList<Place> listOfPlaces = new ArrayList<Place>();
			listOfPlaces.addAll(places);

			//Get places in order of distance from idling spot
			ArrayList<Place> listOfNearby = new ArrayList<Place>();
			for (int j=1; j<listOfPlaces.size(); j++) {
				Place closestPlace = listOfPlaces.get(0);
				for (int i=j; i>0; i--) {
					if (DistanceCalculator.distance(x.getPosition().getLatitude(), x.getPosition().getLongitude(), listOfPlaces.get(i).getPosition().getLatitude(), listOfPlaces.get(i).getPosition().getLongitude()) <
							DistanceCalculator.distance(x.getPosition().getLatitude(), x.getPosition().getLongitude(), closestPlace.getPosition().getLatitude(), closestPlace.getPosition().getLongitude())) {
						closestPlace = listOfPlaces.get(i);
					}
				}
				listOfNearby.add(closestPlace);
			}
			
			/*
			public class destSort implements Comparator<Place>
			{
			   public int compareTo(Object z, Place y)
			   { 
				   int result = 0;
				   if (DistanceCalculator.distance(((Place) z).getPosition().getLatitude(), ((Place) z).getPosition().getLongitude(), ((Place) x).getPosition().getLatitude(), ((Place) x).getPosition().getLongitude()) <
					(DistanceCalculator.distance(((Place) y).getPosition().getLatitude(), ((Place) y).getPosition().getLongitude(), ((Place) x).getPosition().getLatitude(), ((Place) x).getPosition().getLongitude())))
					   result = -1;
				   if (DistanceCalculator.distance(((Place) z).getPosition().getLatitude(), ((Place) z).getPosition().getLongitude(), ((Place) x).getPosition().getLatitude(), ((Place) x).getPosition().getLongitude()) >
							(DistanceCalculator.distance(((Place) y).getPosition().getLatitude(), ((Place) y).getPosition().getLongitude(), ((Place) x).getPosition().getLatitude(), ((Place) x).getPosition().getLongitude())))
					   result = 1;
				   else result = 0;
				   return result;
			   }
			}
			*/
			Collections.sort(listOfPlaces);

			// Find the place with the most ninjas
			Place max = listOfPlaces.get(0);
			for (int i = 0; i < listOfPlaces.size(); i++) 
			{
				if (listOfPlaces.get(i).getWaitingToEmbark().size() > max.getWaitingToEmbark().size())
					max = listOfPlaces.get(i);
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
						if (passenger.getDestination() == dest.getName() && dest.getWaitingToEmbark().size() > 0) {
							nextPlace = passenger.getDestination();}
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
