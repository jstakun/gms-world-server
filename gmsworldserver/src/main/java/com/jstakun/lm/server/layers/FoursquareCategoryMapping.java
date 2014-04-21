/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jstakun.lm.server.layers;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jstakun
 */
public class FoursquareCategoryMapping {

    private static final Logger logger = Logger.getLogger(FoursquareCategoryMapping.class.getName());

    private static final String[][] ENTERTAINMENT = {
        {"4bf58dd8d48988d1e2931735", "114"},
        {"4bf58dd8d48988d1e4931735", "117"},
        {"4bf58dd8d48988d18e941735", "50"},
        {"4bf58dd8d48988d17f941735", "57"},
        {"4bf58dd8d48988d17e941735", "57"},
        {"4bf58dd8d48988d180941735", "57"},
        {"4bf58dd8d48988d181941735", "47"},
        {"4bf58dd8d48988d18f941735", "47"},
        {"4bf58dd8d48988d190941735", "47"},
        {"4bf58dd8d48988d192941735", "47"},
        {"4bf58dd8d48988d191941735", "47"},
        {"4bf58dd8d48988d1e5931735", "54"},
        {"4bf58dd8d48988d1e6931735", "54"},
        {"4bf58dd8d48988d1e7931735", "52"},
        {"4bf58dd8d48988d1e8931735", "54"},
        {"4bf58dd8d48988d1e9931735", "54"},
        {"4bf58dd8d48988d1f2931735", "114"},
        {"4bf58dd8d48988d1f3931735", "114"},
        {"4bf58dd8d48988d134941735", "114"},
        {"4bf58dd8d48988d135941735", "114"},
        {"4bf58dd8d48988d136941735", "114"},
        {"4bf58dd8d48988d137941735", "114"},
        {"4bf58dd8d48988d1e3931735", "55"},
        {"4bf58dd8d48988d1f4931735", "113"},
        {"4bf58dd8d48988d184941735", "113"},
        {"4bf58dd8d48988d18c941735", "113"},
        {"4bf58dd8d48988d18b941735", "113"},
        {"4bf58dd8d48988d18a941735", "113"},
        {"4bf58dd8d48988d189941735", "113"},
        {"4bf58dd8d48988d185941735", "113"},
        {"4bf58dd8d48988d188941735", "113"},
        {"4e39a891bd410d7aed40cbc2", "113"},
        {"4bf58dd8d48988d187941735", "113"},
        {"4bf58dd8d48988d183941735", "48"},
        {"4bf58dd8d48988d182941735", "113"},
        {"4bf58dd8d48988d193941735", "113"},
        {"4bf58dd8d48988d17b941735", "113"},
        {"4bf58dd8d48988d17c941735", "48"},
        {"4bf58dd8d48988d132951735", "55"},
        {"5032792091d4c4b30a586d5c", "54"}
    };
    private static final String[][] SHOPPING = {
        {"4bf58dd8d48988d116951735", "95"},
        {"4bf58dd8d48988d127951735", "95"},
        {"4bf58dd8d48988d124951735", "109"},
        {"4bf58dd8d48988d115951735", "106"},
        {"4bf58dd8d48988d1f1941735", "106"},
        {"4bf58dd8d48988d114951735", "100"},
        {"4bf58dd8d48988d11a951735", "93"},
        {"4bf58dd8d48988d117951735", "87"},
        {"4bf58dd8d48988d103951735", "93"},
        {"4bf58dd8d48988d102951735", "93"},
        {"4bf58dd8d48988d104951735", "93"},
        {"4bf58dd8d48988d105951735", "93"},
        {"4bf58dd8d48988d109951735", "93"},
        {"4bf58dd8d48988d106951735", "93"},
        {"4bf58dd8d48988d108951735", "93"},
        {"4bf58dd8d48988d107951735", "116"},
        {"4d954b0ea243a5684a65b473", "91"},
        {"4bf58dd8d48988d10c951735", "90"},
        {"4bf58dd8d48988d1f6941735", "91"},
        {"4bf58dd8d48988d1f4941735", "95"},
        {"4bf58dd8d48988d10f951735", "92"},
        {"4bf58dd8d48988d122951735", "89"},
        {"4bf58dd8d48988d1f7941735", "96"},
        {"4bf58dd8d48988d11b951735", "94"},
        {"4bf58dd8d48988d1f9941735", "87"},
        {"4bf58dd8d48988d11d951735", "87"},
        {"4bf58dd8d48988d11e951735", "87"},
        {"4bf58dd8d48988d11f951735", "87"},
        {"4bf58dd8d48988d1fa941735", "87"},
        {"4bf58dd8d48988d10e951735", "87"},
        {"4bf58dd8d48988d120951735", "87"},
        {"4bf58dd8d48988d1f5941735", "87"},
        {"4bf58dd8d48988d118951735", "87"},
        {"4bf58dd8d48988d186941735", "87"},
        {"4bf58dd8d48988d119951735", "87"},
        {"4bf58dd8d48988d1f8941735", "110"},
        {"4bf58dd8d48988d128951735", "94"},
        {"4bf58dd8d48988d112951735", "97"},
        {"4bf58dd8d48988d1fb941735", "96"},
        {"4bf58dd8d48988d111951735", "98"},
        {"4bf58dd8d48988d1fe941735", "100"},
        {"4bf58dd8d48988d121951735", "103"},
        {"4bf58dd8d48988d100951735", "88"},
        {"4bf58dd8d48988d10d951735", "100"},
        {"4bf58dd8d48988d1f2941735", "106"},
        {"4d1cf8421a97d635ce361c31", "90"},
        {"4bf58dd8d48988d1ed941735", "90"},
        {"4bf58dd8d48988d1de931735", "90"},
        {"4bf58dd8d48988d101951735", "93"},
        {"4bf58dd8d48988d1f3941735", "108"},
        {"4bf58dd8d48988d10b951735", "100"},
        {"4bf58dd8d48988d126951735", "89"},
        {"4bf58dd8d48988d1ff941735", "91"},
        {"4bf58dd8d48988d1e1931735", "89"},
        {"4d954afda243a5684865b473", "104"},
        {"4eb1bdf03b7b55596b4a7491", "89"},
        {"4f04afc02fb6e1c99f3db0bc", "89"},
        {"4eb1bdf03b7b55596b4a7491", "89"},
        {"4f04afc02fb6e1c99f3db0bc", "89"},
        {"4bf58dd8d48988d179941735", "87"},
        {"4bf58dd8d48988d123951735", "96"},
        {"4eb1bdde3b7b55596b4a7490", "105"},
        {"4bf58dd8d48988d1b1941735", "100"},
        {"4edd64a0c7ddd24ca188df1a", "87"},
   };
    private static final String[][] RESTAURANTS = {
        {"4bf58dd8d48988d1c8941735", "16"},
        {"4bf58dd8d48988d14e941735", "2"},
        {"4bf58dd8d48988d152941735", "28"},
        {"4bf58dd8d48988d107941735", "28"},
        {"4bf58dd8d48988d142941735", "3"},
        {"4bf58dd8d48988d16b941735", "5"},
        {"4bf58dd8d48988d143941735", "6"},
        {"4bf58dd8d48988d1df931735", "4"},
        {"4bf58dd8d48988d16c941735", "8"},
        {"4bf58dd8d48988d16a941735", "111"},
        {"4bf58dd8d48988d153941735", "29"},
        {"4bf58dd8d48988d16d941735", "11"},
        {"4bf58dd8d48988d17a941735", "9"},
        {"4bf58dd8d48988d144941735", "10"},
        {"4bf58dd8d48988d145941735", "12"},
        {"4bf58dd8d48988d1e0931735", "11"},
        {"4bf58dd8d48988d154941735", "13"},
        {"4bf58dd8d48988d1bc941735", "111"},
        {"4bf58dd8d48988d146941735", "14"},
        {"4bf58dd8d48988d1d0941735", "111"},
        {"4bf58dd8d48988d1f5931735", "12"},
        {"4bf58dd8d48988d147941735", "15"},
        {"4bf58dd8d48988d148941735", "111"},
        {"4bf58dd8d48988d10a941735", "16"},
        {"4bf58dd8d48988d10b941735", "16"},
        {"4bf58dd8d48988d16e941735", "22"},
        {"4bf58dd8d48988d114941735", "17"},
        {"4bf58dd8d48988d1cb941735", "17"},
        {"4bf58dd8d48988d10c941735", "18"},
        {"4bf58dd8d48988d108941735", "1"},
        {"4bf58dd8d48988d109941735", "1"},
        {"4bf58dd8d48988d10d941735", "19"},
        {"4bf58dd8d48988d10e941735", "20"},
        {"4bf58dd8d48988d16f941735", "21"},
        {"4bf58dd8d48988d1c9941735", "111"},
        {"4bf58dd8d48988d10f941735", "23"},
        {"4deefc054765f83613cdba6f", "3"},
        {"4bf58dd8d48988d110941735", "25"},
        {"4bf58dd8d48988d111941735", "26"},
        {"4bf58dd8d48988d112941735", "112"},
        {"4bf58dd8d48988d113941735", "27"},
        {"4bf58dd8d48988d1be941735", "28"},
        {"4bf58dd8d48988d1bf941735", "8"},
        {"4bf58dd8d48988d156941735", "3"},
        {"4bf58dd8d48988d1c0941735", "20"},
        {"4bf58dd8d48988d1c1941735", "29"},
        {"4bf58dd8d48988d115941735", "30"},
        {"4bf58dd8d48988d1c3941735", "31"},
        {"4bf58dd8d48988d157941735", "8"},
        {"4bf58dd8d48988d1ca941735", "32"},
        {"4def73e84765ae376e57713a", "1"},
        {"4bf58dd8d48988d1bd941735", "7"},
        {"4bf58dd8d48988d1c5941735", "34"},
        {"4bf58dd8d48988d1c6941735", "1"},
        {"4bf58dd8d48988d1ce941735", "35"},
        {"4bf58dd8d48988d1cd941735", "28"},
        {"4bf58dd8d48988d14f941735", "37"},
        {"4bf58dd8d48988d150941735", "39"},
        {"4bf58dd8d48988d1cc941735", "40"},
        {"4bf58dd8d48988d1d2941735", "41"},
        {"4bf58dd8d48988d158941735", "1"},
        {"4bf58dd8d48988d151941735", "29"},
        {"4bf58dd8d48988d1db931735", "39"},
        {"4bf58dd8d48988d149941735", "43"},
        {"4bf58dd8d48988d1d3941735", "45"},
        {"4bf58dd8d48988d14a941735", "46"},
        {"4d4ae6fc7a7b7dea34424761", "17"},
        {"4bf58dd8d48988d1f0941735", "11"},
        {"4bf58dd8d48988d169941735", "3"},
        {"4f04af1f2fb6e1c99f3db0bb", "30"}
    };
    private static final String[][] OTHER = {
        {"4bf58dd8d48988d175941735", "4", "76"}, //Gym or Fitness Center
        {"4bf58dd8d48988d104941735", "4", "69"}, //Medical Center
        {"4bf58dd8d48988d110951735", "3", "61"}, //Salon and Barbershop
        {"4bf58dd8d48988d1f7931735", "4", "72"}, //Travel Plane
        {"4bf58dd8d48988d1ed931735", "4", "72"}, //Travel Airplane
        {"4bf58dd8d48988d1fd941735", "4", ""}, //Shoping Mall
        {"4bf58dd8d48988d116941735", "2", "48"}, //Bar
        {"4bf58dd8d48988d1c4941735", "2", ""}, //Restaurant
        {"4bf58dd8d48988d123941735", "2", "48"}, //Wine Bar
        {"4bf58dd8d48988d11f941735", "2", "51"}, //Night Club
        {"4bf58dd8d48988d14c941735", "2", ""}, //Wings Joint
        {"4bf58dd8d48988d1fc941735", "4", "81"}, //Laundromat or Dry Cleaner
        {"4bf58dd8d48988d1fa931735", "4", "72"},//Hotel
        {"4bf58dd8d48988d1ee931735", "4", "72"}, //Hostel
        {"4bf58dd8d48988d12f951735", "4", "72"}, //"name":"Resorts"
        {"4bf58dd8d48988d118941735", "2", "48"}, //Dive Bar
        {"4bf58dd8d48988d121941735", "2", ""}, //Lounge
        {"4bf58dd8d48988d176941735", "4", "76"}, //Gym
        {"4bf58dd8d48988d1d5941735", "2", "48"}, //Hotel Bar
        {"4bf58dd8d48988d1f8931735", "4", "72"}, //"Bed & Breakfast"
        {"4d954af4a243a5684765b473", "4", "73"}, //Veterinarian"
        {"4bf58dd8d48988d1f1931735", "2", ""}, //,"General Entertainment"
        {"4bf58dd8d48988d1d6941735", "2", "48"}, //Strip Club"
        {"4bf58dd8d48988d131951735", "2", "48"}, //Hotel Bar
        {"4bf58dd8d48988d14b941735", "2", "48"}, //Winery
        {"4bf58dd8d48988d11b941735", "2", "48"}, //Pub
        {"4bf58dd8d48988d117941735", "2", "48"}, //Beer Garden
        {"4bf58dd8d48988d1dc931735", "2", "48"}, //Tea Room
        {"4bf58dd8d48988d11e941735", "2", "48"}, //Cocktail Bar
        {"4bf58dd8d48988d1d8941735", "2", "48"}, //Gay Bars
        {"4bf58dd8d48988d1d7941735", "2", "48"}, //Breweries
        {"4bf58dd8d48988d1cf941735", "2", "48"}, //Breweries
        {"4bf58dd8d48988d155941735", "2", "48"}, //"Gastropubs"
        {"4bf58dd8d48988d18d941735", "2", "48"}, //Gaming Cafes"
        {"4bf58dd8d48988d120941735", "2", "48"}, //"Karaoke Bars"
        {"4bf58dd8d48988d11d941735", "2", "48"},//"name":"Sports Bars"
        {"4bf58dd8d48988d122941735", "2", "48"},//"name":"Whisky Bars"
        {"4bf58dd8d48988d1d1941735", "1", ""}, //Ramen or Noodle House
        {"4bf58dd8d48988d119941735", "2", "48"},//"name":"Hookah Bars"
        {"4bf58dd8d48988d1ef941735", "7", "121"},//"name":"Rental Car Locations"
        {"4bf58dd8d48988d1de941735", "2", "48"}, //"Vineyards"
        {"4bf58dd8d48988d11c941735", "2", "48"}, //"name":"Sake Bars"
        {"4bf58dd8d48988d1fb931735", "7", "129"}, //"name":"Motels"
        {"4bf58dd8d48988d130951735", "4", "72"}, //Taxi
        {"4c38df4de52ce0d596b336e1", "4", "115"}, //Parking
    };
    private static final String[] UNCLASSIFIED = {
        "4bf58dd8d48988d132941735", //Church
        "4bf58dd8d48988d19f941735", //College Technology Building
        "4bf58dd8d48988d1a8941735", //"name":"General College & University"
        "4bf58dd8d48988d171941735",//"name":"Event Space"
        "4bf58dd8d48988d101941735",//"name":"Martial Arts Dojo"
        "4bf58dd8d48988d10a951735",//"name":"Bank"
        "4bf58dd8d48988d1ab941735",//"name":"Student Center"
        "4bf58dd8d48988d12c951735",//"name":"Embassy or Consulate"
        "4bf58dd8d48988d174941735",//"name":"Coworking Space"
        "4d4b7105d754a06375d81259",//"name":"Home, Work, Other"
        "4bf58dd8d48988d1a5941735",//"name":"College Lab"
        "4bf58dd8d48988d198941735",//"name":"College Academic Building
        "4bf58dd8d48988d1a0941735",//"name":"College Classroom
        "4bf58dd8d48988d1b0941735",//"name":"Fraternity House
        "4d4b7105d754a06374d81259",//,"name":"Food"
        "4eb1bea83b7b6f98df247e06",//Factories
        "4bf58dd8d48988d113951735",//"name":"Gas Stations or Garages"
        "4bf58dd8d48988d124941735",//"name":"Offices"
        "4bf58dd8d48988d13d941735",//"name":"High Schools"
        "4bf58dd8d48988d1e7941735",//"name":"Playgrounds"
        "4bf58dd8d48988d130941735",//"name":"Buildings"
        "4bf58dd8d48988d1e0941735",//"name":"Harbors or Marinas"
        "4bf58dd8d48988d1ff931735",//"name":"Convention Centers"
        "4bf58dd8d48988d12f941735",//"name":"Libraries"
        "4e52d2d203646f7c19daa8ae",//"name":"Animal Shelters"
        "4bf58dd8d48988d12d951735",//"name":"Boats or Ferries"
        "4bf58dd8d48988d162941735",//"name":"Other Great Outdoors"
        "4bf58dd8d48988d196941735",//"name":"Hospitals"
        "4bf58dd8d48988d1ae941735",//"name":"Universities"
        "4bf58dd8d48988d1a2941735",//"name":"Community Colleges"
        "4bf58dd8d48988d1ad941735",//"name":"Trade Schools"
        "4bf58dd8d48988d1e6941735",//"name":"Golf Courses"
        "4bf58dd8d48988d126941735",//"name":"Government Buildings"
        "4bf58dd8d48988d13b941735",//"name":"Schools"
        "4bf58dd8d48988d129941735",//"name":"City Halls"
        "4deefb944765f83613cdba6e",//"name":"Historic Sites"
        "4bf58dd8d48988d1f6931735",//"name":"General Travel"
        "4d954b06a243a5684965b473",//"name":"Apartment Buildings / Condo Complexes"
        "4bf58dd8d48988d125941735",//"name":"Tech Startups"
        "4bf58dd8d48988d129951735",//"name":"Train Stations"
        "4bf58dd8d48988d11a941735",//"name":"Other Nightlife"
        "4d954b16a243a5684b65b473",//"name":"Rest Areas"
        "4bf58dd8d48988d1f9931735",//"name":"Highways or Roads"
        "4bf58dd8d48988d1a3941735",//"name":"College Dorms"
        "4bf58dd8d48988d1b4941735",//"name":"College Stadiums"
        "4cce455aebf7b749d5e191f5",//"name":"Soccer Fields"
        "4f04ad622fb6e1c99f3db0b9",//"name":"Newsstands"
        "4bf58dd8d48988d1eb941735",//"name":"Ski Lodges"
        "4bf58dd8d48988d1a7941735",//"name":"College Libraries"
        "4bf58dd8d48988d178941735",//"name":"Dentist's Offices"
        "4bf58dd8d48988d1b2941735",//"name":"College Gyms"
        "4bf58dd8d48988d177941735",//"name":"Doctor's Offices"
        "4bf58dd8d48988d1a6941735",//"name":"Law Schools"
        "4bf58dd8d48988d1a1941735",//"name":"College Cafeterias"
        "4bf58dd8d48988d138941735",//"name":"Mosques"
        "4f4531b14b9074f6e4fb0103",//Laboratory
        "4bf58dd8d48988d1eb931735",//"name":"Airport Terminals"
        "4e52adeebd41615f56317744",//"name":"Military Bases"
        "4bf58dd8d48988d173941735",//"name":"Auditoriums"
        "4f04b08c2fb6e1c99f3db0bd",//"name":"Travel Agencies"
        "4f04b25d2fb6e1c99f3db0c0",//"name":"Travel Lounges"
        "4bf58dd8d48988d1a9941735",//"name":"College Rec Centers"
        "4f4530164b9074f6e4fb00ff",//"name":"Tourist Information Center"
        "4bf58dd8d48988d1f0931735",//"name":"Airport Gates"
        "4f2a25ac4b909258e854f55f",//"name":"Neighborhood"
        "4bf58dd8d48988d127941735",//"name":"Conference Rooms"
        "4bf58dd8d48988d164941735",//"name":"Plazas"
        "4f04b1572fb6e1c99f3db0bf",//"name":"Storage Facilities"
        "4e39a956bd410d7aed40cbc3",//"name":"Tennis Courts"
        "4bf58dd8d48988d172941735",//"name":"Post Offices"
        "4bf58dd8d48988d197941735",//"name":"College Administrative Buildings"
    };

