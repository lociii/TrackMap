package de.jensnistler.routemap.helper;

public class TrackModel {
    private String Key;
    private String Description;
    private String Link;
    private Float Length;

    public TrackModel(String Key) {
        this.Key = Key;
    }

    public String getKey() {
        return Key;
    }

    public void setKey(String Key) {
        this.Key = Key;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String Description) {
        this.Description = Description;
    }

    public String getLink() {
        return Link;
    }

    public void setLink(String Link) {
        this.Link = Link;
    }

    public Float getLength() {
        return Length;
    }

    public void setLength(Float Length) {
        this.Length = Length;
    }

	// will be used by the ArrayAdapter in the ListView
	@Override
	public String toString() {
		return Description;
	}
}
