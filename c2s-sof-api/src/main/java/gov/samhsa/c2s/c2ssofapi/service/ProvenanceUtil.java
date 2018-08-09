package gov.samhsa.c2s.c2ssofapi.service;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import gov.samhsa.c2s.c2ssofapi.constants.ProvenanceConstants;
import gov.samhsa.c2s.c2ssofapi.service.constant.ProvenanceActivityEnum;
import gov.samhsa.c2s.c2ssofapi.service.util.FhirResourceUtil;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Provenance;
import org.hl7.fhir.dstu3.model.Reference;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Service
public class ProvenanceUtil {

    private final IGenericClient fhirClient;

    public ProvenanceUtil(IGenericClient fhirClient) {
        this.fhirClient = fhirClient;
    }

    public void createProvenance(List<String> idList, ProvenanceActivityEnum provenanceActivityEnum, Optional<String> loggedInUser) {
        Provenance provenance = new Provenance();

        //target
        List<Reference> referenceList = idList.stream().map(id -> {
            Reference reference = new Reference();
            reference.setReference(id);
            return reference;
        }).collect(toList());

        provenance.setTarget(referenceList);

        //recorded : When the activity was recorded/ updated
        provenance.setRecorded(new Date());

        //activity
        if (provenanceActivityEnum != null && FhirResourceUtil.isStringNotNullAndNotEmpty(provenanceActivityEnum.toString())) {
            Coding coding = new Coding();
            coding.setCode(provenanceActivityEnum.toString());
            coding.setSystem(ProvenanceConstants.PROVENANCE_ACTIVITY_TYPE_CODING_SYSTEM);
            coding.setDisplay(provenanceActivityEnum.toString().toLowerCase());
            provenance.setActivity(coding);
        }

        //agent.whoReference
        if (loggedInUser.isPresent()) {
            Provenance.ProvenanceAgentComponent agent = new Provenance.ProvenanceAgentComponent();
            Reference whoRef = new Reference();
            whoRef.setReference(loggedInUser.get());
            agent.setWho(whoRef);

            provenance.setAgent(Collections.singletonList(agent));
        }

        fhirClient.create().resource(provenance).execute();
    }
}
