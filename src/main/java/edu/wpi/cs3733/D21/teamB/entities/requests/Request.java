package edu.wpi.cs3733.D21.teamB.entities.requests;

import edu.wpi.cs3733.D21.teamB.entities.IStoredEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Request implements IStoredEntity {


    private String requestID;

    private RequestType requestType;

    private String time;

    private String date;

    private String progress;

    private String employeeName;

    private String location;

    private String description;

    private String submitter;

    public Request(String requestID, RequestType requestType, String time, String date, String progress, String employeeName, String location, String description) {
        this.requestID = requestID;
        this.requestType = requestType;
        this.time = time;
        this.date = date;
        this.progress = progress;
        this.employeeName = employeeName;
        this.location = location;
        this.description = description;
        this.submitter = null;
    }

    public enum RequestType {
        CASE_MANAGER,
        EXTERNAL_TRANSPORT,
        FLORAL,
        GIFT,
        FOOD,
        INTERNAL_TRANSPORT,
        LAUNDRY,
        MEDICINE,
        RELIGIOUS,
        SANITATION,
        SECURITY,
        SOCIAL_WORKER,
        EMERGENCY;

        public static String prettify(RequestType requestType) {
            switch (requestType) {
                case CASE_MANAGER:
                    return "Case Manager";
                case EXTERNAL_TRANSPORT:
                    return "External Transport";
                case FLORAL:
                    return "Floral";
                case FOOD:
                    return "Food";
                case INTERNAL_TRANSPORT:
                    return "Internal Transport";
                case LAUNDRY:
                    return "Laundry";
                case MEDICINE:
                    return "Medicine";
                case RELIGIOUS:
                    return "Religious";
                case SANITATION:
                    return "Sanitation";
                case SECURITY:
                    return "Security";
                case SOCIAL_WORKER:
                    return "Social Worker";
                case GIFT:
                    return "Gift";
                case EMERGENCY:
                    return "Emergency";
                default:
                    throw new IllegalStateException("How did we get here?");
            }
        }
    }
}
