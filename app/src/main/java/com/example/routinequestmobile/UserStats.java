package com.example.routinequestmobile;

import com.google.gson.annotations.SerializedName;

public class UserStats {

    @SerializedName("intelligence") private int intelligence;
    @SerializedName("strength") private int strength;
    @SerializedName("agility") private int agility;
    @SerializedName("resistance") private int resistance;
    @SerializedName("xp") private Integer xp;
    @SerializedName("level") private Integer level;
    @SerializedName("experienceRequired") private Integer experienceRequired;
    @SerializedName("streakDays") private Integer streakDays;
    @SerializedName("missionsToday") private Integer missionsToday;
    @SerializedName("totalPower") private Integer totalPower;
    @SerializedName("className") private String className;

    public int getIntelligence() { return intelligence; }
    public int getStrength() { return strength; }
    public int getAgility() { return agility; }
    public int getResistance() { return resistance; }
    public Integer getXp() { return xp; }
    public Integer getLevel() { return level; }
    public Integer getExperienceRequired() { return experienceRequired; }
    public Integer getStreakDays() { return streakDays; }
    public Integer getMissionsToday() { return missionsToday; }
    public Integer getTotalPower() { return totalPower; }
    public String getClassName() { return className; }
}