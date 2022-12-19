package org.dspace.app.rest.repository;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dspace.app.rest.Parameter;
import org.dspace.app.rest.SearchRestMethod;
import org.dspace.app.rest.exception.UnprocessableEntityException;
import org.dspace.app.rest.model.ClarinLicenseRest;
import org.dspace.app.rest.model.ClarinUserRegistrationRest;
import org.dspace.app.rest.model.ClarinVerificationTokenRest;
import org.dspace.app.rest.model.ItemRest;
import org.dspace.app.rest.security.ShibbolethLoginFilter;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Item;
import org.dspace.content.clarin.ClarinLicense;
import org.dspace.content.clarin.ClarinUserRegistration;
import org.dspace.content.clarin.ClarinVerificationToken;
import org.dspace.content.service.clarin.ClarinVerificationTokenService;
import org.dspace.core.Context;
import org.dspace.web.ContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Component(ClarinVerificationTokenRest.CATEGORY + "." + ClarinVerificationTokenRest.NAME)
public class ClarinVerificationTokenRestRepository extends DSpaceRestRepository<ClarinVerificationTokenRest, Integer> {
    private static final Logger log = LogManager.getLogger(ClarinVerificationTokenRestRepository.class);

    @Autowired
    ClarinVerificationTokenService clarinVerificationTokenService;

    @Override
    @PreAuthorize("permitAll()")
    public ClarinVerificationTokenRest findOne(Context context, Integer integer) {
        ClarinVerificationToken clarinVerificationToken;
        try {
            clarinVerificationToken = clarinVerificationTokenService.find(context, integer);
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        if (Objects.isNull(clarinVerificationToken)) {
            log.error("Cannot find the clarin verification token with id: " + integer);
            return null;
        }
        return converter.toRest(clarinVerificationToken, utils.obtainProjection());
    }

    @Override
    public Page<ClarinVerificationTokenRest> findAll(Context context, Pageable pageable) {
        try {
            List<ClarinVerificationToken> clarinVerificationTokenList = clarinVerificationTokenService.findAll(context);
            return converter.toRestPage(clarinVerificationTokenList, pageable, utils.obtainProjection());
        } catch (SQLException | AuthorizeException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @SearchRestMethod(name = "byNetId")
    public Page<ClarinVerificationTokenRest> findByNetId(@Parameter(value = "netid", required = true) String netid,
                                                          Pageable pageable) throws SQLException {
        Context context = obtainContext();

        ClarinVerificationToken clarinVerificationToken = clarinVerificationTokenService.findByNetID(context, netid);
        if (Objects.isNull(clarinVerificationToken)) {
            return null;
        }

        List<ClarinVerificationToken> clarinVerificationTokenList = new ArrayList<>();
        clarinVerificationTokenList.add(clarinVerificationToken);

        return converter.toRestPage(clarinVerificationTokenList, pageable, utils.obtainProjection());
    }

    @SearchRestMethod(name = "byToken")
    public Page<ClarinVerificationTokenRest> findToken(@Parameter(value = "token", required = true) String token,
                                                         Pageable pageable) throws SQLException {
        Context context = obtainContext();

        ClarinVerificationToken clarinVerificationToken = clarinVerificationTokenService.findByToken(context, token);
        if (Objects.isNull(clarinVerificationToken)) {
            return null;
        }

        List<ClarinVerificationToken> clarinVerificationTokenList = new ArrayList<>();
        clarinVerificationTokenList.add(clarinVerificationToken);

        return converter.toRestPage(clarinVerificationTokenList, pageable, utils.obtainProjection());
    }

    @Override
    @PreAuthorize("permitAll()")
    protected void delete(Context context, Integer id) throws AuthorizeException {
        ClarinVerificationToken clarinVerificationToken ;
        try {
            clarinVerificationToken = clarinVerificationTokenService.find(context, id);
            if (Objects.isNull(clarinVerificationToken)) {
                throw new RuntimeException("Cannot find the clarin verification token with the id: " + id);
            }
            clarinVerificationTokenService.delete(context, clarinVerificationToken);
        } catch (SQLException e) {
            throw new RuntimeException("Cannot delete the clarin verification token with the id: " + id +
                    " because:" + e.getSQLState());
        }
    }

    @Override
    public Class<ClarinVerificationTokenRest> getDomainClass() {
        return ClarinVerificationTokenRest.class;
    }
}