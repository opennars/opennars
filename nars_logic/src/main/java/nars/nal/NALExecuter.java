package nars.nal;

import nars.Global;
import nars.Symbols;
import nars.budget.Budget;
import nars.budget.BudgetFunctions;
import nars.meta.TaskRule;
import nars.nal.nal1.Inheritance;
import nars.nal.nal3.SetExt;
import nars.nal.nal4.Product;
import nars.nal.nal4.Product1;
import nars.narsese.NarseseParser;
import nars.process.ConceptProcess;
import nars.process.TaskProcess;
import nars.task.Sentence;
import nars.task.Task;
import nars.task.TaskSeed;
import nars.term.Compound;
import nars.term.Term;
import nars.term.Variable;
import nars.term.Variables;
import nars.truth.Truth;
import nars.truth.TruthFunctions;
import nars.util.data.ArrayArrayList;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by patrick.hammer on 30.07.2015.
 */
public class NALExecuter {

    public static List<String> loadRuleStrings()
    {
        //1. Load files from list
        List<String> lines = null;

        try
        {
            lines = Files.readAllLines(Paths.get("C:\\Users\\patrick.hammer\\IdeaProjects\\opennars\\nars_logic\\src\\main\\java\\nars\\nal\\NAL_Definition.logic"), StandardCharsets.UTF_8);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        List<String> unparsed_rules = new ArrayList<>();
        String current_rule = "";

        for(String s : lines)
        {
            if(s.startsWith("//") || s.replace(" ","").isEmpty())
            {
                if(!current_rule.isEmpty())
                {
                    unparsed_rules.add(current_rule); //rule is finished, add it
                    current_rule = ""; //start identifying a new rule
                }
            }
            else
            {
                //note, it can also be that the current_rule is not empty and this line contains |- which means
                //its already a new rule, in which case the old rule has to be added before we go on
                if(!current_rule.isEmpty() && s.contains("|-"))
                {
                    if(!current_rule.isEmpty())
                    {
                        unparsed_rules.add(current_rule); //rule is finished, add it
                        current_rule = ""; //start identifying a new rule
                    }
                }
                current_rule += s + "\n";
            }
        }

        return unparsed_rules;
    }

    public static String preprocess(String rule) //minor things like Truth.Comparison -> Truth_Comparison
    {                                     //A_1..n ->  "A_1_n" and adding of "<" and ">" in order to be parsable
        String ret = "<" + rule.replace("Truth.","Truth_").replace("Desire.", "Desire_").replace("Occurrence.", "Occurrence_").replace("Order.","Order_").replace("Stamp.","Stamp_") + ">";
        while(ret.contains("  "))
        {
            ret=ret.replace("  "," ");
        }
        return ret.replace("\n", "")/*.replace("A_1..n","\"A_1..n\"")*/; //TODO: implement A_1...n notation, needs dynamic term construction before matching
    }

    public static List<Term> parseRules(List<String> not_yet_parsed_rules)
    {
        //2. ok we have our unparsed rules, lets parse them to terms now
        NarseseParser meta = NarseseParser.the();
        List<Term> uninterpreted_rules = new ArrayList<>();

        for(String rule : not_yet_parsed_rules)
        {
            String parsable = preprocess(rule);
            try
            {
                TaskRule r = meta.term(parsable);
                r.normalize();
                uninterpreted_rules.add(r); //try to parse it
            }
            catch (Exception ex)
            {
                System.out.println("The following rule can not be parsed so won't be used:\n" + parsable + " \n "+ ex.toString());
            }
        }

        return uninterpreted_rules;
    }

    public class PostCondition //since there can be multiple tasks derived per rule
    {
        Product Term_and_Meta; //what to derive

        public PostCondition(Product Term_and_Meta)
        {
            this.Term_and_Meta = Term_and_Meta;
        }

        public boolean Apply(boolean single_premise, Term[] preconditions, Task task, Sentence belief, ConceptProcess nal)
        {
            Truth truth = null;
            Truth desire = null;

            if(!single_premise && (task == null || belief == null))
                return false;

            if(single_premise && task==null)
                return false;

            //todo consume and use also other meta information
            for(Term t : ((Product)this.Term_and_Meta.term(1)).terms())
            {
                String s = t.toString().replace("_",".").replace("%","");

                switch (s) { //truth value calculation happens here
                    case "Truth.Deduction":
                        truth = TruthFunctions.deduction(task.truth, belief.truth);
                        break;
                    case "Truth.Induction":
                        truth = TruthFunctions.induction(task.truth, belief.truth);
                        break;
                    case "Truth.Abduction":
                        truth = TruthFunctions.abduction(task.truth, belief.truth);
                        break;
                    case "Truth.Comparison":
                        truth = TruthFunctions.comparison(task.truth, belief.truth);
                        break;
                    case "Truth.Conversion":
                        truth = TruthFunctions.conversion(task.truth);
                        break;
                    case "Truth.Negation":
                        truth = TruthFunctions.negation(task.truth);
                        break;
                    case "Truth.Contraposition":
                        truth = TruthFunctions.contraposition(task.truth);
                        break;
                    case "Truth.Resemblance":
                        truth = TruthFunctions.resemblance(task.truth, belief.truth);
                        break;
                    case "Truth.Union":
                        truth = TruthFunctions.union(task.truth, belief.truth);
                        break;
                    case "Truth.Intersection":
                        truth = TruthFunctions.intersection(task.truth, belief.truth);
                        break;
                    case "Truth.Difference":
                        truth = TruthFunctions.difference(task.truth, belief.truth);
                        break;
                    case "Truth.Analogy":
                        truth = TruthFunctions.analogy(task.truth, belief.truth);
                        break;
                    case "Truth.Exemplification":
                        truth = TruthFunctions.exemplification(task.truth, belief.truth);
                        break;
                    case "Truth.DecomposeNegativeNegativeNegative":
                        truth = TruthFunctions.decomposeNegativeNegativeNegative(task.truth, belief.truth);
                        break;
                    case "Truth.DecomposePositiveNegativePositive":
                        truth = TruthFunctions.decomposePositiveNegativePositive(task.truth, belief.truth);
                        break;
                    case "Truth.DecomposeNegativePositivePositive":
                        truth = TruthFunctions.decomposeNegativePositivePositive(task.truth, belief.truth);
                        break;
                    case "Truth.DecomposePositiveNegativeNegative":
                        truth = TruthFunctions.decomposePositiveNegativeNegative(task.truth, belief.truth);
                        break;
                    //TODO all others also
                    case "Desire.Strong": //implicit assumption: happens after, so it overwrites
                        desire = TruthFunctions.desireStrong(task.truth, belief.truth);
                        break;
                    case "Desire.Weak": //implicit assumption: happens after, so it overwrites
                        desire = TruthFunctions.desireWeak(task.truth, belief.truth);
                        break;
                    case "Desire.Ind": //implicit assumption: happens after, so it overwrites
                        desire = TruthFunctions.desireInd(task.truth, belief.truth);
                        break;
                    case "Desire.Ded": //implicit assumption: happens after, so it overwrites
                        desire = TruthFunctions.desireDed(task.truth, belief.truth);
                        break;
                    default: //only these 3 for now, can be extended later, lets go on with the rest for now
                        break;
                }
            }

            if(truth==null && task.isJudgment()) {
                System.out.println("truth value of rule was not specified, deriving nothing: \n" + Term_and_Meta.toString());
                return false; //not specified!!
            }

            if(desire==null && task.isGoal()) {
                System.out.println("desire value of rule was not specified, deriving nothing: \n" + Term_and_Meta.toString());
                return false; //not specified!!
            }

            //now match the rule with the task term <- should probably happen earlier ^^
            final Map<Term, Term> assign = Global.newHashMap();
            final Map<Term, Term> waste = Global.newHashMap();
            Term derive = Term_and_Meta.term(0); //first entry is term
            //precon[0]
            //TODO checking the precondition again for every postcondition misses the point, but is easily fixable (needs to be moved down to Rule)
            if(single_premise) //only match precondition pattern with task
            {
                //match first rule pattern with task
                if(!Variables.findSubstitute(Symbols.VAR_PATTERN, preconditions[0], task.getTerm(), assign, waste, nal.memory.random))
                    return false;
                //now we have to apply this to the derive term
                derive = Term.substituted_version(derive, assign);
            }
            else
            {
                //match first rule pattern with task
                if(!Variables.findSubstitute(Symbols.VAR_PATTERN, preconditions[0], task.getTerm(), assign, waste, nal.memory.random))
                    return false;
                //match second rule pattern with belief
                if(!Variables.findSubstitute(Symbols.VAR_PATTERN, preconditions[1], belief.getTerm(), assign, waste, nal.memory.random))
                    return false;

                //also check if the preconditions are met
                for(int i=2; i<preconditions.length; i++)
                {
                    Inheritance predicate = (Inheritance) preconditions[i];
                    Term predicate_name = predicate.getPredicate();
                    Term[] args = ((Product)(((SetExt)predicate.getSubject()).term(0))).terms();
                    //ok apply substitution to both elements in args

                    Term arg1 = args[0], arg2 = args[1];
                    arg1 = Term.substituted_version(arg1, assign);
                    arg2 = Term.substituted_version(arg2, assign);

                    if(predicate_name.toString().equals("not_equal"))
                    {
                        if(arg1.equals(arg2))
                            return false; //not_equal
                    }

                    if(predicate_name.toString().equals("no_common_subterm"))
                    {
                        //TODO: don't we already have a function for this?
                    }
                }

                //now we have to apply this to the derive term
                derive = Term.substituted_version(derive, assign);
            }

            //TODO also allow substituted evaluation on output side (used by 2 rules I think)

            Budget budget = BudgetFunctions.compoundForward(truth, derive, nal);

            boolean allowOverlap = false; //to be refined
            if(derive instanceof Compound)
            {
                if(!single_premise)
                {
                    TaskSeed seed = nal.newDoublePremise(task, belief, allowOverlap);
                    if(seed != null)
                        nal.deriveDouble(seed.term((Compound) derive).punctuation(task.punctuation).truth(truth).budget(budget)); //TODO newSinglePremise doesnt exist yet but is needed in case of single premise!!
                }
                else //assuming derive means deriveSingle
                {
                    nal.deriveSingle((Compound) derive, truth, budget);
                }
            }
                return true;
        }
    }

    public class Rule
    {
        private boolean single_premise = false; //whether its a single premise inference rule
        //A rule is named by precondition terms
        private Term[] preconditions; //the terms to match
        private PostCondition[] postconditions;
        //it has certain pre-conditions, all given as predicates after the two input premises

        public Rule(Product rule) //here we can already distinguish between preconditions, predicates, postconditions, post-evaluations and metainfo
        {
            //1. construct precondition term array
            Term[] terms = rule.terms();

            Product preprod = ((Product)terms[0]);
            Product postprods = ((Product)terms[1]);
            Term[] precon = preprod.terms();
            Term[] postcons = postprods.terms();
            //single premise
            if(precon.length == 1)
            {
                single_premise = true;
                preconditions = precon;
                postconditions = new PostCondition[1]; //only one conclusion for single premise rules, NAL doesn't need more:
                postconditions[0] = new PostCondition((Product) terms[1]);
            }
            else
            {
                //The last entry is the postcondition
                preconditions = precon;
                postconditions = new PostCondition[postcons.length/2]; //term_1 meta_1 ,..., term_2 meta_2 ...

                int k=0;
                for(int i=0; i<postcons.length; i+=2)
                {
                    Product derived_and_meta = Product.make(postcons[i], postcons[i+1]);
                    postconditions[k] = new PostCondition(derived_and_meta);
                    k++;
                }
            }
        }

        public void CheckAndApply(Task task, Sentence belief, ConceptProcess nal)
        {
            //if preconditions are met:
            for (PostCondition p : postconditions)
                p.Apply(single_premise, preconditions, task, belief, nal);
        }
    }

    public List<Rule> constructRule(List<Term> uninterpreted_rules)
    {
        List<Rule> rules = new ArrayList<>();

        for(Term t : uninterpreted_rules)
            rules.add(new Rule((Product)t));

        return rules;
    }

    List<Rule> rules = null;
    public NALExecuter()
    {
        rules = constructRule(parseRules(loadRuleStrings())); //rules are constructed once
    }

    public boolean reason(Task task, Sentence belief, ConceptProcess nal)
    {
        for(Rule r : rules)
        {
            if(task.isJudgment() || task.isGoal()) //forward inference
            {
                r.CheckAndApply(task, belief, nal); //TODO also allow backward inference by traversing
            }
        }
        return true;
    }
}
