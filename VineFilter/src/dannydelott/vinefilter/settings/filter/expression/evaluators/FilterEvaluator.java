package dannydelott.vinefilter.settings.filter.expression.evaluators;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import dannydelott.vinefilter.settings.SettingsFile;
import dannydelott.vinefilter.settings.config.dataset.Vine;
import dannydelott.vinefilter.settings.filter.FieldType;
import dannydelott.vinefilter.settings.filter.Filter;
import dannydelott.vinefilter.settings.config.dataset.GrammarDependency;
import dannydelott.vinefilter.settings.filter.expression.values.LogicType;

public class FilterEvaluator {

	private SettingsFile settings;
	private Filter filter;
	private boolean flagError;
	private Vine vine;
	private ExpressionEvaluator expressionEvaluator;

	// //////////////
	// CONSTRUCTOR //
	// //////////////

	public FilterEvaluator(SettingsFile s, Filter r) {
		settings = s;
		filter = r;
		expressionEvaluator = new ExpressionEvaluator(settings, filter);

	}

	// /////////////////
	// PUBLIC METHODS //
	// /////////////////

	public boolean evaluateVine() {

		// holds the result
		boolean resultTextContains = true;
		boolean resultScrubbedTextContains = true;
		boolean resultGrammarDependency = true;

		// ------------------------------------
		// 2. Sets vine in expression evaluator
		// ------------------------------------

		expressionEvaluator.setVine(vine);

		// -------------------------
		// 3. Evaluates textContains
		// -------------------------

		if (filter.hasTextContains()) {

			// sets the expression evaluator field to "textContains"
			expressionEvaluator.setFieldType(FieldType.TEXT_CONTAINS);

			// evaluates the expression
			resultTextContains = expressionEvaluator.evaluateExpression();
		}

		// ---------------------------------
		// 4. Evaluates scrubbedTextContains
		// ---------------------------------

		if (filter.hasScrubbedTextContains()) {

			// sets the expression evaluator field to "textContains"
			expressionEvaluator.setFieldType(FieldType.SCRUBBED_TEXT_CONTAINS);

			// evaluates the expression
			resultScrubbedTextContains = expressionEvaluator
					.evaluateExpression();
		}

		// ------------------------------
		// 5. Evaluates grammarDependency
		// ------------------------------

		if (filter.hasGrammarDependency()) {

			boolean resultGrammarDependencyGovernor = false;
			boolean resultGrammarDependencyDependent = false;

			// gets the relation name
			String relationName = filter.getGrammarDependency().getRelation();

			// checks if the vine contains one or more of the relation
			if (vine.containsGrammarDependency(relationName)) {

				// Loops over the grammar dependencies in the vine that fit the
				// relation name defined in the filter. This is done because
				// there may be multiple instances of certain grammar
				// dependencies (eg: det, nn, amod) in a single text.
				for (GrammarDependency relation : vine
						.getGrammarDependencyByName(relationName)) {

					// resets result booleans for new relation
					resultGrammarDependencyGovernor = false;
					resultGrammarDependencyDependent = false;

					// System.out.println("dependency: "
					// + relation.getGrammarDependencyAsString());

					// 1. Sets the specific grammar dependency to operate on
					expressionEvaluator.setGrammarDependency(relation);

					if (filter.hasGrammarDependencyGovernorExpression()) {

						// 2. Sets the expression evaluator field to "governor"
						expressionEvaluator
								.setFieldType(FieldType.GRAMMAR_DEPENDENCY_GOVERNOR);

						// 3. Evaluates the expression on the governor
						resultGrammarDependencyGovernor = expressionEvaluator
								.evaluateExpression();

						// System.out.println("GOV EVALUATOR: "
						// + resultGrammarDependencyGovernor);

					}

					if (filter.hasGrammarDependencyDependentExpression()) {

						// 2. Sets the expression evaluator field to "dependent"
						expressionEvaluator
								.setFieldType(FieldType.GRAMMAR_DEPENDENCY_DEPENDENT);

						// 3. Evaluates the expression on the dependent
						resultGrammarDependencyDependent = expressionEvaluator
								.evaluateExpression();

						// System.out.println("DEP EVALUATOR: "
						// + resultGrammarDependencyDependent);

					}

					// determines the grammar dependency result based on the
					// results in the governor and dependent
					if (resultGrammarDependencyGovernor
							&& resultGrammarDependencyDependent) {
						resultGrammarDependency = true;
						break;
					} else {
						resultGrammarDependency = false;
					}

				}
			}

		}

		// -----------------
		// 6. Returns result
		// -----------------

		if (resultTextContains && resultScrubbedTextContains
				&& resultGrammarDependency) {
			return true;
		} else {
			return false;
		}

	}

	public static boolean interpretResult(HashMap<Boolean, LogicType> r) {

		// ----------------
		// Method variables
		// ----------------

		// current argument evaluation
		Entry<Boolean, LogicType> currentEntry;
		boolean currentResult;
		LogicType currentLogic;

		// next argument evaluation
		Entry<Boolean, LogicType> nextEntry;
		boolean nextResult;
		LogicType nextLogic;

		// holds return value
		boolean interpretation = false;

		// ------------------
		// Interprets results
		// ------------------

		// makes iterator
		Iterator<Entry<Boolean, LogicType>> it = r.entrySet().iterator();

		// gets the current entry
		if (!it.hasNext()) {
			return false;
		}
		currentEntry = it.next();
		currentResult = currentEntry.getKey();
		currentLogic = currentEntry.getValue();

		// loops over results
		while (true) {

			// gets next entry if exists
			if (currentLogic != null && it.hasNext()) {
				nextEntry = it.next();
				nextResult = nextEntry.getKey();
				nextLogic = nextEntry.getValue();

				// compares currentResult and nextResult with logic
				switch (currentLogic) {
				case AND:

					if (currentResult && nextResult) {
						interpretation = true;
					} else {
						interpretation = false;
						return interpretation;
					}

					break;
				case OR:
					if (currentResult || nextResult) {
						interpretation = true;
					} else {
						interpretation = false;
						return interpretation;
					}

					break;
				default:
					return false;
				}

				// sets nextResult to currentResult
				currentResult = nextResult;
				currentLogic = nextLogic;

			}

			// single argument in expression
			else {
				interpretation = currentResult;
				return interpretation;
			}
		}

	}

	// /////////////////
	// GLOBAL SETTERS //
	// /////////////////

	public void setVine(Vine v) {
		vine = v;
	}

	// /////////////////
	// GLOBAL GETTERS //
	// /////////////////

	public boolean getFlagError() {
		return flagError;
	}

}
