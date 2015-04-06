> ### InputOutputFormat  
> The input/output format of the system 

***

### I/O Format

Each line in the system's input and output is either a task (as defined in the following grammar, in BNF notation), or an integer, indicating the number of inference steps between tasks.

In a task, all the space characters are optional, and will be ignored by the system in processing.

### Narsese Grammar

```
                        GRAMMAR RULE                          BRIEF EXPLANATION
               <task> ::= [<budget>] <sentence>              // task to be processed
           <sentence> ::= <statement>"." [<tense>] [<truth>] // judgment to be remembered
                        | <statement>"?" [<tense>]           // question to be answered
                        | <statement>"@" [<tense>]           // question on desire value to be answered
                        | <statement>"!" [<truth>]           // goal to be realized
          <statement> ::= "<"<term> <copula> <term>">"       // two terms related to each other
                        | <term>                             // a term can name a statement
                        | "(^"<word> {","<term>} ")"         // an operation to be executed                         
             <copula> ::= "-->"                              // inheritance
                        | "<->"                              // similarity
                        | "{--"                              // instance
                        | "--]"                              // property
                        | "{-]"                              // instance-property
                        | "==>"                              // implication
                        | "=/>"                              // predictive implication
                        | "=|>"                              // concurrent implication
                        | "=\>"                              // retrospective implication
                        | "<=>"                              // equivalence
                        | "</>"                              // predictive equivalence
                        | "<|>"                              // concurrent equivalence
               <term> ::= <word>                             // an atomic constant term
                        | <variable>                         // an atomic variable term
                        | <compound-term>                    // a term with internal structure
                        | <statement>                        // a statement can serve as a term
      <compound-term> ::= "{" <term> {","<term>} "}"         // extensional set
                        | "[" <term> {","<term>} "]"         // intensional set
                        | "(&," <term> {","<term>} ")"       // extensional intersection
                        | "(|," <term> {","<term>} ")"       // intensional intersection
                        | "(-," <term> "," <term> ")"        // extensional difference
                        | "(~," <term> "," <term> ")"        // intensional difference
                        | "(*," <term> {","<term>} ")"       // product
                        | "(/," <term> {","<term>} ")"       // extensional image
                        | "(\," <term> {","<term>} ")"       // intensional image
                        | "(--," <term> ")"                  // negation
                        | "(||," <term> {","<term>} ")"      // disjunction
                        | "(&&," <term> {","<term>} ")"      // conjunction
                        | "(&/," <term> {","<term>} ")"      // sequential events
                        | "(&|," <term> {","<term>} ")"      // parallel events
           <variable> ::= "$"<word>                          // independent variable
                        | "#"[<word>]                        // dependent variable
                        | "?"[<word>]                        // query variable in question
              <tense> ::= ":/:"                              // future event
                        | ":|:"                              // present event
                        | ":\:"                              // past event
              <truth> ::= "%"<frequency>[";"<confidence>]"%" // two numbers in [0,1]x(0,1)
             <budget> ::= "$"<priority>[";"<durability>]"$"  // two numbers in [0,1]x(0,1)
               <word> : Unicode string in an arbitrary alphabet
```

### Example Usage

* Tim is dead.

  `<{Tim} --> [dead]>.`

* Tim is a human.

  `<{Tim} --> human>.`

* Humans are a lifeform.

  `<human --> lifeform>.`

* Lifeforms are like machines.

  `<lifeform <-> machine>.`

* Tom eats chocolate.
  
  ```
  <(*,{Tom},chocolate) --> eat>.

  <{Tom} --> (/,eat,_,chocolate)>.

  <chocolate --> (/,eat,{Tom},_)>.
  ```