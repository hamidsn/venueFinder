package com.ebookfrenzy.foursquare;

/**
 * Created by hsedghinezhad on 14/07/2015.
 */
public class FoursquareVenue {
	private String name;
	private int distance;
	private String formattedAddress;
	private String contact;
	private String category;
	private String imageHref;


	public FoursquareVenue() {
		this.name = "";
		this.contact = "";
		this.distance = -1;
		this.formattedAddress = "";
		this.imageHref = "";
		this.setCategory("");
	}

	public int getDistance() {
		if (distance > -1) {
			return distance;
		}
		return distance;
	}

	public void setDistance(int distance) {
		if (distance > 0) {
			this.distance = distance;
		}
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String getContact() {
		return contact;
	}

	public void setContact(String contact) {
		this.contact = contact;
	}

	public String getImageHref() {
		return imageHref;
	}

	public void setImageHref(String imageHref) {
		this.imageHref = imageHref;
	}

	public String getFormattedAddress() {
		return formattedAddress;
	}

	public void setFormattedAddress(String formattedAddress) {
		formattedAddress = formattedAddress.substring(1, formattedAddress.length()-1);
		this.formattedAddress = formattedAddress;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

}
