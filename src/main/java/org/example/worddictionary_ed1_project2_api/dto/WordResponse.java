package org.example.worddictionary_ed1_project2_api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class WordResponse {

    private int id;
    private String word;
    private String definition;
    private int frequency;

    public WordResponse(){}

    public WordResponse(int id, String word, String definition, int frequency) {
        this.id = id;
        this.word = word;
        this.definition = definition;
        this.frequency = frequency;
    }

    @JsonProperty("id")
    public int getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(int id) {
        this.id = id;
    }

    @JsonProperty("palabra")
    public String getWord() {
        return word;
    }

    @JsonProperty("palabra")
    public void setWord(String word) {
        this.word = word;
    }

    @JsonProperty("significado")
    public String getDefinition() {
        return definition;
    }

    @JsonProperty("significado")
    public void setDefinition(String definition) {
        this.definition = definition;
    }

    @JsonProperty("frecuencia")
    public int getFrequency() {
        return frequency;
    }

    @JsonProperty("frecuencia")
    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }
}
