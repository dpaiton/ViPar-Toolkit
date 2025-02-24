package dannydelott.vinefilter.settings.filter.expression.evaluators;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import dannydelott.vinefilter.settings.SettingsFile;
import dannydelott.vinefilter.settings.config.dataset.GrammarDependency;
import dannydelott.vinefilter.settings.config.dataset.Vine;
import dannydelott.vinefilter.settings.filter.FieldType;
import dannydelott.vinefilter.settings.filter.Filter;
import dannydelott.vinefilter.settings.filter.expression.Argument;
import dannydelott.vinefilter.settings.filter.expression.Token;
import dannydelott.vinefilter.settings.filter.expression.values.LogicType;

public class ArgumentEvaluator {

	private SettingsFile settings;
	private Filter filter;
	private FieldType fieldType;
	private Vine vine;
	private GrammarDependency relation;

	private TokenEvaluator tokenEvaluator;

	private boolean flagError;

	// //////////////
	// CONSTRUCTOR //
	// //////////////

	public ArgumentEvaluator(SettingsFile s, Filter f) {
		settings = s;
		filter = f;
		tokenEvaluator = new TokenEvaluator(settings, filter);
	}

	// /////////////////
	// PUBLIC METHODS //
	// /////////////////

	public boolean evaluateArgument(Argument a) {

		// ---------------------------
		// 1. Gets the argument tokens
		// ---------------------------

		LinkedHashMap<Token, LogicType> tokens = a.getTokens(); 
		

		// ---------------------
		// 2. Evaluates argument
		// ---------------------

		// passes vine and field type to token evaluator
		tokenEvaluator.setVine(vine);
		tokenEvaluator.setFieldType(fieldType);
		if (fieldType == FieldType.GRAMMAR_DEPENDENCY_GOVERNOR
				|| fieldType == FieldType.GRAMMAR_DEPENDENCY_DEPENDENT) {
			tokenEvaluator.setGrammarDependency(relation);
		}

		// holds the result + logic
		LinkedHashMap<Boolean, LogicType> evaluation;

		// gets the evaluation
		evaluation = evaluate(a, tokens);
		if (evaluation == null || flagError) {
			return false;
		}

		// --------------------
		// 3. Interprets result
		// --------------------

		boolean result = FilterEvaluator.interpretResult(evaluation);
		if (flagError) {
			return false;
		}

		return result;
	}

	// //////////////////
	// PRIVATE METHODS //
	// //////////////////

	private LinkedHashMap<Boolean, LogicType> evaluate(Argument a,
			LinkedHashMap<Token, LogicType> t) {

		// current token
		Entry<Token, LogicType> currentEntry;
		Token currentToken;
		LogicType currentLogic;

		// next token
		Entry<Token, LogicType> nextEntry;
		Token nextToken;
		LogicType nextLogic;

		// list of tokens to give to the token evaluator
		// (in order to handle side-by-side tokens, if any)
		List<Token> tokensList = new ArrayList<Token>();

		// result of the token evaluation
		boolean result;

		// holds the result + logic to return
		LinkedHashMap<Boolean, LogicType> evaluation = new LinkedHashMap<Boolean, LogicType>();

		// --------------------
		// 2. Loops over tokens
		// --------------------

		Iterator<Entry<Token, LogicType>> it = t.entrySet().iterator();
		if (!it.hasNext()) {
			flagError = true;
			return null;
		}

		while (it.hasNext()) {

			tokensList.clear();

			// gets current entry, token and logic
			currentEntry = it.next();
			currentToken = currentEntry.getKey();
			currentLogic = currentEntry.getValue();
			tokensList.add(currentToken);

			// loops to check for adjacent tokens
			// eg: [@Literal:#][@TargetWord:NO_SPACES]
			if (currentLogic == null) {
				while (currentLogic == null && it.hasNext()) {

					// gets next token and logic
					nextEntry = it.next();
					nextToken = nextEntry.getKey();
					nextLogic = nextEntry.getValue();

					// adds it to the list
					tokensList.add(nextToken);

					// resets current token and logic
					currentToken = nextToken;
					currentLogic = nextLogic;

				}
			}

			// -------------------
			// 3. Evaluates tokens
			// -------------------

			// sets tokens list in token evaluator
			tokenEvaluator.setTokens(tokensList);

			// evaluates the token list
			result = tokenEvaluator.evaluateTokens();

			// adds result to final evaluation
			evaluation.put(result, currentLogic);

		}

		return evaluation;

	}

	// /////////////////
	// GLOBAL SETTERS //
	// /////////////////

	public void setVine(Vine v) {
		vine = v;
	}

	public void setFieldType(FieldType ft) {
		fieldType = ft;
	}

	public void setGrammarDependency(GrammarDependency r) {
		relation = r;
	}

	// /////////////////
	// GLOBAL GETTERS //
	// /////////////////

	public boolean getFlagError() {
		return flagError;
	}

}
