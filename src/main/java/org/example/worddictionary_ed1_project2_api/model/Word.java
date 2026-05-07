package org.example.worddictionary_ed1_project2_api.model;

public class Word {

    private int  id;
    private String word;
    private String definition;
    private int frequency;

    //-------------- CONSTRUCTORES ----------------
    public Word(){}

    public Word(int id, String word, String definition, int frequency) {
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

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    @Override
    public String toString() {
        return "Word{" +
                "id=" + id +
                ", word='" + word + '\'' +
                ", definition='" + definition + '\'' +
                ", frequency=" + frequency +
                '}';
    }
}
