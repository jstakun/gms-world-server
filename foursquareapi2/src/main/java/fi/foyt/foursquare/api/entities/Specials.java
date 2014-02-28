package fi.foyt.foursquare.api.entities;

import fi.foyt.foursquare.api.FoursquareEntity;

public class Specials implements FoursquareEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6697384546257157078L;
	
	private CompleteSpecial[] items;

	public CompleteSpecial[] getItems() {
		return items;
	}

	public void setItems(CompleteSpecial[] items) {
		this.items = items;
	}

}
