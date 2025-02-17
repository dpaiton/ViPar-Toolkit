package dannydelott.vinefilter.settings.filter.expression.evaluators;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import dannydelott.vinefilter.Messages;
import dannydelott.vinefilter.settings.SettingsFile;
import dannydelott.vinefilter.settings.config.dataset.GrammarDependency;
import dannydelott.vinefilter.settings.config.dataset.Vine;
import dannydelott.vinefilter.settings.filter.FieldType;
import dannydelott.vinefilter.settings.filter.Filter;
import dannydelott.vinefilter.settings.filter.expression.Argument;
import dannydelott.vinefilter.settings.filter.expression.Expression;
import dannydelott.vinefilter.settings.filter.expression.Token;
import dannydelott.vinefilter.settings.filter.expression.values.LogicType;

public class ExpressionEvaluator {

	private SettingsFile settings;
	private Filter filter;
	private FieldType fieldType;
	private Vine vine;

	private GrammarDependency relation;

	private ArgumentEvaluator argumentEvaluator;

	private boolean flagError;

	// //////////////
	// CONSTRUCTOR //
	// //////////////

	public ExpressionEvaluator(SettingsFile s, Filter f) {

		settings = s;
		filter = f;
		argumentEvaluator = new ArgumentEvaluator(settings, filter);

		flagError = false;
	}

	// /////////////////
	// PUBLIC METHODS //
	// /////////////////

	public boolean evaluateExpression() {

		boolean result;

		// ------------------------------------------
		// 1. Gets the expression from the field type
		// ------------------------------------------

		Expression expression = getExpression(fieldType);
		if (expression == null && flagError) {
			return false;
		}

		// ------------------------------------------
		// 2. Evaluates arguments from the Expression
		// ------------------------------------------

		// gets the arguments from expression
		LinkedHashMap<Argument, LogicType> arguments = expression
				.getArguments();

		// passes the vine and field type to argument evaluator
		argumentEvaluator.setFieldType(fieldType);
		argumentEvaluator.setVine(vine);

		// passes grammar relation to argument evaluator
		if (fieldType == FieldType.GRAMMAR_DEPENDENCY_GOVERNOR
				|| fieldType == FieldType.GRAMMAR_DEPENDENCY_DEPENDENT) {
			argumentEvaluator.setGrammarDependency(relation);
		}

		// evaluates the arguments and gets their results
		LinkedHashMap<Boolean, LogicType> results = evaluate(arguments);
		if (results == null || flagError) {
			return false;
		}

		// --------------------
		// 3. Interpret results
		// --------------------

		result = FilterEvaluator.interpretResult(results);
		if (flagError) {
			return false;
		}

		return result;
	}

	// //////////////////
	// PRIVATE METHODS //
	// //////////////////

	private LinkedHashMap<Boolean, LogicType> evaluate(
			LinkedHashMap<Argument, LogicType> a) {

		// ----------------
		// Method variables
		// ----------------

		// current argument
		Entry<Argument, LogicType> entry;
		Argument argument;

		// holds result and logic
		boolean result;
		LogicType logic;

		// holds the return object
		LinkedHashMap<Boolean, LogicType> evaluation = new LinkedHashMap<Boolean, LogicType>();

		// -------------------
		// Evaluates arguments
		// -------------------

		// makes iterator
		Iterator<Entry<Argument, LogicType>> it = a.entrySet().iterator();

		// loops over arguments
		while (it.hasNext()) {

			entry = it.next();
			argument = entry.getKey();
			logic = entry.getValue();

			// evaluates the argument
			result = argumentEvaluator.evaluateArgument(argument);

			if (result == false && argumentEvaluator.getFlagError()) {
				flagError = true;
				return null;
			}

			// adds result and logic to return Map
			evaluation.put(result, logic);
		}

		return evaluation;
	}

	private Expression getExpression(FieldType ft) {

		Expression expression = null;

		switch (ft) {
		case TEXT_CONTAINS:
			expression = filter.getTextContains();
			break;
		case SCRUBBED_TEXT_CONTAINS:
			expression = filter.getScrubbedTextContains();
			break;
		case GRAMMAR_DEPENDENCY_DEPENDENT:
			expression = filter.getGrammarDependencyDependent();
			break;
		case GRAMMAR_DEPENDENCY_GOVERNOR:
			expression = filter.getGrammarDependencyGovernor();
			break;
		default:
			break;
		}

		// throws flag if cannot get expression
		if (expression == null) {
			flagError = true;
			System.out.println(Messages.ExpressionEvaluator_errorGetExpression);
		}

		return expression;
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
