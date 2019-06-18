package com.example.eduardorodriguez.comeaqui.get;

import com.google.gson.JsonObject;
import java.io.Serializable;

public class GetFoodObject implements Serializable {
    public String id;
    public String plate_name;
    public String price;
    public String type;
    public String description;
    public String food_photo;
    public String owner;
    public GetFoodObject(JsonObject jo){
        id = jo.get("id").getAsNumber().toString();
        plate_name = jo.get("plate_name").getAsString();
        price = jo.get("price").getAsString();
        type = jo.get("food_type").getAsString();
        description = jo.get("description").getAsString();
        food_photo = jo.get("food_photo").getAsString();
        owner = jo.get("owner").getAsString();
    }
}