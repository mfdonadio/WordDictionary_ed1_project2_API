package org.example.worddictionary_ed1_project2_api.structures;

import java.util.ArrayList;
import java.util.List;

public class MyHashMap<K, V> {

    private static final int DEFAULT_CAPACITY = 16;
    private static final float LOAD_FACTOR = 0.75f; //Rehash cuando el 75% de los buckets esten ocupados

    private MyLinkedList<WordEntry<K, V>>[] hashTable;
    private int size;
    private int capacity;
    private int idCounter = 1; //El ID es secuancial, empezando en 1

    //------------------- CONSTRUCTORES -----------------------


    //CASO 1: cuando no sepamos cuantos elementos van a ingresar
    @SuppressWarnings("unchecked")
    public MyHashMap() {
        this.size = 0;
        this.hashTable = new MyLinkedList[DEFAULT_CAPACITY];
        this.capacity = DEFAULT_CAPACITY;
    }

    //CASO 2: cuando sepamos cuantos elementos van a ingresar, reservamos espacion exacto
    @SuppressWarnings("unchecked")
    public MyHashMap(int capacity) {
        this.capacity = capacity;
        this.hashTable = new MyLinkedList[capacity];
        this.size = 0;
    }

    //----------------- AGREGAR -----------------------------
    public void add(K key, V value) {
        //Antes de agregar, verificamos si es necesario haccer rehash
        if((float)size/capacity >= LOAD_FACTOR){
            rehash();
        }

        int hashedKey = hash(key);

        //En caso de que no exista el bucket, lo creamos
        if(hashTable[hashedKey] == null){
            hashTable[hashedKey] = new MyLinkedList<>();
        }

        //Si ya existe la llave, solo actualizamos el valor
        WordEntry<K,V> existing = hashTable[hashedKey].get(e -> e.key.equals(key));
        if(existing != null){
            existing.value = value;
            return;
        }

        //Ahora, si no existe, la agregamos con su respectivo id
        hashTable[hashedKey].add(new WordEntry<>(idCounter++, key, value));
        size++;
    }

    public void addWithId(K key, V value, int existingId) {
        // igual que add() pero usa existingId en lugar de idCounter++
        // y actualiza idCounter si existingId >= idCounter

        int hashedKey = hash(key);

        if(hashTable[hashedKey] == null){
            hashTable[hashedKey] = new MyLinkedList<>();
        }

        if(existingId >= idCounter) idCounter = existingId + 1;
        hashTable[hashedKey].add(new WordEntry<>(existingId, key, value));
        size++;
    }

    //-------------- BUSCAR (OBTENER) ---------------------------------
    public V get(K key) {
        int hashedKey = hash(key);

        if(hashTable[hashedKey] == null) return null;

        WordEntry<K,V> entry = hashTable[hashedKey].get(e -> e.key.equals(key));
        return entry != null ? entry.value : null;
    }

    //Busqueda por ID
    public V getById(int id){
        for(MyLinkedList<WordEntry<K, V>> bucket : hashTable){
            if(bucket == null) continue;
            WordEntry<K,V> entry = bucket.get(e -> e.id == id);
            if(entry != null) return entry.value;
        }
        return null;
    }

    //Busqueda  para obtener WorEntry completa dada la llave
    public WordEntry<K,V>  getEntry(K key) {
        int hashedKey = hash(key);

        if(hashTable[hashedKey] == null) return null;

        return hashTable[hashedKey].get(e -> e.key.equals(key));
    }

    //Busqueda para obtener WordEntry completa dado un ID
    public WordEntry<K,V> getEntryById(int id){
        for(MyLinkedList<WordEntry<K, V>> bucket : hashTable){
            if(bucket == null) continue;
            WordEntry<K,V> entry = bucket.get(e -> e.id == id);
            if(entry != null) return entry;
        }
        return null;
    }

    //---------------- ELIMINAR -------------------------
    public boolean remove(K key) {
        int hashedKey = hash(key);

        if(hashTable[hashedKey] == null) return false;

        boolean removed = hashTable[hashedKey].remove(e -> e.key.equals(key));
        if(removed) size --;
        return removed;
    }

    //Eliminar por ID
    public boolean removeById(int id) {
        for(MyLinkedList<WordEntry<K, V>> bucket : hashTable){
            if(bucket == null) continue;
            boolean removed = bucket.remove(e -> e.id == id);
            if(removed) {
                size --;
                return true;
            }
        }
        return false;
    }

    //---------------- CONTAINS? ------------------------
    public boolean contains(K key) {
        int hashedKey = hash(key);
        if(hashTable[hashedKey] == null) return false;
        return hashTable[hashedKey].contains(e -> e.key.equals(key));
    }

    //---------------- UTILIDADES EXTRAS ---------------
    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    //Retorno de todas las entries, me va a servir para el CSV
    public List<WordEntry<K,V>> getAll() {
        List<WordEntry<K,V>> result = new ArrayList<>();
        for(MyLinkedList<WordEntry<K, V>> bucket : hashTable){
            if(bucket == null) continue;
            for(WordEntry<K,V> entry : bucket){
                result.add(entry);
            }
        }
        return result;
    }

    //Imprimir todos
    public void printAll(){
        for(MyLinkedList<WordEntry<K, V>> bucket : hashTable){
            if(bucket == null) continue;
            for(WordEntry<K,V> entry : bucket){
                System.out.println(entry);
            }
        }
    }

    //---------------- REHASH -----------------------------
    /*Cuando la tabla llegue al 75% de ocupacion, vamos a duplicar el tamaño
        y redistribuimos todas las entradas con el nuevo hash
     */
    @SuppressWarnings("unchecked")
    private void rehash() {
        int newCapacity = capacity * 2;
        MyLinkedList<WordEntry<K, V>>[] newTable = new MyLinkedList[newCapacity];

        for(MyLinkedList<WordEntry<K, V>> bucket : hashTable){
            if(bucket == null) continue;
            for(WordEntry<K, V> entry : bucket){
                int newIndex = Math.abs(entry.key.hashCode()) % newCapacity;
                if(newTable[newIndex] == null){
                    newTable[newIndex] = new MyLinkedList<>();
                }
                newTable[newIndex].add(entry);
            }
        }

        hashTable = newTable;
        capacity = newCapacity;
    }


    //---------------- HASH  -------------------------------
    private int hash(K key) {
        return Math.abs(key.hashCode()) % capacity;
    }


    //--------------- ENTRY -----------------------------

    /*Esta clase es la version "generica" de la clase Producto
        que hicimos en clase :D */
    public static class WordEntry<K, V> {
        public final int id;
        public final K key;
        public V value;

        public WordEntry(int id, K key, V value) {
            this.id = id;
            this.key = key;
            this.value = value;
        }


        @Override
        public String toString() {
            return "Entry{" +
                    "id=" + id +
                    ", key=" + key +
                    ", value=" + value +
                    '}';
        }
    }
}

