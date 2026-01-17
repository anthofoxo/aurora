package xyz.anthofoxo.aurora;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

import javax.swing.JOptionPane;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

public class Updater {
	/**
	 * Fetches the latest stable release and latest pre-release from GitHub.
	 *
	 * @param owner GitHub repository owner
	 * @param repo  GitHub repository name
	 * @return ObjectNode with "latestStable" and "latestPreRelease"
	 */
	public static ObjectNode fetchLatestReleases(String owner, String repo) {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode resultNode = mapper.createObjectNode();

		try {
			// 1. Get the latest stable release
			String stableUrl = "https://api.github.com/repos/" + owner + "/" + repo + "/releases/latest";
			JsonNode stableRelease = fetchJsonFromUrl(stableUrl, mapper);
			if (stableRelease != null) {
				resultNode.set("latestStable", stableRelease);
			}

			// 2. Get all releases to find the latest pre-release
			String allReleasesUrl = "https://api.github.com/repos/" + owner + "/" + repo + "/releases";
			JsonNode releasesArray = fetchJsonFromUrl(allReleasesUrl, mapper);

			if (releasesArray != null && releasesArray.isArray()) {
				for (JsonNode release : releasesArray) {
					if (release.get("prerelease").asBoolean()) {
						resultNode.set("latestPreRelease", release);
						break; // first pre-release found
					}
				}
			}

			// If no pre-release exists, set null
			if (!resultNode.has("latestPreRelease")) {
				resultNode.putNull("latestPreRelease");
			}

			return resultNode;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private static JsonNode fetchJsonFromUrl(String urlString, ObjectMapper mapper) {
		try {
			URL url = new URI(urlString).toURL();
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("Accept", "application/vnd.github.v3+json");

			int status = con.getResponseCode();
			if (status != 200) {
				System.out.println("GitHub API request failed: " + status);
				return null;
			}

			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			StringBuilder content = new StringBuilder();
			String line;
			while ((line = in.readLine()) != null) {
				content.append(line);
			}
			in.close();
			con.disconnect();

			return mapper.readTree(content.toString());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private static JsonNode originStable;
	private static JsonNode originNightly;

	public static void announceReleases() {
		ObjectNode releases = fetchLatestReleases("anthofoxo", "aurora");

		if (releases != null) {

		} else {
			System.out.println("Failed to fetch releases.");
			return;
		}

		String stableTag = releases.get("latestStable").get("tag_name").asString();
		String preTag = releases.get("latestPreRelease").isNull() ? "None"
				: releases.get("latestPreRelease").get("tag_name").asString();

		originStable = releases.get("latestStable");
		originNightly = releases.get("latestPreRelease");

		boolean isStableNewer = SemVerComparator.compareSemVer(Aurora.TAG, stableTag) == -1;
		boolean isNightlyNewer = SemVerComparator.compareSemVer(Aurora.TAG, preTag) == -1;

		System.out.println("Current: " + Aurora.TAG);
		System.out.println("Remote Stable: " + stableTag);
		System.out.println("Remote Nightly: " + preTag);

		if (isStableNewer) {
			if (JOptionPane.YES_OPTION == Util.showOptionDialog(
					"A new release is available. Do you want to open the download page?", "Update Available",
					JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE)) {
				Util.openURL(originStable.get("html_url").asString());
			}
		} else if (isNightlyNewer) {
			if (JOptionPane.YES_OPTION == Util.showOptionDialog(
					"A new pre-release is available. Do you want to open the download page?", "Update Available",
					JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE)) {
				Util.openURL(originNightly.get("html_url").asString());
			}
		}
	}
}
