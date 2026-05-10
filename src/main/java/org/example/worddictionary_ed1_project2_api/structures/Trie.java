package org.example.worddictionary_ed1_project2_api.structures;

import java.util.*;

public class Trie<T> {

    private Node root;
    private int insertionCounter = 0;

    public Trie(){
        root = new Node();
    }

    //------------------------------------- INSERT ----------------------------------------------
    //Ahora tambien recibe el significado
    public void insert(String word, T meaning){
        //"Normalizamos" la palabra
        String normalized = normalize(word);
        //Si no logramos normalizarla, ya ni hace falta seguir, retornamos nada mas
        if(normalized.isEmpty()) return;

        Node current = root;

        //POr cada caracter en la palabra...
        for (char c : normalized.toCharArray()) {
            //El siguiente nodo es el hijo --> lo obtenemos
            Node next = current.children.get(c);
            //Si es nulo...
            if (next == null) {
                //El siguiente es un nuevo nodo
                next = new Node();
                //Al que le insertamos el hijo del nodo actual
                current.children.add(c, next);
            }
            //Ahora el actual sera el siguiente para repetir elproceso con todos los hijos
            current = next;
        }

        //Si la marcamos por primera vez como 'ultima', aumentamos el contador de insercion y lo igualamos al timestamp
        if(!current.isLast){
            current.isLast = true;
            current.timestamp = insertionCounter++;
        }
        current.frequency++; //Auemantamos la frecuencia...SIEMPRE
        current.meaning = meaning;//Tambien, siempre actualizamos su significado
    }

    //------------------------------------------ SEARCH --------------------------------------------------
    public boolean search(String word){
        //Buscamos el nodo con getNode y normalizando la palabra para estandarizar
        Node node = getNode(normalize(word));
        return node != null && node.isLast;
    }

    public WordEntry<T> getEntry(String word){
        //Estandarizamos la palabra
        String normalized = normalize(word);
        //Obtenemos el nodo
        Node node = getNode(normalized);

        //Si es null o no esta marcado como ultimo... retornamos null
        if (node == null || !node.isLast) return null;
        //Retornamos la palabra
        return new WordEntry<>(normalized, node.meaning, node.frequency, node.timestamp);

    }

    public boolean startsWith(String prefix){
        //Estandarizamos el prefijo
        String normalized = normalize(prefix);
        if (normalized.isEmpty()) return true;
        return getNode(normalized) != null;
    }

    public List<WordEntry<T>> autoComplete(String prefix){
        //Estandarizamos el prefijo
        String normalized = normalize(prefix);
        //Obtenemos el nodo
        Node current = getNode(normalized);
        //Si es null, retornamos una nueva lisra VACIA
        if (current == null) return new ArrayList<>();

        //Inicializamos una lista vacia para almacenar las posibles palabras
        List<WordEntry<T>> result = new ArrayList<>();
        //Recollectamos todas las posibles palabras dado el prefijo
        collectEntries(current, new StringBuilder(normalized), result);
        //Retornamos la lisra de posibilidades
        return result;
    }

    // Modificamos para que el comparator se inyecte de afuera
    public List<WordEntry<T>> autoComplete(String prefix, int k, Comparator<WordEntry<T>> comparator) {
       //Normalizamos y obtenemos el nodo
        String normalized = normalize(prefix);
        Node current = getNode(normalized);
        if(current == null || k <= 0) return new ArrayList<>();

        //inicializamos la lista de posibles prefijos
        List<WordEntry<T>> candidates = new ArrayList<>();
        //Recolectamos todas las entradas
        collectEntries(current, new StringBuilder(normalized), candidates);
        //Ordenamos segun lo solicitado
        candidates.sort(comparator); //Inyeccion del criterio

        //Creamos la lisra en donde enlistaremos los posibles resultados
        List<WordEntry<T>> result = new ArrayList<>();
        //Seteamos un limite de iteracion
        int limit = Math.min(k, candidates.size());
        for (int i = 0; i < limit; i++) {
            //Agregamos a los candidatos
            result.add(candidates.get(i));
        }
        return result;
    }

    //Adaptado de un video
    public boolean searchWithWildcards(String pattern){
        String normalized = normalize(pattern);
        if (normalized.isEmpty()) return false;
        //Buscamos recursivamente en el trie
        return searchRecursive(root, normalized, 0);

    }

    //Creo que quedaria bien para el proyecto, ya no solo buscar para un coincidencia sino que tambien para todas
    //Algo mas parecido y adaptado a las herramientas que usamos
    public List<WordEntry<T>> searchAllWithWildcards(String pattern){
        String normalized = normalize(pattern);
        List<WordEntry<T>> result = new ArrayList<>();

        if (normalized.isEmpty()) return result;

        collectWithWildcards(root, normalized, 0, new StringBuilder(), result);
        return result;
    }

    //--------------------------------------- "UPDATERS" ---------------------------------------------
    public boolean updateMeaning(String word, T newMeaning){
        //Estandarizamos
        Node node = getNode(normalize(word));
        if (node == null || !node.isLast) return false;

        //Actualizamos el significado
        node.meaning = newMeaning;
        return true;
    }

