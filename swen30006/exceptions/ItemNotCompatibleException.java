package exceptions;

public class ItemNotCompatibleException extends Exception {
	public ItemNotCompatibleException() {
		super("Cant carry normal item on special arms!!");
	}
}