    protected static String[] findMapping(String fsCategory) {

        for (int i = 0; i < ENTERTAINMENT.length; i++) {
            String[] cat = ENTERTAINMENT[i];
            if (cat[0].equals(fsCategory)) {
                String[] result = new String[]{"2", cat[1]};
                return result;
            }
        }

        for (int i = 0; i < SHOPPING.length; i++) {
            String[] cat = SHOPPING[i];
            if (cat[0].equals(fsCategory)) {
                String[] result = new String[]{"6", cat[1]};
                return result;
            }
        }

        for (int i = 0; i < RESTAURANTS.length; i++) {
            String[] cat = RESTAURANTS[i];
            if (cat[0].equals(fsCategory)) {
                String[] result = new String[]{"1", cat[1]};
                return result;
            }
        }

        for (int i = 0; i < OTHER.length; i++) {
            String[] cat = OTHER[i];
            if (cat[0].equals(fsCategory)) {
                String[] result = new String[]{cat[1], cat[2]};
                return result;
            }
        }

        for (int i = 0; i < UNCLASSIFIED.length; i++) {
            String cat = UNCLASSIFIED[i];
            if (cat.equals(fsCategory)) {
                logger.log(Level.INFO, "Found unclassified category: {0}", fsCategory);
                String[] result = new String[]{"8", ""};
                return result;
            }
        }

        logger.log(Level.WARNING, "No matching category: {0}", fsCategory);
        String[] result = new String[]{"8", ""};
        return result;

    }
}
