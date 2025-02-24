package dannydelott.vinefilter.settings.filter.expression;

import dannydelott.vinefilter.Messages;
import dannydelott.vinefilter.settings.SettingsFile;
import dannydelott.vinefilter.settings.filter.expression.values.LogicType;
import dannydelott.vinefilter.settings.filter.expression.values.Tokens;

import java.util.LinkedHashMap;
import java.util.Stack;

public class Expression {

	// ///////////////////
	// GLOBAL VARIABLES //
	// ///////////////////

	// holds the settings file used for
	// constructing filterExpressionArgument objects
	private SettingsFile settings;

	// holds the raw rule expression to split into groups
	private String filterExpression;

	// holds the arguments and the logic type found after it if any
	private LinkedHashMap<Argument, LogicType> arguments;

	private boolean flagError;

	// //////////////////////
	// FACTORY CONSTRUCTOR //
	// //////////////////////

	/**
	 * Returns an Expression object containing the array of rule expression
	 * groups and the operators. For now, rule expression groups must not
	 * contain nested groups.
	 * 
	 * eg: (([@TargetWord] && ![@Strings:BLACKLIST]) || [@Literal:dog]) ...
	 * 
	 * ((not)(allowed))
	 * 
	 * @param s
	 *            SettingsFile object
	 * @param re
	 *            rule expression string from filter
	 * @return
	 */
	public static Expression newInstance(SettingsFile s, String fe) {
		Expression r = new Expression(s, fe);
		if (r.getFlagError()) {
			System.out.println("rule expression groups is null");
			return null;
		}
		return r;
	}

	// //////////////
	// CONSTRUCTOR //
	// //////////////

	private Expression(SettingsFile s, String fe) {
		settings = s;
		filterExpression = fe;
		arguments = parseArgumentGroups();

	}

	// //////////////////
	// PRIVATE METHODS //
	// //////////////////

	/**
	 * Parses the Expression into Argument/Logic groups.
	 * 
	 * Currently does not support error handling for multiple LogicTypes.
	 * 
	 * Eg: ([@Literal:dog] || && ![@Strings:LIST_OF_WORDS])
	 * 
	 */
	private LinkedHashMap<Argument, LogicType> parseArgumentGroups() {

		// 1. checks if parenthesis/brackets are balanced
		if (!isBalanced()) {
			System.out.println(Messages.Expression_errorParenthesis + " (\""
					+ filterExpression + "\")");
			flagError = true;
			return null;
		}

		// Initializes remainder to the full rule expression from the method
		// arguments. This will be reset as groups and logical operators are
		// split off.
		// eg: "([@Literal:#][@TargetWord:NO_SPACES]) && ([@Literal:dog])"
		String remainder = filterExpression;
		if (remainder == null) {
			return null;
		}

		// holds the return rule expression arguments and logic types
		LinkedHashMap<Argument, LogicType> tempGroups = new LinkedHashMap<Argument, LogicType>();
		Argument tempArgument = null;
		LogicType tempLogicType = null;

		// holds the current group and logic parsed
		String[] group = null;
		String[] logic = null;

		while (true) {

			if (remainder == null) {
				break;
			}

			// gets the next group from what's left of the rule expression
			// eg: [0] => "([@Literal:#][@TargetWord:NO_SPACES])"
			// eg: [1] => "&& ([@Literal:dog])"
			group = nextGroup(remainder);
			if (group == null) {
				System.out.println("group is null");
				flagError = true;
				return null;
			}

			// creates Argument from group
			tempArgument = Argument.newInstance(settings, group[0]);
			if (tempArgument == null) {
				System.out.println("tempArgument is null");
				flagError = true;
				return null;
			}

			// sets remainder
			// eg: "&& ([@Literal:dog])"
			remainder = group[1];
			if (remainder == null) {
				tempGroups.put(tempArgument, tempLogicType);
				return tempGroups;
			}

			// gets the logic operator from the remainder
			// eg: [0] => "&&"
			// eg: [1] => "([@Literal:dog])"
			if (remainder != null) {
				logic = nextLogic(remainder);
				if (logic.length == 0) {
					System.out.println("logic is null");
					flagError = true;
					return null;
				}
			}

			// sets the logicType
			if (logic.length == 2) {
				if (logic[0].contentEquals(Tokens.AND)) {
					tempLogicType = LogicType.AND;
				}
				if (logic[0].contentEquals(Tokens.OR)) {
					tempLogicType = LogicType.OR;
				}

				// sets the remainder
				remainder = logic[1];
			}

			// add group and logic type to map
			tempGroups.put(tempArgument, tempLogicType);

			if (logic.length == 1) {
				break;
			}
		}

		return tempGroups;
	}

