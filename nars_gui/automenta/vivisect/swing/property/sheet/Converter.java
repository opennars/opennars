package automenta.vivisect.swing.property.sheet;

public interface Converter<T> {

	T toObject(String string);

	String toString(T object);
}
