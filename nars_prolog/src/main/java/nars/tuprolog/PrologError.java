package nars.tuprolog;

/**
 * @author Matteo Iuliani
 */
@SuppressWarnings("serial")
public class PrologError extends Throwable {

	// termine Prolog che rappresenta l'argomento di throw/1
	private Term error;
	/*Castagna 06/2011*/
	private String descriptionError;
	/**/

	public PrologError(Term error) {
		this.error = error;
	}

	/*Castagna 06/2011*/	
	/*	
	sintassi da prevedere:
	TYPE	in	argument	ARGUMENT	of			GOAL				(instantiation, type, domain, existence, representation, evaluation)
	TYPE	in				GOAL										(permission, resource)
	TYPE	at clause#CLAUSE, line#LINE, position#POS: DESCRIPTION		(syntax)
	TYPE																(system)
	*/
	/**/

	/*Castagna 06/2011*/
	public String toString()
	{
		return descriptionError;
	}
	/**/

	/*Castagna 06/2011*/	
	public PrologError(Term error, String descriptionError) {
		this.error = error;	
		this.descriptionError = descriptionError;
	}
	/**/

	public Term getError() {
		return error;
	}

	public static PrologError instantiation_error(EngineManager engineManager, int argNo) {
		Term errorTerm = new Struct("instantiation_error");
		Term tuPrologTerm = new Struct("instantiation_error", engineManager.getEnv().currentContext.currentGoal, new Int(argNo));
		/*Castagna 06/2011*/
		//return new PrologError(new Struct("error", errorTerm, tuPrologTerm));		
		String descriptionError =  "Instantiation error" +
		" in argument " + argNo + 
		" of " + engineManager.getEnv().currentContext.currentGoal.toString();
		return new PrologError(new Struct("error", errorTerm, tuPrologTerm), descriptionError);
		/**/	
	}

	public static PrologError type_error(EngineManager e, int argNo, String validType, Term culprit) {
		Term errorTerm = new Struct("type_error", new Struct(validType), culprit);
		Term tuPrologTerm = new Struct("type_error", e.getEnv().currentContext.currentGoal, new Int(argNo), new Struct(validType), culprit);
		/*Castagna 06/2011*/
		//return new PrologError(new Struct("error", errorTerm, tuPrologTerm));	
		String descriptionError =  "Type error" + 
		" in argument " + argNo + 
		" of " + e.getEnv().currentContext.currentGoal.toString();
		return new PrologError(new Struct("error", errorTerm, tuPrologTerm), descriptionError);
		/**/
	}

	public static PrologError domain_error(EngineManager e, int argNo, String validDomain, Term culprit) {
		Term errorTerm = new Struct("domain_error", new Struct(validDomain), culprit);
		Term tuPrologTerm = new Struct("domain_error", e.getEnv().currentContext.currentGoal, new Int(argNo), new Struct(validDomain), culprit);
		/*Castagna 06/2011*/		
		//return new PrologError(new Struct("error", errorTerm, tuPrologTerm));	
		String descriptionError =  "Domain error" + 
		" in argument " + argNo + 
		" of " + e.getEnv().currentContext.currentGoal.toString();
		return new PrologError(new Struct("error", errorTerm, tuPrologTerm), descriptionError);
		/**/		
	}

	public static PrologError existence_error(EngineManager e, int argNo, String objectType, Term culprit, Term message) {
		Term errorTerm = new Struct("existence_error", new Struct(objectType), culprit);
		Term tuPrologTerm = new Struct("existence_error", e.getEnv().currentContext.currentGoal, new Int(argNo), new Struct(objectType), culprit, message);
		/*Castagna 06/2011*/
		//return new PrologError(new Struct("error", errorTerm, tuPrologTerm));	
		String descriptionError =  "Existence error" + 
		" in argument " + argNo + 
		" of " + e.getEnv().currentContext.currentGoal.toString();
		return new PrologError(new Struct("error", errorTerm, tuPrologTerm), descriptionError);
		/**/		
	}

	public static PrologError permission_error(EngineManager e,	String operation, String objectType, Term culprit, Term message) {
		Term errorTerm = new Struct("permission_error", new Struct(operation), new Struct(objectType), culprit);
		Term tuPrologTerm = new Struct("permission_error", e.getEnv().currentContext.currentGoal, new Struct(operation), new Struct(objectType), culprit, message);
		/*Castagna 06/2011*/
		//return new PrologError(new Struct("error", errorTerm, tuPrologTerm));	
		String descriptionError =  "Permission error" + 
		" in  " + e.getEnv().currentContext.currentGoal.toString();	
		return new PrologError(new Struct("error", errorTerm, tuPrologTerm), descriptionError);
		/**/		
	}

