package org.dspace.app.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dspace.app.rest.converter.ConverterService;
import org.dspace.app.rest.converter.MetadataConverter;
import org.dspace.app.rest.model.BitstreamRest;
import org.dspace.app.rest.model.BundleRest;
import org.dspace.app.rest.utils.Utils;
import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.service.AuthorizeService;
import org.dspace.checker.service.MostRecentChecksumService;
import org.dspace.content.Bitstream;
import org.dspace.content.BitstreamFormat;
import org.dspace.content.Bundle;
import org.dspace.content.Item;
import org.dspace.content.service.BitstreamFormatService;
import org.dspace.content.service.BitstreamService;
import org.dspace.content.service.BundleService;
import org.dspace.content.service.ItemService;
import org.dspace.content.service.clarin.ClarinBitstreamService;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.dspace.app.rest.utils.ContextUtil.obtainContext;
import static org.dspace.app.rest.utils.RegexUtils.REGEX_REQUESTMAPPING_IDENTIFIER_AS_UUID;

@RestController
@RequestMapping("/api/clarin/import/" + BitstreamRest.CATEGORY)
public class ClarinBitstreamImportController {
    private static final Logger log = LogManager.getLogger();
    @Autowired
    private BundleService bundleService;

    @Autowired
    private ClarinBitstreamService clarinBitstreamService;

    @Autowired
    private BitstreamService bitstreamService;
    @Autowired
    private AuthorizeService authorizeService;

    @Autowired
    private MetadataConverter metadataConverter;

    @Autowired
    private ItemService itemService;

    @Autowired
    private BitstreamFormatService bitstreamFormatService;
    @Autowired
    private ConverterService converter;
    @Autowired
    private Utils utils;

    @Autowired
    private MostRecentChecksumService checksumService;

    @PreAuthorize("hasAuthority('ADMIN')")
    @RequestMapping(method =  RequestMethod.POST, value = "/bitstream")
    public BitstreamRest importBitstreamForExistingFile(HttpServletRequest request) {
        Context context = obtainContext(request);
        if (Objects.isNull(context)) {
            throw new RuntimeException("Context is null!");
        }
        Bundle bundle = null;
        String bundleUUIDString = request.getParameter("bundle_id");
        if (StringUtils.isNotBlank(bundleUUIDString)) {
            UUID bundleUUID = UUID.fromString(bundleUUIDString);
            try {
                bundle = bundleService.find(context, bundleUUID);
            } catch (SQLException e) {
                log.error("Something went wrong trying to find the Bundle with uuid: " + bundleUUID, e);
            }
        }
        BitstreamRest bitstreamRest;
        Bitstream bitstream;
        Item item = null;
        try {
            //process bitstream creation
            ObjectMapper mapper = new ObjectMapper();
            bitstreamRest = mapper.readValue(request.getInputStream(), BitstreamRest.class);
            //create empty bitstream
            bitstream = clarinBitstreamService.create(context, bundle);
            //internal_id contains path to file
            String internalId = request.getParameter("internal_id");
            bitstream.setInternalId(internalId);
            String storeNumberString = request.getParameter("storeNumber");
            bitstream.setStoreNumber(getIntegerFromString(storeNumberString));
            String sequenceIdString = request.getParameter("sequenceId");
            Integer sequenceId = getIntegerFromString(sequenceIdString);
            bitstream.setSequenceID(sequenceId);
            //add bitstream Format
            String bitstreamFormatIdString = request.getParameter("bitstreamFormat");
            Integer bitstreamFormatId = getIntegerFromString(bitstreamFormatIdString);
            BitstreamFormat bitstreamFormat = null;
            if (!Objects.isNull(bitstreamFormatId)) {
                 bitstreamFormat = bitstreamFormatService.find(context, bitstreamFormatId);
            }
            bitstream.setFormat(context, bitstreamFormat);
            String deletedString = request.getParameter("deleted");
            if (clarinBitstreamService.addExistingFile(context, bitstream, bitstreamRest.getSizeBytes(),
                    bitstreamRest.getCheckSum().getValue(), bitstreamRest.getCheckSum().getCheckSumAlgorithm())) {
                if (bitstreamRest.getMetadata().getMap().size() > 0) {
                    metadataConverter.setMetadata(context, bitstream, bitstreamRest.getMetadata());
                }
                if (Boolean.parseBoolean(deletedString)) {
                    bitstreamService.delete(context, bitstream);
                } else {
                    //set bitstream as primary
                    String primaryBitstream = request.getParameter("primaryBitstream");
                    if (Boolean.parseBoolean(primaryBitstream)) {
                        bundle.setPrimaryBitstreamID(bitstream);
                    }
                }
                bitstreamService.update(context, bitstream);
            } else {
                return null;
            }
            if (bundle != null) {
                List<Item> items = bundle.getItems();
                if (!items.isEmpty()) {
                    item = items.get(0);
                }
                if (item != null && !(authorizeService.authorizeActionBoolean(context, item, Constants.WRITE)
                        && authorizeService.authorizeActionBoolean(context, item, Constants.ADD))) {
                    throw new AccessDeniedException("You do not have write rights to update the Bundle's item");
                }
                if (item != null) {
                    itemService.update(context, item);
                }
                bundleService.update(context, bundle);
            }
            bitstreamRest = converter.toRest(bitstream, utils.obtainProjection());
            context.commit();
        } catch (AuthorizeException | SQLException | IOException e) {
            String message = "Something went wrong with trying to create the single bitstream for file with internal_id: "
                    + request.getParameter("internal_id")
                    + " for bundle with uuid: " + bundle.getID();
            log.error("message", e);
            throw new RuntimeException("message", e);
        }
        return bitstreamRest;
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @RequestMapping(method =  RequestMethod.POST, value = "/bitstream/checksum")
    public void doUpdateBitstreamsChecksum(HttpServletRequest request) throws SQLException {
        Context context = obtainContext(request);
        if (Objects.isNull(context)) {
            throw new RuntimeException("Context is null!");
        }
        checksumService.updateMissingBitstreams(context);
    }


    private Integer getIntegerFromString(String value) {
        Integer output = null;
        if (StringUtils.isNotBlank(value)) {
            output = Integer.parseInt(value);
        }
        return output;
    }
}