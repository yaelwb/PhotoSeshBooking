package utilities;

import enums.State;
import models.Status;
import play.db.jpa.JPA;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yael on 10/11/15.
 */
public class StatusMap {

    private static Map<String, Status> statusMap = new HashMap<>();
    private static Map<String, Long> statusIdMap = new HashMap<>();
    private static Map<Long, Status> idStatusMap = new HashMap<>();

    private static void updateMaps(String state) {
        EntityManager em = JPA.em("default");
        Query query = em.createQuery("select status from Status where state =:state");
        query.setParameter("state", state);
        Status status = (Status) query.getSingleResult();
        if (status != null) {
            statusMap.put(state, status);
            statusIdMap.put(state, status.getId());
            idStatusMap.put(status.getId(), status);
        }
    }

    public static Status getStatus(String state) {
        String upperCaseState = state.toUpperCase();
        Status status = statusMap.get(upperCaseState);
        if(status == null) {
            updateMaps(upperCaseState);
            status = statusMap.get(upperCaseState);
        }
        return status;
    }

    public static Long getStatusId(String state) {
        String upperCaseState = state.toUpperCase();
        Long id = statusIdMap.get(upperCaseState);
        if(id == null) {
            updateMaps(upperCaseState);
            id = statusIdMap.get(upperCaseState);
        }
        return id;
    }

    public static Status getStatus(Long id) {
        return idStatusMap.get(id);
    }

 }
