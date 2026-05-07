package org.example.worddictionary_ed1_project2_api.service;

import jakarta.annotation.PostConstruct;
import org.example.worddictionary_ed1_project2_api.dto.WordRequest;
import org.example.worddictionary_ed1_project2_api.dto.WordResponse;
import org.example.worddictionary_ed1_project2_api.model.Word;
import org.example.worddictionary_ed1_project2_api.structures.MyHashMap;
import org.example.worddictionary_ed1_project2_api.structures.MyPriorityQueue;
import org.example.worddictionary_ed1_project2_api.structures.Trie;
import org.example.worddictionary_ed1_project2_api.util.CsvUtil;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class DictionaryService {

    private final Trie<String> trie;
    private final MyHashMap<String, Word> hashMap;
    private final CsvUtil csvUtil;

    public DictionaryService(Trie<String> trie, MyHashMap<String, Word> hashMap, CsvUtil csvUtil) {
        this.trie = trie;
        this.hashMap = hashMap;
        this.csvUtil = csvUtil;
    }

    //Cuando arranca el servidor, cargamos el CSV en las estructuras
    @PostConstruct
    public void loadFromCSV(){
        List<Word> words = csvUtil.importCSV();
        for(Word w : words){
            trie.insert(w.getWord(), w.getDefinition());
            hashMap.addWithId(w.getWord(), w, w.getId()); //Preservamos el Id
        }
        System.out.println("Diccionario cargado con " + words.size() + " palabras.");
    }

    //--------- INSERCION ---------- (lo mismo que hice en MyPriorityQueue pero para el service)
    public WordResponse insert(WordRequest request) {
        String w = request.getWord().toLowerCase();

        // Si ya existe, solo aumentamos la frecuencia
        if (hashMap.contains(w)) {
            Word existente = hashMap.get(w);
            existente.setFrequency(existente.getFrequency() + 1);
            trie.updateFrequency(w, existente.getFrequency());
            saveCSV();
            return toResponse(existente);
        }

        // Si no existe, insertamos
        Word n = new Word(0, w, request.getDefinition(), 1);
        hashMap.add(w, n);

        // Obtenemos el ID que le asigno el HashMap
        MyHashMap.WordEntry<String, Word> entryCreated = hashMap.getEntry(w);
        n.setId(entryCreated.id);
        trie.insert(w, request.getDefinition()); // solo una vez, después del hashMap
        saveCSV();
        return toResponse(n);
    }

    //--------- UPDATE -----------------
    public WordResponse update(int id, WordRequest request) {
        MyHashMap.WordEntry<String, Word> entry = hashMap.getEntryById(id);
        if (entry == null) return null;

        Word current = entry.value;
        String oldWord = current.getWord();
        String newWord = request.getWord().toLowerCase();

        // Si cambiaron la palabra, renombramos en el Trie y en el HashMap
        if (!oldWord.equals(newWord)) {
            trie.renameWord(oldWord, newWord);
            hashMap.remove(oldWord);
            current.setWord(newWord);
            hashMap.add(newWord, current);
        }

        // Actualizamos significado y frecuencia
        current.setDefinition(request.getDefinition());
        current.setFrequency(request.getFrequency());
        trie.updateMeaning(newWord, request.getDefinition());
        trie.updateFrequency(newWord, request.getFrequency());

        saveCSV();
        return toResponse(current);
    }

    //----------- DELETE -------------
    public boolean delete(String word) {
        word = word.toLowerCase();
        if (!hashMap.contains(word)) return false;

        trie.delete(word);
        hashMap.remove(word);
        saveCSV();
        return true;
    }

    public boolean deleteById(int id) {
        MyHashMap.WordEntry<String, Word> entry = hashMap.getEntryById(id);
        if (entry == null) return false;

        trie.delete(entry.value.getWord());
        hashMap.removeById(id);
        saveCSV();
        return true;
    }

    //---------- SEARCH --------------
    // Busqueda exacta por palabra
    public WordResponse search(String palabra) {
        Word p = hashMap.get(palabra.toLowerCase());
        if (p == null) return null;

        // Cada busqueda aumenta la frecuencia
        p.setFrequency(p.getFrequency() + 1);
        trie.updateFrequency(palabra.toLowerCase(), p.getFrequency());
        saveCSV();
        return toResponse(p);
    }

    // Busqueda por ID
    public WordResponse searchById(int id) {
        Word p = hashMap.getById(id);
        if (p == null) return null;

        p.setFrequency(p.getFrequency() + 1);
        trie.updateFrequency(p.getWord(), p.getFrequency());
        saveCSV();
        return toResponse(p);
    }

    //-------- AUTOCOMPLETE/PREFIJO -------
    public List<WordResponse> serchByPrefix(String prefix, Integer limit, String orderBy, String order) {
        Comparator<Trie.WordEntry<String>> comparator = resolveComparator(orderBy, order);

        List<Trie.WordEntry<String>> entries = (limit != null)
                ? trie.autoComplete(prefix, limit, comparator)
                : trie.autoComplete(prefix);
        return enrich(entries);
    }

    //--------- WILDCARD ------------
    public List<WordResponse> searchByWildcard(String pattern, Integer limit, String orderBy, String order) {
        List<Trie.WordEntry<String>> entries = trie.searchAllWithWildcards(pattern);

        //Ahora ordenamos la cola con la prioridad prara al TopK
        Comparator<Trie.WordEntry<String>> comparator = resolveComparator(orderBy, order);
        MyPriorityQueue<Trie.WordEntry<String>> queue = new MyPriorityQueue<>(comparator);
        for(Trie.WordEntry<String> e : entries) queue.insertar(e);

        List<Trie.WordEntry<String>> result = (limit != null)
                ? queue.topK(limit)
                : queue.topK(entries.size());
        return enrich(result);
    }

    //--------- TOP K -----------------
    public List<WordResponse> topK (int k, String orderBy, String order) {
        //Obtenemos las palabras del HashMap
        List<MyHashMap.WordEntry<String, Word>> all = hashMap.getAll();

        //Comparator sobre Word directamente
        Comparator<Word> comparator;
        if("frecuencia".equalsIgnoreCase(orderBy)) {
            comparator = "desc".equalsIgnoreCase(order)
                    ? (a, b) -> b.getFrequency() - a.getFrequency()
                    : (a, b) -> a.getFrequency() - b.getFrequency();
        } else {
            comparator = "desc".equalsIgnoreCase(order)
                    ? (a, b) -> b.getWord().compareTo(a.getWord())
                    :  (a, b) -> a.getWord().compareTo(b.getWord());
        }

        MyPriorityQueue<Word> queue = new MyPriorityQueue<>(comparator);
        for(MyHashMap.WordEntry<String, Word> e : all) queue.insertar(e.value);

        List<WordResponse> result = new ArrayList<>();
        for(Word w : queue.topK(Math.min(k, all.size()))){
            result.add(toResponse(w));
        }
        return result;
    }

    //-------- CSV ---------------------
    public void saveCSV() {
        List<Word> todas = new ArrayList<>();
        for (MyHashMap.WordEntry<String, Word> entry : hashMap.getAll()) {
            todas.add(entry.value);
        }
        csvUtil.exportCSV(todas);
    }

    // ----------- HELPERS ---------------
    // Convierte un Trie.WordEntry a PalabraResponse enriquecido con el ID del HashMap
    private List<WordResponse> enrich (List<Trie.WordEntry<String>> entries) {
        List<WordResponse> result = new ArrayList<>();
        for (Trie.WordEntry<String> e : entries) {
            Word p = hashMap.get(e.word);
            if (p != null) result.add(toResponse(p));
        }
        return result;
    }

    private WordResponse toResponse(Word p) {
        return new WordResponse(p.getId(), p.getWord(), p.getDefinition(), p.getFrequency());
    }

    // Resuelve el comparator segun los parametros de la request
    private Comparator<Trie.WordEntry<String>> resolveComparator(String ordenarPor, String orden) {
        if ("frecuencia".equalsIgnoreCase(ordenarPor)) {
            return "desc".equalsIgnoreCase(orden)
                    ? Trie.byFrequencyDesc()
                    : Trie.byFrequencyAsc();
        }
        // Por defecto: alfabetico
        return "desc".equalsIgnoreCase(orden)
                ? Trie.byAlphaDesc()
                : Trie.byAlphaAsc();
    }


}