	public static PrologError representation_error(EngineManager e, int argNo, String flag) {
		Term errorTerm = new Struct("representation_error", new Struct(flag));
		Term tuPrologTerm = new Struct("representation_error", e.getEnv().currentContext.currentGoal, new Int(argNo), new Struct(flag));
		/*Castagna 06/2011*/
		//return new PrologError(new Struct("error", errorTerm, tuPrologTerm));
		String descriptionError =  "Representation error" + 
		" in argument " + argNo + 
		" of " + e.getEnv().currentContext.currentGoal.toString();
		return new PrologError(new Struct("error", errorTerm, tuPrologTerm), descriptionError);
		/**/
	}

	public static PrologError evaluation_error(EngineManager e, int argNo, String error) {
		Term errorTerm = new Struct("evaluation_error", new Struct(error));
		Term tuPrologTerm = new Struct("evaluation_error", e.getEnv().currentContext.currentGoal, new Int(argNo), new Struct(error));
		/*Castagna 06/2011*/		
		//return new PrologError(new Struct("error", errorTerm, tuPrologTerm));	
		String descriptionError =  "Evaluation error" + 
		" in argument " + argNo + 
		" of " + e.getEnv().currentContext.currentGoal.toString();
		return new PrologError(new Struct("error", errorTerm, tuPrologTerm), descriptionError);
		/**/		
	}

	public static PrologError resource_error(EngineManager e, Term resource) {
		Term errorTerm = new Struct("resource_error", resource);
		Term tuPrologTerm = new Struct("resource_error", e.getEnv().currentContext.currentGoal, resource);
		/*Castagna 06/2011*/		
		//return new PrologError(new Struct("error", errorTerm, tuPrologTerm));		
		String descriptionError =  "Resource error" + 
		" in " + e.getEnv().currentContext.currentGoal.toString();
		return new PrologError(new Struct("error", errorTerm, tuPrologTerm), descriptionError);
		/**/		
	}

	public static PrologError syntax_error(EngineManager e, 
			/*Castagna 06/2011*/			
			int clause, 
			/**/			
			int line, int position, Term message) {
		Term errorTerm = new Struct("syntax_error", message);
		Term tuPrologTerm = new Struct("syntax_error", e.getEnv().currentContext.currentGoal, new Int(line), new Int(position), message);
		/*Castagna 06/2011*/
		//return new PrologError(new Struct("error", errorTerm, tuPrologTerm));

		int[] errorInformation = {clause, line, position};
		String[] nameInformation = {"clause", "line", "position"};
		String syntaxErrorDescription = message.getTerm().toString();

		{
			//Sostituzione degli eventuali caratteri di nuova linea con uno spazio
			syntaxErrorDescription = syntaxErrorDescription.replace("\n", " ");
			//Eliminazione apice di apertura e chiusura stringa
			syntaxErrorDescription = syntaxErrorDescription.substring(1, syntaxErrorDescription.length()-1);
			String start = 	(""+syntaxErrorDescription.charAt(0)).toLowerCase();
			//Resa minuscola l'iniziale
			syntaxErrorDescription = start + syntaxErrorDescription.substring(1);
		}

		String descriptionError = "Syntax error";

		boolean firstSignificativeInformation = true;
		for(int i = 0; i < errorInformation.length; i++)
		{
			if(errorInformation[i] != -1)
				if(firstSignificativeInformation)
				{
					descriptionError += " at " + nameInformation[i] + '#' + errorInformation[i];
					firstSignificativeInformation = false;
				}
				else
					descriptionError += ", " + nameInformation[i] + '#' + errorInformation[i];
		}
		descriptionError += ": " + syntaxErrorDescription;

		return new PrologError(new Struct("error", errorTerm, tuPrologTerm), descriptionError);
		/**/
	}

	public static PrologError system_error(Term message) {
		Term errorTerm = new Struct("system_error");
		Term tuPrologTerm = new Struct("system_error", message);
		/*Castagna 06/2011*/		
		//return new PrologError(new Struct("error", errorTerm, tuPrologTerm));
		String descriptionError = "System error";
		return new PrologError(new Struct("error", errorTerm, tuPrologTerm), descriptionError);
		/**/		
	}

}
