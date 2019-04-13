package com.akhila.amaps;

public class Feed {
    private double lat,lang;

    public Feed() {
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLang() {
        return lang;
    }

    public void setLang(double lang) {
        this.lang = lang;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    private String feedback;
    public Feed(double lat, double lang, String feedback) {
        this.lat = lat;
        this.lang = lang;
        this.feedback = feedback;
    }


}
