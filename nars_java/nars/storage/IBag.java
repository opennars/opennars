package nars.storage;

import nars.entity.Item;


public interface IBag<E extends Item> {

    void clear();

    /**
     * Check if an item is in the bag
     *
     * @param it An item
     * @return Whether the Item is in the Bag
     */
    boolean contains(final E it);

    /**
     * Get an Item by key
     *
     * @param key The key of the Item
     * @return The Item with the given key
     */
    E get(final String key);

    int getCapacity();

    float getMass();

    /**
     * Add a new Item into the Bag
     *
     * @param newItem The new Item
     * @return Whether the new Item is added into the Bag
     */
    boolean putIn(final E newItem);
    
    /**
     * Put an item back into the itemTable
     * <p>
     * The only place where the forgetting rate is applied
     *
     * @param oldItem The Item to put back
     * @return Whether the new Item is added into the Bag
     */
    boolean putBack(final E oldItem);

    /**
     * The number of items in the bag
     *
     * @return The number of items
     */
    int size();

    /**
     * Choose an Item according to priority distribution and take it out of the
     * Bag
     *
     * @return The selected Item, or null if this bag is empty
     */
    E takeOut();
    E pickOut(final String key);    
}
