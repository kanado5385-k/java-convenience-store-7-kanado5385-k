package store.utilities;

import store.enums.constants.AnswerConstants;
import store.enums.messages.ErrorMessage;

public class Validator {
    private static final int MINIMUM_AMOUNT = 0;
    private static final String START = "[";
    private static final String SEPARATOR = "-";
    private static final String END = "]";

    public static void validateFormatOfOrder(String order) {
        validateStartsWithBracket(order);
        validateEndsWithBracket(order);
        validateContainsHyphen(order);
    }
    
    private static void validateStartsWithBracket(String order) {
        if (!order.startsWith(START)) {
            throw new IllegalArgumentException(ErrorMessage.WRONG_FORMAT_OF_ORDER.getMessage());
        }
    }
    
    private static void validateEndsWithBracket(String order) {
        if (!order.endsWith(END)) {
            throw new IllegalArgumentException(ErrorMessage.WRONG_FORMAT_OF_ORDER.getMessage());
        }
    }
    
    private static void validateContainsHyphen(String order) {
        if (!order.contains(SEPARATOR)) {
            throw new IllegalArgumentException(ErrorMessage.WRONG_FORMAT_OF_ORDER.getMessage());
        }
    }

    public static void validateQuantityNumber(int quantity) {
        if (quantity <= MINIMUM_AMOUNT) {
            throw new IllegalArgumentException(ErrorMessage.NOT_NATURAL_NUMBER.getMessage());
        }
    }

    public static void validateAnswer(String answer) {
        if (!answer.equals(AnswerConstants.ANSWER_YES.getConstants()) && 
            !answer.equals(AnswerConstants.ANSWER_NO.getConstants())) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_INPUT_ANSWER.getMessage());
        }
    }
}
