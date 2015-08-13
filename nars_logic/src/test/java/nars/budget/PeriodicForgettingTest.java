package nars.budget;


/** TODO make this into a utility for demonstrating budget dynamics */
public class PeriodicForgettingTest {


    public static void main(String[] args) {

        //TODO write some comparison tests for budgets of different durability after time T

        int forgetCycles = 5;
        int maxTime = 100;

        float initPriority = 1f;
        float durability = 0.0f;
        float quality = 0.01f;
        float budgetThreshold = 0.01f;

        Budget b = new Budget(initPriority, durability, quality);

        for (int t = 0; t < maxTime; t++) {
            BudgetFunctions.forgetPeriodic(b, forgetCycles, budgetThreshold, t);
            System.out.println(t + "," + b.getPriority() + "," + b.getDurability() + "," + b.getQuality());
        }

    }
}
