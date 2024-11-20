package com.dolphin.localsocket.deserializer;

import com.dolphin.localsocket.cmd.BaseLocalCmd;
import com.dolphin.localsocket.cmd.LocalCmd;
import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

public class LocalDataDeserializer implements JsonDeserializer<BaseLocalCmd> {
    @Override
    public BaseLocalCmd deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        int type = jsonObject.get("cmd").getAsInt();
        switch (type) {
            case LocalCmd.PUSH_ANSWER:
                return new Gson().fromJson(json,new TypeToken<BaseLocalCmd>(){}.getType());


        }
        return new Gson().fromJson(json, BaseLocalCmd.class);
    }
}
