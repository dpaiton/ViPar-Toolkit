package dannydelott.vinefilter.settings.config.dataset;

public class EnumeratedToken {

	private String token;
	private int position;

	public EnumeratedToken(String k, int p) {
		token = k;
		position = p;
	}

	public String getToken() {
		return token;
	}

	public int getPosition() {
		return position;
	}
}
