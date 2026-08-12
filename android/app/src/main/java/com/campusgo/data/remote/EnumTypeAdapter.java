package com.campusgo.data.remote;

import androidx.annotation.NonNull;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

final class EnumTypeAdapter<T extends Enum<T>> extends TypeAdapter<T> {

    private final Class<T> enumClass;

    EnumTypeAdapter(@NonNull Class<T> enumClass) {
        this.enumClass = enumClass;
    }

    @Override
    public void write(JsonWriter out, T value) throws IOException {
        out.value(value == null ? null : value.name());
    }

    @Override
    public T read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        String name = in.nextString();
        return Enum.valueOf(enumClass, name);
    }
}
