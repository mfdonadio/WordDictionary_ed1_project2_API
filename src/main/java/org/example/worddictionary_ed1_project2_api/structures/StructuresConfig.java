package org.example.worddictionary_ed1_project2_api.structures;

import org.example.worddictionary_ed1_project2_api.model.Word;
import org.example.worddictionary_ed1_project2_api.structures.MyHashMap;
import org.example.worddictionary_ed1_project2_api.structures.MyPriorityQueue;
import org.example.worddictionary_ed1_project2_api.structures.Trie;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StructuresConfig {

    //El trie almacena la palabra como llave y el significado como valor
    @Bean
    public Trie<String> trie(){
        return new Trie<>();
    }

    //El HashMap almacena la palabra como llave y el objeto Palabra como valor
    @Bean
    public MyHashMap<String,Word> hashMap(){
        return new MyHashMap<>();
    }

    // La cola de prioridad arranca vacia --- el Service la llena segun la operacion
    @Bean
    public MyPriorityQueue<Word> priorityQueue() {
        // Comparador por defecto: mayor frecuencia primero
        return new MyPriorityQueue<>((a, b) -> b.getFrequency() - a.getFrequency());
    }
}
