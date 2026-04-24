package quarkus.gateway;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.List;

@RegisterForReflection
@XmlRootElement(name = "EmployeeProfiles")
@XmlAccessorType(XmlAccessType.FIELD)
public class EmployeeProfiles {

    @XmlElement(name = "EmployeeProfile")
    public List<EmployeeProfileXml> profiles;
}
