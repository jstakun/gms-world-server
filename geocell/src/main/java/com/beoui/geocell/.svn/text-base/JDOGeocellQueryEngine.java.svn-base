package com.beoui.geocell;

import java.util.ArrayList;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.beoui.geocell.model.GeocellQuery;

public class JDOGeocellQueryEngine implements GeocellQueryEngine {

	PersistenceManager pm;

	public void setPersistenceManager(PersistenceManager pm) {
    	this.pm = pm;
    }

	/**
     * @see com.beoui.geocell.GeocellQueryEngine#query(com.beoui.geocell.model.GeocellQuery, java.util.List, java.lang.Class)
     */
    @Override
	@SuppressWarnings("unchecked")
    public <T> List<T> query(GeocellQuery baseQuery, List<String> curGeocellsUnique, Class<T> entityClass) {
        // Run query on the next set of geocells.
        String queryStart = baseQuery.getBaseQuery() == null || baseQuery.getBaseQuery().trim().length() == 0 ? " " : baseQuery.getBaseQuery() + " && ";
        Query query = pm.newQuery(entityClass, queryStart + GeocellUtils.getGeocellsFieldName(entityClass) + ".contains(geocellsP)");

        if(baseQuery.getDeclaredParameters() == null || baseQuery.getDeclaredParameters().trim().length() == 0) {
            query.declareParameters("String geocellsP");
        } else {
            query.declareParameters(baseQuery.getDeclaredParameters() + ", String geocellsP");
        }

        List<T> newResultEntities;
        if(baseQuery.getParameters() == null || baseQuery.getParameters().isEmpty()) {
            newResultEntities = (List<T>) query.execute(curGeocellsUnique);
        } else {
            List<Object> parameters = new ArrayList<Object>(baseQuery.getParameters());
            parameters.add(curGeocellsUnique);
            newResultEntities = (List<T>) query.executeWithArray(parameters.toArray());
        }

        return newResultEntities;
	}

}
