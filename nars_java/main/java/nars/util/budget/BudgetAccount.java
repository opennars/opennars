package nars.util.budget;

import nars.core.Parameters;
import com.google.common.util.concurrent.AtomicDouble;
import nars.entity.BudgetValue;

/**
 * Manages a quantity of priority funds, its inflow and outflow, and analytics
 */
public class BudgetAccount {
    
    private AtomicDouble balance;
    
    public BudgetAccount(float initialAmount) {
        this.balance = new AtomicDouble(initialAmount);
    }
    
    /** deposit: inject an amount of funds, from nothing */
    public void inc(float additional) {
        this.balance.addAndGet( additional );
    }
    
    /** volume = throughput of the economy; ie. how much trading */
    public void addVolume(float v) {
        
    }
    
    /** withdrawal: returns amount actually decremented, which may be less than the amount requested */
    public float dec(float amountRequested) {
        float current = (float) this.balance.get();
        if (current < amountRequested) {
            amountRequested = current;
            this.balance.set(0);
        }
        else {        
            this.balance.addAndGet(-amountRequested);
        }
        return amountRequested;
        
    }
    
    /** sets before and after priority after a transfer between two BudgetValue's
     * 
     * @param absorb  whether to absorb the remaining balance into this account, or redistribute half to each BudgetValue
     * @return returns the "change" value, whether absorbed or not
     * 
     */
    public float transfer(BudgetValue a, float aAfter, BudgetValue b, float bAfter, boolean absorb) {
        float aBefore = a.getPriority();
        float bBefore = b.getPriority();
        float totalBefore = aBefore + bBefore;
        float totalAfter = aAfter + bAfter;
        
        if ((aAfter < 0) || (bAfter < 0)) {
            throw new RuntimeException("Requires postiive values");
        }
            
        if (totalBefore < totalAfter - Parameters.BUDGET_EPSILON) {
            throw new RuntimeException("Fraudulent transaction: " + totalBefore + " < " + totalAfter);
        }
        
        float change = 0;
        if (totalBefore > totalAfter)
            change = totalBefore - totalAfter;

        
        if (!absorb) {
            aAfter += change/2f;
            bAfter += change/2f;
            if (aAfter > 1.0f) {
                change += aAfter - 1.0f;
                aAfter = 1.0f;            
            }
            if (bAfter > 1.0f) {
                change += bAfter - 1.0f;
                bAfter = 1.0f;            
            }
        }        
        
        a.setPriority(aAfter);
        b.setPriority(bAfter);

        //change or remainder
        if (change > 0) {
            inc(change);
        }
        
        //TODO verify correct:
        addVolume(change + (Math.abs(aAfter-aBefore) + Math.abs(bAfter - bBefore))/2f );
                
        return change;
    }

    public float getBalance() {
        return (float) balance.get();
    }

    /** spend funds to make a budgetvalue's priority reach a specific higher target priority, or as close as possible */
    public float invest(BudgetValue a, float requestedTarget) {
        if (requestedTarget < a.getPriority()) return 0;
        if (requestedTarget > 1f) requestedTarget = 1f;
        
        float diff = requestedTarget - a.getPriority();
                
        if (diff < 0) return 0;        
        
        diff = (float) Math.min(diff, balance.get());
        balance.addAndGet(-diff);
        a.setPriority(a.getPriority() + diff);
        
        return diff;
    }
    
    /** sell priority to gain funds, by specifying a lower budgetvalue's target priority */
    public float absorb(BudgetValue a, float requestedTarget) { 
        if (requestedTarget > a.getPriority()) return 0;
        if (requestedTarget < 0f) requestedTarget = 0f;
        
        float diff = a.getPriority() - requestedTarget;
                
        if (diff < 0) return 0;
                
        balance.addAndGet(diff);
        a.setPriority(requestedTarget);
        
        return diff;
        
    }
}
