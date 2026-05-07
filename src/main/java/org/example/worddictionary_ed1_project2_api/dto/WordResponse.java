package org.example.worddictionary_ed1_project2_api.dto;

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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }
}
