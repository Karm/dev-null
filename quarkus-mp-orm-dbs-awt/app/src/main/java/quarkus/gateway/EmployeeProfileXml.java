package quarkus.gateway;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@RegisterForReflection
@XmlRootElement(name = "EmployeeProfile")
@XmlAccessorType(XmlAccessType.FIELD)
public class EmployeeProfileXml {

    @NotNull
    @XmlElement(name = "EmployeeId")
    public String employeeId;

    @Valid
    @NotNull
    @XmlElement(name = "Demographics")
    public Demographics demographics;

    @Valid
    @XmlElement(name = "Security")
    public Security security;

    @NotNull
    @XmlElement(name = "ProfilePictureBase64")
    public String profilePictureBase64;

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Demographics {
        @NotNull
        @Size(min = 2, max = 50)
        @XmlElement(name = "FullName")
        public String fullName;

        @Valid
        @XmlElement(name = "ContactInfo")
        public ContactInfo contactInfo;

        @XmlAccessorType(XmlAccessType.FIELD)
        public static class ContactInfo {
            @XmlElement(name = "Email")
            public String email;

            @Valid
            @XmlElement(name = "Address")
            public Address address;

            @XmlAccessorType(XmlAccessType.FIELD)
            public static class Address {
                @XmlElement(name = "Street")
                public String street;

                @XmlElement(name = "City")
                public String city;
            }
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Security {
        @Valid
        @XmlElement(name = "Clearance")
        public Clearance clearance;

        @XmlAccessorType(XmlAccessType.FIELD)
        public static class Clearance {
            @NotNull
            @XmlElement(name = "Level")
            public String level;

            @XmlElement(name = "BackgroundCheck")
            public BackgroundCheck backgroundCheck;

            @XmlAccessorType(XmlAccessType.FIELD)
            public static class BackgroundCheck {
                @XmlElement(name = "Passed")
                public boolean passed;
                @XmlElement(name = "Date")
                public String date;
            }
        }
    }
}
