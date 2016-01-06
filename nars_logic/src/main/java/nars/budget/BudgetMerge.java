package nars.budget;

/**
 * Budget merge function, with input scale factor
 */
@FunctionalInterface
public interface BudgetMerge {

    /** merge 'incoming' budget (scaled by incomingScale) into 'existing' */
    void merge(Budget existing, Budget incoming, float incomingScale);


    BudgetMerge plusDQDominated = (tgt, src, srcScale) -> {
        float nextPriority = src.getPriority() * srcScale;

        float currentPriority = tgt.getPriorityIfNaNThenZero();

        float sumPriority = currentPriority + nextPriority;
        if (sumPriority > 1) sumPriority = 1f;

        boolean currentWins = currentPriority > nextPriority;

        tgt.budget( sumPriority,
                (currentWins ? tgt.getDurability() : src.getDurability()),
                (currentWins ? tgt.getQuality() : src.getQuality()));
    };
    /** add priority, interpolate durability and quality according to the relative change in priority
     *  WARNING untested
     * */
    BudgetMerge plusDQInterp = (tgt, src, srcScale) -> {
        float dp = src.getPriority() * srcScale;

        float currentPriority = tgt.getPriorityIfNaNThenZero();

        float nextPri = currentPriority + dp;
        if (nextPri > 1) nextPri = 1f;

        float currentNextPrioritySum = (currentPriority + nextPri);

        /* current proportion */
        final float cp;
        cp = currentNextPrioritySum != 0 ? currentPriority / currentNextPrioritySum : 0.5f;

        /* next proportion = 1 - cp */
        float np = 1.0f - cp;


        float nextDur = (cp * tgt.getDurability()) + (np * src.getDurability());
        float nextQua = (cp * tgt.getQuality()) + (np * src.getQuality());

        if (Float.isNaN(nextDur))
            throw new RuntimeException("NaN dur: " + src + ' ' + tgt.getDurability());
        if (Float.isNaN(nextQua)) throw new RuntimeException("NaN quality");

        tgt.budget( nextPri, nextDur, nextQua );
    };

//    /** the max priority, durability, and quality of two tasks */
//    default Budget mergeMax(Budget b) {
//        return budget(
//                Util.max(getPriority(), b.getPriority()),
//                Util.max(getDurability(), b.getDurability()),
//                Util.max(getQuality(), b.getQuality())
//        );
//    }

//    /**
//     * merges another budget into this one, averaging each component
//     */
//    default void mergeAverageLERP(Budget that) {
//        if (this == that) return;
//
//        float currentPriority = getPriority();
//
//        float otherPriority = that.getPriority();
//
//        float prisum = (currentPriority + otherPriority);
//
//        /* current proportion */
//        float cp = (Util.equal(prisum, 0, BUDGET_EPSILON)) ?
//                0.5f : /* both are zero so they have equal infleunce */
//                (currentPriority / prisum);
//
//        /* next proportion */
//        float np = 1.0f - cp;
//
//        budget(
//                cp * getPriority() + np * that.getPriority(),
//                cp * getDurability() + np * that.getDurability(),
//                cp * getQuality() + np * that.getQuality()
//        );
//    }

//    /* ----------------------- Concept ----------------------- */
//    /**
//     * Activate a concept by an incoming TaskLink
//     *
//     *
//     * @param factor linear interpolation factor; 1.0: values are applied fully,  0: values are not applied at all
//     * @param receiver The budget receiving the activation
//     * @param amount The budget for the new item
//     */
//    public static void activate(final Budget receiver, final Budget amount, final Activating mode, final float factor) {
//        switch (mode) {
//            /*case Max:
//                receiver.max(amount);
//                break;*/
//
//            case Accum:
//                receiver.accumulate(amount);
//                break;
//
//            case Classic:
//                float priority = or(receiver.getPriority(), amount.getPriority());
//                float durability = aveAri(receiver.getDurability(), amount.getDurability());
//                receiver.setPriority(priority);
//                receiver.setDurability(durability);
//                break;
//
//            case WTF:
//
//                final float currentPriority = receiver.getPriority();
//                final float targetPriority = amount.getPriority();
//                /*receiver.setPriority(
//                        lerp(or(currentPriority, targetPriority),
//                                currentPriority,
//                                factor) );*/
//                float op = or(currentPriority, targetPriority);
//                if (op > currentPriority) op = lerp(op, currentPriority, factor);
//                receiver.setPriority( op );
//
//                final float currentDurability = receiver.getDurability();
//                final float targetDurability = amount.getDurability();
//                receiver.setDurability(
//                        lerp(aveAri(currentDurability, targetDurability),
//                                currentDurability,
//                                factor) );
//
//                //doesnt really change it:
//                //receiver.setQuality( receiver.getQuality() );
//
//                break;
//        }
//
//    }
//
}
