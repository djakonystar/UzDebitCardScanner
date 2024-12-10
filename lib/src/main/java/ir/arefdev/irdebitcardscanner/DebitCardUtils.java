package ir.arefdev.irdebitcardscanner;

import java.util.HashMap;

class DebitCardUtils {

    private static final String CARD_TYPE_UZCARD = "b_uzcard";
	private static final String CARD_TYPE_HUMO = "b_humo";
	private static final String CARD_TYPE_ATTO = "b_atto";
	private static final String CARD_TYPE_VISA = "b_visa";
	private static final String CARD_TYPE_MASTERCARD = "b_mastercard";
	private static final String CARD_TYPE_UNION_PAY = "b_union_pay";

	private static HashMap<String, String> CARD_NUMBER_STARTER = new HashMap<>();

	private static void init() {
		if (CARD_NUMBER_STARTER.isEmpty()) {
			CARD_NUMBER_STARTER.put("5614", CARD_TYPE_UZCARD);
			CARD_NUMBER_STARTER.put("8600", CARD_TYPE_UZCARD);
			CARD_NUMBER_STARTER.put("9860", CARD_TYPE_HUMO);
			CARD_NUMBER_STARTER.put("9987", CARD_TYPE_ATTO);
			CARD_NUMBER_STARTER.put("6262", CARD_TYPE_UNION_PAY);
			CARD_NUMBER_STARTER.put("4", CARD_TYPE_VISA);

			for (int i = 1; i <= 5; i++) {
				CARD_NUMBER_STARTER.put("5" + i, CARD_TYPE_MASTERCARD);
			}
		}
	}

	public static String getBankSlugFromCardNumber(String cardNumber) {
		init();

		if (cardNumber.length() < 6)
			return null;

		if (CARD_NUMBER_STARTER.containsKey(cardNumber.substring(0, 4))) {
            return CARD_NUMBER_STARTER.get(cardNumber.substring(0, 4));
        } else if (cardNumber.matches("^4[0-9]{12}(?:[0-9]{3})?$")) {
			return CARD_TYPE_VISA;
		} else if (cardNumber.matches("^(5[1-5][0-9]{14}|2(22[1-9][0-9]{12}|2[3-9][0-9]{13}|[3-6][0-9]{14}|7[0-1][0-9]{13}|720[0-9]{12}))$")) {
			return CARD_TYPE_MASTERCARD;
		}

		return null;
	}

	public static boolean isCardNumberValid(String cardNumber) {
		return luhnCheck(cardNumber);
	}

	// https://en.wikipedia.org/wiki/Luhn_algorithm#Java
	static boolean luhnCheck(String ccNumber) {
		if (ccNumber == null || ccNumber.length() != 16 || getBankSlugFromCardNumber(ccNumber) == null) {
			return false;
		}

		int sum = 0;
		boolean alternate = false;
		for (int i = ccNumber.length() - 1; i >= 0; i--) {
			int n = Integer.parseInt(ccNumber.substring(i, i + 1));
			if (alternate) {
				n *= 2;
				if (n > 9) {
					n = (n % 10) + 1;
				}
			}
			sum += n;
			alternate = !alternate;
		}
		return (sum % 10 == 0);
	}

	public static String format(String number) {
		if (number.length() == 16) {
			return format16(number);
		}

		return number;
	}

	private static String format16(String number) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < number.length(); i++) {
			if (i == 4 || i == 8 || i == 12) {
				result.append(" ");
			}
			result.append(number.charAt(i));
		}

		return result.toString();
	}
}
