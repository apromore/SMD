package org.apromore.dao.model;

public class GNode {

	private Integer vid;
    private String vname;
    private String vtype;
    private String ctype;
    private String originalId;
    private Boolean configuration = false;
    private String contentId;
    
	public Integer getVid() {
		return vid;
	}
	
	public void setVid(Integer vid) {
		this.vid = vid;
	}
	
	public String getVname() {
		return vname;
	}
	
	public void setVname(String vname) {
		this.vname = vname;
	}
	
	public String getVtype() {
		return vtype;
	}
	
	public void setVtype(String vtype) {
		this.vtype = vtype;
	}
	
	public String getCtype() {
		return ctype;
	}
	
	public void setCtype(String ctype) {
		this.ctype = ctype;
	}
	
	public String getOriginalId() {
		return originalId;
	}
	
	public void setOriginalId(String originalId) {
		this.originalId = originalId;
	}
	
	public Boolean getConfiguration() {
		return configuration;
	}
	
	public void setConfiguration(Boolean configuration) {
		this.configuration = configuration;
	}
	
	public String getContentId() {
		return contentId;
	}
	
	public void setContentId(String contentId) {
		this.contentId = contentId;
	}
}