    public boolean updateFrequency(String word, int newFrequency){
        //Obtenemos el nodo
        Node node =  getNode(normalize(word));
        //Verificamos que no sea nulo o no sea el ultimo...
        if (node == null || !node.isLast) return false;

        node.frequency = newFrequency;
        return true;
    }

    public boolean renameWord(String oldWord, String newWord){
        //Normalizamos el nombre viejo
        String oldNormalized = normalize(oldWord);
        //Normalizamos el nombre nuevo
        String newNormalized = normalize(newWord);

        //Si el viejo o nuevo estan vacios, retornamos falso... no hay nada que renombrar.
        if (oldNormalized.isEmpty() || newNormalized.isEmpty()) return false;
        //Si se da el caso en que el nombre viejo sea igual al nuevo... retornamos true porque es lo mismo
        if (oldNormalized.equals(newNormalized)) return true;

        //Ahora, obtenemos la entrada del nombre viejo
        WordEntry<T> entry = getEntry(oldNormalized);
        //Si es nula retornamos falso
        if (entry == null) return false;

        //Primero eliminamos el nombre viejo
        delete(oldNormalized);
        //Ahora insertamos el nombre nuevo con la definicion que tenia el nombre viejo y la insertamos en el trie
        insert(newNormalized, entry.meaning);

        //Obtenemos el nodo actual (el nuevo)
        Node current = getNode(newNormalized);
        //Si es nulo retornamos false
        if (current == null) return false;

        //Seteamos la frecuencia de la nueva nombre con el del nombre viejo
        current.frequency = entry.frequency;
        //Lo mismo con su timestamp, el nuevo nombre usa el del viejo
        current.timestamp = entry.timestamp;
        //Retornamos true.
        return true;
    }

    //Delete
    public boolean delete(String word){
       //Estandarizamos
        String normalized = normalize(word);
        if (normalized.isEmpty() || !search(normalized)) return false;

        //Eliminamos recursivamente
        deleteRecursive(root, normalized, 0);
        return true;
    }

    //Borre los recorridos, en este proyecto no me sirven de nada. :D

    //------------------------------ COMPARATORS ---------------------------------------

    /*Encontre esta otra manera de definir comparators, se me hizo
    interesante, asi que la adapte y  la implemente jiji. Todo siempre
    de STO.
     */
    public static <T> Comparator<WordEntry<T>> byFrequencyDesc(){
        return (a,b)->{
            int byFrquency = Integer.compare(b.frequency, a.frequency);
                    if(byFrquency != 0) return byFrquency;
                    return Integer.compare(a.timestamp, b.timestamp);//Si hay empate, el mas antiguo primero :D
        };
    }

    public static <T> Comparator<WordEntry<T>> byFrequencyAsc() {
        return (a, b) -> {
            int byFrquency = Integer.compare(a.frequency, b.frequency);
            if(byFrquency != 0) return byFrquency;
            return Integer.compare(a.timestamp, b.timestamp);
        };
    }

    public static <T> Comparator<WordEntry<T>> byAlphaAsc() {
        return Comparator.comparing(e -> e.word);
    }

    public static <T> Comparator<WordEntry<T>> byAlphaDesc() {
        return (a, b) -> b.word.compareTo(a.word);
    }


    //---------------------------------- PRIVATE METHODS ---------------------------------------
    //Esta funcion me ayuda a estandarizar (o normalizar) el texto que ingrese, acortando espacios y poniendo los caracteres en minusculas
    private String normalize(String text){
        if(text == null) return "";
        return text.trim().toLowerCase();
    }

    //Esta funcion me va a ayudar a obtener los nodos (caracteres) de una palabra
    private Node getNode(String text){
        if(text == null) return null;

        Node current = root;
        for(char c : text.toLowerCase().toCharArray()){
            current = current.children.get(c);
            if(current == null) return null;
        }
        return current;
    }

    //Devuelve una lista ordenada de los caracteres de un palabra.
    private List<Character> sortedLetters(Node node){
        List<Character> letters = new ArrayList<>();
        for(MyHashMap.WordEntry<Character, Node> entry : node.children.getAll()){
            letters.add(entry.key);
        }
        Collections.sort(letters);
        return letters;
    }


    //Para recolectar las estadisticas de los nodos ---> igual adaptado de StackOverflow
    private void collectEntries(Node node, StringBuilder prefix, List<WordEntry<T>> result){
        if(node.isLast){
            result.add(new WordEntry<>(prefix.toString(), node.meaning, node.frequency,  node.timestamp));
        }

        for (char letter : sortedLetters(node)) {
            prefix.append(letter);
            collectEntries(node.children.get(letter), prefix, result);
            prefix.setLength(prefix.length() - 1);
        }
    }

