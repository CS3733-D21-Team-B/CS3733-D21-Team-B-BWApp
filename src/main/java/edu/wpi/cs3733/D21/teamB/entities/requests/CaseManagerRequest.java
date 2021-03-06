package edu.wpi.cs3733.D21.teamB.entities.requests;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CaseManagerRequest extends Request {

    private String patientName;
    private String arrivalDate;
    private String timeForArrival;

    public CaseManagerRequest(String patientName, String arrivalDate, String timeForArrival, String requestID, String time, String date, String complete, String employeeName, String location, String description) {
        super(requestID, RequestType.CASE_MANAGER, time, date, complete, employeeName, location, description);
        this.patientName = patientName;
        this.arrivalDate = arrivalDate;
        this.timeForArrival = timeForArrival;
    }
}
