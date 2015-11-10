package net.gmsworld.server.etl;

public class Hotel {
	
	private Long _id;
	private String name;
	private String address;
	private String zip;
	private String city_hotel;
	private String cc1;	//country code
	private String ufi; //city_id	
	private Double stars; //class	
	private String currencycode;	
	private Double minrate;
	private Double maxrate;
	private Integer preferred;
	private Integer nr_rooms;
	private Double longitude;
	private Double latitude;	
	private Integer public_ranking;
	private String hotel_url;
	private String photo_url;	
	private String desc_en;
	private String desc_fr;
	private String desc_es;
	private String desc_de;
	private String desc_nl;
	private String desc_it;
	private String desc_pt;
	private String desc_ja;
	private String desc_zh;
	private String desc_pl;	
	private String desc_ru;	
	private String desc_sv;	
	private String desc_ar;	
	private String desc_el;	
	private String desc_no;	
	private String city_unique;
	private String city_preferred;
	private Integer continent_id;
	private Integer review_score;
	private Integer review_nr;
	
	public Long get_id() {
		return _id;
	}
	
	public void set_id(Long id) {
		this._id = id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getAddress() {
		return address;
	}
	
	public void setAddress(String address) {
		this.address = address;
	}

	public String getZip() {
		return zip;
	}

	public void setZip(String zip) {
		this.zip = zip;
	}

	public String getCity_hotel() {
		return city_hotel;
	}

	public void setCity_hotel(String city_hotel) {
		this.city_hotel = city_hotel;
	}

	public String getCc1() {
		return cc1;
	}

	public void setCc1(String cc1) {
		this.cc1 = cc1;
	}

	public String getUfi() {
		return ufi;
	}

	public void setUfi(String ufi) {
		this.ufi = ufi;
	}

	public Double getStars() {
		return stars;
	}

	public void setStars(Double stars) {
		this.stars = stars;
	}

	public String getCurrencycode() {
		return currencycode;
	}

	public void setCurrencycode(String currencycode) {
		this.currencycode = currencycode;
	}

	public Double getMinrate() {
		return minrate;
	}

	public void setMinrate(Double minrate) {
		this.minrate = minrate;
	}

	public Double getMaxrate() {
		return maxrate;
	}

	public void setMaxrate(Double maxrate) {
		this.maxrate = maxrate;
	}

	public Integer getPreferred() {
		return preferred;
	}

	public void setPreferred(Integer preferred) {
		this.preferred = preferred;
	}

	public Integer getNr_rooms() {
		return nr_rooms;
	}

	public void setNr_rooms(Integer nr_rooms) {
		this.nr_rooms = nr_rooms;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Integer getPublic_ranking() {
		return public_ranking;
	}

	public void setPublic_ranking(Integer public_ranking) {
		this.public_ranking = public_ranking;
	}

	public String getHotel_url() {
		return hotel_url;
	}

	public void setHotel_url(String hotel_url) {
		this.hotel_url = hotel_url;
	}

	public String getPhoto_url() {
		return photo_url;
	}

	public void setPhoto_url(String photo_url) {
		this.photo_url = photo_url;
	}

	public String getDesc_en() {
		return desc_en;
	}

	public void setDesc_en(String desc_en) {
		this.desc_en = desc_en;
	}

	public String getDesc_fr() {
		return desc_fr;
	}

	public void setDesc_fr(String desc_fr) {
		this.desc_fr = desc_fr;
	}

	public String getDesc_es() {
		return desc_es;
	}

	public void setDesc_es(String desc_es) {
		this.desc_es = desc_es;
	}

	public String getDesc_de() {
		return desc_de;
	}

	public void setDesc_de(String desc_de) {
		this.desc_de = desc_de;
	}

	public String getDesc_nl() {
		return desc_nl;
	}

	public void setDesc_nl(String desc_nl) {
		this.desc_nl = desc_nl;
	}

	public String getDesc_it() {
		return desc_it;
	}

	public void setDesc_it(String desc_it) {
		this.desc_it = desc_it;
	}

	public String getDesc_pt() {
		return desc_pt;
	}

	public void setDesc_pt(String desc_pt) {
		this.desc_pt = desc_pt;
	}

	public String getDesc_ja() {
		return desc_ja;
	}

	public void setDesc_ja(String desc_ja) {
		this.desc_ja = desc_ja;
	}

	public String getDesc_zh() {
		return desc_zh;
	}

	public void setDesc_zh(String desc_zh) {
		this.desc_zh = desc_zh;
	}

	public String getDesc_pl() {
		return desc_pl;
	}

	public void setDesc_pl(String desc_pl) {
		this.desc_pl = desc_pl;
	}

	public String getDesc_ru() {
		return desc_ru;
	}

	public void setDesc_ru(String desc_ru) {
		this.desc_ru = desc_ru;
	}

	public String getDesc_sv() {
		return desc_sv;
	}

	public void setDesc_sv(String desc_sv) {
		this.desc_sv = desc_sv;
	}

	public String getDesc_ar() {
		return desc_ar;
	}

	public void setDesc_ar(String desc_ar) {
		this.desc_ar = desc_ar;
	}

	public String getDesc_el() {
		return desc_el;
	}

	public void setDesc_el(String desc_el) {
		this.desc_el = desc_el;
	}

	public String getDesc_no() {
		return desc_no;
	}

	public void setDesc_no(String desc_no) {
		this.desc_no = desc_no;
	}

	public String getCity_unique() {
		return city_unique;
	}

	public void setCity_unique(String city_unique) {
		this.city_unique = city_unique;
	}

	public String getCity_preferred() {
		return city_preferred;
	}

	public void setCity_preferred(String city_preferred) {
		this.city_preferred = city_preferred;
	}

	public Integer getContinent_id() {
		return continent_id;
	}

	public void setContinent_id(Integer continent_id) {
		this.continent_id = continent_id;
	}

	public Integer getReview_score() {
		return review_score;
	}

	public void setReview_score(Integer review_score) {
		this.review_score = review_score;
	}

	public Integer getReview_nr() {
		return review_nr;
	}

	public void setReview_nr(Integer review_nr) {
		this.review_nr = review_nr;
	}
	
	public String toString() {
		return get_id() + ": " + getName();
	}
}
