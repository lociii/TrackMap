package de.jensnistler.routemap.helper;

public class MapModel {
    private String Key;
    private String Description;
    private Integer Date;
    private Long Size;
    private String Url;
    private Integer Updated;

    public MapModel(String Key) {
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
	
    public Integer getDate() {
        return Date;
    }

    public void setDate(Integer Date) {
        this.Date = Date;
    }

    public Long getSize() {
        return Size;
    }

    public void setSize(Long Size) {
        this.Size = Size;
    }

    public String getUrl() {
        return Url;
    }

    public void setUrl(String Url) {
        this.Url = Url;
    }

    public Integer getUpdated() {
        return Updated;
    }

    public void setUpdated(Integer Updated) {
        this.Updated = Updated;
    }

	// will be used by the ArrayAdapter in the ListView
	@Override
	public String toString() {
		return Description;
	}
}
