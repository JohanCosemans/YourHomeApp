package net.yourhome.app.views;

public class SpinnerKeyValue<T,S> {
    private T key;
    private S value;
    
    public SpinnerKeyValue( T key, S value  ) {
        this.key = key;
        this.value = value;
    }
    public S getValue() {
        return value;
    }
    public T getKey() {
        return key;
    }
    public String toString() {
        return value.toString();
    }
}
