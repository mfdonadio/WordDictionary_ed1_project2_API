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
    //CASO 1: cuando sepamos cuantos elementos van a ingresar, reservamos espacion exacto
    @SuppressWarnings("unchecked")
    public MyHashMap(int capacity){
        if(capacity <= 0){
            throw new IllegalArgumentException("La capacidad inicial debe ser mayor a cero.");
        }
        this.capacity = capacity;
        this.hashTable = new MyLinkedList[capacity];
        this.size = 0;
    }

    //CASO 2: cuando no sepamos cuantos elementos van a ingresar
    @SuppressWarnings("unchecked")
    public MyHashMap() {
        this.capacity = DEFAULT_CAPACITY;
        this.hashTable = new MyLinkedList[DEFAULT_CAPACITY];
        this.size = 0;
    }

    //----------------- AGREGAR -----------------------------
    public void add(K key, V value) {
        validateKey(key);

        //Obtenemos la entrada
        WordEntry<K,V> existing = getEntry(key);
        //Si la palabra ya existe...
        if(existing != null){
            //Le asignamos el valor ya existente en nuestra tabla.
            existing.value = value;
            return;
        }

        //Si al agregar, superamos el factor de capacidad, hacemos "rehash"
        if((float) (size + 1)/capacity >= LOAD_FACTOR){
            rehash();
        }

        //Obtenemos el indice
        int index = hash(key);
        //Si el espacio indicado por el indice en nuestra tabala esta vacio...
        if(hashTable[index] == null){
            //Creamos un nuevo "bucket" en el espacio relacionado a ese indice
            hashTable[index] = new MyLinkedList<>();
        }

        //Agregamos la nueva palabra y aumentamos el contador para los id's.
        hashTable[index].add(new WordEntry<>(idCounter++, key, value));
        //Aumentamos el tamaño de la tabla.
        size++;
    }

    public void addWithId(K key, V value, int existingId) {
        // igual que add() pero usa existingId en lugar de idCounter++
        // y actualiza idCounter si existingId >= idCounter

        validateKey(key);

        //Obtenemos la entrada
        WordEntry<K,V> existing = getEntry(key);
        //Si la palabra ya existe...
        if(existing != null){
            //Le asignamos el valor ya existente en nuestra tabla.
            existing.value = value;
            //Ahora, si el id existente es mayor que el contador... seteamos el contador una unidad arriba del id ingresado para mantener cierto orden
            if(existingId >= idCounter){ idCounter = existingId + 1; }
            return;
        }

        //Si al agregar, superamos el factor de capacidad, hacemos "rehash"
        if((float) (size + 1)/capacity >= LOAD_FACTOR){
            rehash();
        }

        //Obtenemos el indice
        int index = hash(key);
        //Si el espacio indicado por el indice en nuestra tabla esta vacio...
        if(hashTable[index] == null){
            //Creamos un nuevo "bucket" en el espacio relacionado a ese indice
            hashTable[index] = new MyLinkedList<>();
        }

        //Ya que comprobamos que la palabra no existe y que obtuvimos su indice..
        //Hacemos lo mismo que antes, sumamos el idCounter para mantener el orden en caso de que el id de la palabra sea mayor o igual
        if (existingId >= idCounter) idCounter = existingId + 1;
        //Ahora si agregamos la nueva palabra
        hashTable[index].add(new WordEntry<>(existingId, key, value));
        size++;
    }

    //-------------- BUSCAR (OBTENER) ---------------------------------
    public V get(K key) {
        //Obtenemos la palabra por su llave...
        WordEntry<K, V> entry = getEntry(key);
        //Retornamos la misma; o null si no existiese
        return entry != null ? entry.value : null;
    }

    //Busqueda por ID
    public V getById(int id){
        //Obtenemos la palabra por su id...
        WordEntry<K, V> entry = getEntryById(id);
        //Retornamos la misma; o null si no existiese
        return entry != null ? entry.value : null;
    }

    //Busqueda  para obtener WordEntry, osea toda la entrada, dada la llave
    public WordEntry<K,V>  getEntry(K key) {
        //Validamos la llave
        validateKey(key);

        //Obtenemos el indice
        int index = hash(key);
        if(hashTable[index] == null) return null;

        return hashTable[index].get(e->e.key.equals(key));
    }

    //Busqueda para obtener WordEntry completa dado un ID
    public WordEntry<K,V> getEntryById(int id){
        //Recorremos todos los buckets de la tabla...
        for(MyLinkedList<WordEntry<K, V>> bucket : hashTable){
            //Siempre que no esten vacios...
            if(bucket == null) continue;
            //Obtenemos todas las entradas que coincidan con el id
            WordEntry<K,V> entry = bucket.get(e -> e.id == id);
            //Retornamos la entrada, siempre que no sea nula
            if(entry != null) return entry;
        }
        return null;
    }

    //---------------- ELIMINAR -------------------------
    public boolean remove(K key) {
        //Validamos la llave
        validateKey(key);

        int index = hash(key);
        if(hashTable[index] == null) return false;

        //Eliminamos la palabra, esto nos retornara un 'true' o false'
        boolean removed = hashTable[index].remove(e -> e.key.equals(key));
        //Si se logro eliminar, disminuye el tamaño de la tabla
        if(removed) size --;
         //Retornamos el booleano
        return removed;
    }

    //Eliminar por ID
    public boolean removeById(int id) {
        //Recorremos todos los buckets de la tabla...
        for(MyLinkedList<WordEntry<K, V>> bucket : hashTable){
            //Siempre que no esten vacios..
            if(bucket == null) continue;
            //Obtenemos 'true' o 'false' si se lograron eliminar coincidencias del id
            boolean removed = bucket.remove(e -> e.id == id);
            //Si se logró elimnar... disminuimos el tamaño de la tabla y retornamos 'true'
            if(removed) {
                size --;
                return true;
            }
        }
        //Si nada se cumple, retornamos false
        return false;
    }

    //---------------- CONTAINS? ------------------------
    public boolean contains(K key) {
        return getEntry(key) != null;
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
        //Seteamos la nueva capacidad (el doble de la anterior)
        int newCapacity = capacity * 2;
        //Creammos una nueva table con la nueva capacidad
        MyLinkedList<WordEntry<K, V>>[] newTable = new MyLinkedList[newCapacity];

        //Y por cada bucket en mi tabla actual...
        for(MyLinkedList<WordEntry<K, V>> bucket : hashTable){
            if(bucket == null) continue;

            //Obtengo cada palabra dentro de los buckets...
            for(WordEntry<K, V> entry : bucket){
                //Calculo su nuevo indice
                int newIndex = indexFor(entry.key, newCapacity);
                if(newTable[newIndex] == null){
                    newTable[newIndex] = new MyLinkedList<>();
                }
                //Agrego la palabra en la nueva table
                newTable[newIndex].add(entry);
            }
        }

        //Seteo la nueva tabla como la tabla principal
        hashTable = newTable;
        //Y por último seteo la nueva capacidad de la table principal
        capacity = newCapacity;
    }


    //---------------- HASH  -------------------------------
    private int hash(K key) {
        return indexFor(key, capacity);
    }

    //---------------- INDEX FOR ------------------------
    private int indexFor(K key, int tableCapacity){
        /*
        Indagando, encontre como es que Java mapea un objeto a un indice especifico en sus tablas hash.
        Esta formula asegura que siempre sea un indice positivo y que este dentro de  el rango
        de la tabla. Esta formula la encontre en STO :D
         */
        return (key.hashCode() & 0x7fffffff) % tableCapacity;
    }

    //---------------- VALIDATE KEY ----------------------
    //Simple, validar las llaves para garantizar un mejor funcionamiento y evitar errores
    private void validateKey(K key) {
        if(key == null) {
            throw new IllegalArgumentException("La llave del HashMap no puede ser null.");
        }
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

