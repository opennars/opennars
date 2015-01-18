//
// Translated by CS2J (http://www.cs2j.com)
//
package nars.art;

public class DynamicVector <Type>  
{
    public Type[] array;
    public DynamicVector(int arraySize) {
        array = (Type[])new Object[arraySize];
    }

    public Type get___idx(int i) {
        return array[i];
    }

    public void set___idx(int i, Type value) {
        array[i] = value;
    }
}

