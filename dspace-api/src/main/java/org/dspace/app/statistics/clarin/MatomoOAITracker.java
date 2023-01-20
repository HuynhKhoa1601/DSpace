package org.dspace.app.statistics.clarin;

import org.apache.log4j.Logger;
import org.dspace.content.Item;
import org.dspace.core.Context;
import org.dspace.services.ConfigurationService;
import org.dspace.services.factory.DSpaceServicesFactory;
import org.matomo.java.tracking.CustomVariable;
import org.matomo.java.tracking.MatomoException;
import org.matomo.java.tracking.MatomoRequest;

import javax.servlet.http.HttpServletRequest;
import java.net.URL;
import java.util.Objects;

public class MatomoOAITracker extends AbstractMatomoTracker {
    /** log4j category */
    private static Logger log = Logger.getLogger(MatomoOAITracker.class);

    private final ConfigurationService configurationService =
            DSpaceServicesFactory.getInstance().getConfigurationService();

    private int siteId;

    public MatomoOAITracker() {
        super();
        siteId = configurationService.getIntProperty("matomo.tracker.oai.site_id");
    }

    @Override
    protected void preTrack(Context context, MatomoRequest matomoRequest, Item item, HttpServletRequest request) {
        super.preTrack(context, matomoRequest, item, request);

        matomoRequest.setSiteId(siteId);
        log.debug("Logging to site " + matomoRequest.getSiteId());
        try {
            matomoRequest.setPageCustomVariable(new CustomVariable("source", "oai"), 1);
        } catch (MatomoException e) {
            log.error(e);
        }
    }

    @Override
    public void trackPage(Context context, HttpServletRequest request, Item item, String pageName)
    {
        pageName = expandPageName(request, pageName);
        log.debug("Matomo tracks " + pageName);
        String pageURL = getFullURL(request);

        MatomoRequest matomoRequest = createMatomoRequest(request, pageName, pageURL);
        if (Objects.isNull(matomoRequest)) {
            return;
        }

        // Add some headers and parameters to the request
        preTrack(context, matomoRequest, item, request);
        sendTrackingRequest(matomoRequest);
    }

    private String expandPageName(HttpServletRequest request, String pageName)
    {
        String[] metadataPrefix = request.getParameterValues("metadataPrefix");
        if(metadataPrefix != null && metadataPrefix.length > 0) {
            pageName = pageName + "/" + metadataPrefix[0];
        }
        return pageName;
    }
}
