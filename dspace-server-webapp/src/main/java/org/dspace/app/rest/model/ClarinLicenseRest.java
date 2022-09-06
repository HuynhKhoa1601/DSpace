package org.dspace.app.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.dspace.app.rest.RestResourceController;

public class ClarinLicenseRest extends BaseObjectRest<Integer> {

    public static final String NAME = "clarinlicense";
    public static final String CATEGORY = RestAddressableModel.CORE;

    @JsonIgnore
    private ClarinLicenseLabelRest licenseLabel;
    private String definition;
    private Integer confirmation;
    private String requiredInfo;

    public ClarinLicenseRest() {
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public Integer getConfirmation() {
        return confirmation;
    }

    public void setConfirmation(Integer confirmation) {
        this.confirmation = confirmation;
    }

    public String getRequiredInfo() {
        return requiredInfo;
    }

    public void setRequiredInfo(String requiredInfo) {
        this.requiredInfo = requiredInfo;
    }

    public ClarinLicenseLabelRest getLicenseLabel() {
        return licenseLabel;
    }

    public void setLicenseLabel(ClarinLicenseLabelRest licenseLabel) {
        this.licenseLabel = licenseLabel;
    }

    @Override
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public String getType() {
        return NAME;
    }

    @Override
    public Class getController() {
        return RestResourceController.class;
    }

    @Override
    public String getCategory() {
        return CATEGORY;
    }
}
