package utilities;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by yael on 10/3/15.
 */
public class Parse {
    /** isNameValid: Validate name using Java reg ex.
     * This method checks if the input string is a valid name.
     * @param name String. Name to validate.
     * @return boolean: true if name is valid, false otherwise.
     */
    public static boolean isNameValid(String name) {
        String expression = "^[\\w'\\.-]+$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(name);
        if(matcher.matches()){
             return true;
        }
        return false;
    }

    /** isEmailValid: Validate email address using Java reg ex.
     * This method checks if the input string is a valid email address.
     * @param email String. Email address to validate.
     * @return boolean: true if email address is valid, false otherwise.
     */
    public static boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        if(matcher.matches()){
            return true;
        }
        return false;
    }

    /** isPhoneNumberValid: Validate phone number using Java reg ex.
     * This method checks if the input string is a valid phone number.
     * Phone Number formats: (nnn)nnn-nnnn; nnnnnnnnnn; nnn-nnn-nnnn.
     * @param phoneNumber String. Phone number to validate.
     * @return boolean: true if phone number is valid, false otherwise.
     */
    public static boolean isPhoneNumberValid(String phoneNumber){
        String expression = "^\\(?(\\d{3})\\)?[- ]?(\\d{3})[- ]?(\\d{4})$";
        Pattern pattern = Pattern.compile(expression);
        Matcher matcher = pattern.matcher(phoneNumber);
        if(matcher.matches()){
            return true;
        }
        return false;
    }
}