    //Apatado de un video
    private boolean searchRecursive(Node current, String pattern, int index){
        //Si llegamos al final de la palabra, verificamos si es valida
        if(current == null) return false;
        if(index == pattern.length()) return current.isLast;

        //Obtiene el char en el indice especificado en el patron
        char c = pattern.charAt(index);


        //Si es un comodin de 1 char, intentamos con todos los hijos posibles --> 26 letras (a-z)
        if(c == '.') {
            for (char letter : sortedLetters(current)) {
                if (searchRecursive(current.children.get(letter), pattern, index + 1)) {
                    return true;
                }
            }
            return false;
        }
        if(c == '*'){
            //Si es un asterisco, puede reemplazar 0 o mas caracteres, no esta limitado como el punto
            if(searchRecursive(current, pattern, index+1)) return true;
            for(char letter : sortedLetters(current)) {
                if(searchRecursive(current.children.get(letter), pattern, index)) return true; //Si encontramos coincidencias
            }
            return false; //Si no encontramos coincidencias
        }
            return searchRecursive(current.children.get(c), pattern, index + 1);
    }

    //Recolecta todas las WordEntry que coinciden con el patron
    private void collectWithWildcards(Node current, String pattern, int index,
                                      StringBuilder prefix, List<WordEntry<T>> result){
        if(index == pattern.length()){
            if(current.isLast){
                addIfAbsent(result, new WordEntry<>(prefix.toString(),
                        current.meaning, current.frequency, current.timestamp));
            }
            return;
        }

        //Busca el char en el indice indicado dentro del pattern
        char c = pattern.charAt(index);

        if (c == '.') {
            for (char letter : sortedLetters(current)) {
                prefix.append(letter);
                collectWithWildcards(current.children.get(letter), pattern, index + 1, prefix, result);
                prefix.setLength(prefix.length() - 1);
            }
            return;
        }

        if (c == '*') {
            // Caso 1: '*' consume cero caracteres.
            collectWithWildcards(current, pattern, index + 1, prefix, result);

            // Caso 2: '*' consume uno o más caracteres.
            for (char letter : sortedLetters(current)) {
                prefix.append(letter);
                collectWithWildcards(current.children.get(letter), pattern, index, prefix, result);
                prefix.setLength(prefix.length() - 1);
            }
            return;
        }

        Node next = current.children.get(c);
        if (next != null) {
            prefix.append(c);
            collectWithWildcards(next, pattern, index + 1, prefix, result);
            prefix.setLength(prefix.length() - 1);
        }
    }

    // Agrega el candidato a la lista solo si su palabra aún no existe en los resultados.
    // Esto evita duplicados, especialmente en búsquedas con comodines como '*'.
    private void addIfAbsent(List<WordEntry<T>> result, WordEntry<T> candidate) {
        for (WordEntry<T> entry : result) {
            if (entry.word.equals(candidate.word)) return;
        }
        result.add(candidate);
    }



    //Delete recursivo se encarga de limpiar los nodos huerfanos hacia arriba
    private boolean deleteRecursive(Node current, String word, int index){
        if(index == word.length()){
            if(!current.isLast) return false; // la palabra no existe

            current.isLast = false;
            current.meaning = null;
            current.frequency = 0;
            current.timestamp = Integer.MAX_VALUE;

            return current.children.isEmpty(); // true = este nodo puede eliminarse
        }

        char c = word.charAt(index);
        Node child = current.children.get(c);
        if(child == null) return false;

        boolean shouldDeleteChild = deleteRecursive(child, word, index + 1);

        if(shouldDeleteChild){
            current.children.remove(c);
            // Este nodo puede eliminarse si ya no tiene hijos y no es fin de otra palabra
            return current.children.isEmpty() && !current.isLast;
        }

        return false;
    }


    //----------------------------------- INTERN CLASSES ------------------------------------
    private class Node{
        //Para mejorar la eficiencia, mejor que un arreglo, es un HashMap
        private final MyHashMap<Character, Node> children;
        private boolean isLast;
        private int frequency;
        private int timestamp;//El cirterio de desmpate en caso de que la frecuencia sea la misma
        private T meaning;

        public Node(){
            children = new MyHashMap<>();
            isLast = false;
            frequency = 0;
            timestamp = Integer.MAX_VALUE;
            meaning = null;
        }
    }

    //Este 'wrapper' nos ayuda a manetener el nodo junto con su palabra acumulada ---> lo vi en un video jeje
    private class NodeWrapper {
        private final Node node;
        private final String prefix;

        //Constructor
        NodeWrapper(Node node, String prefix) {
            this.node = node;
            this.prefix = prefix;
        }
    }

    //Creamos un record para guardar las frecuencias de las palabras
    public static class WordEntry<T>{
        public final String word;
        public final T meaning;
        public final int frequency;
        public final int timestamp;


        public WordEntry(String word, T meaning, int frequency, int timestamp) {
            this.word = word;
            this.meaning = meaning;
            this.frequency = frequency;
            this.timestamp = timestamp;
        }

        @Override
        public String toString() {
            return "WordEntry{" +
                    "word='" + word + '\'' +
                    ", meaning=" + meaning +
                    ", frequency=" + frequency +
                    ", timestamp=" + timestamp +
                    '}';
        }
    }
}
