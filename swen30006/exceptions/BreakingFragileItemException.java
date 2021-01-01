package exceptions;

/**
 * An exception throws when robot wrongly handle fragile item
 */
public class BreakingFragileItemException extends Exception {
	public BreakingFragileItemException() {
		super("Breaking Fragile Item!!");
	}
}
