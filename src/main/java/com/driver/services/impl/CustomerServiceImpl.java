package com.driver.services.impl;

import com.driver.model.TripBooking;
import com.driver.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.driver.model.Customer;
import com.driver.model.Driver;
import com.driver.repository.CustomerRepository;
import com.driver.repository.DriverRepository;
import com.driver.repository.TripBookingRepository;
import com.driver.model.TripStatus;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomerServiceImpl implements CustomerService {

	@Autowired
	CustomerRepository customerRepository2;

	@Autowired
	DriverRepository driverRepository2;

	@Autowired
	TripBookingRepository tripBookingRepository2;

	@Override
	public void register(Customer customer) {
		//Save the customer in database
		customerRepository2.save(customer);
	}

	@Override
	public void deleteCustomer(Integer customerId) {
		// Delete customer without using deleteById function
		Customer customer = customerRepository2.findById(customerId).get();
		customerRepository2.delete(customer);
	}

	@Override
	public TripBooking bookTrip(int customerId, String fromLocation, String toLocation, int distanceInKm) throws Exception{
		//Book the driver with lowest driverId who is free (cab available variable is Boolean.TRUE). If no driver is available, throw "No cab available!" exception
		//Avoid using SQL query

		TripBooking tripBooking = new TripBooking();
		tripBooking.setFromLocation(fromLocation);
		tripBooking.setToLocation(toLocation);
		tripBooking.setDistanceInKm(distanceInKm);

		List<Driver> drivers = driverRepository2.findAll();

		Driver reqDriver=null;
		int max = Integer.MAX_VALUE;
		for(Driver driver: drivers){
			if(driver.getCab().getAvailable() && driver.getDriverId()<max){
				max=driver.getDriverId();
				reqDriver = driver;
			}
		}

		if(reqDriver.equals(null)) {
			tripBooking.setStatus(TripStatus.CANCELED);
			tripBookingRepository2.save(tripBooking);
			throw new Exception("No cab available!");
		}

		Customer customer = customerRepository2.findById(customerId).get();
		tripBooking.setStatus(TripStatus.CONFIRMED);
		tripBooking.setDriver(reqDriver);

		tripBooking.setCustomer(customer);

		reqDriver.getTripBookingList().add(tripBooking);
		customer.getTripBookingList().add(tripBooking);


		customerRepository2.save(customer);
		return tripBooking;
	}

	@Override
	public void cancelTrip(Integer tripId){
		//Cancel the trip having given trip Id and update TripBooking attributes accordingly

		TripBooking tripBooking = tripBookingRepository2.findById(tripId).get();

		Customer customer = tripBooking.getCustomer();
		List<TripBooking> list = customer.getTripBookingList();
		for(int i=0; i<list.size(); i++){
			if(list.get(i).getTripBookingId()==tripId){
				list.get(i).setStatus(TripStatus.CANCELED);
				customer.setTripBookingList(list);
				break;
			}
		}

		Driver driver = tripBooking.getDriver();
		List<TripBooking> list1 = driver.getTripBookingList();
		for(int i=0; i<list1.size(); i++){
			if(list1.get(i).getTripBookingId()==tripId){
				list1.get(i).setStatus(TripStatus.CANCELED);
				driver.setTripBookingList(list1);
				break;
			}
		}

		tripBooking.setStatus(TripStatus.CANCELED);
		tripBookingRepository2.save(tripBooking);
	}

	@Override
	public void completeTrip(Integer tripId){
		//Complete the trip having given trip Id and update TripBooking attributes accordingly

		TripBooking tripBooking = tripBookingRepository2.findById(tripId).get();

		Customer customer = tripBooking.getCustomer();
		List<TripBooking> list = customer.getTripBookingList();
		for(int i=0; i<list.size(); i++){
			if(list.get(i).getTripBookingId()==tripId){
				list.get(i).setStatus(TripStatus.COMPLETED);
				customer.setTripBookingList(list);
				break;
			}
		}

		Driver driver = tripBooking.getDriver();
		List<TripBooking> list1 = driver.getTripBookingList();
		for(int i=0; i<list1.size(); i++){
			if(list1.get(i).getTripBookingId()==tripId){
				list1.get(i).setStatus(TripStatus.COMPLETED);
				driver.setTripBookingList(list1);
				break;
			}
		}

		tripBooking.setStatus(TripStatus.COMPLETED);
		tripBookingRepository2.save(tripBooking);
	}
}
