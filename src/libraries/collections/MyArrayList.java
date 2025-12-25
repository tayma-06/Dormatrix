package libraries.collections;
// This is my personal duck duck list...
/*Here equals() method was used to remove element (DUCK) ny value (name)
it was used in :
            public boolean remove(Object o) method
    here equals was need to see if two objects were same or not
*/




// one duck follows after another in a perfectly indexed line


public class MyArrayList<T> { //Now T has a magical way of checking my DUCK type dynamically...So why not use it?
    private static final int DEFAULT_CAPACITY = 10;
    private static final Object[] EMPTY_ELEMENTDATA = {};

    private Object[] elementData; //Object if the great-grandparent of my DUCK FAMILY... it is the grandparent of T
    private int size;

    // 1st construstor of my DUCKDUCK
    public MyArrayList()
    {
        this.elementData = new Object[DEFAULT_CAPACITY];
        this.size = 0;
    }
    // 2nd constructor of my DUCKDUCK
    public MyArrayList(int initialCapacity)
    {
        if (initialCapacity > 0)
        {
            this.elementData = new Object[initialCapacity];
        }
        else if (initialCapacity == 0)
        {
            this.elementData = EMPTY_ELEMENTDATA;
        }
        else{
            throw new IllegalArgumentException("Illegal Capacity: " +initialCapacity);
        }
        this.size = 0;
    }

    // a method to replace System.copyArray
    // it will help my DUCKS move their position and more...
    public static void copyArray(Object[] src, int srcPos, Object[] dest, int destPos, int length){
        // making sure that the arrays are not null
        if (src == null || dest == null)
        {
            throw new NullPointerException("Source or destination array is null");
        }

        if (srcPos < 0 || destPos < 0|| length < 0 || srcPos + length > src.length || destPos + length > dest.length)
        {
            throw new IndexOutOfBoundsException("Array index out of bounds");
        }

        // copying backward
        if (src == dest && srcPos < destPos)
        {
            for (int i = length - 1;  i >= 0; i--)
            {
                dest[destPos + i] = src[srcPos + i];
            }
        }
        else
        {
            // copying forward
            for (int i = 0; i < length; i++)
            {
                dest[destPos + i] = src[srcPos + i];
            }
        }
    }

    // Adding a BABY DUCK at the end of FAMILY
    public boolean add(T element)
    {
        ensureCapacity(size + 1);
        elementData[size++] = element;
        return true;
    }

    // Adding a BABY DUCK at a specific indexed position
    public void add(int index, T element)
    {
        rangeCheckForAdd(index);
        ensureCapacity(size + 1);

        // Time to Shift my other BABY DUCKS!!
        copyArray(elementData, index, elementData, index + 1, size - index);
        elementData[index] = element;
        size++;
    }

    // Get my DUCK by index
    @SuppressWarnings("unchecked") // I know which DUCK i am choosing, get out
    public T get(int index)
    {
        rangeCheck(index);
        return (T) elementData[index];
    }

    // Set my DUCK at a specific index
    @SuppressWarnings("unchecked")
    public T set(int index, T element)
    {
        rangeCheck(index);
        T oldValue = (T) elementData[index];
        elementData[index] = element;
        return oldValue;
    }

    // Remove DUCK by index as it matured
    @SuppressWarnings("unchecked")
    public T remove(int index)
    {
        rangeCheck(index);
        T removedElement = (T) elementData[index];

        int numMoved = size - index - 1;
        if (numMoved > 0)
        {
            copyArray(elementData, index + 1, elementData, index, numMoved);
        }
        elementData[--size] = null;
        return removedElement;
    }

    // Remove DUCK by name TT
    public boolean remove(Object o)
    {
        if (o == null)
        {
            for (int i = 0; i < size; i++)
            {
                if (elementData[i] == null)
                {
                    fastRemove(i);
                    return true;
                }
            }
        }
        else
        {
            for (int i = 0; i < size; i++)
            {
                if (o.equals(elementData[i]))
                {
                    fastRemove(i);
                    return true;
                }
            }
        }
        return false;
    }

    private void fastRemove(int index)
    {
        int numMoved = size - index -1;
        if (numMoved > 0)
        {
            copyArray(elementData, index + 1, elementData, index, numMoved);
        }
        elementData[--size] = null;
    }

    // Check if an element(DUCK) is present
    public boolean contains(Object o)
    {
        return indexOf(o) >= 0;
    }

    // Find index of element(DUCK)
    public int indexOf(Object o)
    {
        if (o == null){
            for (int i = 0; i < size; i++)
            {
                if (elementData[i] == null)
                {
                    return i;
                }
            }
        }
        else
        {
            for (int i = 0; i< size; i++)
            {
                if (o.equals(elementData[i]))
                {
                    return i;
                }
            }
        }
        return -1;
    }

    // Get the size of my DUCK Family
    public int size()
    {
        return size;
    }

    // Check if my DUCK family is empty
    public boolean isEmpty()
    {
        return size == 0;
    }

    // Clear my DUCK family TT
    public void clear()
    {
        for (int i = 0; i < size; i++)
        {
            elementData[i] = null;
        }
        size = 0;
    }

    // Ensure my DUCK family capacity
    private void ensureCapacity(int minCapacity)
    {
        if (elementData.length < minCapacity)
        {
            int newCapacity = elementData.length * 2;
            while (newCapacity < minCapacity)
            {
                newCapacity = newCapacity * 2;
            }
            grow(newCapacity);
        }
    }

    // Grow the size of my DUCK Family(updated)
    private void grow(int newCapacity)
    {
        Object[] newArray = new Object[newCapacity];
        copyArray(elementData, 0, newArray, 0, size);
        elementData = newArray;
    }

    // Convert to array (updated)
    // I dont know when my DUCK family will need it
    // I dont want to decrease the size of my DUCK family
    // But still it should not contain unoccupied blocks
    public Object[] toArray()
    {
        Object[] array = new Object[size];
        copyArray(elementData, 0, array, 0, size);
        return array;
    }

    // Range check for get/set/remove function
    private void rangeCheck(int index)
    {
        if (index >= size || index < 0)
        {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: "+size);
        }
    }

    private void rangeCheckForAdd(int index)
    {
        if (index > size || index < 0)
        {
            throw new IndexOutOfBoundsException("Index: "+index+", Size: "+size);
        }
    }



}
