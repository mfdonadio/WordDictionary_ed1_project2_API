package org.example.worddictionary_ed1_project2_api.dto;
//DTO significa "Data Transfer Object"
//Me ayuda a tranferir los datos del controlador al servicio

public class WordRequest {

    private String word;
    private String definition;
    private int frequency;

    public WordRequest(){}


    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
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
}
