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
        Node current = root;

        for(char c : word.toLowerCase().toCharArray()){
            current.children.putIfAbsent(c, new Node());
            current = current.children.get(c);
        }
        //Si la marcamos por primera vez como 'ultima', aumentamos el coontador de insercion y lo igualamos al timestamp
        if(!current.isLast){
            current.isLast = true;
            current.timestamp = insertionCounter++;
        }
        current.frequency++; //Auemantamos la frecuencia...SIEMPRE
        current.meaning = meaning;//Tambien, siempre actualizamos su significado
    }

    //------------------------------------------ SEARCH --------------------------------------------------
    public boolean search(String word){
        Node current = root;

        for(char c : word.toLowerCase().toCharArray()){
            current = current.children.get(c);
            if(current == null) return false;
        }
        return current.isLast;
    }

    public WordEntry<T> getEntry(String word){
        Node current = root;

        for(char c : word.toLowerCase().toCharArray()){
            current = current.children.get(c);
            if(current == null) return null;
        }
        if(!current.isLast) return null;
        return new WordEntry<>(word.toLowerCase(), current.meaning, current.frequency, current.timestamp);
    }

    public boolean startsWith(String prefix){
        Node current = root;

        for(char c : prefix.toLowerCase().toCharArray()){
            current = current.children.get(c);
            if(current == null) return false;
        }
        return true;
    }

    public List<WordEntry<T>> autoComplete(String prefix){
        Node current = root;

        for(char c : prefix.toLowerCase().toCharArray()){
            current = current.children.get(c);
            if(current == null) return List.of();
        }

        List<WordEntry<T>> result = new ArrayList<>();
        autoComplete(current, result, new StringBuilder(prefix.toLowerCase()));
        return result;
    }

    // Modificamos para que el comparator se inyecte de afuera
    public List<WordEntry<T>> autoComplete(String prefix, int k, Comparator<WordEntry<T>> comparator) {
        Node current = root;

        //Vamos al nodo del prefijo
        for (char c : prefix.toLowerCase().toCharArray()) {
            current = current.children.get(c);
            if(current == null) return List.of();
        }

        //Recolectamos todas las palabras posibles desde este nodo
        List<WordEntry<T>> candidates = new ArrayList<>();
        collectEntries(current, new StringBuilder(prefix.toLowerCase()), candidates);
        //Ordenamos segun lo solicitado
        candidates.sort(comparator); //Inyeccion del criterio

        List<WordEntry<T>> result = new ArrayList<>();
        for (int i = 0; i < Math.min(k, candidates.size()); i++) {
            result.add(candidates.get(i));
        }
        return result;
    }

    //Adaptado de un video
    public boolean searchWithWildcards(String word){
        return searchRecursive(root, word.toLowerCase(), 0); //Indice empieza en 0
    }

    //Creo que quedaria bien para el proyecto, ya no solo buscar para un coincidencia sino que tambien para todas
    //Algo mas parecido y adaptado a las herramientas que usamos
    public List<WordEntry<T>> searchAllWithWildcards(String pattern){
        List<WordEntry<T>> result = new ArrayList<>();
        collectWithWildcards(root, pattern.toLowerCase(), 0, new StringBuilder(), result);
        return result;
    }

    //--------------------------------------- "UPDATERS" ---------------------------------------------
    public boolean updateMeaning(String word, T newMeaning){
        Node current = root;

        for (char c : word.toLowerCase().toCharArray()) {
            current = current.children.get(c);
            if(current == null) return false;
        }
        if(!current.isLast) return false;
        current.meaning = newMeaning;
        return true;
    }

    public boolean updateFrequency(String word, int newFrequency){
        Node current = root;

        for(char c : word.toLowerCase().toCharArray()){
            current = current.children.get(c);
            if(current == null) return false;
        }
        if(!current.isLast) return false;
        current.frequency = newFrequency;
        return true;
    }

    public boolean renameWord(String oldWord, String newWord){
        WordEntry<T> entry = getEntry(oldWord);
        if(entry == null) return false;

        delete(oldWord);
        insert(newWord, entry.meaning);

        Node current = root;
        for(char c : newWord.toLowerCase().toCharArray()){
            current = current.children.get(c);
        }
        current.frequency = entry.frequency;
        return true;
    }

    //Delete
    public boolean delete(String word){
        return deleteRecursive(root, word.toLowerCase(), 0);
    }


    //----------------------------------------- RECORRIDOS ------------------------------------------
    public List<String> getWords(){
        List<String> result = new ArrayList<>();
        preOrder(this.root, "", result);
        return result;
    }

    public List<String> treeTraversal(){
        List<String> result = new ArrayList<>();
        postOrder(this.root, "", result);
        return result;
    }

    public List<String> nodesByLevel(){
        List<String> result = new ArrayList<>();
        if(this.root == null) return result;

        //Hacemos una cola para almacenar los nodos que debemos visitar
        Queue<NodeWrapper> queue = new LinkedList<>();

        //Primero la raiz
        queue.add(new NodeWrapper(this.root, ""));

        while(!queue.isEmpty()){
            //Sacamos el primero nodo
            NodeWrapper current = queue.poll();

            //Agregamos el nodo a 'result'
            result.add(current.prefix);

            //Obtenemos las llaves y ordenamos
            List<Character> letters = new ArrayList<>(current.node.children.keySet());
            Collections.sort(letters);

            //Agregamos a los hijos a la cola para procesarlos despues
            for(char letter : letters){
                queue.add(new NodeWrapper(current.node.children.get(letter), current.prefix + letter));
            }
        }
        return result;
    }

    //------------------------------ COMPARATORS ---------------------------------------
    public static <T> Comparator<WordEntry<T>> byFrequencyDesc(){
        return (a,b)->{
            if(b.frequency != a.frequency) return b.frequency - a.frequency;
            return a.timestamp - b.timestamp; //Si hay empate, el mas antiguo primero :D
        };
    }

    public static <T> Comparator<WordEntry<T>> byFrequencyAsc() {
        return (a, b) -> {
            if (a.frequency != b.frequency) return a.frequency - b.frequency;
            return b.timestamp - a.timestamp;
        };
    }

    public static <T> Comparator<WordEntry<T>> byAlphaAsc() {
        return Comparator.comparing(e -> e.word);
    }

    public static <T> Comparator<WordEntry<T>> byAlphaDesc() {
        return (a, b) -> b.word.compareTo(a.word);
    }


    //---------------------------------- PRIVATE METHODS ---------------------------------------

    private void preOrder(Node root, String prefix,List<String> result){
        if(root == null) return;

        //Si el nodo 'raiz' es el fin de una palabra, lo guardamos
        if(root.isLast){
            result.add(prefix);
        }

        //Creamos la lista de caracteres --- obtenemos las llaves
        List<Character> letters = new ArrayList<>(root.children.keySet());
        Collections.sort(letters);

        //Exploramos los hijos
        for(char letter : letters){
            preOrder(root.children.get(letter), prefix + letter, result);
        }
    }

    private void postOrder(Node root, String prefix, List<String> result){
        if(root == null) return;

        //Primero visitamos los hijos
        //Creamos la lista de caracteres --- obtenemos las llaves
        List<Character> letters = new ArrayList<>(root.children.keySet());
        Collections.sort(letters);

        //Exploramos los hijos
        for(char letter : letters){
            postOrder(root.children.get(letter), prefix + letter, result);
        }

        //De ultimo procesamos la raiz
        result.add(prefix);
    }

    private void autoComplete(Node root, List<WordEntry<T>> result, StringBuilder sb){
        if(root.isLast){
            result.add(new WordEntry<>(sb.toString(), root.meaning, root.frequency, root.timestamp));
        }
        //Creamos la lista de caracteres --- obtenemos las llaves
        List<Character> letters = new ArrayList<>(root.children.keySet());
        Collections.sort(letters);

        //Exploramos los hijos
        for(char letter : letters){
            sb.append(letter);
            autoComplete(root.children.get(letter), result, sb);
            sb.setLength(sb.length() - 1);
        }
    }

    //Para recolectar las estadisticas de los nodos ---> igual adaptado de StackOverflow
    private void collectEntries(Node node, StringBuilder sb, List<WordEntry<T>> candidates){
        if(node.isLast){
            candidates.add(new WordEntry<>(sb.toString(), node.meaning, node.frequency,  node.timestamp));
        }

        //Creamos la lista de caracteres --- obtenemos las llaves
        List<Character> letters = new ArrayList<>(node.children.keySet());
        Collections.sort(letters); //Ordena el map

        for (char letter : letters) {
            sb.append(letter);
            collectEntries(node.children.get(letter), sb, candidates);
            sb.setLength(sb.length() - 1);
        }
    }

    //Apatado de un video
    private boolean searchRecursive(Node current, String word, int index){
        //Si llegamos al final de la palapra, verificamos si es valida
        if(index == word.length()) return current.isLast;

        char c = word.charAt(index);

        if(c == '.'){
            //Si es un comodin de 1 char, intentamos con todos los hijos posibles --> 26 letras (a-z)
            for(Node child : current.children.values()){
                if(searchRecursive(child, word, index+1))
                    return true; //Si alguna rama encuentra a la palabra, retornamos true
            }
            return false; //Si es que ninguna rama coincide
        } else if(c == '*'){
            if(searchRecursive(current, word, index+1)) return true;
            for(Node child : current.children.values()){
                if(searchRecursive(child, word, index)) return true;
            }
            return false;
        }else {
            Node nextNode = current.children.get(c); //buscamos el caracter especifico en el HashMap
            if(nextNode == null){
                return false;
            }
            return searchRecursive(nextNode, word, index + 1);
        }
    }

    //Recolecta todas las WorEntry que coinciden con el patron
    private void collectWithWildcards(Node current, String pattern, int index,
                                      StringBuilder sb, List<WordEntry<T>> result){
        if(index == pattern.length()){
            if(current.isLast){
                result.add(new WordEntry<>(sb.toString(), current.meaning, current.frequency, current.timestamp));
            }
            return;
        }

        char c = pattern.charAt(index);

        if(c == '.'){
            List<Character> letters = new ArrayList<>(current.children.keySet());
            Collections.sort(letters);
            for(char letter : letters){
                sb.append(letter);
                collectWithWildcards(current.children.get(letter), pattern, index + 1, sb, result);
                sb.setLength(sb.length() - 1);
            }

        } else if(c == '*'){
            // Cero chars: saltamos el '*'
            collectWithWildcards(current, pattern, index + 1, sb, result);
            // Uno o más chars: consumimos un hijo y mantenemos el indice del patron
            List<Character> letters = new ArrayList<>(current.children.keySet());
            Collections.sort(letters);
            for(char letter : letters){
                sb.append(letter);
                collectWithWildcards(current.children.get(letter), pattern, index, sb, result);
                sb.setLength(sb.length() - 1);
            }

        } else {
            Node next = current.children.get(c);
            if(next != null){
                sb.append(c);
                collectWithWildcards(next, pattern, index + 1, sb, result);
                sb.setLength(sb.length() - 1);
            }
        }
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
        public Map<Character, Node> children;
        public boolean isLast;
        public int frequency;
        public int timestamp;//El cirterio de desmpate en caso de que la frecuencia sea la misma
        public T meaning;

        public Node(){
            children = new HashMap<>();
            isLast = false;
            frequency = 0;
            timestamp = Integer.MAX_VALUE;
            meaning = null;
        }
    }

    //Este 'wrapper' nos ayuda a manetener el nodo junto con su palabra acumulada ---> lo vi en un video jeje
    private class NodeWrapper {
        Node node;
        String prefix;

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
