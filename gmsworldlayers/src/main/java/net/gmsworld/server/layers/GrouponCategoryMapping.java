/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.gmsworld.server.layers;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author jstakun
 */
public class GrouponCategoryMapping {

    private static final Logger logger = Logger.getLogger(GrouponCategoryMapping.class.getName());
    private static final String[][] PARENT_CATEGORIES = {
        {"Arts and Entertainment", "2"},
        {"Automotive", "4"},
        {"Beauty & Spas", "3"},
        {"Food & Drink", "6"},
        {"Health & Fitness", "4"},
        {"Home Services", "4"},
        {"Nightlife", "2"},
        {"Pets", "4"},
        {"Professional Services", "4"},
        {"Restaurants", "1"},
        {"Shopping", "6"},
        {"Travel", "4"},
        {"Education", "4"},
        {"Tickets", "2"},
        {"Services", "4"},
        {"Gifts & Giving", "6"}
    };
    private static final String[][] GROUPON_CATEGORIES = {
        {"Alcohol Event", "Arts and Entertainment", "113", "2"},
        {"Amusement Parks", "Arts and Entertainment", "113", "2"},
        {"Aquariums", "Arts and Entertainment", "113", "2"},
        {"Arcades", "Arts and Entertainment", "113", "2"},
        {"Art Galleries", "Arts and Entertainment", "114", "2"},
        {"Arts & Crafts Activites", "Arts and Entertainment", "114", "2"},
        {"Arts/Crafts/Hobbies", "Arts and Entertainment", "114", "2"},
        {"Ballet & Dance", "Arts and Entertainment", "114", "2"},
        {"Dance", "Arts and Entertainment", "114", "2"},
        {"Biking", "Arts and Entertainment", "113", "2"},
        {"Bike Tours", "Arts and Entertainment", "113", "2"},
        {"Bus Tours", "Arts and Entertainment", "113", "2"},
        {"Boating", "Arts and Entertainment", "113", "2"},
        {"Botanical Gardens", "Arts and Entertainment", "113", "2"},
        {"Bowling", "Arts and Entertainment", "117", "2"},
        {"Canoe & Kayak Rentals", "Arts and Entertainment", "113", "2"},
        {"Casinos", "Arts and Entertainment", "113", "2"},
        {"Children's Museum", "Arts and Entertainment", "54", "2"},
        {"Circus", "Arts and Entertainment", "113", "2"},
        {"Country Clubs", "Arts and Entertainment", "", "2"},
        {"Dance Companies", "Arts and Entertainment", "", "2"},
        {"Dinner Theater", "Arts and Entertainment", "57", "2"},
        {"Entertainment", "Arts and Entertainment", "113", "2"},
        {"Festivals", "Arts and Entertainment", "54", "2"},
        {"Fishing", "Arts and Entertainment", "113", "2"},
        {"Fun Center", "Arts and Entertainment", "113", "2"},
        {"Fun Centers", "Arts and Entertainment", "113", "2"},
        {"Gambling & Gaming", "Arts and Entertainment", "113", "2"},
        {"Go Karts", "Arts and Entertainment", "113", "2"},
        {"Plane & Helicopter Tours", "Arts and Entertainment", "113", "2"},
        {"Hot Air Balloon", "Arts and Entertainment", "113", "2"},
        {"Kid's Activities", "Arts and Entertainment", "113", "2"},
        {"Laser Tag", "Arts and Entertainment", "113", "2"},
        {"Live Music", "Arts and Entertainment", "54", "2"},
        {"Music Concert", "Arts and Entertainment", "54", "2"},
        {"Miniature Golf", "Arts and Entertainment", "113", "2"},
        {"Movie Theaters", "Arts and Entertainment", "57", "2"},
        {"Museums", "Arts and Entertainment", "54", "2"},
        {"Museum", "Arts and Entertainment", "54", "2"},
        {"Opera", "Arts and Entertainment", "54", "2"},
        {"Outdoor Pursuits", "Arts and Entertainment", "113", "2"},
        {"Paintball", "Arts and Entertainment", "113", "2"},
        {"Psychics & Astrologers", "Arts and Entertainment", "113", "2"},
        {"Private Event", "Arts and Entertainment", "113", "2"},
        {"Segway Tours", "Arts and Entertainment", "113", "2"},
        {"Skating", "Arts and Entertainment", "113", "2"},
        {"Skydiving", "Arts and Entertainment", "113", "2"},
        {"Speedway", "Arts and Entertainment", "113", "2"},
        {"Sporting Events", "Arts and Entertainment", "113", "2"},
        {"Symphony & Orchestra", "Arts and Entertainment", "114", "2"},
        {"Theater & Plays", "Arts and Entertainment", "57", "2"},
        {"Theme Parks", "Arts and Entertainment", "113", "2"},
        {"Tours", "Arts and Entertainment", "56", "2"},
        {"Walking Tours", "Arts and Entertainment", "56", "2"},
        {"Water Parks", "Arts and Entertainment", "113", "2"},
        {"Yacht Clubs", "Arts and Entertainment", "56", "2"},
        {"Zoos", "Arts and Entertainment", "113", "2"},
        {"Horse/Carriage Ride", "Arts and Entertainment", "113", "2"},
        {"Rentals", "Arts and Entertainment", "113", "2"},
        {"Dance Lessons", "Education", "74", "4"},
        {"Auto Glass Services", "Automotive", "119", "4"},
        {"Auto Parts & Accessories", "Automotive", "119", "4"},
        {"Auto Repair & Services", "Automotive", "119", "4"},
        {"Body Shops & Painting", "Automotive", "119", "4"},
        {"Car Dealers", "Automotive", "119", "4"},
        {"Car Wash & Detailing", "Automotive", "119", "4"},
        {"Gas & Services Stations", "Automotive", "119", "4"},
        {"Motorcycle Dealers", "Automotive", "119", "4"},
        {"Motorcycle Repair", "Automotive", "119", "4"},
        {"Oil Change Stations", "Automotive", "119", "4"},
        {"Oil Change", "Automotive", "119", "4"},
        {"Parking", "Automotive", "115", "4"},
        {"Scooters & Mopeds", "Automotive", "119", "4"},
        {"Stereo Installation", "Automotive", "119", "4"},
        {"Tires & Wheels", "Automotive", "119", "4"},
        {"Towing", "Automotive", "119", "4"},
        {"Beauty Supply", "Beauty & Spas", "", "3"},
        {"Spa Package", "Beauty & Spas", "", "3"},
        {"Day Spas", "Beauty & Spas", "65", "3"},
        {"Eyelash Services", "Beauty & Spas", "", "3"},
        {"Hair Removal", "Beauty & Spas", "62", "3"},
        {"Laser Hair Removal", "Beauty & Spas", "62", "3"},
        {"Hair Salons & Barbers", "Beauty & Spas", "61", "3"},
        {"Barber", "Beauty & Spas", "61", "3"},
        {"Hair Salon", "Beauty & Spas", "61", "3"},
        {"Makeup Artists", "Beauty & Spas", "61", "3"},
        {"Massage", "Beauty & Spas", "59", "3"},
        {"Medical Spas", "Beauty & Spas", "65", "3"},
        {"Men's Salons", "Beauty & Spas", "65", "3"},
        {"Men's Salon", "Beauty & Spas", "65", "3"},
        {"Nail Salons", "Beauty & Spas", "63", "3"},
        {"Nail Salon", "Beauty & Spas", "63", "3"},
        {"Piercing", "Beauty & Spas", "68", "3"},
        {"Skin Care & Facials", "Beauty & Spas", "64", "3"},
        {"Tanning Salons", "Beauty & Spas", "66", "3"},
        {"Tanning Salon", "Beauty & Spas", "66", "3"},
        {"Tattoo Removal", "Beauty & Spas", "76", "3"},
        {"Tattoos", "Beauty & Spas", "67", "3"},
        {"Tattoo", "Beauty & Spas", "67", "3"},
        {"Waxing", "Beauty & Spas", "62", "3"},
        {"Alcohol Store", "Food & Drink", "87", "6"},
        {"Bagel Shops", "Food & Drink", "87", "6"},
        {"Bakeries", "Food & Drink", "87", "6"},
        {"Dessert/Bakery", "Food & Drink", "87", "6"},
        {"Breweries", "Food & Drink", "87", "6"},
        {"Butchers & Meat Shops", "Food & Drink", "87", "6"},
        {"Candy Stores", "Food & Drink", "87", "6"},
        {"Cheese Shops", "Food & Drink", "87", "6"},
        {"Chocolate Shops", "Food & Drink", "87", "6"},
        {"Convenience Stores", "Food & Drink", "87", "6"},
        {"Cupcakes", "Food & Drink", "87", "6"},
        {"Espresso Bars", "Food & Drink", "48", "2"},
        {"Ethnic Foods", "Food & Drink", "87", "6"},
        {"Farmers Market", "Food & Drink", "87", "6"},
        {"Food Delivery Services", "Food & Drink", "", "6"},
        {"Fruits & Veggies", "Food & Drink", "87", "6"},
        {"Gourmet Foods", "Food & Drink", "87", "6"},
        {"Grocery Stores", "Food & Drink", "87", "6"},
        {"Health Stores", "Food & Drink", "87", "6"},
        {"Home Brewing", "Food & Drink", "87", "6"},
        {"Ice Cream & Frozen Yogurt", "Food & Drink", "87", "6"},
        {"Juice Bars & Smoothies", "Food & Drink", "112", "1"},
        {"Liquor Stores", "Food & Drink", "87", "6"},
        {"Seafood Markets", "Food & Drink", "87", "6"},
        {"Snack Bars", "Food & Drink", "48", "2"},
        {"Specialty Food", "Food & Drink", "87", "6"},
        {"Takeout", "Food & Drink", "48", "2"},
        {"Wine Shops", "Food & Drink", "87", "6"},
        {"Wineries", "Food & Drink", "87", "6"},
        {"Acupuncture", "Health & Fitness", "69", "4"},
        {"Addiction Treatment Centers", "Health & Fitness", "69", "4"},
        {"Adult Day Care Centers", "Health & Fitness", "78", "4"},
        {"Allergists", "Health & Fitness", "69", "4"},
        {"Alternative Medicine Practitioners", "Health & Fitness", "69", "4"},
        {"Ambulance Services", "Health & Fitness", "69", "4"},
        {"Anesthesiologists", "Health & Fitness", "69", "4"},
        {"Aromatherapy", "Health & Fitness", "69", "4"},
        {"Assisted Living Facilities", "Health & Fitness", "69", "4"},
        {"Bootcamps", "Health & Fitness", "69", "4"},
        {"Bootcamp", "Health & Fitness", "69", "4"},
        {"Cannabis Clinics/Evaluations", "Health & Fitness", "69", "4"},
        {"Cardiologists", "Health & Fitness", "69", "4"},
        {"Child Psychologists", "Health & Fitness", "69", "4"},
        {"Chiropractors", "Health & Fitness", "69", "4"},
        {"Chiropractor", "Health & Fitness", "69", "4"},
        {"Cosmetic Dentistry / Teeth Whitening", "Health & Fitness", "69", "4"},
        {"Teeth Whitening", "Health & Fitness", "69", "4"},
        {"Cosmetic Surgeons", "Health & Fitness", "69", "4"},
        {"Counseling & Mental Health", "Health & Fitness", "69", "4"},
        {"Dentists", "Health & Fitness", "69", "4"},
        {"Dermatologists", "Health & Fitness", "69", "4"},
        {"Detoxification", "Health & Fitness", "69", "4"},
        {"Doctors", "Health & Fitness", "69", "4"},
        {"Ear, Nose & Throat", "Health & Fitness", "69", "4"},
        {"Endodontists", "Health & Fitness", "69", "4"},
        {"Exercise Equipment", "Health & Fitness", "69", "4"},
        {"Family Practice", "Health & Fitness", "69", "4"},
        {"Fertility", "Health & Fitness", "69", "4"},
        {"Fitness Classes", "Health & Fitness", "69", "4"},
        {"Gerontologists", "Health & Fitness", "69", "4"},
        {"Golf", "Health & Fitness", "69", "4"},
        {"Gyms & Fitness Centers", "Health & Fitness", "76", "4"},
        {"Health Clubs", "Health & Fitness", "69", "4"},
        {"Holistic Medicine", "Health & Fitness", "69", "4"},
        {"Home Health Care", "Health & Fitness", "78", "4"},
        {"Hospitals", "Health & Fitness", "69", "4"},
        {"Infectious Disease Physicians", "Health & Fitness", "69", "4"},
        {"Internal Medicine", "Health & Fitness", "69", "4"},
        {"Laser Eye Surgery/Lasik", "Health & Fitness", "69", "4"},
        {"Martial Arts", "Health & Fitness", "69", "4"},
        {"Medical Groups", "Health & Fitness", "69", "4"},
        {"Midwives", "Health & Fitness", "69", "4"},
        {"Naturopathic", "Health & Fitness", "69", "4"},
        {"Neonatal Physicians", "Health & Fitness", "69", "4"},
        {"Nutritionists", "Health & Fitness", "69", "4"},
        {"Obstetricians and Gynecologists", "Health & Fitness", "69", "4"},
        {"Occupational Medical Physicians", "Health & Fitness", "69", "4"},
        {"Ophthalmologists", "Health & Fitness", "69", "4"},
        {"Optometrists", "Health & Fitness", "69", "4"},
        {"Oral Surgeons", "Health & Fitness", "69", "4"},
        {"Orthodontists", "Health & Fitness", "69", "4"},
        {"Orthopedists", "Health & Fitness", "69", "4"},
        {"Osteopathic Physicians", "Health & Fitness", "69", "4"},
        {"Pain Management Physicians", "Health & Fitness", "69", "4"},
        {"Pediatric Dentists", "Health & Fitness", "69", "4"},
        {"Pediatricians", "Health & Fitness", "69", "4"},
        {"Periodontists", "Health & Fitness", "69", "4"},
        {"Personal Trainers", "Health & Fitness", "69", "4"},
        {"Pharmacies", "Health & Fitness", "69", "4"},
        {"Physical Therapy", "Health & Fitness", "69", "4"},
        {"Pilates", "Health & Fitness", "69", "4"},
        {"Podiatrists", "Health & Fitness", "69", "4"},
        {"Proctologists", "Health & Fitness", "69", "4"},
        {"Psychiatrists", "Health & Fitness", "69", "4"},
        {"Racquetball Clubs", "Health & Fitness", "69", "4"},
        {"Recreation Centers", "Health & Fitness", "69", "4"},
        {"Retirement Homes", "Health & Fitness", "69", "4"},
        {"Rock Climbing", "Health & Fitness", "69", "4"},
        {"Sexy Fitness", "Health & Fitness", "69", "4"},
        {"Skiing", "Health & Fitness", "69", "4"},
        {"Sports Medicine", "Health & Fitness", "69", "4"},
        {"Tennis", "Health & Fitness", "69", "4"},
        {"Urgent Care", "Health & Fitness", "69", "4"},
        {"Weight Loss Centers", "Health & Fitness", "69", "4"},
        {"Yoga", "Health & Fitness", "75", "4"},
        {"Agricultural Services", "Home Services", "70", "4"},
        {"Appliance Repair & Supplies", "Home Services", "70", "4"},
        {"Building Supplies", "Home Services", "70", "4"},
        {"Cable & Satellite Equipment & Services", "Home Services", "70", "4"},
        {"Carpet Cleaning", "Home Services", "70", "4"},
        {"Carpeting", "Home Services", "70", "4"},
        {"Chimney Sweep", "Home Services", "70", "4"},
        {"Cleaning Services & Supplies", "Home Services", "70", "4"},
        {"Contractors", "Home Services", "70", "4"},
        {"Custom Home Builders", "Home Services", "70", "4"},
        {"Electricians", "Home Services", "70", "4"},
        {"Fire Protection", "Home Services", "70", "4"},
        {"Fireplaces", "Home Services", "70", "4"},
        {"Garage Doors", "Home Services", "70", "4"},
        {"Gardeners", "Home Services", "70", "4"},
        {"Gutter Cleaning Services", "Home Services", "70", "4"},
        {"Handyman Services", "Home Services", "70", "4"},
        {"Heating, Ventilation & Air Conditioning", "Home Services", "70", "4"},
        {"Heating", "Home Services", "70", "4"},
        {"Home", "Home Services", "70", "4"},
        {"Home Cleaning", "Home Services", "70", "4"},
        {"Home Inspectors", "Home Services", "70", "4"},
        {"Home Repair", "Home Services", "70", "4"},
        {"Home Theatre Installation", "Home Services", "70", "4"},
        {"House Cleaning", "Home Services", "70", "4"},
        {"Interior Designers & Decorators", "Home Services", "70", "4"},
        {"Keys & Locksmiths", "Home Services", "70", "4"},
        {"Landscape Architects", "Home Services", "70", "4"},
        {"Landscaping", "Home Services", "70", "4"},
        {"Lawn Care Services", "Home Services", "70", "4"},
        {"Movers", "Home Services", "70", "4"},
        {"Painters", "Home Services", "70", "4"},
        {"Pest & Animal Control", "Home Services", "70", "4"},
        {"Plumbing", "Home Services", "70", "4"},
        {"Pool Cleaners", "Home Services", "70", "4"},
        {"Roofing", "Home Services", "70", "4"},
        {"Security Systems", "Home Services", "70", "4"},
        {"Snow Removal Services", "Home Services", "70", "4"},
        {"Solar Installation", "Home Services", "70", "4"},
        {"Sprinklers & Irrigation", "Home Services", "70", "4"},
        {"Swimming Pool Equipment & Supplies", "Home Services", "70", "4"},
        {"Tree Services", "Home Services", "70", "4"},
        {"Utilities - Gas, Water & Electric", "Home Services", "70", "4"},
        {"Vacuum Cleaners", "Home Services", "70", "4"},
        {"Waste Management Services", "Home Services", "70", "4"},
        {"Water Heaters", "Home Services", "70", "4"},
        {"Window Installation", "Home Services", "70", "4"},
        {"Window Washing", "Home Services", "70", "4"},
        {"Bars", "Nightlife", "48", "2"},
        {"Champagne Bars", "Nightlife", "48", "2"},
        {"Cigar Bars", "Nightlife", "48", "2"},
        {"Cocktail Bars", "Nightlife", "48", "2"},
        {"Comedy Clubs", "Nightlife", "50", "2"},
        {"Dance Clubs", "Nightlife", "51", "2"},
        {"Dive Bars", "Nightlife", "48", "2"},
        {"Gay Bars", "Nightlife", "48", "2"},
        {"Hookah Bars", "Nightlife", "48", "2"},
        {"Irish Pubs", "Nightlife", "48", "2"},
        {"Jazz & Blues Clubs", "Nightlife", "52", "2"},
        {"Karaoke", "Nightlife", "53", "2"},
        {"Lounges", "Nightlife", "49", "2"},
        {"Music Venues", "Nightlife", "54", "2"},
        {"Night Clubs", "Nightlife", "48", "2"},
        {"Piano Bars", "Nightlife", "48", "2"},
        {"Pool Halls", "Nightlife", "55", "2"},
        {"Pubs", "Nightlife", "48", "2"},
        {"Pubs/Sports Bars", "Nightlife", "48", "2"},
        {"Social Clubs", "Nightlife", "48", "2"},
        {"Sports Bars", "Nightlife", "48", "2"},
        {"Wine Bars", "Nightlife", "48", "2"},
        {"Animal Hospitals", "Pets", "73", "4"},
        {"Animal Shelters", "Pets", "73", "4"},
        {"Breeders", "Pets", "73", "4"},
        {"Dog Walkers", "Pets", "73", "4"},
        {"Pet Services", "Pets", "73", "4"},
        {"Horse Services & Equipment", "Pets", "73", "4"},
        {"Pet Boarding/Pet Sitting", "Pets", "73", "4"},
        {"Pet Groomers", "Pets", "73", "4"},
        {"Pet Stores", "Pets", "88", "6"},
        {"Pet Store", "Pets", "88", "6"},
        {"Pet Training", "Pets", "73", "4"},
        {"Veterinarians", "Pets", "73", "4"},
        {"Accountants", "Professional Services", "71", "4"},
        {"Advertising", "Professional Services", "71", "4"},
        {"Appraisers", "Professional Services", "71", "4"},
        {"Architects", "Professional Services", "71", "4"},
        {"Career Counseling", "Professional Services", "71", "4"},
        {"Caretakers", "Professional Services", "71", "4"},
        {"Catering & Bartending Services", "Professional Services", "71", "4"},
        {"Charity", "Professional Services", "71", "4"},
        {"Child Day Care", "Professional Services", "78", "4"},
        {"Construction Companies", "Professional Services", "71", "4"},
        {"Consultants", "Professional Services", "71", "4"},
        {"Copy Shops", "Professional Services", "71", "4"},
        {"Courier & Delivery Services", "Professional Services", "80", "4"},
        {"Dating Services", "Professional Services", "71", "4"},
        {"Demolition Companies", "Professional Services", "71", "4"},
        {"Diaper Services", "Professional Services", "71", "4"},
        {"DJs", "Professional Services", "71", "4"},
        {"Dry Cleaning & Laundry", "Professional Services", "81", "4"},
        {"Electronics Repair", "Professional Services", "71", "4"},
        {"Employment Agencies", "Professional Services", "71", "4"},
        {"Engineers", "Professional Services", "71", "4"},
        {"Equipment Rental", "Professional Services", "71", "4"},
        {"Equipment Repair", "Professional Services", "71", "4"},
        {"Event Planner", "Professional Services", "71", "4"},
        {"Family Counselors", "Professional Services", "71", "4"},
        {"Fashion Design", "Professional Services", "71", "4"},
        {"Florists", "Professional Services", "71", "4"},
        {"Freight Services", "Professional Services", "71", "4"},
        {"Funeral Services & Cemeteries", "Professional Services", "71", "4"},
        {"Furniture Reupholstery", "Professional Services", "71", "4"},
        {"Genealogists", "Professional Services", "71", "4"},
        {"General Contractors", "Professional Services", "71", "4"},
        {"Graphic Design", "Professional Services", "71", "4"},
        {"Internet Services Providers", "Professional Services", "71", "4"},
        {"Investigation Services", "Professional Services", "71", "4"},
        {"IT Services & Computer Repair", "Professional Services", "83", "4"},
        {"Junk Removal and Hauling", "Professional Services", "71", "4"},
        {"Life Coaches", "Professional Services", "71", "4"},
        {"Marketing", "Professional Services", "71", "4"},
        {"Modeling", "Professional Services", "71", "4"},
        {"Music Production", "Professional Services", "71", "4"},
        {"Non-Profit Organizations", "Professional Services", "71", "4"},
        {"Notaries", "Professional Services", "71", "4"},
        {"Party & Event Planning", "Professional Services", "71", "4"},
        {"Personal Chefs", "Professional Services", "71", "4"},
        {"Personal Shopping", "Professional Services", "71", "4"},
        {"Photographers", "Professional Services", "71", "4"},
        {"Photography", "Professional Services", "71", "4"},
        {"Portrait Studios", "Professional Services", "71", "4"},
        {"Printing & Copying Equipment & Services", "Professional Services", "79", "4"},
        {"Public & Social Services", "Professional Services", "71", "4"},
        {"Publishers", "Professional Services", "71", "4"},
        {"Recording & Rehearsal Studios", "Professional Services", "71", "4"},
        {"Recruiters", "Professional Services", "71", "4"},
        {"Recycling Centers", "Professional Services", "71", "4"},
        {"Screen Printing & Embroidery", "Professional Services", "71", "4"},
        {"Secretarial Services", "Professional Services", "71", "4"},
        {"Security Guards", "Professional Services", "71", "4"},
        {"Self Storage", "Professional Services", "84", "4"},
        {"Sewing & Alterations", "Professional Services", "85", "4"},
        {"Shipping Centers & Mail Services", "Professional Services", "71", "4"},
        {"Shoe Repair", "Professional Services", "86", "4"},
        {"Singing Telegrams", "Professional Services", "71", "4"},
        {"Taxidermy", "Professional Services", "71", "4"},
        {"Ticket Sales", "Professional Services", "71", "4"},
        {"Video & Film Production", "Professional Services", "71", "4"},
        {"Volunteer Organizations", "Professional Services", "71", "4"},
        {"Watch Repair", "Professional Services", "71", "4"},
        {"Website Design", "Professional Services", "71", "4"},
        {"Wedding Planning", "Professional Services", "71", "4"},
        {"Writing Services", "Professional Services", "71", "4"},
        {"Afghan", "Restaurants", "", "1"},
        {"African", "Restaurants", "16", "1"},
        {"American", "Restaurants", "2", "1"},
        {"American/Traditional", "Restaurants", "2", "1"},
        {"Andouille", "Restaurants", "", "1"},
        {"Argentine", "Restaurants", "28", "1"},
        {"Armenian", "Restaurants", "", "1"},
        {"Asian Fusion", "Restaurants", "3", "1"},
        {"Barbeque", "Restaurants", "4", "1"},
        {"Basque", "Restaurants", "39", "1"},
        {"Belgian", "Restaurants", "1", "1"},
        {"Brasseries", "Restaurants", "", "1"},
        {"Brazilian", "Restaurants", "5", "1"},
        {"Breakfast & Brunch", "Restaurants", "6", "1"},
        {"British", "Restaurants", "1", "1"},
        {"Buffets", "Restaurants", "7", "1"},
        {"Burgers", "Restaurants", "8", "1"},
        {"Burmese", "Restaurants", "", "1"},
        {"Café", "Restaurants", "11", "1"},
        {"Cafe", "Restaurants", "11", "1"},
        {"Coffee & Tea", "Restaurants", "11", "1"},
        {"Cafeteria", "Restaurants", "11", "1"},
        {"Cajun", "Restaurants", "9", "1"},
        {"Cambodian", "Restaurants", "", "1"},
        {"Caribbean", "Restaurants", "10", "1"},
        {"Cheese steaks", "Restaurants", "", "1"},
        {"Chicken Wings", "Restaurants", "", "1"},
        {"Chinese", "Restaurants", "12", "1"},
        {"Coffee House", "Restaurants", "11", "1"},
        {"Continental", "Restaurants", "", "1"},
        {"Creole", "Restaurants", "9", "1"},
        {"Creperies", "Restaurants", "", "1"},
        {"Cuban", "Restaurants", "13", "1"},
        {"Cyber Café", "Restaurants", "11", "1"},
        {"Delis", "Restaurants", "14", "1"},
        {"Dessert", "Restaurants", "111", "1"},
        {"Dim Sum", "Restaurants", "12", "1"},
        {"Diners", "Restaurants", "15", "1"},
        {"English", "Restaurants", "1", "1"},
        {"Ethiopian", "Restaurants", "16", "1"},
        {"Family", "Restaurants", "", "1"},
        {"Fast Food", "Restaurants", "22", "1"},
        {"Filipino", "Restaurants", "", "1"},
        {"Fine Dining", "Restaurants", "", "1"},
        {"Fish & Chips", "Restaurants", "", "1"},
        {"Fondue", "Restaurants", "", "1"},
        {"Food Stands", "Restaurants", "", "1"},
        {"French", "Restaurants", "18", "1"},
        {"Gastropubs", "Restaurants", "", "1"},
        {"German", "Restaurants", "19", "1"},
        {"Gluten-Free", "Restaurants", "", "1"},
        {"Greek", "Restaurants", "20", "1"},
        {"Halal", "Restaurants", "", "1"},
        {"Hawaiian", "Restaurants", "", "1"},
        {"Himalayan", "Restaurants", "", "1"},
        {"Hot Dogs", "Restaurants", "21", "1"},
        {"Hungarian", "Restaurants", "", "1"},
        {"Indian", "Restaurants", "23", "1"},
        {"Indonesian", "Restaurants", "", "1"},
        {"Iranian", "Restaurants", "", "1"},
        {"Irish", "Restaurants", "24", "1"},
        {"Italian", "Restaurants", "25", "1"},
        {"Jamaican", "Restaurants", "", "1"},
        {"Japanese", "Restaurants", "26", "1"},
        {"Korean", "Restaurants", "27", "1"},
        {"Kosher", "Restaurants", "118", "1"},
        {"Latin American", "Restaurants", "28", "1"},
        {"Lebanese", "Restaurants", "", "1"},
        {"Live Food", "Restaurants", "", "1"},
        {"Malaysian", "Restaurants", "", "1"},
        {"Mediterranean", "Restaurants", "20", "1"},
        {"Mexican", "Restaurants", "29", "1"},
        {"Mexican/Latin Restaurant", "Restaurants", "29", "1"},
        {"Middle Eastern", "Restaurants", "30", "1"},
        {"Modern European", "Restaurants", "1", "1"},
        {"Mongolian", "Restaurants", "", "1"},
        {"Moroccan", "Restaurants", "31", "1"},
        {"Nepalese", "Restaurants", "", "1"},
        {"Organic", "Restaurants", "", "1"},
        {"Oyster Bars", "Restaurants", "", "1"},
        {"Packaged Meals", "Restaurants", "", "1"},
        {"Pakistani", "Restaurants", "23", "1"},
        {"Pancake House", "Restaurants", "", "1"},
        {"Persian", "Restaurants", "", "1"},
        {"Peruvian", "Restaurants", "", "1"},
        {"Pizza", "Restaurants", "32", "1"},
        {"Po Boys", "Restaurants", "", "1"},
        {"Polish", "Restaurants", "1", "1"},
        {"Portuguese", "Restaurants", "1", "1"},
        {"Raw Food", "Restaurants", "", "1"},
        {"Russian", "Restaurants", "33", "1"},
        {"Sandwiches", "Restaurants", "34", "1"},
        {"Sandwich/Deli", "Restaurants", "34", "1"},
        {"Scandinavian", "Restaurants", "1", "1"},
        {"Seafood", "Restaurants", "35", "1"},
        {"Singaporean", "Restaurants", "36", "1"},
        {"Small Plates", "Restaurants", "", "1"},
        {"Soul Food", "Restaurants", "37", "1"},
        {"Soup", "Restaurants", "", "1"},
        {"Southern", "Restaurants", "38", "1"},
        {"Southwestern", "Restaurants", "38", "1"},
        {"Spanish", "Restaurants", "39", "1"},
        {"Steakhouses", "Restaurants", "40", "1"},
        {"Surinamese", "Restaurants", "", "1"},
        {"Sushi Bars", "Restaurants", "41", "1"},
        {"Asian/Sushi", "Restaurants", "41", "1"},
        {"Swiss", "Restaurants", "1", "1"},
        {"Taiwanese", "Restaurants", "", "1"},
        {"Tapas", "Restaurants", "39", "1"},
        {"Tea Rooms", "Restaurants", "", "1"},
        {"Tex-Mex", "Restaurants", "42", "1"},
        {"Thai", "Restaurants", "43", "1"},
        {"Turkish", "Restaurants", "", "1"},
        {"Ukrainian", "Restaurants", "1", "1"},
        {"Vegan", "Restaurants", "44", "1"},
        {"Vegetarian", "Restaurants", "45", "1"},
        {"Organic/Vegetarian", "Restaurants", "45", "1"},
        {"Vietnamese", "Restaurants", "46", "1"},
        {"Accessories", "Shopping", "", "6"},
        {"Antiques", "Shopping", "95", "6"},
        {"Appliances", "Shopping", "82", "4"},
        {"Art", "Shopping", "95", "6"},
        {"Arts & Crafts Supplies", "Shopping", "95", "6"},
        {"Athletic Apparel", "Shopping", "106", "6"},
        {"Auctions", "Shopping", "", "6"},
        {"Baby Furniture", "Shopping", "110", "6"},
        {"Baby Gear", "Shopping", "", "6"},
        {"Bike Shops", "Shopping", "106", "6"},
        {"Boutiques", "Shopping", "93", "6"},
        {"Bridal", "Shopping", "6", "6"},
        {"Cards & Stationery", "Shopping", "", "6"},
        {"Carpet & Flooring", "Shopping", "97", "6"},
        {"Children's Clothing", "Shopping", "93", "6"},
        {"Clothing Sales", "Shopping", "93", "6"},
        {"Collectibles", "Shopping", "", "6"},
        {"Comic Books", "Shopping", "100", "6"},
        {"Computers", "Shopping", "89", "6"},
        {"Costumes", "Shopping", "93", "6"},
        {"Dance Apparel", "Shopping", "", "6"},
        {"Department Stores", "Shopping", "91", "6"},
        {"Discount Stores", "Shopping", "91", "6"},
        {"Drugstores", "Shopping", "92", "6"},
        {"Electronics", "Shopping", "89", "6"},
        {"Eyewear & Opticians", "Shopping", "104", "6"},
        {"Eyewear", "Shopping", "104", "6"},
        {"Fabric Stores", "Shopping", "", "6"},
        {"Flea Markets", "Shopping", "87", "6"},
        {"Florists", "Shopping", "", "6"},
        {"Formal Wear", "Shopping", "93", "6"},
        {"Framing", "Shopping", "97", "6"},
        {"Furniture Stores", "Shopping", "110", "6"},
        {"Gift Shops", "Shopping", "94", "6"},
        {"Gifts & Giving", "Shopping", "94", "6"},
        {"Hardware Stores", "Shopping", "97", "6"},
        {"Hobby Shops", "Shopping", "96", "6"},
        {"Home Décor", "Shopping", "97", "6"},
        {"Home Decor", "Shopping", "97", "6"},
        {"Home Improvement Stores", "Shopping", "97", "6"},
        {"Hot Tub and Pool", "Shopping", "97", "6"},
        {"Industrial Equipment Supplier", "Shopping", "", "6"},
        {"Jewelry", "Shopping", "98", "6"},
        {"Junk & Scrap Dealers", "Shopping", "", "6"},
        {"Kitchen & Bath", "Shopping", "97", "6"},
        {"Leather Goods", "Shopping", "93", "6"},
        {"Lighting Fixtures", "Shopping", "97", "6"},
        {"Lingerie", "Shopping", "93", "6"},
        {"Luggage", "Shopping", "99", "6", "97", "6"},
        {"Maternity Stores", "Shopping", "", "6"},
        {"Mattresses", "Shopping", "", "6"},
        {"Men's Clothing", "Shopping", "93", "6"},
        {"Mobile Phones", "Shopping", "89", "6"},
        {"Music & DVDs", "Shopping", "100", "6"},
        {"Musical Instruments", "Shopping", "102", "6"},
        {"Newspapers & Magazines", "Shopping", "100", "6"},
        {"Nurseries & Garden Centers", "Shopping", "97", "6"},
        {"Office Supplies & Equipment", "Shopping", "103", "6"},
        {"Outlet Stores", "Shopping", "", "6"},
        {"Party Supplies", "Shopping", "109", "6"},
        {"Pawn Shops", "Shopping", "", "6"},
        {"Photography Stores & Services", "Shopping", "105", "6"},
        {"Religious Goods", "Shopping", "", "6"},
        {"Shades & Blinds", "Shopping", "", "6"},
        {"Shoe Stores", "Shopping", "116", "6"},
        {"Shopping Centers", "Shopping", "91", "6"},
        {"Sporting Goods", "Shopping", "106", "6"},
        {"Sporting Goods Store", "Shopping", "106", "6"},
        {"Swimwear", "Shopping", "106", "6"},
        {"Thrift Stores", "Shopping", "", "6"},
        {"Tobacco Shops", "Shopping", "", "6"},
        {"Toy Stores", "Shopping", "108", "6"},
        {"Trophies & Engraving", "Shopping", "", "6"},
        {"Uniforms", "Shopping", "93", "6"},
        {"Used, Vintage & Consignment", "Shopping", "", "6"},
        {"Used", "Shopping", "", "6"},
        {"Vending Machines", "Shopping", "", "6"},
        {"Videos nd Video Game Rental", "Shopping", "100", "6"},
        {"Watches", "Shopping", "98", "6"},
        {"Wholesale Stores", "Shopping", "91", "6"},
        {"Women's Clothing", "Shopping", "93", "6"},
        {"Airlines", "Travel", "72", "4"},
        {"Bed & Breakfasts", "Travel", "72", "4"},
        {"Bus Lines", "Travel", "72", "4"},
        {"Car Rental", "Travel", "72", "4"},
        {"Cruises", "Travel", "72", "4"},
        {"Hostels", "Travel", "72", "4"},
        {"Hotels", "Travel", "72", "4"},
        {"Hotel", "Travel", "72", "4"},
        {"Lodging", "Travel", "72", "4"},
        {"Motels", "Travel", "72", "4"},
        {"Resorts", "Travel", "72", "4"},
        {"RV - Recreational Vehicles", "Travel", "72", "4"},
        {"Taxis", "Travel", "72", "4"},
        {"Timeshare Agencies", "Travel", "72", "4"},
        {"Travel Agencies", "Travel", "72", "4"},
        {"Vacation Home Rental", "Travel", "72", "4"},
        {"Elementary Schools", "Education", "", "4"},
        {"Flight Instruction", "Education", "", "4"},
        {"Art Classes", "Education", "", "4"},
        {"Specialty Schools", "Education", "", "4"},
        {"Private Tutors", "Education", "", "4"},
        {"Training & Vocational Schools", "Education", "", "4"},
        {"Bartending Schools", "Education", "", "4"},
        {"Adult Education", "Education", "", "4"},
        {"Music Lessons", "Education", "", "4"},
        {"Cooking Classes", "Education", "", "4"},
        {"Wine Classes", "Education", "", "4"},
        {"Massage Schools", "Education", "", "4"},
        {"Educational Services", "Education", "", "4"},
        {"Swimming Lessons", "Education", "", "4"},
        {"Acting Classes", "Education", "", "4"},
        {"Language Schools", "Education", "", "4"},
        {"Culinary Schools", "Education", "", "4"},
        {"Cosmetology Schools", "Education", "", "4"},
        {"Stock Brokers", "Financial Services", "71", "4"},
    };

