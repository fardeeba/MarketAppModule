package com.skylarksit.module.pojos.services;

import javax.xml.transform.Source;

public class RequestTokenSession {

    public String repositoryId;
    public Response response;
    public String result;
    public SourceOfFunds sourceOfFunds;
    public String status;
    public String token;
    public Usage usage;
    public String verificationStrategy;

    public RequestTokenSession() {
    }

    public class Response {
        public String gatewayCode;
    }

    public class SourceOfFunds {

        public Provided provided;
        public String type;

        public class Provided {
            public Card card;

            public class Card {
                public String brand;
                public String expiry;
                public String fundingMethod;
                public String issuer;
                public String number;
                public String scheme;
            }
        }
    }

    public class Usage {
        public String lastUpdated;
        public String lastUpdatedBy;
        public String lastUsed;
    }
}

