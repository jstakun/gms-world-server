/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jstakun.gms.social;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author jstakun
 */
public class FoursquareCategoryParser {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            File f = new File("categories.json");
            String jsonFileContent = FileUtils.readFileToString(f, "UTF-8");
            String parsed = parseFoursquareCategories(jsonFileContent);
            FileUtils.write(new File("categoriesParsed.json"), parsed, "UTF-8");
        } catch (Exception ex) {
            Logger.getLogger(FoursquareCategoryParser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static String parseFoursquareCategories(String jsonFileContent) throws JSONException {
        JSONArray cats = new JSONArray();

        JSONObject jsonRoot = new JSONObject(jsonFileContent);
        JSONObject meta = jsonRoot.getJSONObject("meta");
        int code = meta.getInt("code");
        if (code == 200) {
            JSONObject response = jsonRoot.getJSONObject("response");
            JSONArray categories = response.getJSONArray("categories");
            for (int i = 0; i < categories.length(); i++) {
                JSONObject parentCategory = categories.getJSONObject(i);
                ArrayList<Map<String, String>> childArray = new ArrayList<Map<String, String>>();
                parseFoursquareCategory(parentCategory.getJSONArray("categories"), childArray);
                String id = parentCategory.getString("id");
                //String name = parentCategory.getString("pluralName");
                JSONObject json = new JSONObject();
                json.put("id", id);
                //json.put("name", name);
                json.put("subcategories", childArray);
                System.out.println(json.toString());
                cats.put(json);
            }
        }

        return cats.toString();
    }

    private static void parseFoursquareCategory(JSONArray categories, List<Map<String, String>> childCategories) throws JSONException {
        for (int i = 0; i < categories.length(); i++) {
            JSONObject parentCategory = categories.getJSONObject(i);
            //boolean hasSubcat = false;
            if (parentCategory.has("categories")) {
                JSONArray childCats = parentCategory.getJSONArray("categories");
                if (childCats.length() > 0) {
                    parseFoursquareCategory(childCats, childCategories);
                    //hasSubcat = true;
                }
            }

            //if (!hasSubcat) {
            Map<String, String> category = new HashMap<String, String>();
            String id = parentCategory.getString("id");
            String name = parentCategory.getString("pluralName");
            category.put("id", id);
            category.put("name", name);
            childCategories.add(category);
            //}
        }
    }
}
