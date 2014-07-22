# an introduction to LOAN for the Narsese user

# Introduction

NARS provides an amazing system for the balanced handling of uncertain and
partial information. This is a great fit for the semantic web, which being
an open system with much of its data being human generated is full of
uncertainty. LOAN has been developed to provide an easy to use serialisation
for NAL which is suitable for talking about resources on the Web. It is also
intended to be eminently 'scribbleable'; one can include snippets of LOAN in
conversations by email, on IRC, on conference sides, etc. with the knowledge
that they'll be easy for others to understand.

LOAN owes a significant debt to Notation3, an RDF serialisation with similar
scribbleable properties. Many aspects of LOAN's syntax have been lifted from
Notation3 and it is hoped that this will make NARS a more attractive prospect
for existing semantic web developers. Of course, lots of the syntax comes from
Narsese too. It is the balancing of these two influences which has been the
major challenge in designing LOAN. I personally feel that it has worked rather
well, but I'll leave that up to you to decide for yourself.

LOAN adds to NARS a number of new constant term sorts; URI references and
numeric, boolean and string literals. URI references are supported in two basic
forms; as full URIs, either absolute or relative, and as abbreviated URIs,
often called CURIEs, with a pre-defined prefix and a local part separated by a
colon. A full URI reference is indicated in the syntax by being surrounded by
angle brackets; for example {{{<http://open-nars.googlecode.com/>}}} or
{{{</wiki>}}}. A CURIE is often more convenient especially when referring to
terms with URIs at other locations to the document; for example to refer to
the title element from Dublin Core, assuming that the Dublin Core prefix has
been declared as {{{dc:}}}, one may simply use {{{dc:title}}}. Literals have
their normal syntax as found in most languages; numbers are sequences of digits
with a decimal point included for doubles, {{{true}}} and {{{false}}} are
booleans and strings are surrounded by double quote marks.

Other than the introduction of new atomic terms, LOAN also changes the syntax
of Narsese to be easier to parse for humans (who haven't transcended to the 
plane of LISP gods.) Many of the compound term operators in Narsese are here
represented as infix operators. Products are represented as a familiar tuple
syntax, as found in many existing languages. In order to support pragmatic
information for the parser and reasoner, LOAN also includes a range of what
are called '@-rules'. The most important two @-rules are {{{@base}}} and
{{{@prefix}}} which declare the base URI (for relative reference) and prefixes
for CURIEs, respectfully. Other @-rules include {{{@import}}}, which instructs
the system to download and parse another document, {{{@delay}}}, which pauses
insertion of sentences for the given number of cycles, {{{@operator}}} which
declares a new operator, and {{{@budget}}} which is used to attach a budget
value to a task. The last three are not actually implemented in OpenNARS yet,
but do not break the parser if they're found in a file.

The rest of this document runs through the specification of an ontology for a
domain. Each example is given in both LOAN and Narsese with the idea that one
can compare the two. For a more formal, and complete, description of the syntax
there is a PDF of the BNF for the grammar in the SVN of the open-nars module.
Where Narsese doesn't have an equivalent concept, we either leave its entry
blank, or (in the case of URIs) give a rough approximation using atoms. This
introduction only runs through first-order NAL, for reasons of simplicity. The
syntax for higher-order NAL follows the same principles (infix binary operators
and prefix unary ones with minimal parentheses.) Of note is that negation,
future, present and past tense operators are indicated with the keywords
{{{not}}}, {{{future}}}, {{{present}}} and {{{past}}} respectively. This
ontology doesn't include any questions or goals, but that doesn't mean that
such things aren't possible in LOAN! The syntax for a goal is the same as that
for a judgement except with the full-stop replaced by an exclamation mark. A
question is the same, but lacking a truth-value and including a question mark
to replace the full-stop. Variables are indicated with the same syntaxes as in
Narsese. 

# The Wine Domain

Imagine yourself sitting in a field in France, having taken early retirement to
live out your dream of being a wine maker. Your vines are planted, and the
grapes are ripening. To help yourself become an expert, and because you were a
knowledge engineer before you retired, you want to describe your wine, and the
processes you've used, formally. Now, the inherent uncertainties of wine making
haven't escaped you, and so you decide to use NARS to represent the knowledge
you obtain and to draw inferences from it.

You first want to declare a base URI for your document and a prefix into which
all your wine-making terms will go:

<table>
  <tr><td>**Narsese**</td><td>**LOAN**</td></tr>
  <tr><td></td><td>{{{@base <http://www.example.com/wine/cellar/>.}}}</td></tr>
  <tr><td></td><td>{{{@prefix : <http://www.example.com/wine/ontology#>.}}}</td></tr>
</table>

Similarly, you define prefixes for Dublin Core (as {{{dc:}}}), FOAF ({{{foaf:}}})
and WordNet ({{{wn:}}}).

We now define some of our basic terms; particularly red wines and white wines:

<table>
  <tr><td>**Narsese**</td><td>**LOAN**</td></tr>
  <tr><td>{{{<redwine --> wine>.}}}</td><td>{{{:RedWine --> wn:Wine.}}}</td></tr>
  <tr><td>{{{<whitewine --> wine>.}}}</td><td>{{{:WhiteWine --> wn:Wine.}}}</td></tr>
</table>

We can now describe what it means to be a red or a white wine in terms of
having the property of being red or white, respectively:

<table>
  <tr><td>**Narsese**</td><td>**LOAN**</td></tr>
  <tr><td>{{{<redwine --> [--> [wn:Red](red]>.}}}</td><td>{{{:RedWine).}}}</td></tr>
  <tr><td>{{{<whitewine --[ white>.}}}</td><td>{{{:WhiteWine --[ wn:Wine.}}}</td></tr>
</table>

You have a large number of friends, and being known as a wine lover they gifted
you with many bottles of wine before you moved to France. We can describe these
instances as follows:

<table>
  <tr><td>**Narsese**</td><td>**LOAN**</td></tr>
  <tr><td>{{{<{bottle-from-james} --> redwine>.}}}</td><td>{{{{</bottle-from-james>} --> :RedWine.}}}</td></tr>
  <tr><td>{{{<bottle-from-sally }-> whitewine>.}}}</td><td>{{{</bottle-from-sally> }-> :WhiteWine.}}}</td></tr>
</table>

The bottle we got given by Peter is medium dry:

<table>
  <tr><td>**Narsese**</td><td>**LOAN**</td></tr>
  <tr><td>{{{<bottle-from-peter }-[ dry>. %0.5%}}}</td><td>{{{</bottle-from-peter> }-[ wn:Dry %0.5%.}}}</td></tr>
</table>

Now, a wine which is red isn't a wine which is white. We indicate this using a
similarity statement with a frequency of 0, knowing that NARS will handle the
inconsistency:

<table>
  <tr><td>**Narsese**</td><td>**LOAN**</td></tr>
  <tr><td>{{{<redwine <-> whitewine>. %0%}}}</td><td>{{{:RedWine <-> :WhiteWine %0.0%.}}}</td></tr>
</table>

But we want to say that we're fairly sure that ros� is a bit like both red and white wine so
we use a full form truth value with a frequency of 0.4 and a confidence of 0.7:

<table>
  <tr><td>**Narsese**</td><td>**LOAN**</td></tr>
  <tr><td>{{{ <rosewine <-> redwine>. %0.4;0.7% }}}</td><td>{{{:RoseWine <-> :RedWine %0.4;0.7%.}}}</td></tr>
  <tr><td>{{{ <rosewine <-> whitewine>. %0.4;0.7% }}}</td><td>{{{:RoseWine <-> :WhiteWine %0.4;0.7%.}}}</td></tr>
</table>

In spite of this, and in spite of NARS being able to infer it, we want to
explicitly describe what ros� wine is, it is something which is both a wine
and which has the property of being pink:

<table>
  <tr><td>**Narsese**</td><td>**LOAN**</td></tr>
  <tr><td>{{{ <rosewine --> (&,wine,[}}}</td><td>{{{:RoseWine --> wn:Wine & [wn:Pink](pink])>.).}}}</td></tr>
</table>

We now get on to describing the vineyard you have taken over. One of our grapes
is a bit like a cross between Shiraz and Cabernet and any grape of that sort has properties
from the intersection of those varieties:

<table>
  <tr><td>**Narsese**</td><td>**LOAN**</td></tr>
  <tr><td>{{{ <(|,shiraz,cabernet) --> grape1>. }}}</td><td>{{{ :Shiraz | :Cabernet --> :Grape1. }}}</td></tr>
</table>

We also should describe what it means to be 'bad soil'; a bad soil is any instance of soil
which isn't an instance of the property of being good:

<table>
  <tr><td>**Narsese**</td><td>**LOAN**</td></tr>
  <tr><td>{{{ <badsoil --> (-,soil,[}}}</td><td>{{{ :BadSoil --> wn:Soil - [wn:Good](good])>.). }}}</td></tr>
</table>

We also take a moment to detail what you believe about grapes; that grapes which aren't
red, cannot be good:

<table>
  <tr><td>**Narsese**</td><td>**LOAN**</td></tr>
  <tr><td>{{{ <(~,grape,redgrape) --> [%0% }}}</td><td>{{{ wn:Grape ~ :RedGrape --> [wn:Good](good]>.) %0.0%. }}}</td></tr>
</table>

Now, knowledge is also relational and a particular important relation for you
is that between a grape variety and the soil it grows best in. We declare
such a relation as follows:

<table>
  <tr><td>**Narsese**</td><td>**LOAN**</td></tr>
  <tr><td>{{{ <growingsoil --> (*,grape,soil)>. }}}</td><td>{{{ :growingSoil --> (wn:Grape, wn:Soil). }}}</td></tr>
</table>

Our first variety of grape grows well in coarse soil, so we describe that relationship:

<table>
  <tr><td>**Narsese**</td><td>**LOAN**</td></tr>
  <tr><td>{{{ <(*,grape1,(&,[--> growingsoil>. }}}</td><td>{{{ (:Grape1, [wn:Course](coarse],soil))) & wn:Soil) --> :growingSoil. }}}</td></tr>
</table>

With LOAN however, we have a second choice for indicating relationships directly;
the operator shorthand:

<table>
  <tr><td>**Narsese**</td><td>**LOAN**</td></tr>
  <tr><td>{{{ <(*,grape2,(&,[--> growingsoil>. }}}</td><td>{{{ :growingSoil(:Grape2, [wn:Moist, wn:Alkaline](moist,alkaline],soil))) & wn:Soil). }}}</td></tr>
</table>

We can say that riesling grapes inherit from those things which like slatey
soil by using the extensional image syntax. In LOAN such images resemble
the operator shorthand except with the list of arguments separated into two
parts using a {{{/}}} or {{{\}}} (for extensional and intensional image,
respectively). The wildcard is at the position of the slash.

<table>
  <tr><td>**Narsese**</td><td>**LOAN**</td></tr>
  <tr><td>{{{ <riesling --> (/, growingsoil, _, slatey)>. }}}</td><td>{{{ :Riesling --> :growingSoil(/ [}}}</td></tr>
</table>

We can also join together extensional and intensional images in a single term:

<table>
  <tr><td>*Narsese*</td><td>*LOAN*</td></tr>
  <tr><td>{{{ <(\,region,_,porto) --> (/,growingsoil,_,[bitumeny](:Slatey]).))>. }}}</td><td>{{{ :region(\ :Porto) --> :growingSoil(/ [:Bitumeny]). }}}</td></tr>
</table>

Finally we can describe some metadata for our ontology. This information
uses full URIs to refer to resources, both this document and other
resources like homepages and email addresses:

<table>
  <tr><td>**Narsese**</td><td>**LOAN**</td></tr>
  <tr><td>{{{ }}}</td><td>{{{ dc:creator(<http://www.example.com/wine/ontology#>, :JohnSmith). }}}</td></tr>
  <tr><td>{{{ <johnsmith --> person>. }}}</td><td>{{{ :JohnSmith --> foaf:Person. }}}</td></tr>
  <tr><td>{{{ }}}</td><td>{{{ foaf:mbox(:JohnSmith, <mailto:jsmith@example.com>). }}}</td></tr>
  <tr><td>{{{ }}}</td><td>{{{ foaf:homepage(:JohnSmith, <http://www.example.com/blog/>). }}}</td></tr>
</table>
|| {{{ }}} || {{{ "Exemplar Wines" }-> foaf:title(<http://www.example.com/blog/> /). }}} || 

# The LOAN Example in Full

      {--- An ontology of wine in LOAN
       --- Apologies to any wine lovers out there; this is a very simple
       --- example for pedagogical reasons.
       ---}
      @base <http://www.example.com/wine/cellar/>.
      @prefix : <http://www.example.com/wine/ontology#>.
      @prefix dc: <http://purl.org/dc/elements/1.1/>.
      @prefix foaf: <http://xmlns.com/foaf/1.1/>.
      @prefix wn: <http://xmlns.com/wordnet/1.6/>.
      
      --- We define two sorts of wine.
      :RedWine --> wn:Wine.
      :WhiteWine --> wn:Wine.
      
      --- RedWine is, unsurprisingly, red and WhiteWine is white. 
      :RedWine --> [wn:Red].
      :WhiteWine --[ wn:White.
      
      --- We have received a bottle of red, and a bottle of white from friends
      {</bottle-from-james>} --> :RedWine.
      </bottle-from-sally> }-> :WhiteWine.
      
      --- The bottle we got from Peter is medium dry
      </bottle-from-peter> }-[ wn:Dry %0.5%.
      
      --- RedWine and WhiteWine are totally distinct
      :RedWine <-> :WhiteWine %0.0%.
      
      --- But we're quite sure that Ros� is a bit like Red and a bit like White
      :RoseWine <-> :RedWine %0.4;0.7%.
      :RoseWine <-> :WhiteWine %0.4;0.7%.
      
      --- RoseWine is wine and it is pink
      :RoseWine --> wn:Wine & [wn:Pink].
      
      --- One of the grapes we are growing is like a cross between Shiraz and Cabernet
      :Shiraz | :Cabernet --> :Grape1.
      
      --- Bad soil is any soil which isn't good
      :BadSoil --> wn:Soil - [wn:Good].
      
      --- We're not a fan of any grapes but red ones
      wn:Grape ~ :RedGrape --> [wn:Good] %0.0%.
      
      --- Any grape has an appropriate sort of soil
      :growingSoil --> (wn:Grape, wn:Soil).
      
      --- Our first grape variety grows well in coarse soil
      (:Grape1, [wn:Coarse] & wn:Soil) --> :growingSoil.
      
      --- Our second in moist, alkaline soil
      :growingSoil(:Grape2, [wn:Moist, wn:Alkaline] & wn:Soil).
      
      --- Riesling grapes like slatey soil
      :Riesling --> :growingSoil(/ [:Slatey]).
      
      --- All grapes in Porto grow well in bitumen rich soil
      :region(\ :Porto) --> :growingSoil(/ [:Bitumeny]).
      
      --- Some information about the ontology
      dc:creator(<http://www.example.com/wine/ontology#>, :JohnSmith).
      :JohnSmith --> foaf:Person.
      foaf:mbox(:JohnSmith, <mailto:jsmith@example.com>).
      foaf:homepage(:JohnSmith, <http://www.example.com/blog/>).
      "Exemplar Wines" }-> foaf:title(<http://www.example.com/blog/> /). 
       
 