	private boolean isBalanced() {
		Stack<Character> stack = new Stack<Character>();

		for (int i = 0; i < filterExpression.length(); i++) {

			if (filterExpression.charAt(i) == Tokens.L_PAREN)
				stack.push(Tokens.L_PAREN);

			else if (filterExpression.charAt(i) == Tokens.L_BRACE)
				stack.push(Tokens.L_BRACE);

			else if (filterExpression.charAt(i) == Tokens.L_BRACKET)
				stack.push(Tokens.L_BRACKET);

			else if (filterExpression.charAt(i) == Tokens.R_PAREN) {
				if (stack.isEmpty())
					return false;
				if (stack.pop() != Tokens.L_PAREN)
					return false;
			}

			else if (filterExpression.charAt(i) == Tokens.R_BRACE) {
				if (stack.isEmpty())
					return false;
				if (stack.pop() != Tokens.L_BRACE)
					return false;
			}

			else if (filterExpression.charAt(i) == Tokens.R_BRACKET) {
				if (stack.isEmpty())
					return false;
				if (stack.pop() != Tokens.L_BRACKET)
					return false;
			}

			// ignore all other characters

		}

		return stack.isEmpty();
	}

	private static boolean isLastCharValid(String re) {

		// returns null if last char in rule expression is the open-parenthesis,
		// open-bracket, or not operator
		if ((re.charAt(re.length() - 1) == Tokens.L_PAREN)
				|| (re.charAt(re.length() - 1) == Tokens.L_BRACKET)
				|| (re.charAt(re.length() - 1) == Tokens.NOT)) {

			return false;

		}
		return true;
	}

	// /////////////////
	// PUBLIC METHODS //
	// /////////////////

	/**
	 * Returns an array where element [0] contains the parsed group and element
	 * [1] contains the remainder of the rule expression.
	 * 
	 * @param re
	 *            rule expression or remainder to parse
	 * @return String array<br />
	 *         [0] = parsed group<br/>
	 *         [1] = remainder
	 */
	public static String[] nextGroup(String re) {

		// holds the return values
		String group;
		String remainder;
		String[] nextGroup = new String[2];

		boolean endOffilterExpression = false;

		int startPos = 0;
		int endPos = 0;
		boolean foundStart = false;
		boolean foundEnd = false;

		// returns null if last char in rule expression
		// is open parenthesis/bracket
		if (!isLastCharValid(re)) {
			System.out.println(Messages.Expression_errorParenthesis + " (\""
					+ re + "\")");
			return null;
		}

		// loops over rule expression to get the next group
		for (int i = 0; i < re.length(); i++) {

			// open parenthesis
			if (re.charAt(i) == Tokens.L_PAREN && !foundStart) {

				startPos = i + 1;
				foundStart = true;

			}

			// close parenthesis
			if (re.charAt(i) == Tokens.R_PAREN && !foundEnd) {

				// finds out if close parenthesis is end of the rule expression
				if (i == (re.length() - 1)) {
					endOffilterExpression = true;
				}

				endPos = i;
				foundEnd = true;
			}
		}

		// sets the group (excluding parenthesis)
		group = re.substring(startPos, endPos);

		// sets the remainder of the rule expression if it exists
		if (endOffilterExpression) {
			remainder = null;
		} else {
			remainder = re.substring(endPos);
		}

		nextGroup[0] = group;
		nextGroup[1] = remainder;

		return nextGroup;

	}