    protected static String[] findMapping(String categoryStr) {
        String[] split = categoryStr.split(", ");

        if (split.length > 1) {
            String parent = split[0].trim();
            String attribute = split[1].trim();

            for (int i = 0; i < GROUPON_CATEGORIES.length; i++) {
                String[] category = GROUPON_CATEGORIES[i];
                if (category[1].equals(parent) && attribute.startsWith(category[0])) { //category[0].equals(attribute)) {
                    try {
                        return new String[]{category[3], category[2]};
                    } catch (Exception e) {
                        logger.log(Level.SEVERE, "Error matching category " + categoryStr, e);
                    }
                }
            }

            logger.log(Level.WARNING, "No matching category: {0}", parent + "/" + attribute);
            return new String[]{"8", ""};
        } else {
            if (StringUtils.isEmpty(categoryStr)) {
                logger.log(Level.SEVERE, "Category is empty");               
            } else {
                for (int i = 0; i < PARENT_CATEGORIES.length; i++) {
                    String[] category = PARENT_CATEGORIES[i];
                    if (category[0].equals(categoryStr)) {
                        return new String[]{category[1], ""};
                    }
                }
                logger.log(Level.SEVERE, "No matching parent category: {0}", categoryStr);
            }
            return new String[]{"8", ""};
        }
    }
}
