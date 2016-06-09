package au.com.museumvictoria.fieldguide.vic.fork.model;

public class Group {
  private String order;
	private String label;
  private String iconWhiteFilename;
  private String iconDarkFilename;
  private String iconCredit;
  private String description;

	public Group() {}

  public String getOrder() {
    return order;
  }

  public void setOrder(String order) {
    this.order = order;
  }

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getIconWhiteFilename() {
		return iconWhiteFilename;
	}

	public void setIconWhiteFilename(String iconWhiteFilename) {
		this.iconWhiteFilename = iconWhiteFilename;
	}

	public String getIconDarkFilename() {
		return iconDarkFilename;
	}

	public void setIconDarkFilename(String iconDarkFilename) {
		this.iconDarkFilename = iconDarkFilename;
	}

	public String getIconCredit() {
		return iconCredit;
	}

	public void setIconCredit(String iconCredit) {
		this.iconCredit = iconCredit;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
