package de.jensnistler.routemap.helper;

public class TrackModel {
    private Integer Key;
    private String Description;

    public TrackModel(Integer Key) {
        this.Key = Key;
    }

    public Integer getKey() {
        return Key;
    }

    public void setKey(Integer Key) {
        this.Key = Key;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String Description) {
        this.Description = Description;
    }

	// will be used by the ArrayAdapter in the ListView
	@Override
	public String toString() {
		return Description;
	}
}
