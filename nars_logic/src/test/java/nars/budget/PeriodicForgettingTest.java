package nars.budget;


/** TODO make this into a utility for demonstrating budget dynamics */
public class PeriodicForgettingTest {


    public static void main(String[] args) {

        //TODO write some comparison tests for budgets of different durability after time T

        int forgetCycles = 5;
        int maxTime = 100;

        float initPriority = 1f;
        float durability = 0.5f;
        float quality = 0.5f;
        float budgetThreshold = 0.01f;

        Budget b = new Budget(initPriority, durability, quality);

        for (int t = 0; t < maxTime; t++) {
            b.forget(t, forgetCycles, 0);
            System.out.println(t + "," + b.getPriority() + "," + b.getDurability() + "," + b.getQuality());
        }

    }
}
