package org.gitub.pm2media;

public class WikiParams {
	/** url to Wiki */
	private String url;
	/** User name for wiki  */
	private String username;
	/** Password for wiki */
	private String password;

	public WikiParams() {
	}

	public String getURL() {
		return url;
	}

	public void setURL(String URL) {
		this.url = URL;
	}
	
	public WikiParams withURL(String url) {
		setURL(url);
		return this;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public WikiParams withUsername(String username) {
		setUsername(username);
		return this;
	}
	
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	public WikiParams withPassword(String password) {
		setPassword(password);
		return this;
	}
}