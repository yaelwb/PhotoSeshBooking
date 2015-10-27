package utilities;

import enums.State;
import models.Status;
import play.db.jpa.JPA;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.*;

/**
 * Created by yael on 10/11/15.
 * Cache to reduce db calls, as statuses are unlikely to change
 * Mapping from the state string to the Status or StatusId - used by models.Booking.setStatus
 * A reverse map from StatusId to the status - used by models.Booking.getStatus
 */
public class StatusUtil {

    private static Map<State, Status> statusMap = new HashMap<>();
    private static Map<State, Long> statusIdMap = new HashMap<>();
    private static Map<Long, Status> idStatusMap = new HashMap<>();

    private static Map<State, Set<State>> stateChanges = new HashMap<>();

    static {
        //either booked or cancelled. can't postpone as no date is set yet
        stateChanges.put(State.CREATED,
                new HashSet<>(Arrays.asList(new State[] {State.BOOKED, State.CANCELED})));

        //booked, downpaynemt, and preparation can get update calls without changing a state,
        //in order to update event information
        stateChanges.put(State.BOOKED,
                new HashSet<>(Arrays.asList(new State[] {State.DOWNPAYMENT, State.CANCELED, State.POSTPONED, State.BOOKED})));

        stateChanges.put(State.DOWNPAYMENT,
                new HashSet<>(Arrays.asList(new State[] {State.PREPARATION, State.CANCELED, State.POSTPONED, State.DOWNPAYMENT})));

        stateChanges.put(State.PREPARATION,
                new HashSet<>(Arrays.asList(new State[] {State.PHOTOSHOOT, State.CANCELED, State.POSTPONED, State.PREPARATION})));

        //once the photoshoot has taken place, no option to cancel or postpone
        stateChanges.put(State.PHOTOSHOOT,
                new HashSet<>(Arrays.asList(new State[] {State.PAYMENT, State.PHOTOSHOOT})));

        stateChanges.put(State.PAYMENT,
                new HashSet<>(Arrays.asList(new State[] {State.SELECTIONS, State.PAYMENT})));

        //selections and editing can get update calls without changing a state, for num selected/num edited updates
        stateChanges.put(State.SELECTIONS,
                new HashSet<>(Arrays.asList(new State[] {State.EDITING, State.SELECTIONS})));

        stateChanges.put(State.EDITING,
                new HashSet<>(Arrays.asList(new State[] {State.REVIEW, State.EDITING})));

        //from review it can go further to complete, or back to either selections or editing
        stateChanges.put(State.REVIEW,
                new HashSet<>(Arrays.asList(new State[] {State.COMPLETE, State.SELECTIONS, State.EDITING})));

        //end of the process
        stateChanges.put(State.COMPLETE,
                new HashSet<>(Arrays.asList(new State[] {})));

        //can't be resumed after cancellation
        stateChanges.put(State.CANCELED,
                new HashSet<>(Arrays.asList(new State[] {})));
    }

    public static Boolean stateChangeExists(String from, String to) {
        //not null - states with no following states map to an empty set
        Set<State> states = stateChanges.get(getState(from));
        return states.contains(getState(to));
    }

    public static State getState(String str) {
        return Enum.valueOf(State.class, str);
    }

    private static void updateMaps(State state) {
        EntityManager em = JPA.em("default");
        Query query = em.createQuery("from Status where state =:state").setParameter("state", state.toString());
        Status status = (Status) query.getSingleResult();
        if (status != null) {
            statusMap.put(state, status);
            statusIdMap.put(state, status.getId());
            idStatusMap.put(status.getId(), status);
        }
    }

    public static Status getStatus(String stateStr) {
        State state = getState(stateStr.toUpperCase());

        Status status = statusMap.get(state);
        if(status == null) {
            updateMaps(state);
            status = statusMap.get(state);
        }
        return status;
    }

    public static Long getStatusId(String stateStr) {
        State state = getState(stateStr.toUpperCase());

        Long id = statusIdMap.get(state);
        if(id == null) {
            updateMaps(state);
            id = statusIdMap.get(state);
        }
        return id;
    }

    public static Status getStatus(Long id) {
        return idStatusMap.get(id);
    }

    public String getStatusDescription(String stateStr) {
        Status status = StatusUtil.getStatus(getStatusId(stateStr));
        if(status != null) {
            return status.getDescription();
        }
        return null;
    }
 }
