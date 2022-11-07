package org.dspace.content.clarin;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.apache.log4j.Logger;
import org.dspace.core.ReloadableEntity;

@Entity
@Table(name = "user_metadata")
public class ClarinUserMetadata  implements ReloadableEntity<Integer> {

    private static Logger log = Logger.getLogger(ClarinUserMetadata.class);
    @Id
    @Column(name = "user_metadata_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST})
    @JoinColumn(name = "user_registration_id")
    private ClarinUserRegistration eperson;

    @Column(name = "metadata_key")
    private String metadataKey = null;

    @Column(name = "metadata_value")
    private String metadataValue = null;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id")
    private ClarinLicenseResourceUserAllowance transaction;

    @Override
    public Integer getID() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public ClarinUserRegistration getEperson() {
        return eperson;
    }

    public void setEperson(ClarinUserRegistration eperson) {
        this.eperson = eperson;
    }

    public String getMetadataKey() {
        return metadataKey;
    }

    public void setMetadataKey(String metadataKey) {
        this.metadataKey = metadataKey;
    }

    public String getMetadataValue() {
        return metadataValue;
    }

    public void setMetadataValue(String metadataValue) {
        this.metadataValue = metadataValue;
    }

    public ClarinLicenseResourceUserAllowance getTransaction() {
        return transaction;
    }

    public void setTransaction(ClarinLicenseResourceUserAllowance transaction) {
        this.transaction = transaction;
    }
}
