/**
 * Created by nronsse on 4/7/17.
 */
public class SkuEntity {
    private String Id;
    private long Timestamp;

    public long getTs() {
        return Timestamp;
    }

    public void setTs(String ts) {
        this.Timestamp = Long.valueOf(ts);
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        this.Id = id;
    }
    public SkuEntity(String id, String ts) {
        this.Id = id;
        this.Timestamp = Long.valueOf(ts);
    }

}

