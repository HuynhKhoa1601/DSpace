package org.dspace.app.rest.repository;

import org.dspace.app.rest.model.ClarinLicenseResourceMappingRest;
import org.dspace.app.rest.model.ClarinLicenseResourceUserAllowanceRest;
import org.dspace.app.rest.model.ClarinUserRegistrationRest;
import org.dspace.app.rest.projection.Projection;
import org.dspace.content.clarin.ClarinLicenseResourceMapping;
import org.dspace.content.clarin.ClarinLicenseResourceUserAllowance;
import org.dspace.content.clarin.ClarinUserRegistration;
import org.dspace.content.service.clarin.ClarinLicenseResourceUserAllowanceService;
import org.dspace.core.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;
import java.util.Objects;

/**
 * CLRUA = ClarinLicenseResourceUserAllowance
 */
@Component(ClarinLicenseResourceUserAllowanceRest.CATEGORY + "." + ClarinLicenseResourceUserAllowanceRest.NAME +
        "." + ClarinLicenseResourceUserAllowanceRest.RESOURCE_MAPPING)
public class CLRUAResourceMappingLinkRepository extends AbstractDSpaceRestRepository
        implements LinkRestRepository {

    @Autowired
    ClarinLicenseResourceUserAllowanceService clarinLicenseResourceUserAllowanceService;

    public ClarinLicenseResourceMappingRest getResourceMapping(@Nullable HttpServletRequest request,
                                                               Integer clruaID,
                                                               @Nullable Pageable optionalPageable,
                                                               Projection projection) throws SQLException {
        Context context = obtainContext();

        ClarinLicenseResourceUserAllowance clarinLicenseResourceUserAllowance =
                clarinLicenseResourceUserAllowanceService.find(context, clruaID);
        if (Objects.isNull(clarinLicenseResourceUserAllowance)) {
            throw new ResourceNotFoundException("The ClarinLicenseResourceUserAllowance for id: " + clruaID +
                    " couldn't be found");
        }
        ClarinLicenseResourceMapping clarinLicenseResourceMapping =
                clarinLicenseResourceUserAllowance.getLicenseResourceMapping();

        if (Objects.isNull(clarinLicenseResourceMapping)) {
            throw new ResourceNotFoundException("The ClarinLicenseResourceMapping for " +
                    "ClarinLicenseResourceUserAllowance with id: " + clruaID + " doesn't exists.");
        }
        return converter.toRest(clarinLicenseResourceMapping, projection);
    }
}
