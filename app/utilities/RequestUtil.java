package utilities;

import java.math.BigDecimal;
import java.util.Map;

import play.Play;
import play.mvc.Controller;
import org.hibernate.Criteria;

/**
 * Created by yael on 10/3/15.
 */
public class RequestUtil extends Controller {

    public static String getConfig(String key) {
        return Play.application().configuration().getString(key);
    }

    /** paginate: Auxiliary method to help divide query results and show the requested item for a page num.
     * @param criteria - a criteria about to be executed, to set first results and max results.
     */

    public static void paginate(Criteria criteria) {
        Integer pageNum = getQueryParamAsInt("page");
        if (pageNum != null && pageNum > 0) {
            Integer maxItems = getQueryParamAsInt("maxItems");
            if (maxItems == null)
                maxItems = Integer.parseInt(getConfig("photosesh.pagination.maxitems"));

            criteria.setFirstResult(maxItems * (pageNum - 1));
            criteria.setMaxResults(maxItems);
        }
    }

    /** getQueryParam: Auxiliary method to a common task of retrieving a parameter value list from a url request.
     * @param field - the field to extract value for.
     * @return String[]: the values if extracted, null otherwise.
     */
    public static String[] getQueryParams(String field) {
        Map<String, String[]> requestString = request().queryString();

        if(requestString == null || requestString.isEmpty())
            return null;
        String[] params = requestString.get(field);
        if (params == null || params.length == 0 || params[0] == null) {
            return null;
        }
        return params;
    }

    /** getQueryParam: Auxiliary method to a common task of retrieving a parameter value from a url request.
     * @param field - the field to extract value for.
     * @return String: the value if extracted, null otherwise.
     */
    public static String getQueryParam(String field) {
        Map<String, String[]> requestString = request().queryString();
        if(requestString == null || requestString.isEmpty())
            return null;
        String[] params = requestString.get(field);
        if (params == null || params.length == 0 || params[0] == null) {
            return null;
        }
        return params[0];
    }

    /** getQueryParamAsInt: Auxiliary method for retrieving a parameter value from a url request, as an integer.
     * @param field - the field to extract value for.
     * @return Integer: the value if extracted, null otherwise.
     */
    public static Integer getQueryParamAsInt(String field) {
        Map<String, String[]> requestString = request().queryString();
        if(requestString == null || requestString.isEmpty())
            return null;
        String[] params = requestString.get(field);
        if (params == null || params.length == 0 || params[0] == null) {
            return null;
        }
        return Integer.parseInt(params[0]);
    }

    /** getQueryParamAsLong: Auxiliary method for retrieving a parameter value from a url request, as a long.
     * @param field - the field to extract value for.
     * @return Long: the value if extracted, null otherwise.
     */
    public static Long getQueryParamAsLong(String field) {
        Map<String, String[]> requestString = request().queryString();
        if(requestString == null || requestString.isEmpty())
            return null;
        String[] params = requestString.get(field);
        if (params == null || params.length == 0 || params[0] == null) {
            return null;
        }
        return Long.parseLong(params[0]);
    }

    /** getQueryParamAsLong: Auxiliary method for retrieving a parameter value from a url request, as a long.
     * @param field - the field to extract value for.
     * @return Long: the value if extracted, null otherwise.
     */
    public static BigDecimal getQueryParamAsBigDecimal(String field) {
        Map<String, String[]> requestString = request().queryString();
        if(requestString == null || requestString.isEmpty())
            return null;
        String[] params = requestString.get(field);
        if (params == null || params.length == 0 || params[0] == null) {
            return null;
        }
        return new BigDecimal(params[0]);
    }
}
