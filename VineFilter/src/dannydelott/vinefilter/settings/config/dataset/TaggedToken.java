package dannydelott.vinefilter.settings.config.dataset;

public final class TaggedToken {

	private String tag;
	private String token;

	public TaggedToken(String g, String k) {
		tag = g;
		token = k;
	}

	public String getTag() {
		return tag;
	}

	public String getToken() {
		return token;
	}
}
