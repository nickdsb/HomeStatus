package com.ff.homestatus;

import java.io.Serializable;

/**
 * 地图标注信息实体类
 * @author jing__jie
 *
 */
public class User implements Serializable{
    private static final long serialVersionUID = 8633299996744734593L;

    private String team,username,avater,lat,lng;

    public User() {}
    public User(String team, String username, String lat, String lng) {
        this.team=team;
        this.username = username;
        this.lat = lat;
        this.lng = lng;
    }

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }



    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAvater() {
        return avater;
    }

    public void setAvater(String avater) {
        this.avater = avater;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }
}
