package org.dspace.content.dao.impl.clarin;

import org.dspace.content.clarin.ClarinLicenseResourceMapping;
import org.dspace.content.clarin.ClarinLicenseResourceUserAllowance;
import org.dspace.content.dao.clarin.ClarinLicenseResourceUserAllowanceDAO;
import org.dspace.core.AbstractHibernateDAO;
import org.dspace.core.Context;
import org.dspace.services.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.Query;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class ClarinLicenseResourceUserAllowanceDAOImpl extends AbstractHibernateDAO<ClarinLicenseResourceUserAllowance>
        implements ClarinLicenseResourceUserAllowanceDAO {

    @Autowired
    ConfigurationService configurationService;

    @Override
    public List<ClarinLicenseResourceUserAllowance> findByTokenAndBitstreamId(Context context, UUID resourceID,
                                                                              String token) throws SQLException {
        Query query = createQuery(context, "SELECT clrua " +
                "FROM ClarinLicenseResourceUserAllowance clrua " +
                "WHERE clrua.token = :token AND clrua.licenseResourceMapping.bitstream.id = :resourceID " +
                "AND clrua.createdOn >= :notGeneratedBefore");

        // Token is expired after 30 days by default, the default value could be changed by the value from
        // the configuration
        int tokenExpirationDays =
                configurationService.getIntProperty("bitstream.download.token.expiration.days", 30);

        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DAY_OF_MONTH, -tokenExpirationDays);

        query.setParameter("token", token);
        query.setParameter("resourceID", resourceID);
        query.setParameter("notGeneratedBefore", cal.getTime());
        query.setHint("org.hibernate.cacheable", Boolean.TRUE);

        return list(query);
    }

    @Override
    public List<ClarinLicenseResourceUserAllowance> findByEPersonId(Context context, UUID userID) throws SQLException {
        Query query = createQuery(context, "SELECT clrua " +
                "FROM ClarinLicenseResourceUserAllowance clrua " +
                "WHERE clrua.userRegistration.ePersonID = :userID");

        query.setParameter("userID", userID);
        query.setHint("org.hibernate.cacheable", Boolean.TRUE);

        return list(query);
    }
}
