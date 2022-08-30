package mandrel_perf_demo;

public class ClassA {
    public final int myNumber;
    public final String myString;

    public ClassA(int myNumber, String myString) {
        this.myNumber = myNumber;
        this.myString = myString;
    }

    @Override
    public String toString() {
        return "{\"myString\":\"" + myString + "\",\"myNumber\":\"" + myNumber + "\"}";
    }
}

