package com.film_backend.film.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Genre {
    ACTION, DRAMA, COMEDY, HORROR, SCIFI, ROMANCE, THRILLER;

    @JsonValue
    public String getValue() {
        return name();
    }

    @JsonCreator
    public static Genre fromValue(String value) {
        return valueOf(value);
    }
}