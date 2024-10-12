package com.vaultmessenger.database

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@ProvidedTypeConverter
class Converters {

    @TypeConverter
    fun fromHashMap(value: HashMap<String, String>?): String? {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toHashMap(value: String?): HashMap<String, String>? {
        val type = object : TypeToken<HashMap<String, String>>() {}.type
        return Gson().fromJson(value, type)
    }

    @TypeConverter
    fun fromHashMapBoolean(value: HashMap<String, Boolean>?): String? {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toHashMapBoolean(value: String?): HashMap<String, Boolean>? {
        val type = object : TypeToken<HashMap<String, Boolean>>() {}.type
        return Gson().fromJson(value, type)
    }
}
