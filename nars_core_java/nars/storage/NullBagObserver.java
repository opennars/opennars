package nars.storage;

import nars.entity.Item;

/** a {@link BagObserver} that does nothing (null design pattern) */
public class NullBagObserver<BagType extends Item> implements BagObserver<BagType> {
	@Override
	public void setTitle(String title) {}
	@Override
	public void setBag( Bag<BagType> concepts ) {
	}
	@Override
	public void post(String str) {}
	@Override
	public void refresh(String string) {}
	@Override
	public void stop() {}    

    @Override
    public boolean isActive() {
        return false;
    }
        
}