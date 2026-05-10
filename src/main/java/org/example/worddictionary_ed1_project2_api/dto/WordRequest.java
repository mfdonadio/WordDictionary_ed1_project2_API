package org.example.worddictionary_ed1_project2_api.dto;
//DTO significa "Data Transfer Object"
//Me ayuda a tranferir los datos del controlador al servicio


//@JsonProperty me ayuda a mapear un campo del objeto a un nombre especifico en JSON
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

public class WordRequest {

    private Integer id;
    private String word;
    private String definition;
    private Integer frequency;

    public WordRequest(){}

    @JsonProperty("id")
    public Integer getId() {
        return id;
    }

    @JsonProperty("id")
    public void setID(Integer id) {
        this.id = id;
    }

    @JsonProperty("palabra")
    public String getWord() {
        return word;
    }

    @JsonProperty("palabra")
    @JsonAlias("word") //Le damos un "apodo" al nombre mapeado, para que acepte el mismo tambien
    public void setWord(String word) {
        this.word = word;
    }

    @JsonProperty("significado")
    public String getDefinition() {
        return definition;
    }

    @JsonProperty("significado")
    @JsonAlias("definition")
    public void setDefinition(String definition) {
        this.definition = definition;
    }

    @JsonProperty("frecuencia")
    public Integer getFrequency() {
        return frequency;
    }

    @JsonProperty("frecuencia")
    @JsonAlias("frequency")
    public void setFrequency(Integer frequency) {
        this.frequency = frequency;
    }
}
