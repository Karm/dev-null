package quarkus.client;

public class SummaryDto {
    public String employeeId;
    public String status;
    public boolean processed;

    // Without the getters-setters circus, Yasson fails in native
    // to deal with the boolean on `processed` attribute, throwing:
    // Caused by: java.lang.IllegalArgumentException:
    // Can not set boolean field quarkus.client.SummaryDto.processed to java.lang.Integer
    public boolean isProcessed() {
        return processed;
    }

    public void setProcessed(boolean processed) {
        this.processed = processed;
    }
}
