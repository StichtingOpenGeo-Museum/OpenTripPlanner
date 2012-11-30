package controllers;

import play.*;
import play.mvc.*;

import java.util.*;

import models.*;

public class FieldTrip extends Application {


    public static void index() {
        //index at present does nothing
        render();
    }

    /**
     * y/m/d are the day for which we would like a calendar.
     */
    public static void getCalendar(int year, int month, int day) {
        List<FieldTrip> fieldTrips;

        fieldTrips = ScheduledFieldTrip.find("year(departure) == ? and month(departure) == ? and day(departure) == ? " +
                                              "order by departure", 
                                              year, month, day).fetch();

        renderJSON(fieldTrips);
    }

    public static void getFieldTrip(int id) {
        ScheduledFieldTrip fieldTrip = ScheduledFieldTrip.findById(id);
        renderJSON(fieldTrip);
    }

    public static void newTrip(ScheduledFieldTrip trip) {
        trip.save();
        Long id = trip.id;
        render(id);
    }

    public static void addTripFeedback(FieldTripFeedback feedback) {
        feedback.id = null;
        feedback.save();
        render();
    }

    public static void addItinerary(GroupItinerary itinerary) {
        itinerary.save();
        Long id = itinerary.id;
        render(id);
    }

    public static void deleteTrip(Long id) {
        ScheduledFieldTrip trip = ScheduledFieldTrip.findById(id);
        trip.delete();
        render(id);
    }

}