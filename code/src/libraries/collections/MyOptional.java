package libraries.collections;

public final class MyOptional<T> {
    private final T value;

    private MyOptional(T value){
        this.value = value;
    }

    // Wraps a non-null value. Throws if value is null
    public static <T> MyOptional<T> of(T value){
        if (value == null) throw new NullPointerException("value if null");
        return new MyOptional<>(value);
    }

    // Wraps a value that may be null. If null, result is empty
    public static <T> MyOptional<T> ofNullable(T value){
        return new MyOptional<>(value);
    }

    // returns an empty MyOptional
    public static <T> MyOptional<T> empty(){
        return new MyOptional<>(null);
    }

    // True if a value exists
    public boolean isPresent(){
        return value != null;
    }

    // true if empty
    public boolean isEmpty(){
        return value == null;
    }

    // gets the value if present, throws otherwise
    public T get(){
        if (value == null) throw new IllegalStateException("No value present");
        return value;
    }

    // returns value if present , otherwise returns fallback
    public T orElse(T other){
        return (value != null) ? value : other;
    }


}
