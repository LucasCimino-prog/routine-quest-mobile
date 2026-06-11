package com.example.routinequestmobile;

import com.google.gson.annotations.SerializedName;

public class UserStats {

    // Os nomes dentro de " " devem ser exatamente iguais aos que a sua API devolve no JSON
    @SerializedName("intelligence")
    private int intelligence;

    @SerializedName("strength")
    private int strength;

    @SerializedName("agility")
    private int agility;

    @SerializedName("resistance")
    private int resistance;

    public int getIntelligence() { return intelligence; }
    public int getStrength() { return strength; }
    public int getAgility() { return agility; }
    public int getResistance() { return resistance; }
}