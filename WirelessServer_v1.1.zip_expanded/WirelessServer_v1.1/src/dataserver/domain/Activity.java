package dataserver.domain;

public class Activity {
    
    private Integer id;
    private String type;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        String ret = "";
        ret = ret + "Id = [" + getId() + "] ";
        ret = ret + "Type = [" + getType() + "] ";
        return ret;
    }
}
