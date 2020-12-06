package userauth.domain;

public class Pair<K,V> implements java.io.Serializable {
    private K key;
    private V value;


    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public Pair() {
    }

    public K getKey() {return this.key;}
    public V getValue() {return this.value;}
}