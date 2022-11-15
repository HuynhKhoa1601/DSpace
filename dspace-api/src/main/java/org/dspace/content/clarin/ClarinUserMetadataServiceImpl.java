/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.clarin;

import java.sql.SQLException;
import java.util.List;

import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.service.AuthorizeService;
import org.dspace.content.dao.clarin.ClarinUserMetadataDAO;
import org.dspace.content.service.clarin.ClarinUserMetadataService;
import org.dspace.core.Context;
import org.dspace.core.LogHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class ClarinUserMetadataServiceImpl implements ClarinUserMetadataService {
    private static final Logger log = LoggerFactory.getLogger(ClarinUserMetadataService.class);

    @Autowired
    AuthorizeService authorizeService;
    @Autowired
    ClarinUserMetadataDAO clarinUserMetadataDAO;

    @Override
    public ClarinUserMetadata create(Context context) throws SQLException, AuthorizeException {
        if (!authorizeService.isAdmin(context)) {
            throw new AuthorizeException(
                    "You must be an admin to create an Clarin user metadata");
        }
        // Create a table row
        ClarinUserMetadata clarinUserMetadata = clarinUserMetadataDAO.create(context,
                new ClarinUserMetadata());

        log.info(LogHelper.getHeader(context, "create_clarin_user_metadata",
                "clarin_user_metadata_id=" + clarinUserMetadata.getID()));

        return clarinUserMetadata;
    }

    @Override
    public ClarinUserMetadata find(Context context, int valueId) throws SQLException {
        return clarinUserMetadataDAO.findByID(context, ClarinUserMetadata.class, valueId);
    }

    @Override
    public List<ClarinUserMetadata> findAll(Context context) throws SQLException {
        return clarinUserMetadataDAO.findAll(context, ClarinUserMetadata.class);
    }

    @Override
    public void delete(Context context, ClarinUserMetadata clarinUserMetadata) throws SQLException, AuthorizeException {
        if (!authorizeService.isAdmin(context)) {
            throw new AuthorizeException(
                    "You must be an admin to create an Clarin user metadata");
        }
        clarinUserMetadataDAO.delete(context, clarinUserMetadata);
    }
}
