package org.example.worddictionary_ed1_project2_api.structures;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

public class MyLinkedList<T> implements Iterable<T> {

    private Node<T> head;
    private int size;

    public MyLinkedList() {
        head = null;
        size = 0;
    }

    //--------- AGREGAR A LA LISTA -------

    /* Agregamos al final de la lista */
    public void add(T data) {
        Node<T> newNode = new Node<>(data);

        if (head == null) {
            head = newNode;
        } else {
            // Llegamos al ultimo nodo
            Node<T> current = head;
            while (current.next != null) {
                current = current.next;
            }
            current.next = newNode;
        }
        size++;
    }

    //------- BUSQUEDA -----------

    /*Buscamos el primer elemento que cumpla con el predicado*/
    public T get(Predicate<T> condition) {
        Node<T> current = head;

        while (current != null) {
            if (condition.test(current.data)) {
                return current.data;
            }
            current = current.next;
        }
        return null; // No se encontro
    }

    //--------- ELIMINACION --------

    /*Eliminamos el primer elemento que cumpla con el predicado*/
    public boolean remove(Predicate<T> condition) {
        if (head == null) return false;

        // Caso especial: el head cumple la condicion
        if (condition.test(head.data)) {
            head = head.next;
            size--;
            return true;
        }

        // Caso general: buscamos el nodo anterior al que queremos eliminar
        Node<T> current = head;
        while (current.next != null) {
            if (condition.test(current.next.data)) {
                current.next = current.next.next; // saltamos el nodo
                size--;
                return true;
            }
            current = current.next;
        }
        return false; // No se encontro
    }

    //------------ ACTUALIZAR --------

    /*Actualizamos el primer elemento que cumpla con la condicion*/
    public boolean update(Predicate<T> condition, T newData) {
        Node<T> current = head;

        while(current != null){
            if(condition.test(current.data)){
                current.data = newData;
                return true;
            }
            current = current.next;
        }
        return false;
    }

    //------- VERFICACION DEL CONTENIDO -----

    /*Corroboramos que se cumpla, o "contenga", la condicion*/
    public boolean contains(Predicate<T> condition) {
        return get(condition) != null;
    }

    //------ UTILIDADES EXTRAS -------

    /*Getter de size*/
    public int size() {
        return size;
    }

    /*Verificacion de vacia/no vacia*/
    public boolean isEmpty() {
        return head == null;
    }

    //------ ITERATOR ----------

    /*Adaptado de SO, esto nos ayuda a poder usar el for-each sobre la lista sin
    mayor complicacion */
    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            Node<T> current = head;

            @Override
            public boolean hasNext() {
                return current != null;
            }

            @Override
            public T next() {
                if(!hasNext()) throw new NoSuchElementException();
                T data = current.data;
                current = current.next;
                return data;
            }
        };
    }
    //-------- CLASS NODE --------
    private static class Node<T>{
        T data;
        Node<T> next;

        Node(T data){
            this.data = data;
            next = null;
        }
    }
}
