package com.ebenezer.gana.newsapp.data.local

import androidx.room.TypeConverter
import com.ebenezer.gana.newsapp.data.models.Source

class Converters {

    @TypeConverter
    fun fromSource(source: Source):String{
        return  source.name
    }

    @TypeConverter
    fun toSource(name:String): Source {
        return Source(name, name)
    }
}