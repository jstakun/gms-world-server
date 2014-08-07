package net.gmsworld.server.layers;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

public class YelpCategoryMapping {
	
	private static final Logger logger = Logger.getLogger(YelpCategoryMapping.class.getName());
	    
	private static final String[][] YELP_CATEGORIES = {
		{"italian", "25", "1"},
		{"pizza", "25", "1"},
		{"greek", "20", "1"},
		{"latin", "28", "1"},
		{"spanish", "39", "1"},
		{"mexican", "29", "1"},
		{"cuban", "13", "1"},
		{"ethiopian", "16", "1"},
		{"portuguese", "1", "1"},
		{"french", "18", "1"},
		{"coffee", "11", "1"},
		{"cafes", "11", "1"},
		{"vietnamese", "46", "1"},
		{"japanese", "26", "1"},
		{"russian", "33", "1"},
		{"vegetarian", "45", "1"},
		{"asianfusion", "3", "1"},
		{"newamerican", "2", "1"},
		{"tradamerican", "2", "1"},
		{"mediterranean", "20", "1"},
		{"seafood", "35", "1"},
		{"desserts", "111", "1"},
		{"brazilian", "5", "1"},
		{"cheesesteaks", "", "1"},
		{"sandwiches", "34", "1"},
		{"korean", "27", "1"},
		{"indian", "23", "1"},
		{"burgers", "8", "1"},
		{"pakistani", "23", "1"},
		{"african", "16", "1"},
		{"creperies", "", "1"},
		{"german", "19", "1"},
		{"hotdogs", "8", "1"},
		{"hotdog", "8", "1"},
		{"delis", "14", "1"},
		{"steak", "40", "1"},
		{"vegan", "44", "1"},
		{"mideastern", "30", "1"},
		
		{"jewelry", "98", "6"},
		{"gyms", "76", "4"},
		{"massage", "59", "3"},
		{"bookstores", "100", "6"},
		{"hair", "61", "3"},
		{"gardeners", "70", "4"},
		{"gardening", "70", "4"},
		{"movers", "70", "4"},
		{"tanning", "66", "3"},
		{"martialarts", "69", "4"},
		{"electronicsrepair", "71", "4"},
		{"mobilephonerepair", "71", "4"},
		{"massage", "59", "3"},
		{"carwash", "119", "4"},
		{"auto_detailing", "119", "4"},
		{"bowling", "117", "2"},
		{"homecleaning", "70", "4"},
		{"drycleaninglaundry", "81", "4"},
		{"carpet_cleaning", "70", "4"},
		
		{"opticians", "104", "6"},
		{"florists", "71", "4"},
		{"framing", "97", "6"},
		{"photographystores", "105", "6"},
		{"mattresses", "", "6"},
		{"photographers", "71", "4"},
		{"hvac", "70", "4"},
		{"officeequipment", "103", "6"},
		{"furniture", "110", "6"},
		{"antiques", "95", "6"},
		{"tobaccoshops", "", "6"},
		{"hookah_bars", "48", "2"},
		{"homedecor", "97", "6"},
		{"pet_training", "73", "4"},
		{"groomer", "73", "4"},
        {"pet_sitting", "73", "4"},
        {"pet_training", "73", "4"},
        {"dogwalkers", "73", "4"},
        {"cosmetics", "69", "4"},
        {"galleries", "114", "2"},
        {"massage_therapy", "59", "3"},
        {"yoga", "75", "4"},
        {"fooddeliveryservices", "", "6"},
        {"personalchefs", "71", "4"},
        {"chocolate", "87", "6"},
        {"dance_schools", "114", "2"},
        {"musicvenues", "54", "2"},
        {"optometrists","104", "6"},
        {"opticians","104", "6"},
        {"barbers", "61", "3"},
        {"healthtrainers", "69", "4"},
        {"bagels", "87", "6"},
        {"catering", "71", "4"},
        {"acupuncture", "69", "4"},
        {"hairstylists", "61", "3"},
        {"autorepair", "119", "4"},
        {"autopartssupplies", "119", "4"},
        {"baby_gear", "", "6"},
        {"musicalinstrumentsandteachers", "102", "6"},
        {"privatetutors", "", "4"},
        {"skatingrinks", "113", "2"},
        {"nutritionists", "69", "4"},
        {"womenscloth", "93", "6"},
        {"menscloth", "93", "6"},
        {"childcloth", "93", "6"},
        {"vintage", "", "6"},
        {"artsupplies", "95", "6"},
        {"toys", "108", "6"},
        {"comicbooks", "100", "6"},
        {"pest_control", "70", "4"},
        {"arcades", "113", "2"},
        {"eventplanning", "71", "4"},
        {"videofilmproductions", "71", "4"},
        {"videographers", "71", "4"},
        {"formalwear", "93", "6"},
        {"homeappliancerepair", "70", "4"},
        {"appliances", "70", "4"},
        {"sportgoods", "106", "6"},
        {"icecream", "87", "6"},
        {"medicalspa", "65", "3"},
        {"spas", "65", "3"},
        {"professional", "71", "4"},
        {"partysupplies", "109", "6"},
        {"bikes", "106", "6"},
        {"drugstores", "92", "6"},
        {"florists", "", "6"},
        {"bakeries", "87", "6"},
        {"wedding_planning", "71", "4"},
        {"interiordesign", "70", "4"},
        {"watches", "98", "6"},
        {"comedyclubs", "50", "2"},
        {"swimwear", "106", "6"},
        {"scuba", "106", "6"},
        {"lingerie", "93", "6"},
        {"accessories", "", "6"},
        {"watch_repair", "71", "4"},
        {"wineries", "87", "6"},
        {"mobilephones", "89", "6"},
        {"theater", "57", "2"},
        {"eyelashservice", "", "3"},
        {"skincare", "64", "3"},
        {"healthmarkets", "69", "4"},
        {"sewingalterations", "85", "4"},
        {"bridal", "6", "6"},
        {"personal_shopping", "71", "4"},
        {"stationery", "", "6"},
        {"giftshops", "94", "6"},
        {"fashion", "71", "4"},
        {"copyshops", "71", "4"},
        {"screenprinting", "71", "4"},
        {"stereo_installation", "119", "4"},
        {"electronics", "89", "6"},
        {"djs", "71", "4"},
        
        /*
         * 2013-10-03 19:33:06.980 com.jstakun.lm.server.layers.YelpCategoryMapping findMapping: No matching category: adult,adultedu
W 2013-10-03 19:33:07.048 com.jstakun.lm.server.layers.YelpCategoryMapping findMapping: No matching category: breakfast_brunch,diners
W 2013-10-03 19:33:07.049 com.jstakun.lm.server.layers.YelpCategoryMapping findMapping: No matching category: tours,beer_and_wine
W 2013-10-03 19:33:07.247 com.jstakun.lm.server.layers.YelpCategoryMapping findMapping: No matching category: adult
W 2013-10-03 19:33:07.248 com.jstakun.lm.server.layers.YelpCategoryMapping findMapping: No matching category: chinese
W 2013-10-03 19:33:07.248 com.jstakun.lm.server.layers.YelpCategoryMapping findMapping: No matching category: caribbean
W 2013-10-03 19:33:07.249 com.jstakun.lm.server.layers.YelpCategoryMapping findMapping: No matching category: autoglass,blinds
W 2013-10-03 19:33:07.250 com.jstakun.lm.server.layers.YelpCategoryMapping findMapping: No matching category: bbq
W 2013-10-03 19:33:07.251 com.jstakun.lm.server.layers.YelpCategoryMapping findMapping: No matching category: videoandgames
W 2013-10-03 19:33:07.252 com.jstakun.lm.server.layers.YelpCategoryMapping findMapping: No matching category: kitchenandbath
W 2013-10-03 19:33:07.257 com.jstakun.lm.server.layers.YelpCategoryMapping findMapping: No matching category: tours
W 2013-10-03 19:33:07.258 com.jstakun.lm.server.layers.YelpCategoryMapping findMapping: No matching category: itservices
W 2013-10-03 19:33:07.348 com.jstakun.lm.server.layers.YelpCategoryMapping findMapping: No matching category: petstore
W 2013-10-03 19:33:07.357 com.jstakun.lm.server.layers.YelpCategoryMapping findMapping: No matching category: himalayan,indpak
W 2013-10-03 19:33:07.357 com.jstakun.lm.server.layers.YelpCategoryMapping findMapping: No matching category: candy
W 2013-10-03 19:33:07.447 com.jstakun.lm.server.layers.YelpCategoryMapping findMapping: No matching category: danceclubs,dancestudio
W 2013-10-03 19:33:07.551 com.jstakun.lm.server.layers.YelpCategoryMapping findMapping: No matching category: gourmet,hobbyshops
W 2013-10-03 19:33:07.552 com.jstakun.lm.server.layers.YelpCategoryMapping findMapping: No matching category: indpak
2013-10-03 13:18:24.347 com.jstakun.lm.server.layers.YelpCategoryMapping findMapping: No matching category: limos,airport_shuttles
W 2013-10-03 13:18:24.348 com.jstakun.lm.server.layers.YelpCategoryMapping findMapping: No matching category: dancestudio
W 2013-10-03 13:18:24.349 com.jstakun.lm.server.layers.YelpCategoryMapping findMapping: No matching category: limos
W 2013-10-03 13:18:24.350 com.jstakun.lm.server.layers.YelpCategoryMapping findMapping: No matching category: indpak
W 2013-10-03 13:18:24.351 com.jstakun.lm.server.layers.YelpCategoryMapping findMapping: No matching category: lifecoach
W 2013-10-03 13:18:24.352 com.jstakun.lm.server.layers.YelpCategoryMapping findMapping: No matching category: himalayan,indpak
W 2013-10-03 13:18:24.352 com.jstakun.lm.server.layers.YelpCategoryMapping findMapping: No matching category: generaldentistry,cosmeticdentists
W 2013-10-03 13:18:24.353 com.jstakun.lm.server.layers.YelpCategoryMapping findMapping: No matching category: indpak,himalayan
W 2013-10-03 13:18:24.354 com.jstakun.lm.server.layers.YelpCategoryMapping findMapping: No matching category: itservices
W 2013-10-03 13:18:24.447 com.jstakun.lm.server.layers.YelpCategoryMapping findMapping: No matching category: indpak
W 2013-10-03 13:18:24.448 com.jstakun.lm.server.layers.YelpCategoryMapping findMapping: No matching category: itservices,computers
W 2013-10-03 13:18:24.449 com.jstakun.lm.server.layers.YelpCategoryMapping findMapping: No matching category: web_design,marketing
W 2013-10-03 13:18:24.449 com.jstakun.lm.server.layers.YelpCategoryMapping findMapping: No matching category: naturopathic
W 2013-10-03 13:18:24.450 com.jstakun.lm.server.layers.YelpCategoryMapping findMapping: No matching category: chiropractors
W 2013-10-03 13:18:24.549 com.jstakun.lm.server.layers.YelpCategoryMapping findMapping: No matching category: plumbing
W 2013-10-03 13:18:24.551 com.jstakun.lm.server.layers.YelpCategoryMapping findMapping: No matching category: careercounseling
W 2013-10-03 13:18:24.551 com.jstakun.lm.server.layers.YelpCategoryMapping findMapping: No matching category: psychic_astrology
W 2013-10-03 13:18:24.552 com.jstakun.lm.server.layers.YelpCategoryMapping findMapping: No matching category: adultedu,artschools,culturalcenter
W 2013-10-03 13:18:24.553 com.jstakun.lm.server.layers.YelpCategoryMapping findMapping: No matching category: bodyshops
W 2013-10-03 13:18:24.747 com.jstakun.lm.server.layers.YelpCategoryMapping findMapping: No matching category: locksmiths
W 2013-10-03 13:18:24.747 com.jstakun.lm.server.layers.YelpCategoryMapping findMapping: No matching category: midwives,obgyn
2013-10-02 18:25:54.733 com.jstakun.lm.server.layers.YelpCategoryMapping findMapping: No matching category: streetvendors
W 2013-10-02 18:25:54.735 com.jstakun.lm.server.layers.YelpCategoryMapping findMapping: No matching category: itservices,computers
W 2013-10-02 18:25:54.736 com.jstakun.lm.server.layers.YelpCategoryMapping findMapping: No matching category: laser_hair_removal
W 2013-10-02 18:25:54.737 com.jstakun.lm.server.layers.YelpCategoryMapping findMapping: No matching category: junkremovalandhauling,thrift_stores
W 2013-10-02 18:25:54.738 com.jstakun.lm.server.layers.YelpCategoryMapping findMapping: No matching category: musicvideo
W 2013-10-02 18:25:54.837 com.jstakun.lm.server.layers.YelpCategoryMapping findMapping: No matching category: videoandgames
W 2013-10-02 18:25:54.838 com.jstakun.lm.server.layers.YelpCategoryMapping findMapping: No matching category: beer_and_wine,ticketsales
W 2013-10-02 18:25:54.840 com.jstakun.lm.server.layers.YelpCategoryMapping findMapping: No matching category: polish,pubs
W 2013-10-02 18:25:54.841 com.jstakun.lm.server.layers.YelpCategoryMapping findMapping: No matching category: peruvian,lounges
W 2013-10-02 18:25:54.842 com.jstakun.lm.server.layers.YelpCategoryMapping findMapping: No matching category: chinese,sushi
W 2013-10-02 18:25:54.931 com.jstakun.lm.server.layers.YelpCategoryMapping findMapping: No matching category: sportsbars,danceclubs,soulfood
W 2013-10-02 18:25:54.933 com.jstakun.lm.server.layers.YelpCategoryMapping findMapping: No matching category: divebars
W 2013-10-02 18:25:54.935 com.jstakun.lm.server.layers.YelpCategoryMapping findMapping: No matching category: thrift_stores
W 2013-10-02 18:25:55.032 com.jstakun.lm.server.layers.YelpCategoryMapping findMapping: No matching category: generaldentistry
W 2013-10-02 18:25:55.032 com.jstakun.lm.server.layers.YelpCategoryMapping findMapping: No matching category: karaoke
W 2013-10-02 18:25:55.033 com.jstakun.lm.server.layers.YelpCategoryMapping findMapping: No matching category: musicvideo,videoandgames
W 2013-10-02 18:25:55.035 com.jstakun.lm.server.layers.YelpCategoryMapping findMapping: No matching category: juicebars

         */
	};
	
		
	protected static String[] findMapping(String[] category) {
		for (int i=0;i<YELP_CATEGORIES.length;i++) {
			for (int j=0;j<category.length;j++) {
				if (YELP_CATEGORIES[i][0].equals(category[j])) {
					//logger.log(Level.INFO, "Found matching category {0}", category[j]);
				    return new String[]{YELP_CATEGORIES[i][2], YELP_CATEGORIES[i][1]};	
				}	
			}
		}
		logger.log(Level.WARNING, "No matching category: {0}", StringUtils.join(Arrays.asList(category), ','));
		return new String[]{"8", null};
	}
}
