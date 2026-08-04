package com.getpayin.paylink.internal;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** JSON serialization + typed reads over a response {@link JsonElement}, backed by Gson. */
public final class Json {

    private static final Gson GSON = new Gson();

    private Json() {
    }

    public static String serialize(Object value) {
        return GSON.toJson(value);
    }

    public static JsonElement parse(String text) {
        return JsonParser.parseString(text);
    }

    public static boolean isObject(JsonElement element) {
        return element != null && element.isJsonObject();
    }

    public static String str(JsonElement element, String name) {
        String value = strOrNull(element, name);
        return value != null ? value : "";
    }

    public static String strOrNull(JsonElement element, String name) {
        JsonElement value = child(element, name);
        return value != null && value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()
                ? value.getAsString()
                : null;
    }

    public static long int64(JsonElement element, String name) {
        Long value = int64OrNull(element, name);
        return value != null ? value : 0L;
    }

    public static Long int64OrNull(JsonElement element, String name) {
        JsonElement value = child(element, name);
        return isNumber(value) ? value.getAsLong() : null;
    }

    public static BigDecimal decimal(JsonElement element, String name) {
        BigDecimal value = decimalOrNull(element, name);
        return value != null ? value : BigDecimal.ZERO;
    }

    public static BigDecimal decimalOrNull(JsonElement element, String name) {
        JsonElement value = child(element, name);
        return isNumber(value) ? value.getAsBigDecimal() : null;
    }

    public static JsonElement child(JsonElement element, String name) {
        if (element != null && element.isJsonObject()) {
            return element.getAsJsonObject().get(name);
        }
        return null;
    }

    public static boolean has(JsonElement element, String name) {
        return element != null && element.isJsonObject() && element.getAsJsonObject().has(name);
    }

    /** Converts a JSON element into a plain Map/List/String/Number/Boolean/null graph. */
    public static Object toPlain(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return null;
        }
        if (element.isJsonObject()) {
            Map<String, Object> map = new LinkedHashMap<>();
            for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
                map.put(entry.getKey(), toPlain(entry.getValue()));
            }
            return map;
        }
        if (element.isJsonArray()) {
            List<Object> list = new ArrayList<>();
            for (JsonElement item : (JsonArray) element) {
                list.add(toPlain(item));
            }
            return list;
        }
        JsonPrimitive primitive = element.getAsJsonPrimitive();
        if (primitive.isBoolean()) {
            return primitive.getAsBoolean();
        }
        if (primitive.isNumber()) {
            return primitive.getAsBigDecimal();
        }
        return primitive.getAsString();
    }

    private static boolean isNumber(JsonElement element) {
        return element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber();
    }
}
