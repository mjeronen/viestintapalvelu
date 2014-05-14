package fi.vm.sade.viestintapalvelu.model;

import java.util.Date;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

import fi.vm.sade.generic.model.BaseEntity;

/*
 * CREATE TABLE kirjeet.kirjepohja (
 id bigint NOT NULL,
 version bigint NOT NULL,
 nimi character varying(255),
 tyylit character varying(3000),
 kielikoodi character varying (5),
 aikaleima timestamp without time zone,
 oid_tallentaja character varying(255),
 oid_organisaatio character varying(255)
 );
 */

@ApiModel(value = "Kirjetemplate")
@Table(name = "kirjepohja", schema="kirjeet")
@Entity(name = "Template")
public class Template extends BaseEntity {

    public Set<TemplateContent> getContents() {
        return contents;
    }

    public void setContents(Set<TemplateContent> contents) {
        this.contents = contents;
    }

    public Set<Replacement> getReplacements() {
        return replacements;
    }

    public void setReplacements(Set<Replacement> replacements) {
        this.replacements = replacements;
    }

    private static final long serialVersionUID = 4178735997933155683L;

    @Column(name = "aikaleima", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;

    @Column(name = "nimi", nullable = false)
    private String name;

    @ApiModelProperty(value = "Kielikoodi ISO 639-1, default = 'FI'")
    @Column(name = "kielikoodi", nullable = false)
    private String language;

	@Column(name="versionro", nullable=false)
	private String versionro;	
	    
    @ApiModelProperty(value = "CSS styles")
    @Column(name = "tyylit", nullable = false)
    private String styles;
    
    public String getStyles() {
        return styles;
    }

    public void setStyles(String styles) {
        this.styles = styles;
    }

    @Column(name = "oid_tallentaja", nullable = true)
    private String storingOid;

    @Column(name = "oid_organisaatio", nullable = true)
    private String organizationOid;

    @OneToMany(mappedBy = "template", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Set<TemplateContent> contents;

    @OneToMany(mappedBy = "template", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Set<Replacement> replacements;

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
    
    public String getVersionro() {
		return versionro;
	}

	public void setVersionro(String versionro) {
		this.versionro = versionro;
	}

	public String getStoringOid() {
        return storingOid;
    }

    public void setStoringOid(String storingOid) {
        this.storingOid = storingOid;
    }

    public String getOrganizationOid() {
        return organizationOid;
    }

    public void setOrganizationOid(String organizationOid) {
        this.organizationOid = organizationOid;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

}