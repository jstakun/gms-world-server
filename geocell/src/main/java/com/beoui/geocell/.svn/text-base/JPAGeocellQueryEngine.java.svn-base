package com.beoui.geocell;

import java.util.List;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.beoui.geocell.model.GeocellQuery;

public class JPAGeocellQueryEngine implements GeocellQueryEngine {

    private static final Logger logger = GeocellLogger.get();

    private final static String ORDER_BY_RE = "[Oo][Rr][Dd][Ee][Rr]\\s*[Bb][Yy]";
	private final static String WHERE_RE = "[Ww][Hh][Ee][Rr][Ee]";

	private EntityManager entityManager;

	public void setEntityManager(EntityManager entityManager) {
    	this.entityManager = entityManager;
    }

    /**
     * Modifies JPA EJB QL to include geocell lookup.
     *  
     * @see com.beoui.geocell.GeocellQueryEngine#query(com.beoui.geocell.model.GeocellQuery, java.util.List, java.lang.Class)
     */
	@Override
	@SuppressWarnings("unchecked")
	public <T> List<T> query(GeocellQuery baseQuery, List<String> curGeocellsUnique, Class<T> entityClass) {
		String[] splitQueryWhere = baseQuery.getBaseQuery().split(WHERE_RE);
		String[] splitQueryOrderBy = null;
		if(splitQueryWhere.length > 1) {
			splitQueryOrderBy = splitQueryWhere[1].split(ORDER_BY_RE);
		}
		StringBuffer ejbql = new StringBuffer(splitQueryWhere[0].trim());

		if(!curGeocellsUnique.isEmpty()) {
			Object geocellsFieldName = GeocellUtils.getGeocellsFieldName(entityClass);
			if(splitQueryWhere.length == 1) {
				ejbql.append(" where ");
			} else {
				ejbql.append(" where ");
				ejbql.append(splitQueryOrderBy[0].trim());
				ejbql.append(" and ");
			}
			ejbql.append(geocellsFieldName);
			ejbql.append(" in (");
			boolean first = true;
			for(String geocell : curGeocellsUnique) {
				if(!first) {
					ejbql.append(",");
				}
				ejbql.append("\'" + geocell + "\'");
				first = false;
			}
			ejbql.append(")");
		}

		if(splitQueryOrderBy != null && splitQueryOrderBy.length > 1) {
			ejbql.append(" order by");
			ejbql.append(splitQueryOrderBy[1]);
		}
		logger.info("running EJB QL=["+ejbql+"]");
		Query query = entityManager.createQuery(ejbql.toString());

		int position = 0;
		if( baseQuery.getParameters() != null) {
			for(Object parameter : baseQuery.getParameters()) {
				position++;
				query.setParameter(position, parameter);
			}
		}

		return (List<T>) query.getResultList();
	}

}
