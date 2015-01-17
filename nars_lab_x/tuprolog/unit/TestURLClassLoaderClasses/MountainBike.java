package TestURLClassLoaderClasses;

public class MountainBike extends Bicycle {
        
    // the MountainBike subclass adds
    // one field
    private int seatHeight;

    // the MountainBike subclass has one
    // constructor
    public MountainBike(int startHeight,
                        int startCadence,
                        int startSpeed,
                        int startGear) {
        super(startCadence, startSpeed, startGear);
        seatHeight = startHeight;
    }   
        
    // the MountainBike subclass adds
    // one method
    public void setHeight(int newValue) {
        seatHeight = newValue;
    }   
    
    public int getSeatHeight()
    {return this.seatHeight;}
}