	public static String[] nextLogic(String re) {

		String filterExpression = re.trim();
		String logic = null;
		String remainder = null;
		LogicType anticipatedLogic = null;
		String[] nextLogic = new String[0];

		int logicCounter = 0;

		// loops over rule expression to get the next group
		for (int i = 0; i < filterExpression.length(); i++) {

			// ----------------------------
			// 1.
			// HANDLES "AND" LOGIC OPERATOR
			// ----------------------------

			if (filterExpression.charAt(i) == '&') {

				// sets logic type to anticipate for second char
				if (logicCounter == 0) {
					anticipatedLogic = LogicType.AND;
				}

				// error handling:
				// if logic type of next character isn't the same
				// eg: |&
				if (anticipatedLogic != LogicType.AND) {
					System.out.println(Messages.Expression_errorLogicType
							+ " (\"" + re + "\")");
					return null;
				}

				logicCounter++;

				// error handling:
				// returns null if last char is && logic operator
				if (i == (filterExpression.length() - 1)) {
					System.out.println(Messages.Expression_errorLogicType
							+ " (\"" + re + "\")");
					return null;
				}

				if (logicCounter == 2) {
					logic = Tokens.AND;
					remainder = filterExpression.substring(i);
					break;
				}
			}

			// ----------------------------
			// 2.
			// HANDLES "OR" LOGIC OPERATOR
			// ----------------------------

			if (filterExpression.charAt(i) == '|') {

				// sets logic type to anticipate for second char
				if (logicCounter == 0) {
					anticipatedLogic = LogicType.OR;
				}

				// error handling:
				// if logic type of next character isn't the same
				// eg: &|
				if (anticipatedLogic != LogicType.OR) {
					System.out.println(Messages.Expression_errorLogicType
							+ " (\"" + re + "\")");
					return null;
				}

				logicCounter++;

				// error handling:
				// returns null if last char is && logic operator
				if (i == (filterExpression.length() - 1)) {
					System.out.println(Messages.Expression_errorLogicType
							+ " (\"" + re + "\")");
					return null;
				}

				if (logicCounter == 2) {
					logic = Tokens.OR;
					remainder = re.substring(i);
					break;
				}
			}

		}
		if (logic != null) {
			nextLogic = new String[2];
			nextLogic[0] = logic;
			nextLogic[1] = remainder;
		} else {

			// sets nextLogic size to 1 to indicate that there was no logic
			// found, but no errors were thrown
			nextLogic = new String[1];

		}

		return nextLogic;

	}

	public static String[] nextToken(String re) {

		// holds the return values
		String[] nextToken = new String[2];
		String token;
		String remainder;

		boolean foundNotOperator = false;
		boolean foundStart = false;
		boolean foundEnd = false;

		int startPos = 0;
		int endPos = 0;

		boolean endOfArgument = false;

		// returns null if last char in rule expression
		// is open parenthesis/bracket
		if (!isLastCharValid(re)) {
			System.out.println(Messages.Expression_errorBrackets + " (\"" + re
					+ "\")");
			return null;
		}

		// loops over argument to get the next token
		for (int i = 0; i < re.length(); i++) {

			// not operator
			if (re.charAt(i) == Tokens.NOT && !foundNotOperator) {
				foundNotOperator = true;
			}

			// open bracket
			if (re.charAt(i) == Tokens.L_BRACKET && !foundStart) {
				startPos = i;
				foundStart = true;
			}

			// close bracket
			if (re.charAt(i) == Tokens.R_BRACKET && !foundEnd) {

				// finds out if close bracket is end of the argument
				if (i == (re.length() - 1)) {
					endOfArgument = true;
				}
				endPos = i + 1;
				foundEnd = true;
				break;
			}
		}

		// sets the token (including brackets)
		if (foundNotOperator) {
			token = "!" + re.substring(startPos, endPos);
		} else {
			token = re.substring(startPos, endPos);
		}

		// sets the remainder of the rule expression if it exists
		if (endOfArgument) {
			remainder = null;
		} else {
			remainder = re.substring(endPos);
		}

		nextToken[0] = token;
		nextToken[1] = remainder;

		return nextToken;
	}

	// /////////////////
	// GLOBAL GETTERS //
	// /////////////////

	public LinkedHashMap<Argument, LogicType> getArguments() {
		return arguments;
	}

	public boolean getFlagError() {
		return flagError;
	}

}
