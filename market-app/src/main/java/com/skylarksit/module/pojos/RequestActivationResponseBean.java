package com.skylarksit.module.pojos;

public class RequestActivationResponseBean extends ResponseBean {

	public enum VerificationType {
		SMS
	}

	public String uid;
	public Long idUser;
	public String e164;
	public Long idCountry;
	public String supportEmail;
	public VerificationType verificationType;
}
