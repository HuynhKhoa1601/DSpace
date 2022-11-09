package org.dspace.content.dao.clarin;

import org.dspace.AbstractIntegrationTest;
import org.dspace.content.Bitstream;
import org.dspace.content.clarin.ClarinLicenseResourceMapping;
import org.dspace.content.clarin.ClarinLicenseResourceUserAllowance;
import org.dspace.content.factory.ClarinServiceFactory;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.BitstreamService;
import org.dspace.content.service.clarin.ClarinLicenseResourceMappingService;
import org.dspace.content.service.clarin.ClarinLicenseResourceUserAllowanceService;
import org.dspace.handle.dao.HandleDAO;
import org.dspace.services.ConfigurationService;
import org.dspace.services.factory.DSpaceServicesFactory;
import org.dspace.utils.DSpace;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class ClarinLicenseResourceUserAllowanceDAOImplTest extends AbstractIntegrationTest {

    private ConfigurationService configurationService = DSpaceServicesFactory.getInstance().getConfigurationService();
    private ClarinLicenseResourceMappingService clarinLicenseResourceMappingService =
            ClarinServiceFactory.getInstance().getClarinLicenseResourceMappingService();
    private BitstreamService bitstreamService = ContentServiceFactory.getInstance().getBitstreamService();
    private ClarinLicenseResourceUserAllowance clarinLicenseResourceUserAllowance;

    private ClarinLicenseResourceUserAllowanceDAO clarinLicenseResourceUserAllowanceDAO =
            new DSpace().getServiceManager().getServicesByType(ClarinLicenseResourceUserAllowanceDAO.class).get(0);

    @Override
    public void init() {
        super.init();
        try {
            context.turnOffAuthorisationSystem();
            clarinLicenseResourceUserAllowance =
                    clarinLicenseResourceUserAllowanceDAO.create(context, new ClarinLicenseResourceUserAllowance());
            context.restoreAuthSystemState();
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
    }

    @After
    @Override
    public void destroy() {
        clarinLicenseResourceUserAllowance = null;
        clarinLicenseResourceUserAllowanceDAO = null;
        clarinLicenseResourceMappingService = null;
        bitstreamService = null;
    }

    @Test
    public void checkExpiredToken() throws Exception {
        // Add token and mapping to the record.
        ClarinLicenseResourceMapping clarinLicenseResourceMapping = clarinLicenseResourceMappingService.create(context);
        File f = new File(testProps.get("test.bitstream").toString());
        Bitstream bitstream = bitstreamService.create(context, new FileInputStream(f));
        clarinLicenseResourceMapping.setBitstream(bitstream);
        // Update changes to the database.
        clarinLicenseResourceMappingService.update(context, clarinLicenseResourceMapping);

        String token = "amazingToken";
        clarinLicenseResourceUserAllowance.setToken(token);
        clarinLicenseResourceUserAllowance.setCreatedOn(new Date());
        clarinLicenseResourceUserAllowance.setLicenseResourceMapping(clarinLicenseResourceMapping);
        // Update changes to the database.
        clarinLicenseResourceUserAllowanceDAO.save(context, clarinLicenseResourceUserAllowance);

        List<ClarinLicenseResourceUserAllowance> clruaList =
                clarinLicenseResourceUserAllowanceDAO.findByTokenAndBitstreamId(context, bitstream.getID(), token);
        Assert.assertEquals(clruaList.size(), 1);
    }
}
